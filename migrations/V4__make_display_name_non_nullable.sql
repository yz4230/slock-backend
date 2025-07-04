UPDATE users SET display_name = 'Anonymous' WHERE display_name IS NULL;
ALTER TABLE users ALTER COLUMN display_name TYPE VARCHAR(128), ALTER COLUMN display_name SET NOT NULL;
