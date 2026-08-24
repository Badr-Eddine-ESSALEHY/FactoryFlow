ALTER TABLE kpi_entries
    ADD COLUMN suggestion_strength VARCHAR(20),
    ADD COLUMN suggestion_match_method VARCHAR(50),
    ADD CONSTRAINT ck_kpi_entry_suggestion_strength
        CHECK (suggestion_strength IS NULL OR suggestion_strength IN ('WEAK', 'STRONG'));

ALTER TABLE report_unrecognized_lines
    ADD COLUMN unknown_kind VARCHAR(30) NOT NULL DEFAULT 'KPI_LIKE',
    ADD COLUMN classification_reason VARCHAR(80) NOT NULL DEFAULT 'UNCLASSIFIED',
    ADD COLUMN safe_to_ignore BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT ck_unrecognized_kind CHECK (unknown_kind IN ('KPI_LIKE', 'SAFE_NOISE')),
    ADD CONSTRAINT ck_unrecognized_safe_noise CHECK (NOT safe_to_ignore OR unknown_kind = 'SAFE_NOISE');

CREATE INDEX ix_unrecognized_lines_safe_noise
    ON report_unrecognized_lines(report_id, resolution_status)
    WHERE safe_to_ignore = TRUE;
