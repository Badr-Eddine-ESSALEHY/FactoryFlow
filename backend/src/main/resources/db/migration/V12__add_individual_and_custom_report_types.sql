ALTER TABLE generated_reports DROP CONSTRAINT ck_generated_report_type;
ALTER TABLE generated_reports ADD CONSTRAINT ck_generated_report_type
    CHECK (type IN ('INDIVIDUAL','DAILY','WEEKLY','MONTHLY','CUSTOM','MANUAL'));
