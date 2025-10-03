-- src/main/resources/db/migration/V2__insert_inventory_data.sql
INSERT INTO t_inventory (quantity, sku_code)
VALUES  (100, 'iphone_15'),
        (2, 'sofa'),
        (3, 'bed'),
        (3, 'wall paper');