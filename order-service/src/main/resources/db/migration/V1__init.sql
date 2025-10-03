--CREATE TABLE `order_service`
--{
--`id` bigint(20) NOT NULL AUTO_INCREMENT,
--`order_number` varchar(255) DEFAULT NULL,
--`sky_code` varchar(255),
--`price` decimal(19,2),
--`quantity` int(11)
--PRIMARY KEY(`id`)
--};

CREATE TABLE order_service (
    id BIGSERIAL PRIMARY KEY,
    order_name VARCHAR(255),
    sku_code VARCHAR(255),
    price NUMERIC(19,2),
    quantity INT
);