# FactoryFlow Maintenance Intelligence runtime

Private analytical HTTP runtime for the Spring Boot backend. It has no database access: Spring Boot selects confirmed KPI history and sends traceable observations to `POST /v1/analyze`.

Run locally from this directory:

```powershell
python -m uvicorn app:app --host 127.0.0.1 --port 8092
```

The runtime uses scikit-learn Isolation Forest and statsmodels forecasting models. Insufficient data is returned as a normal typed analytical state.

## Analytical semantics

- Isolation Forest uses three per-observation features: confirmed value, change per actual elapsed day, and deviation from the median of the preceding configured window. Robust scaling is fitted within the KPI window. Duplicate effective dates make the time-aware analysis abstain with `DUPLICATE_EFFECTIVE_DATES`; zero elapsed days are never rewritten as one day. Every source observation remains in an insufficient result without a fabricated score.
- `decisionFunction` follows scikit-learn semantics and `anomalyScore = -decisionFunction`, so higher values mean stronger model-relative anomaly evidence. The score is neither a probability nor a severity and is not comparable across independently fitted KPI windows.
- Forecasting requires unique observations and a defensible regular cadence. The contract distinguishes `observedCadenceDays` from optional `expectedCadenceDays` and records `INFERRED_OBSERVED`, `CONFIGURED_EXPECTED`, or `UNKNOWN` as its basis. Missing records make inferred business cadence ambiguous. The runtime never interpolates, aggregates duplicate dates, or rewrites dates.
- Candidate models are last-observation naive, seasonal naive, ETS simple exponential smoothing, ETS Holt additive trend, ETS Holt-Winters additive seasonality, and a bounded explicit SARIMA candidate set.
- Candidate eligibility is conservative and horizon-aware. With seasonal period `p`, seasonal naive requires at least `max(3, configuredCycles) * p` training observations, Holt-Winters requires `max(4, configuredCycles + 1) * p`, non-seasonal SARIMA requires `max(20, 2H + 6)`, and seasonal SARIMA requires `max(max(5, configuredCycles + 2) * p, 30, 2H + 3p)`. Every eligible candidate must also leave at least the configured minimum number of complete horizon-aligned rolling origins.
- At every common expanding-window origin, a candidate forecasts the complete requested horizon `H`; evaluation compares only the following known observations and records MAE, RMSE, sMAPE, non-seasonal MASE, seasonal MASE, and per-step metrics for `t+1 ... t+H`. The latest common origins are capped by configuration. There is no random split or future leakage.
- Selection first identifies the lowest mean fold sMAPE. If its fold errors are `e_1 ... e_K`, the competitive threshold is `mean(e) + sampleStdDev(e) / sqrt(K)`. Every candidate whose mean fold sMAPE is at or below that threshold is competitive; the lowest-complexity competitive candidate is selected, with MAE, RMSE, family, and serialized configuration providing deterministic secondary ordering. The result separately records the raw best, parsimonious choice, final selected model, whether parsimony changed the choice, and whether final-fit fallback occurred.
- Every SARIMA fit must converge, have finite parameters, stable AR roots, and invertible MA roots. A Ljung-Box residual check is retained when enough residuals exist; detected autocorrelation is exposed as warning evidence rather than automatically rejected. A rejected final fit falls back to the next evaluated candidate.
- `nonSeasonalMase` uses the lag-one naive training scale. `seasonalMase` uses lag `p` and is null when that denominator is unavailable or zero. Neither metric is fabricated for constant or insufficient training history.
- Prediction intervals are emitted only when statsmodels supplies model-based intervals. Naive models deliberately return no interval.
- `latestObservationExpectation` is a separate one-step out-of-sample operation. The latest confirmed observation is removed before model evaluation and fitting; the selected prior-history model forecasts its date, and only then is the withheld actual compared with a genuine model interval. If the selected model has no interval, `outsideInterval` remains null.

The runtime is CPU-bound and deliberately uses one Isolation Forest worker. Production orchestration should execute KPI requests with bounded concurrency rather than allowing unbounded analytical fan-out.
