ALTER TABLE kpi_entries
    ADD COLUMN secondary_extracted_value NUMERIC(20,6),
    ADD COLUMN secondary_current_value NUMERIC(20,6),
    ADD COLUMN secondary_final_value NUMERIC(20,6),
    ADD COLUMN secondary_unit VARCHAR(50);

COMMENT ON COLUMN kpi_entries.secondary_extracted_value IS
    'Secondary measurement extracted from the same source KPI line, for example the percentage in 77108-77%.';
COMMENT ON COLUMN kpi_entries.secondary_final_value IS
    'Human-confirmed secondary measurement; null remains a legitimate missing value and never means zero.';
