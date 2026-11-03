-- public.provinces definition

-- Drop table

-- DROP TABLE public.provinces;

CREATE TABLE public.provinces (
                                  code int4 NOT NULL,
                                  phone_code int4 NOT NULL,
                                  division_type varchar(50) NOT NULL,
                                  codename varchar(100) NOT NULL,
                                  "name" varchar(100) NOT NULL,
                                  CONSTRAINT provinces_code_not_null NOT NULL code,
                                  CONSTRAINT provinces_codename_not_null NOT NULL codename,
                                  CONSTRAINT provinces_division_type_not_null NOT NULL division_type,
                                  CONSTRAINT provinces_name_not_null NOT NULL name,
                                  CONSTRAINT provinces_phone_code_not_null NOT NULL phone_code,
                                  CONSTRAINT provinces_pkey PRIMARY KEY (code),
                                  CONSTRAINT uk_provinces_codename UNIQUE (codename)
);


-- public.wards definition

-- Drop table

-- DROP TABLE public.wards;

CREATE TABLE public.wards (
                              code int4 NOT NULL,
                              province_code int4 NOT NULL,
                              division_type varchar(50) NOT NULL,
                              codename varchar(100) NOT NULL,
                              "name" varchar(100) NOT NULL,
                              CONSTRAINT wards_code_not_null NOT NULL code,
                              CONSTRAINT wards_codename_not_null NOT NULL codename,
                              CONSTRAINT wards_division_type_not_null NOT NULL division_type,
                              CONSTRAINT wards_name_not_null NOT NULL name,
                              CONSTRAINT wards_pkey PRIMARY KEY (code),
                              CONSTRAINT wards_province_code_not_null NOT NULL province_code,
                              CONSTRAINT fk_wards_province FOREIGN KEY (province_code) REFERENCES public.provinces(code)
);
CREATE INDEX idx_wards_codename ON public.wards USING btree (codename);
CREATE INDEX idx_wards_province_code ON public.wards USING btree (province_code);


-- public.restaurants definition

-- Drop table

-- DROP TABLE public.restaurants;

CREATE TABLE public.restaurants (
                                    ward_code int4 NOT NULL,
                                    created_at timestamptz(6) NOT NULL,
                                    deleted_at timestamptz(6) NULL,
                                    updated_at timestamptz(6) NOT NULL,
                                    "version" int8 NOT NULL,
                                    created_by uuid NULL,
                                    delete_batch_id uuid NULL,
                                    deleted_by uuid NULL,
                                    id uuid NOT NULL,
                                    owner_id uuid NOT NULL,
                                    updated_by uuid NULL,
                                    phone varchar(30) NOT NULL,
                                    status varchar(30) NOT NULL,
                                    address_line varchar(255) NOT NULL,
                                    "name" varchar(255) NOT NULL,
                                    CONSTRAINT restaurants_address_line_not_null NOT NULL address_line,
                                    CONSTRAINT restaurants_created_at_not_null NOT NULL created_at,
                                    CONSTRAINT restaurants_id_not_null NOT NULL id,
                                    CONSTRAINT restaurants_name_not_null NOT NULL name,
                                    CONSTRAINT restaurants_owner_id_not_null NOT NULL owner_id,
                                    CONSTRAINT restaurants_phone_not_null NOT NULL phone,
                                    CONSTRAINT restaurants_pkey PRIMARY KEY (id),
                                    CONSTRAINT restaurants_status_check CHECK (((status)::text = ANY ((ARRAY['OPENING'::character varying, 'CLOSED'::character varying])::text[]))),
	CONSTRAINT restaurants_status_not_null NOT NULL status,
	CONSTRAINT restaurants_updated_at_not_null NOT NULL updated_at,
	CONSTRAINT restaurants_version_not_null NOT NULL version,
	CONSTRAINT restaurants_ward_code_not_null NOT NULL ward_code,
	CONSTRAINT fk_restaurants_ward FOREIGN KEY (ward_code) REFERENCES public.wards(code)
);


-- public.menu_categories definition

-- Drop table

-- DROP TABLE public.menu_categories;

