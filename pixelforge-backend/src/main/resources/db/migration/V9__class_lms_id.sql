ALTER TABLE classes ADD COLUMN lms_class_id VARCHAR(255);
ALTER TABLE classes ADD CONSTRAINT uq_classes_org_lms UNIQUE (org_id, lms_class_id);
