ALTER TABLE kpi_entries
    ADD COLUMN suggested_kpi_definition_id BIGINT,
    ADD COLUMN suggestion_score NUMERIC(5,4),
    ADD CONSTRAINT fk_kpi_entry_suggested_definition
        FOREIGN KEY (suggested_kpi_definition_id) REFERENCES kpi_definitions(id),
    ADD CONSTRAINT ck_kpi_entry_suggestion_score
        CHECK (suggestion_score IS NULL OR (suggestion_score >= 0 AND suggestion_score <= 1));

CREATE INDEX ix_kpi_entries_suggested_definition
    ON kpi_entries(suggested_kpi_definition_id)
    WHERE suggested_kpi_definition_id IS NOT NULL;
