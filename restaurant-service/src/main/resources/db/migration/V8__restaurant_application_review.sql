-- Existing restaurants are NOT automatically approved. They require admin review.
ALTER TABLE restaurants ADD COLUMN approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';
ALTER TABLE restaurants ADD COLUMN submitted_at TIMESTAMPTZ;
UPDATE restaurants SET submitted_at = created_at;
ALTER TABLE restaurants ALTER COLUMN submitted_at SET NOT NULL;
ALTER TABLE restaurants ADD COLUMN reviewed_by UUID;
ALTER TABLE restaurants ADD COLUMN reviewed_at TIMESTAMPTZ;
ALTER TABLE restaurants ADD COLUMN rejection_reason VARCHAR(1000);
ALTER TABLE restaurants ADD CONSTRAINT ck_restaurant_approval_status
    CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED'));
CREATE INDEX idx_restaurants_approval_submitted ON restaurants (approval_status, submitted_at, id)
    WHERE deleted_at IS NULL;