CREATE TABLE public.menu_categories (
                                        display_order int4 NULL,
                                        created_at timestamptz(6) NOT NULL,
                                        deleted_at timestamptz(6) NULL,
                                        updated_at timestamptz(6) NOT NULL,
                                        "version" int8 NOT NULL,
                                        created_by uuid NULL,
                                        delete_batch_id uuid NULL,
                                        deleted_by uuid NULL,
                                        id uuid NOT NULL,
                                        restaurant_id uuid NOT NULL,
                                        updated_by uuid NULL,
                                        status varchar(30) NOT NULL,
                                        "name" varchar(100) NOT NULL,
                                        CONSTRAINT menu_categories_created_at_not_null NOT NULL created_at,
                                        CONSTRAINT menu_categories_id_not_null NOT NULL id,
                                        CONSTRAINT menu_categories_name_not_null NOT NULL name,
                                        CONSTRAINT menu_categories_pkey PRIMARY KEY (id),
                                        CONSTRAINT menu_categories_restaurant_id_not_null NOT NULL restaurant_id,
                                        CONSTRAINT menu_categories_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[]))),
	CONSTRAINT menu_categories_status_not_null NOT NULL status,
	CONSTRAINT menu_categories_updated_at_not_null NOT NULL updated_at,
	CONSTRAINT menu_categories_version_not_null NOT NULL version,
	CONSTRAINT fkkx2gof9mxsye30ju5et6a865p FOREIGN KEY (restaurant_id) REFERENCES public.restaurants(id)
);


-- public.menu_items definition

-- Drop table

-- DROP TABLE public.menu_items;

CREATE TABLE public.menu_items (
                                   base_price numeric(19, 2) NOT NULL,
                                   display_order int4 NULL,
                                   created_at timestamptz(6) NOT NULL,
                                   deleted_at timestamptz(6) NULL,
                                   updated_at timestamptz(6) NOT NULL,
                                   "version" int8 NOT NULL,
                                   category_id uuid NOT NULL,
                                   created_by uuid NULL,
                                   delete_batch_id uuid NULL,
                                   deleted_by uuid NULL,
                                   id uuid NOT NULL,
                                   updated_by uuid NULL,
                                   status varchar(30) NOT NULL,
                                   description varchar(255) NULL,
                                   image_url varchar(255) NULL,
                                   "name" varchar(255) NOT NULL,
                                   CONSTRAINT menu_items_base_price_not_null NOT NULL base_price,
                                   CONSTRAINT menu_items_category_id_not_null NOT NULL category_id,
                                   CONSTRAINT menu_items_created_at_not_null NOT NULL created_at,
                                   CONSTRAINT menu_items_id_not_null NOT NULL id,
                                   CONSTRAINT menu_items_name_not_null NOT NULL name,
                                   CONSTRAINT menu_items_pkey PRIMARY KEY (id),
                                   CONSTRAINT menu_items_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SOLD_OUT'::character varying])::text[]))),
	CONSTRAINT menu_items_status_not_null NOT NULL status,
	CONSTRAINT menu_items_updated_at_not_null NOT NULL updated_at,
	CONSTRAINT menu_items_version_not_null NOT NULL version,
	CONSTRAINT fk4pc1grgsms7nqm2i6oig37pro FOREIGN KEY (category_id) REFERENCES public.menu_categories(id)
);


-- public.option_groups definition

-- Drop table

-- DROP TABLE public.option_groups;

CREATE TABLE public.option_groups (
                                      created_at timestamptz(6) NOT NULL,
                                      deleted_at timestamptz(6) NULL,
                                      updated_at timestamptz(6) NOT NULL,
                                      "version" int8 NOT NULL,
                                      created_by uuid NULL,
                                      delete_batch_id uuid NULL,
                                      deleted_by uuid NULL,
                                      id uuid NOT NULL,
                                      restaurant_id uuid NOT NULL,
                                      updated_by uuid NULL,
                                      status varchar(30) NOT NULL,
                                      "name" varchar(255) NOT NULL,
                                      CONSTRAINT option_groups_created_at_not_null NOT NULL created_at,
                                      CONSTRAINT option_groups_id_not_null NOT NULL id,
                                      CONSTRAINT option_groups_name_not_null NOT NULL name,
                                      CONSTRAINT option_groups_pkey PRIMARY KEY (id),
                                      CONSTRAINT option_groups_restaurant_id_not_null NOT NULL restaurant_id,
                                      CONSTRAINT option_groups_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying])::text[]))),
	CONSTRAINT option_groups_status_not_null NOT NULL status,
	CONSTRAINT option_groups_updated_at_not_null NOT NULL updated_at,
	CONSTRAINT option_groups_version_not_null NOT NULL version,
	CONSTRAINT fkt4dgpgpf7x9npwd33bsty7shn FOREIGN KEY (restaurant_id) REFERENCES public.restaurants(id)
);


-- public."options" definition

-- Drop table

-- DROP TABLE public."options";

