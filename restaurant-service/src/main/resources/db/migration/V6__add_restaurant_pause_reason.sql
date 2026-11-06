ALTER TABLE restaurants ADD COLUMN pause_reason varchar(30);
ALTER TABLE restaurants ADD CONSTRAINT restaurants_pause_reason_check
    CHECK (pause_reason IS NULL OR pause_reason IN ('overload', 'out_of_stock', 'shift_break', 'other'));
ALTER TABLE restaurants ADD CONSTRAINT restaurants_open_pause_reason_check
    CHECK (status <> 'OPENING' OR pause_reason IS NULL);
