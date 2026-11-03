-- Mỗi owner chỉ được có một restaurant chưa bị soft delete.
-- Cho phép đăng ký lại sau khi restaurant cũ đã bị soft delete.
CREATE UNIQUE INDEX uq_restaurants_owner_active
    ON public.restaurants (owner_id)
    WHERE deleted_at IS NULL;