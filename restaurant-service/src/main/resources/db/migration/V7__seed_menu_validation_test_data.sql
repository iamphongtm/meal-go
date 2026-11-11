INSERT INTO restaurants (
    id, owner_id, ward_code, name, phone, status,
    address_line, latitude, longitude, pause_reason,
    created_at, updated_at, version
)
VALUES (
           '10000000-0000-0000-0000-000000000001',
           '90000000-0000-0000-0000-000000000001',
           4,
           'MealGo Test Restaurant',
           '0900000000',
           'OPENING',
           '123 Test Street',
           10.7769,
           106.7009,
           NULL,
           now(), now(), 0
       )
    ON CONFLICT (id) DO UPDATE SET
    status = 'OPENING',
                            pause_reason = NULL,
                            deleted_at = NULL;

INSERT INTO menu_categories (
    id, restaurant_id, name, status, display_order,
    created_at, updated_at, version
)
VALUES (
           '20000000-0000-0000-0000-000000000001',
           '10000000-0000-0000-0000-000000000001',
           'Món chính',
           'ACTIVE',
           1,
           now(), now(), 0
       )
    ON CONFLICT (id) DO UPDATE SET
    status = 'ACTIVE',
                            deleted_at = NULL;

INSERT INTO menu_items (
    id, category_id, name, description, base_price,
    status, display_order, created_at, updated_at, version
)
VALUES
    (
        '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        'Bún bò đặc biệt',
        'Món dùng để test validate',
        65000,
        'ACTIVE',
        1,
        now(), now(), 0
    ),
    (
        '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000001',
        'Bún bò hết hàng',
        'Món sold out dùng để test',
        55000,
        'SOLD_OUT',
        2,
        now(), now(), 0
    )
    ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
                            base_price = EXCLUDED.base_price,
                            deleted_at = NULL;

INSERT INTO option_groups (
    id, restaurant_id, name, status,
    created_at, updated_at, version
)
VALUES (
           '40000000-0000-0000-0000-000000000001',
           '10000000-0000-0000-0000-000000000001',
           'Kích cỡ',
           'ACTIVE',
           now(), now(), 0
       )
    ON CONFLICT (id) DO UPDATE SET
    status = 'ACTIVE',
                            deleted_at = NULL;

INSERT INTO options (
    id, option_group_id, name, status, display_order,
    created_at, updated_at, version
)
VALUES
    (
        '50000000-0000-0000-0000-000000000001',
        '40000000-0000-0000-0000-000000000001',
        'Lớn',
        'ACTIVE',
        1,
        now(), now(), 0
    ),
    (
        '50000000-0000-0000-0000-000000000002',
        '40000000-0000-0000-0000-000000000001',
        'Siêu lớn',
        'SOLD_OUT',
        2,
        now(), now(), 0
    )
    ON CONFLICT (id) DO UPDATE SET
    status = EXCLUDED.status,
                            deleted_at = NULL;

INSERT INTO menu_item_option_groups (
    id, menu_item_id, option_group_id,
    min_select, max_select, display_order,
    created_at, updated_at, version
)
VALUES (
           '60000000-0000-0000-0000-000000000001',
           '30000000-0000-0000-0000-000000000001',
           '40000000-0000-0000-0000-000000000001',
           1, 1, 1,
           now(), now(), 0
       )
    ON CONFLICT (id) DO UPDATE SET
    min_select = 1,
                            max_select = 1,
                            deleted_at = NULL;

INSERT INTO menu_item_option_prices (
    id, menu_item_option_group_id, option_id,
    additional_price, created_at, updated_at, version
)
VALUES
    (
        '70000000-0000-0000-0000-000000000001',
        '60000000-0000-0000-0000-000000000001',
        '50000000-0000-0000-0000-000000000001',
        15000,
        now(), now(), 0
    ),
    (
        '70000000-0000-0000-0000-000000000002',
        '60000000-0000-0000-0000-000000000001',
        '50000000-0000-0000-0000-000000000002',
        25000,
        now(), now(), 0
    )
    ON CONFLICT (id) DO UPDATE SET
    additional_price = EXCLUDED.additional_price,
                            deleted_at = NULL;