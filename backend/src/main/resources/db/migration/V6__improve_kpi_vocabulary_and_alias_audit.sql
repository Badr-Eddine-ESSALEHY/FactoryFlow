ALTER TABLE kpi_aliases
    ADD COLUMN origin VARCHAR(30) NOT NULL DEFAULT 'CONFIGURED',
    ADD COLUMN approved_by_user_id BIGINT,
    ADD COLUMN approved_at TIMESTAMPTZ,
    ADD CONSTRAINT fk_kpi_alias_approved_by
        FOREIGN KEY (approved_by_user_id) REFERENCES users(id);

CREATE INDEX ix_kpi_aliases_approved_by ON kpi_aliases(approved_by_user_id)
    WHERE approved_by_user_id IS NOT NULL;

INSERT INTO kpi_definitions (code, display_name, category, unit, plausible_min, active) VALUES
    ('COMPRESSEUR_2', 'Compresseur 2', 'Utilités', NULL, 0, TRUE),
    ('DRYER_TEMPERATURE', 'Température sécheur', 'Utilités', '°C', NULL, TRUE),
    ('WATER_METER', 'Compteur eau', 'Utilités', NULL, 0, TRUE),
    ('SOFT_WATER_DURATION', 'Durée eau adoucie', 'Utilités', NULL, 0, TRUE),
    ('EUROTECH_METER', 'Compteur Eurotech', 'Production', NULL, 0, TRUE),
    ('CICALIM_METER', 'Compteur Cicalim', 'Production', NULL, 0, TRUE),
    ('P1_OPERATING_HOURS', 'Heures de fonctionnement P1', 'Production', 'h', 0, TRUE),
    ('P1_PRODUCT_QUANTITY', 'Quantité produite P1', 'Production', NULL, 0, TRUE),
    ('P2_OPERATING_HOURS', 'Heures de fonctionnement P2', 'Production', 'h', 0, TRUE),
    ('P2_PRODUCT_QUANTITY', 'Quantité produite P2', 'Production', NULL, 0, TRUE),
    ('GRINDER_1_OPERATING_HOURS', 'Heures de fonctionnement Broyeur 1', 'Production', 'h', 0, TRUE),
    ('GRINDER_2_OPERATING_HOURS', 'Heures de fonctionnement Broyeur 2', 'Production', 'h', 0, TRUE),
    ('OIL_TANK_LEVEL', 'Niveau citerne Huile', 'Stockage', NULL, 0, TRUE),
    ('MOLASSES_TANK_LEVEL', 'Niveau citerne Mélasse', 'Stockage', NULL, 0, TRUE),
    ('P1_GREASE_QUANTITY', 'Quantité graisse P1', 'Maintenance', NULL, 0, TRUE),
    ('P2_GREASE_QUANTITY', 'Quantité graisse P2', 'Maintenance', NULL, 0, TRUE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO kpi_aliases (kpi_definition_id, alias, normalized_alias, origin)
SELECT definition.id, values.alias, values.normalized_alias, 'CONFIGURED'
FROM (VALUES
    ('COMPRESSEUR_1', 'Compresseur1', 'compresseur1'),
    ('COMPRESSEUR_1', 'Comp 1', 'comp 1'),
    ('COMPRESSEUR_2', 'Compresseur2', 'compresseur2'),
    ('COMPRESSEUR_2', 'Comp 2', 'comp 2'),
    ('DRYER_TEMPERATURE', 'Temps sécheur', 'temps secheur'),
    ('DRYER_TEMPERATURE', 'Temperature secheur', 'temperature secheur'),
    ('WATER_METER', 'Compteur d eau', 'compteur d eau'),
    ('SOFT_WATER_DURATION', 'Durée l eau adous', 'duree l eau adous'),
    ('SOFT_WATER_DURATION', 'Duree eau adoucie', 'duree eau adoucie'),
    ('EUROTECH_METER', 'Cpt Eurotech', 'cpt eurotech'),
    ('CICALIM_METER', 'Cpt Cicalim', 'cpt cicalim'),
    ('P1_OPERATING_HOURS', 'Heures Fct P1', 'heures fct p1'),
    ('P1_OPERATING_HOURS', 'Heures Fct. P1', 'heures fct p1'),
    ('P1_PRODUCT_QUANTITY', 'Q. Produit P1', 'q produit p1'),
    ('P1_PRODUCT_QUANTITY', 'Q Produit P1', 'q produit p1'),
    ('P2_OPERATING_HOURS', 'Heures Fct P2', 'heures fct p2'),
    ('P2_PRODUCT_QUANTITY', 'Q. Produit P2', 'q produit p2'),
    ('P2_PRODUCT_QUANTITY', 'Q Produit P2', 'q produit p2'),
    ('GRINDER_1_OPERATING_HOURS', 'Heures de Fct Broyeur 1', 'heures de fct broyeur 1'),
    ('GRINDER_2_OPERATING_HOURS', 'Heures de Fct Broyeur 2', 'heures de fct broyeur 2'),
    ('OIL_TANK_LEVEL', 'Niveau citerne huile', 'niveau citerne huile'),
    ('MOLASSES_TANK_LEVEL', 'Niveau citerne melasse', 'niveau citerne melasse'),
    ('P1_GREASE_QUANTITY', 'Quantité graisse P1', 'quantite graisse p1'),
    ('P2_GREASE_QUANTITY', 'Quantité graisse P2', 'quantite graisse p2')
) AS values(code, alias, normalized_alias)
JOIN kpi_definitions definition ON definition.code = values.code
ON CONFLICT (normalized_alias) DO NOTHING;
