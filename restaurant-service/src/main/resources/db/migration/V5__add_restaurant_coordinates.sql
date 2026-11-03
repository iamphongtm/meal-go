-- Keep coordinates nullable until existing restaurants have been backfilled.
ALTER TABLE restaurants
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION,
    ADD CONSTRAINT chk_restaurant_latitude
        CHECK (latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_restaurant_longitude
        CHECK (longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT chk_restaurant_coordinates_pair
        CHECK ((latitude IS NULL) = (longitude IS NULL));
