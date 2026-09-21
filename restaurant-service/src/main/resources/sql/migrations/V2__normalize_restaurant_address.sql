-- Run once for databases created with the previous province_code mapping.
-- A restaurant's province is derived from restaurants.ward_code -> wards.province_code.
DROP INDEX IF EXISTS idx_restaurants_province_code;

ALTER TABLE restaurants
    DROP COLUMN IF EXISTS province_code CASCADE;

ALTER TABLE restaurants
    DROP CONSTRAINT IF EXISTS fk_restaurants_ward;

ALTER TABLE restaurants
    ADD CONSTRAINT fk_restaurants_ward
        FOREIGN KEY (ward_code)
        REFERENCES wards (code);
