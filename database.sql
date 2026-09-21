-- run this database file first in MySql before luanching the PIMS system

-- Drop and recreate schema
DROP DATABASE IF EXISTS pims_database;
CREATE DATABASE pims_database
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci; -- allows comparison of strings in a case-insensitive manner
                                -- eg. if username is stored as 'admin', and a query like 
                                -- `WHERE username = 'Admin'` is executed, it will still 
                                -- match the stored username 'admin' because of the case-insensitive collation
USE pims_database;

-- tables
-- user table
CREATE TABLE users (
    user_id   INT          NOT NULL AUTO_INCREMENT,
    username  VARCHAR(50)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      ENUM('Admin','Cashier') NOT NULL DEFAULT 'Cashier',
    full_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id)
);

-- suppliers
CREATE TABLE suppliers (
    supplier_id     INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20),
    email           VARCHAR(100),
    address         TEXT,
    PRIMARY KEY (supplier_id)
);

-- medicine table
CREATE TABLE medicines (
    medicine_id       INT            NOT NULL AUTO_INCREMENT,
    name              VARCHAR(150)   NOT NULL,
    company           VARCHAR(100),
    medicine_type     VARCHAR(50),
    price             DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    quantity_in_stock INT            NOT NULL DEFAULT 0,
    reorder_level     INT            NOT NULL DEFAULT 10,
    expiry_date       DATE,
    supplier_id       INT,
    PRIMARY KEY (medicine_id),
    CONSTRAINT fk_medicine_supplier -- gives the foreign key relationship the name fk_medicine_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) -- ensures that the supplier_id in the medicines table must exist in the suppliers table
        ON DELETE SET NULL -- if a supplier is deleted, the supplier_id in the medicines table will be set to NULL  

);

-- sales table
CREATE TABLE sales (
    sale_id      INT           NOT NULL AUTO_INCREMENT,
    sale_date    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    user_id      INT,
    PRIMARY KEY (sale_id),
    CONSTRAINT fk_sale_user -- gives the foreign key relationship the name fk_sale_user 
        FOREIGN KEY (user_id) REFERENCES users(user_id) -- ensures that the user_id in the sales table must exist in the users table
        ON DELETE SET NULL -- if a user is deleted, the user_id in the sales table will be set to NULL
);


-- sales items table
CREATE TABLE sale_items (
    sale_item_id   INT           NOT NULL AUTO_INCREMENT,
    sale_id        INT           NOT NULL,
    medicine_id    INT,
    quantity_sold  INT           NOT NULL DEFAULT 1,
    price_at_sale  DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (sale_item_id),
    CONSTRAINT fk_item_sale
        FOREIGN KEY (sale_id) REFERENCES sales(sale_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_item_medicine -- gives the foreign key relationship the name fk_item_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) -- ensures that the medicine_id in the sale_items table must exist in the medicines table
        ON DELETE SET NULL -- if a medicine is deleted, the medicine_id in the sale_items table will be set to NULL
);

-- seeding data used for testing purposes

-- seeding users table
-- two admins and three cashiers, each with their own name
INSERT INTO users (username, password, role, full_name) VALUES
('admin',    'admin123',   'Admin',   'Naledi Khumalo'),
('admin2',   'admin456',   'Admin',   'Johan Botha'),
('cashier',  'cash123',    'Cashier', 'Palesa Mokoena'),
('cashier2', 'cash456',    'Cashier', 'Ryan Govender'),
('cashier3', 'cash789',    'Cashier', 'Zanele Mahlangu');

-- seeding suppliers table
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('MediCo Distributors',   'Thabo Molefe',  '011-456-7890', 'thabo@medico.co.za',   '45 Park Lane, Sandton, GP'),
('PharmAssist SA',        'Ayesha Patel',  '021-789-0123', 'ayesha@pharmassist.za','12 Bree Street, Cape Town, WC'),
('LifeLine Pharma',       'Rudi van Dyk',  '031-234-5678', 'rudi@lifeline.co.za',  '78 Umgeni Road, Durban, KZN'),
('Genesis Healthcare',    'Nomsa Zulu',    '012-345-6789', 'nomsa@genesis.co.za',  '33 Church Street, Pretoria, GP'),
('African Health Supply', 'Peter Cronje',  '011-567-8901', 'peter@ahs.co.za',      '9 Goldman Street, Joburg, GP');

-- seeding medicines table
INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Panado 500mg',           'Adcock Ingram',   'Tablet',    12.50,  250,  30, '2026-08-15', 1),
('Amoxicillin 250mg',      'Aspen Pharma',    'Capsule',   45.00,  180,  20, '2025-12-31', 2),
('Brufen 400mg',           'Abbott SA',       'Tablet',    18.75,  120,  25, '2026-03-20', 1),
('Cough Syrup Benylin',    'J&J SA',          'Syrup',     55.99,  75,   15, '2025-10-10', 3),
('Insulin Actrapid 10ml',  'Novo Nordisk SA', 'Injection', 189.00, 40,   10, '2025-11-30', 4),
('Hydrocortisone 1% Cream','Pharmos SA',      'Cream',     32.50,  90,   15, '2026-06-01', 2),
('Vitamin C 1000mg',       'Dis-Chem Labs',   'Tablet',    25.00,  300,  50, '2027-01-20', 5),
('Losartan 50mg',          'Cipla Medpro',    'Tablet',    64.00,  60,   15, '2026-09-30', 3),
('Metformin 500mg',        'Aspen Pharma',    'Tablet',    38.00,  95,   20, '2026-07-01', 2),
('Eye Drops Visine',       'J&J SA',          'Drops',     79.99,  8,    10, '2025-09-05', 4),
('Atenolol 50mg',          'Adcock Ingram',   'Tablet',    42.50,  4,    15, '2025-10-15', 1),
('Chloramphenicol Oint',   'Pharmos SA',      'Ointment',  29.00,  55,   12, '2026-02-28', 5),
('Salbutamol Inhaler',     'GSK SA',          'Injection', 110.00, 35,   10, '2025-12-20', 4),
('Dettol 500ml',           'Reckitt SA',      'Syrup',     49.95,  150,  20, '2027-03-31', 5),
('Prednisone 5mg',         'Cipla Medpro',    'Tablet',    22.00,  70,   15, '2026-05-15', 3);

-- seeding historical sales, for reports
-- user_id 3 = Palesa Mokoena, 4 = Ryan Govender, 5 = Zanele Mahlangu
INSERT INTO sales (sale_date, total_amount, user_id) VALUES
('2025-07-11 08:42:00',  70.25, 3),
('2025-07-12 13:05:00', 189.00, 4),
('2025-07-13 16:50:00',  58.25, 5),
('2025-07-14 09:20:00', 141.75, 3),
('2025-07-15 17:35:00',  85.50, 4);

INSERT INTO sale_items (sale_id, medicine_id, quantity_sold, price_at_sale) VALUES
(1, 1, 2, 12.50),
(1, 3, 1, 18.75),
(1, 7, 1, 25.00),
(2, 5, 1, 189.00),
(3, 4, 1, 55.99),
(3, 1, 1, 12.50),
(4, 8, 1, 64.00),
(4, 9, 1, 38.00),
(4, 7, 1, 25.00),
(5, 6, 1, 32.50),
(5, 3, 1, 18.75),
(5, 13, 1, 34.25);
