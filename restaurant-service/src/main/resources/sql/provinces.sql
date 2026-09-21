CREATE TABLE IF NOT EXISTS provinces (
    code INTEGER PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    division_type VARCHAR(50) NOT NULL,
    codename VARCHAR(100) NOT NULL UNIQUE,
    phone_code INTEGER NOT NULL
);

INSERT INTO provinces (code, name, division_type, codename, phone_code)
VALUES
    (1,  'Thành phố Hà Nội',      'thành phố trung ương', 'ha_noi',      24),
    (4,  'Tỉnh Cao Bằng',         'tỉnh',                 'cao_bang',    206),
    (8,  'Tỉnh Tuyên Quang',      'tỉnh',                 'tuyen_quang', 207),
    (11, 'Tỉnh Điện Biên',        'tỉnh',                 'dien_bien',   215),
    (12, 'Tỉnh Lai Châu',         'tỉnh',                 'lai_chau',    213),
    (14, 'Tỉnh Sơn La',           'tỉnh',                 'son_la',      212),
    (15, 'Tỉnh Lào Cai',          'tỉnh',                 'lao_cai',     214),
    (19, 'Tỉnh Thái Nguyên',      'tỉnh',                 'thai_nguyen', 208),
    (20, 'Tỉnh Lạng Sơn',         'tỉnh',                 'lang_son',    205),
    (22, 'Tỉnh Quảng Ninh',       'tỉnh',                 'quang_ninh',  203),
    (24, 'Tỉnh Bắc Ninh',         'tỉnh',                 'bac_ninh',    222),
    (25, 'Tỉnh Phú Thọ',          'tỉnh',                 'phu_tho',     210),
    (31, 'Thành phố Hải Phòng',   'thành phố trung ương', 'hai_phong',   225),
    (33, 'Tỉnh Hưng Yên',         'tỉnh',                 'hung_yen',    221),
    (37, 'Tỉnh Ninh Bình',        'tỉnh',                 'ninh_binh',   229),
    (38, 'Tỉnh Thanh Hóa',        'tỉnh',                 'thanh_hoa',   237),
    (40, 'Tỉnh Nghệ An',          'tỉnh',                 'nghe_an',     238),
    (42, 'Tỉnh Hà Tĩnh',          'tỉnh',                 'ha_tinh',     239),
    (44, 'Tỉnh Quảng Trị',        'tỉnh',                 'quang_tri',   233),
    (46, 'Thành phố Huế',         'thành phố trung ương', 'hue',         234),
    (48, 'Thành phố Đà Nẵng',     'thành phố trung ương', 'da_nang',     236),
    (51, 'Tỉnh Quảng Ngãi',       'tỉnh',                 'quang_ngai',  255),
    (52, 'Tỉnh Gia Lai',          'tỉnh',                 'gia_lai',     269),
    (56, 'Tỉnh Khánh Hòa',        'tỉnh',                 'khanh_hoa',   258),
    (66, 'Tỉnh Đắk Lắk',          'tỉnh',                 'dak_lak',     262),
    (68, 'Tỉnh Lâm Đồng',         'tỉnh',                 'lam_dong',    263),
    (75, 'Tỉnh Đồng Nai',         'tỉnh',                 'dong_nai',    251),
    (79, 'Thành phố Hồ Chí Minh', 'thành phố trung ương', 'ho_chi_minh', 28),
    (80, 'Tỉnh Tây Ninh',         'tỉnh',                 'tay_ninh',    276),
    (82, 'Tỉnh Đồng Tháp',        'tỉnh',                 'dong_thap',   277),
    (86, 'Tỉnh Vĩnh Long',        'tỉnh',                 'vinh_long',   270),
    (91, 'Tỉnh An Giang',         'tỉnh',                 'an_giang',    296),
    (92, 'Thành phố Cần Thơ',     'thành phố trung ương', 'can_tho',     292),
    (96, 'Tỉnh Cà Mau',           'tỉnh',                 'ca_mau',      290)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    division_type = EXCLUDED.division_type,
    codename = EXCLUDED.codename,
    phone_code = EXCLUDED.phone_code;

CREATE INDEX IF NOT EXISTS idx_provinces_codename
    ON provinces (codename);
