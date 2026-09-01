from __future__ import annotations

import math

import numpy as np

from .contracts import ForecastMetrics


def calculate_metrics(
    actual: list[float],
    predicted: list[float],
    training_series: list[float],
    seasonal_period: int | None = None,
) -> ForecastMetrics:
    actual_values = np.asarray(actual, dtype=float)
    predicted_values = np.asarray(predicted, dtype=float)
    errors = actual_values - predicted_values
    mae = float(np.mean(np.abs(errors)))
    rmse = float(math.sqrt(np.mean(np.square(errors))))
    smape = float(np.mean(smape_errors(actual_values, predicted_values)))

    training = np.asarray(training_series, dtype=float)
    naive_scale = float(np.mean(np.abs(np.diff(training)))) if len(training) > 1 else 0.0
    non_seasonal_mase = None if naive_scale == 0.0 else mae / naive_scale
    seasonal_mase = None
    if seasonal_period is not None and seasonal_period > 1 and len(training) > seasonal_period:
        seasonal_scale = float(np.mean(np.abs(training[seasonal_period:] - training[:-seasonal_period])))
        if seasonal_scale != 0.0:
            seasonal_mase = mae / seasonal_scale
    return ForecastMetrics(
        mae=mae,
        rmse=rmse,
        smape=smape,
        nonSeasonalMase=non_seasonal_mase,
        seasonalMase=seasonal_mase,
    )


def smape_errors(actual: np.ndarray | list[float], predicted: np.ndarray | list[float]) -> np.ndarray:
    actual_values = np.asarray(actual, dtype=float)
    predicted_values = np.asarray(predicted, dtype=float)
    denominator = np.abs(actual_values) + np.abs(predicted_values)
    return 100.0 * np.divide(
        2.0 * np.abs(actual_values - predicted_values),
        denominator,
        out=np.zeros_like(actual_values),
        where=denominator != 0,
    )