CREATE TABLE public."options" (
                                  display_order int4 NULL,
                                  created_at timestamptz(6) NOT NULL,
                                  deleted_at timestamptz(6) NULL,
                                  updated_at timestamptz(6) NOT NULL,
                                  "version" int8 NOT NULL,
                                  created_by uuid NULL,
                                  delete_batch_id uuid NULL,
                                  deleted_by uuid NULL,
                                  id uuid NOT NULL,
                                  option_group_id uuid NOT NULL,
                                  updated_by uuid NULL,
                                  status varchar(30) NOT NULL,
                                  "name" varchar(255) NOT NULL,
                                  CONSTRAINT options_created_at_not_null NOT NULL created_at,
                                  CONSTRAINT options_id_not_null NOT NULL id,
                                  CONSTRAINT options_name_not_null NOT NULL name,
                                  CONSTRAINT options_option_group_id_not_null NOT NULL option_group_id,
                                  CONSTRAINT options_pkey PRIMARY KEY (id),
                                  CONSTRAINT options_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'INACTIVE'::character varying, 'SOLD_OUT'::character varying])::text[]))),
	CONSTRAINT options_status_not_null NOT NULL status,
	CONSTRAINT options_updated_at_not_null NOT NULL updated_at,
	CONSTRAINT options_version_not_null NOT NULL version,
	CONSTRAINT fk3d2wqyaloehtbx2xpc0qbe91s FOREIGN KEY (option_group_id) REFERENCES public.option_groups(id)
);


-- public.menu_item_option_groups definition

-- Drop table

-- DROP TABLE public.menu_item_option_groups;

CREATE TABLE public.menu_item_option_groups (
                                                display_order int4 NOT NULL,
                                                max_select int4 NOT NULL,
                                                min_select int4 NOT NULL,
                                                created_at timestamptz(6) NOT NULL,
                                                deleted_at timestamptz(6) NULL,
                                                updated_at timestamptz(6) NOT NULL,
                                                "version" int8 NOT NULL,
                                                created_by uuid NULL,
                                                delete_batch_id uuid NULL,
                                                deleted_by uuid NULL,
                                                id uuid NOT NULL,
                                                menu_item_id uuid NOT NULL,
                                                option_group_id uuid NOT NULL,
                                                updated_by uuid NULL,
                                                CONSTRAINT menu_item_option_groups_created_at_not_null NOT NULL created_at,
                                                CONSTRAINT menu_item_option_groups_display_order_not_null NOT NULL display_order,
                                                CONSTRAINT menu_item_option_groups_id_not_null NOT NULL id,
                                                CONSTRAINT menu_item_option_groups_max_select_not_null NOT NULL max_select,
                                                CONSTRAINT menu_item_option_groups_menu_item_id_not_null NOT NULL menu_item_id,
                                                CONSTRAINT menu_item_option_groups_min_select_not_null NOT NULL min_select,
                                                CONSTRAINT menu_item_option_groups_option_group_id_not_null NOT NULL option_group_id,
                                                CONSTRAINT menu_item_option_groups_pkey PRIMARY KEY (id),
                                                CONSTRAINT menu_item_option_groups_updated_at_not_null NOT NULL updated_at,
                                                CONSTRAINT menu_item_option_groups_version_not_null NOT NULL version,
                                                CONSTRAINT uk_menu_item_option_group UNIQUE (menu_item_id, option_group_id),
                                                CONSTRAINT fk53mvigbyoy2w9a2lq7nkq6xnv FOREIGN KEY (menu_item_id) REFERENCES public.menu_items(id)
);


-- public.menu_item_option_prices definition

-- Drop table

-- DROP TABLE public.menu_item_option_prices;

CREATE TABLE public.menu_item_option_prices (
                                                additional_price numeric(19, 2) NOT NULL,
                                                created_at timestamptz(6) NOT NULL,
                                                deleted_at timestamptz(6) NULL,
                                                updated_at timestamptz(6) NOT NULL,
                                                "version" int8 NOT NULL,
                                                created_by uuid NULL,
                                                delete_batch_id uuid NULL,
                                                deleted_by uuid NULL,
                                                id uuid NOT NULL,
                                                menu_item_option_group_id uuid NOT NULL,
                                                option_id uuid NOT NULL,
                                                updated_by uuid NULL,
                                                CONSTRAINT menu_item_option_prices_additional_price_not_null NOT NULL additional_price,
                                                CONSTRAINT menu_item_option_prices_created_at_not_null NOT NULL created_at,
                                                CONSTRAINT menu_item_option_prices_id_not_null NOT NULL id,
                                                CONSTRAINT menu_item_option_prices_menu_item_option_group_id_not_null NOT NULL menu_item_option_group_id,
                                                CONSTRAINT menu_item_option_prices_option_id_not_null NOT NULL option_id,
                                                CONSTRAINT menu_item_option_prices_pkey PRIMARY KEY (id),
                                                CONSTRAINT menu_item_option_prices_updated_at_not_null NOT NULL updated_at,
                                                CONSTRAINT menu_item_option_prices_version_not_null NOT NULL version,
                                                CONSTRAINT uk_menu_item_group_option_price UNIQUE (menu_item_option_group_id, option_id),
                                                CONSTRAINT fkmraxbs35jrhcrja83ouwwupji FOREIGN KEY (menu_item_option_group_id) REFERENCES public.menu_item_option_groups(id)
);