ALTER TABLE maintenance_reports DROP CONSTRAINT ck_maintenance_report_source;

UPDATE maintenance_reports
SET source = 'GALLERY_OCR'
WHERE source = 'CAMERA_OCR';

ALTER TABLE maintenance_reports
    ADD CONSTRAINT ck_maintenance_report_source
    CHECK (source IN ('MANUAL', 'PASTE', 'GALLERY_OCR', 'SHARE_OCR'));
