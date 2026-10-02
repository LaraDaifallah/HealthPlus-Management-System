-- Publication copy: contact records and account passwords are synthetic demo values.
drop database if exists healthplus;
create database healthplus;
use healthplus;

create table Category (
    CategoryID int auto_increment primary key,
    Name varchar(100) not null,
    Description text
);

create table Product (
    ProductID int auto_increment primary key,
    ProductName varchar(150) not null,
    Description text,
    UnitPrice decimal(10,2) not null,
    ReorderLevel int not null default 10,
    CategoryID int not null,
    foreign key (CategoryID) references Category(CategoryID)
        on delete cascade
        on update cascade
);

create table Warehouse (
    WarehouseID int auto_increment primary key,
    WarehouseName varchar(150) not null,
    Address varchar(200),
    City varchar(80),
    Phone varchar(30),
    Capacity int
);

create table Batch (
    BatchID int auto_increment primary key,
    ProductID int not null,
    WarehouseID int not null,
    QtyInStock int not null default 0,
    ExpiryDate date,
    StorageLocation varchar(80),
    foreign key (ProductID) references Product(ProductID)
        on delete cascade
        on update cascade,
    foreign key (WarehouseID) references Warehouse(WarehouseID)
        on delete cascade
        on update cascade
);

create table Supplier (
    SupplierID int auto_increment primary key,
    SupplierName varchar(150) not null,
    ContactPerson varchar(100),
    Phone varchar(30),
    Email varchar(100),
    City varchar(80)
);

create table SupplierProduct (
    SupplierID int not null,
    ProductID int not null,
    UnitCost decimal(10,2) not null,
    primary key (SupplierID, ProductID),
    foreign key (SupplierID) references Supplier(SupplierID)
        on delete cascade
        on update cascade,
    foreign key (ProductID) references Product(ProductID)
        on delete cascade
        on update cascade
);

create table Client (
    ClientID int auto_increment primary key,
    ClientName varchar(150) not null,
    ClientType enum('Pharmacy','Clinic') not null,
    Phone varchar(30),
    City varchar(80),
    CreditLimit decimal(12,2) default 5000.00
);

create table Employee (
    EmployeeID int auto_increment primary key,
    FirstName varchar(80) not null,
    LastName varchar(80) not null,
    Role varchar(80) not null,
    HireDate date not null,
    Phone varchar(30),
    Salary decimal(10,2)
);

create table PurchaseOrder (
    PONumber int auto_increment primary key,
    SupplierID int not null,
    EmployeeID int not null,
    OrderDate date not null,
    ExpDeliveryDate date,
    Status enum('Pending','Delivered','Cancelled') default 'Pending',
    TotalAmount decimal(12,2) default 0.00,
    foreign key (SupplierID) references Supplier(SupplierID)
        on delete cascade
        on update cascade,
    foreign key (EmployeeID) references Employee(EmployeeID)
        on delete cascade
        on update cascade
);

create table PurchaseOrderItem (
    POItemID int auto_increment,
    PONumber int not null,
    ProductID int not null,
    QtyOrdered int not null,
    UnitCost decimal(10,2) not null,
    QtyReceived int default 0,
    primary key (POItemID, PONumber),
    foreign key (PONumber) references PurchaseOrder(PONumber)
        on delete cascade
        on update cascade,
    foreign key (ProductID) references Product(ProductID)
        on delete cascade
        on update cascade
);

create table SaleOrder (
    SaleOrderID int auto_increment primary key,
    ClientID int not null,
    EmployeeID int not null,
    OrderDate date not null,
    DeliveryDate date,
Status enum('Pending','Approved','Delivered','Cancelled') default 'Pending',    TotalAmount decimal(12,2) default 0.00,
    PaymentStatus enum('Paid','Unpaid','Partial') default 'Unpaid',
    foreign key (ClientID) references Client(ClientID)
        on delete cascade
        on update cascade,
    foreign key (EmployeeID) references Employee(EmployeeID)
        on delete cascade
        on update cascade
);

create table SaleOrderItem (
    SaleItemID int auto_increment,
    SaleOrderID int not null,
    BatchID int not null,
    QtyOrdered int not null,
    UnitPrice decimal(10,2) not null,
    Discount decimal(5,2) default 0.00,
    primary key (SaleItemID, SaleOrderID),
    foreign key (SaleOrderID) references SaleOrder(SaleOrderID)
        on delete cascade
        on update cascade,
    foreign key (BatchID) references Batch(BatchID)
        on delete cascade
        on update cascade
);

create table InventoryTransaction (
    TransactionID int auto_increment primary key,
    BatchID int not null,
    EmployeeID int not null,
    TxnType enum('Receipt','Dispatch','Adjustment') not null,
    Quantity int not null,
    TxnDate datetime default current_timestamp,
    ReferenceID int,
    foreign key (BatchID) references Batch(BatchID)
        on delete cascade
        on update cascade,
    foreign key (EmployeeID) references Employee(EmployeeID)
        on delete cascade
        on update cascade
);

create table Payment (
    PaymentID int auto_increment primary key,
    SaleOrderID int,
    PONumber int,
    PaymentDate date not null,
    Amount decimal(12,2) not null,
    PaymentMethod enum('Cash','BankTransfer','Cheque') not null,
    Direction enum('Incoming','Outgoing') not null,
    foreign key (SaleOrderID) references SaleOrder(SaleOrderID)
        on delete cascade
        on update cascade,
    foreign key (PONumber) references PurchaseOrder(PONumber)
        on delete cascade
        on update cascade
);

CREATE TABLE UserAccount (
    UserID     INT AUTO_INCREMENT PRIMARY KEY,
    Password   VARCHAR(50) NOT NULL,
    Role       ENUM('Admin', 'Employee', 'Client') NOT NULL,
    EmployeeID INT,
    ClientID   INT,
    FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (ClientID) REFERENCES Client(ClientID)
        ON DELETE CASCADE ON UPDATE CASCADE
);




insert into Category (Name, Description) values
('Medications',    'Medicines and treatments'),
('Skincare',       'Skin care products'),
('Childrens Care', 'Products for children'),
('Shampoos',       'Hair care products'),
('Personal Care',  'General personal care products'),
('Vitamins',       'Vitamin and mineral supplements'),
('Medical Devices','Health monitoring devices'),
('Dental Care',    'Dental hygiene products'),
('Baby Care',      'Baby feeding and care accessories'),
('First Aid',      'Emergency and wound care supplies');

INSERT INTO Warehouse (WarehouseName, Address, City, Phone, Capacity) VALUES
('Demo WarehouseName 1', 'Demo address 1', 'Demo City 1', 'DEMO-PHONE-1', 10000),
('Demo WarehouseName 2', 'Demo address 2', 'Nablus', 'DEMO-PHONE-2', 8000),
('Demo WarehouseName 3', 'Demo address 3', 'Jenin', 'DEMO-PHONE-3', 5000);
 
INSERT INTO Supplier (SupplierName, ContactPerson, Phone, Email, City) VALUES
('Demo SupplierName 1', 'Demo ContactPerson 1', 'DEMO-PHONE-1', 'demo1@example.invalid', 'Demo City 1'),
('Demo SupplierName 2', 'Demo ContactPerson 2', 'DEMO-PHONE-2', 'demo2@example.invalid', 'Amman'),
('Demo SupplierName 3', 'Demo ContactPerson 3', 'DEMO-PHONE-3', 'demo3@example.invalid', 'Nablus'),
('Demo SupplierName 4', 'Demo ContactPerson 4', 'DEMO-PHONE-4', 'demo4@example.invalid', 'Demo City 1'),
('Demo SupplierName 5', 'Demo ContactPerson 5', 'DEMO-PHONE-5', 'demo5@example.invalid', 'Dubai'),
('Demo SupplierName 6', 'Demo ContactPerson 6', 'DEMO-PHONE-6', 'demo6@example.invalid', 'Bethlehem');
 
INSERT INTO Employee (FirstName, LastName, Role, HireDate, Phone, Salary) VALUES
('Demo FirstName 1', 'Demo LastName 1', 'Warehouse Manager', '2020-01-15', 'DEMO-PHONE-1', 3500.00),
('Demo FirstName 2', 'Demo LastName 2', 'Sales Officer', '2021-03-01', 'DEMO-PHONE-2', 2800.00),
('Demo FirstName 3', 'Demo LastName 3', 'Procurement Officer', '2019-06-20', 'DEMO-PHONE-3', 2900.00),
('Demo FirstName 4', 'Demo LastName 4', 'Sales Officer', '2022-07-10', 'DEMO-PHONE-4', 2750.00),
('Demo FirstName 5', 'Demo LastName 5', 'Procurement Officer', '2023-01-05', 'DEMO-PHONE-5', 2850.00);
 
insert into Product (ProductName, Description, UnitPrice, ReorderLevel, CategoryID) values
-- Medications
('Panadol Extra', 'Pain relief tablets',4.50,  50, 1),
('Voltaren Gel',         'Anti-inflammatory gel',           12.00,  20, 1),
('Augmentin 625mg',      'Antibiotic tablets',              18.00,  20, 1),
('Flagyl 500mg',         'Antibiotic medication',            9.50,  20, 1),
('Brufen 600mg',         'Pain relief tablets',              5.50,  40, 1),
('Aspirin 100mg',        'Blood thinner',                    3.50,  30, 1),
('Cetirizine 10mg',      'Allergy medication',               7.50,  30, 1),
('Loratadine',           'Antihistamine',                    8.00,  25, 1),
('Omeprazole 20mg',      'Acid reflux treatment',           10.00,  30, 1),
('Nexium 40mg',          'Stomach medication',              16.00,  20, 1),
('Paracetamol Syrup',    'Children fever medicine',          6.00,  20, 1),
('Amoxil Syrup',         'Children antibiotic',             12.00,  15, 1),
('Zyrtec Syrup',         'Allergy syrup',                   11.00,  15, 1),
('Cataflam 50mg',        'Pain relief medicine',             6.50,  30, 1),
('Diclofenac Injection', 'Pain injection',                   9.00,  15, 1),
('Vitamin C 1000mg',     'Supplement',                      15.00,  20, 1),
('Vitamin D3',           'Supplement',                      18.00,  20, 1),
('Omega 3 Capsules',     'Supplement',                      25.00,  15, 1),
('Iron Tablets',         'Iron supplement',                  8.50,  20, 1),
('Calcium Tablets',      'Bone supplement',                 12.50,  20, 1),
-- Skincare 
('Nivea Soft Cream',     'Moisturizer',                     12.00,  20, 2),
('Nivea Creme',          'Moisturizer',                     13.00,  20, 2),
('Eucerin Lotion',       'Skin moisturizer',                22.00,  15, 2),
('Cetaphil Moisturizer', 'Sensitive skin lotion',           28.00,  10, 2),
('Cetaphil Cleanser',    'Facial cleanser',                 25.00,  15, 2),
('Bioderma Sensibio',    'Micellar water',                  30.00,  10, 2),
('La Roche Posay Cleanser','Face cleanser',                 40.00,  10, 2),
('La Roche Moisturizer', 'Face moisturizer',                45.00,  10, 2),
('Vaseline Petroleum Jelly','Skin protection',               8.00,  20, 2),
('Vaseline Cocoa Butter','Body lotion',                     14.00,  15, 2),
('Aloe Vera Gel',        'Skin soothing gel',               10.00,  15, 2),
('Lip Balm Cherry',      'Lip care',                         5.00,  25, 2),
('Lip Balm Original',    'Lip care',                         5.00,  25, 2),
('Hand Cream',           'Moisturizing cream',               7.00,  20, 2),
('Body Lotion',          'Body moisturizer',                12.00,  20, 2),
-- Childrens Care
('Baby Shampoo',         'Gentle shampoo',                   9.50,  15, 3),
('Baby Lotion',          'Baby moisturizer',                14.00,  15, 3),
('Baby Oil',             'Baby massage oil',                11.00,  15, 3),
('Baby Powder',          'Baby powder',                      8.00,  20, 3),
('Baby Wipes Small',     'Wet wipes',                        6.00,  30, 3),
('Baby Wipes Large',     'Wet wipes',                       10.00,  25, 3),
('Baby Diapers Size 1',  'Diapers',                         40.00,  20, 3),
('Baby Diapers Size 2',  'Diapers',                         42.00,  20, 3),
('Baby Diapers Size 3',  'Diapers',                         45.00,  20, 3),
('Baby Diapers Size 4',  'Diapers',                         48.00,  20, 3),
('Baby Diapers Size 5',  'Diapers',                         50.00,  20, 3),
('Baby Feeding Bottle',  'Bottle',                          15.00,  10, 3),
('Baby Pacifier',        'Pacifier',                         7.00,  15, 3),
('Baby Rash Cream',      'Skin cream',                       9.00,  15, 3),
('Baby Bath Soap',       'Baby soap',                        6.50,  20, 3),
-- Shampoos 
('Head & Shoulders',     'Anti-dandruff shampoo',           11.00,  25, 4),
('Pantene Shampoo',      'Hair care shampoo',               13.00,  20, 4),
('Clear Shampoo',        'Anti-dandruff shampoo',           12.00,  20, 4),
('Dove Shampoo',         'Hair repair shampoo',             13.00,  20, 4),
('Sunsilk Shampoo',      'Hair care shampoo',               10.00,  20, 4),
('Herbal Essences Shampoo','Herbal shampoo',                16.00,  15, 4),
('Tresemme Shampoo',     'Professional shampoo',            18.00,  15, 4),
('Johnson Baby Shampoo', 'Baby shampoo',                     9.00,  20, 4),
('Anti Hair Loss Shampoo','Hair strengthening',             20.00,  10, 4),
('Keratin Shampoo',      'Hair treatment',                  22.00,  10, 4),
('Argan Oil Shampoo',    'Hair care',                       19.00,  10, 4),
('Mint Shampoo',         'Refreshing shampoo',              12.00,  15, 4),
('Aloe Vera Shampoo',    'Natural shampoo',                 13.00,  15, 4),
('Color Protect Shampoo','Colored hair shampoo',            17.00,  10, 4),
('Daily Use Shampoo',    'General shampoo',                  9.00,  20, 4),
-- Personal Care 
('Dettol Hand Wash',     'Hand wash',                        6.80,  30, 5),
('Dove Soap',            'Soap',                             4.00,  40, 5),
('Lux Soap',             'Soap',                             3.50,  40, 5),
('Palmolive Soap',       'Soap',                             3.75,  40, 5),
('Toothpaste Colgate',   'Toothpaste',                       6.00,  30, 5),
('Toothpaste Sensodyne', 'Sensitive teeth',                  9.00,  20, 5),
('Toothbrush Soft',      'Toothbrush',                       4.00,  30, 5),
('Toothbrush Medium',    'Toothbrush',                       4.00,  30, 5),
('Mouth Wash',           'Oral hygiene',                    12.00,  15, 5),
('Dental Floss',         'Dental care',                      7.00,  20, 5),
('Shaving Cream',        'Shaving product',                  8.00,  20, 5),
('Disposable Razors',    'Razors',                          10.00,  15, 5),
('Deodorant Men',        'Personal care',                   14.00,  15, 5),
('Deodorant Women',      'Personal care',                   14.00,  15, 5),
('Cotton Swabs',         'Personal hygiene',                 4.50,  30, 5),
('Cotton Pads',          'Personal hygiene',                 5.00,  30, 5),
('Hand Sanitizer',       'Sanitizer',                        7.00,  25, 5),
('Wet Wipes',            'Cleaning wipes',                   6.00,  25, 5),
('Facial Tissues',       'Tissues',                          3.00,  40, 5),
('Paper Towels',         'Paper towels',                     5.00,  30, 5),
-- Vitamins
('Multivitamin Tablets', 'Daily vitamin supplement',        18.00,  20, 6),
('Vitamin B Complex',    'Vitamin supplement',              15.00,  20, 6),
('Zinc Tablets',         'Immune support supplement',       12.00,  20, 6),
('Magnesium Capsules',   'Mineral supplement',              16.00,  15, 6),
('Folic Acid',           'Pregnancy supplement',             8.00,  20, 6),
-- Medical Devices 
('Digital Thermometer',  'Body temperature monitor',        35.00,  10, 7),
('Blood Pressure Monitor','Blood pressure device',         120.00,   5, 7),
('Glucometer',           'Blood sugar measuring device',    95.00,   5, 7),
('Pulse Oximeter',       'Oxygen saturation monitor',       60.00,  10, 7),
('Nebulizer Machine',    'Respiratory treatment device',   180.00,   3, 7),
-- Dental Care 
('Dental Floss Premium', 'Dental cleaning floss',            8.00,  20, 8),
('Whitening Toothpaste', 'Teeth whitening toothpaste',      10.00,  20, 8),
('Children Toothbrush',  'Kids toothbrush',                  5.00,  30, 8),
('Electric Toothbrush Heads','Replacement heads',           25.00,  15, 8),
('Alcohol-Free Mouthwash','Mouth rinse',                    14.00,  20, 8),
-- Baby Care 
('Baby Formula Milk',    'Infant nutrition formula',        45.00,  15, 9),
('Baby Feeding Spoon Set','Feeding accessories',             8.00,  15, 9),
('Baby Bib',             'Baby feeding bib',                 6.00,  20, 9),
('Baby Teething Ring',   'Infant teether',                   7.00,  20, 9),
('Baby Blanket',         'Infant blanket',                  20.00,  10, 9),
-- First Aid (106-110)
('Sterile Gauze',        'Medical dressing',                 5.00,  50, 10),
('Medical Tape',         'Adhesive tape',                    4.00,  50, 10),
('Elastic Bandage',      'Support bandage',                  8.00,  40, 10),
('First Aid Kit',        'Emergency first aid kit',         55.00,  10, 10),
('Antiseptic Solution',  'Wound cleaning solution',          9.00,  25, 10);
 

insert into Batch (ProductID, WarehouseID, QtyInStock, ExpiryDate, StorageLocation) values
(1,  1, 200, '2026-12-31', 'Rack A-1'),
(2,  1,  80, '2027-01-01', 'Rack A-2'),
(3,  1,  50, '2026-07-01', 'Rack A-3'),
(4,  1,  40, '2026-07-15', 'Rack A-4'),
(5,  1,  70, '2027-06-30', 'Rack A-5'),
(6,  1,  60, '2028-11-30', 'Rack A-6'),
(7,  1, 120, '2027-08-31', 'Rack A-7'),
(8,  1,  90, '2027-09-30', 'Rack A-8'),
(9,  1,  75, '2027-11-30', 'Rack A-9'),
(10, 1,  65, '2028-01-15', 'Rack A-10'),
(11, 1, 150, '2027-05-20', 'Rack B-1'),
(12, 1,  80, '2027-06-15', 'Rack B-2'),
(13, 1,  95, '2027-10-10', 'Rack B-3'),
(14, 1, 130, '2027-12-01', 'Rack B-4'),
(15, 1,  55, '2026-11-25', 'Rack B-5'),
(16, 1,  70, '2028-02-28', 'Rack C-1'),
(17, 1,  60, '2028-03-30', 'Rack C-2'),
(18, 1,  45, '2028-04-30', 'Rack C-3'),
(19, 1, 100, '2027-07-30', 'Rack C-4'),
(20, 1,  85, '2027-08-15', 'Rack C-5'),
(21, 1, 110, '2028-01-01', 'Rack D-1'),
(22, 1, 105, '2028-01-20', 'Rack D-2'),
(23, 1,  50, '2027-12-31', 'Rack D-3'),
(24, 1,  40, '2027-09-15', 'Rack D-4'),
(25, 1,  35, '2027-10-15', 'Rack D-5'),
(26, 1,  30, '2028-05-01', 'Rack E-1'),
(27, 1,  25, '2028-06-01', 'Rack E-2'),
(28, 1,  25, '2028-06-30', 'Rack E-3'),
(29, 1, 140, '2027-04-30', 'Rack E-4'),
(30, 1, 115, '2027-05-30', 'Rack E-5'),
(31, 1,  90, '2027-08-30', 'Rack F-1'),
(32, 1, 160, '2029-01-01', 'Rack F-2'),
(33, 1, 155, '2029-01-15', 'Rack F-3'),
(34, 1, 100, '2028-07-15', 'Rack F-4'),
(35, 1,  80, '2028-08-15', 'Rack F-5'),
(36, 1,  70, '2028-09-10', 'Rack G-1');
 

insert into SupplierProduct (SupplierID, ProductID, UnitCost) values
(1,  1,  2.00),
(1,  2,  5.50),
(2,  3,  8.00),
(3,  4,  6.50),
(2,  5,  7.50),
(3,  6,  4.50),
(1,  7,  3.20),
(1, 10,  4.00),
(2,  8,  4.20),
(2, 11,  7.00),
(3,  9,  5.80),
(4, 12, 13.00),
(4, 13,  6.00),
(4, 15,  8.00),
(4, 17, 15.00),
(5, 14,  4.80),
(5, 16,  7.50),
(5, 18,  1.80),
(5, 21,  2.90),
(6, 19,  6.00),
(6, 20,  9.00),
(6, 22,  5.50),
(6, 29,  3.90);
 

INSERT INTO Client (ClientName, ClientType, Phone, City, CreditLimit) VALUES
('Demo ClientName 1', 'Pharmacy', 'DEMO-PHONE-1', 'Demo City 1', 8000.00),
('Demo ClientName 2', 'Clinic', 'DEMO-PHONE-2', 'Birzeit', 5000.00),
('Demo ClientName 3', 'Pharmacy', 'DEMO-PHONE-3', 'Nablus', 6000.00),
('Demo ClientName 4', 'Clinic', 'DEMO-PHONE-4', 'Demo City 1', 7000.00),
('Demo ClientName 5', 'Pharmacy', 'DEMO-PHONE-5', 'Nablus', 4500.00),
('Demo ClientName 6', 'Clinic', 'DEMO-PHONE-6', 'Jenin', 6000.00),
('Demo ClientName 7', 'Pharmacy', 'DEMO-PHONE-7', 'Demo City 1', 5500.00),
('Demo ClientName 8', 'Clinic', 'DEMO-PHONE-8', 'Tulkarm', 3000.00),
('Demo ClientName 9', 'Pharmacy', 'DEMO-PHONE-9', 'Hebron', 6500.00),
('Demo ClientName 10', 'Clinic', 'DEMO-PHONE-10', 'Qalqilya', 4000.00),
('Demo ClientName 11', 'Clinic', 'DEMO-PHONE-11', 'Bethlehem', 5500.00),
('Demo ClientName 12', 'Pharmacy', 'DEMO-PHONE-12', 'Salfit', 3500.00),
('Demo ClientName 13', 'Pharmacy', 'DEMO-PHONE-13', 'Jericho', 4800.00);
 

insert into PurchaseOrder (SupplierID, EmployeeID, OrderDate, ExpDeliveryDate, Status, TotalAmount) values
(1, 3, '2026-03-05', '2026-03-15', 'Delivered',  560.00),
(2, 5, '2026-03-12', '2026-03-22', 'Delivered',  840.00),
(6, 3, '2026-03-20', '2026-03-30', 'Delivered',  390.00),
(3, 5, '2026-04-02', '2026-04-12', 'Delivered',  720.00),
(5, 3, '2026-04-25', '2026-05-05', 'Delivered',  480.00),
(1, 3, '2026-05-01', '2026-05-10', 'Delivered',  400.00),
(2, 3, '2026-05-05', '2026-05-15', 'Delivered',  600.00),
(3, 5, '2026-04-10', '2026-04-20', 'Delivered',  870.00),
(4, 3, '2026-04-18', '2026-04-28', 'Delivered', 1040.00),
(1, 5, '2026-05-03', '2026-05-12', 'Delivered',  480.00),
(5, 3, '2026-05-15', '2026-05-25', 'Delivered',  630.00),
(2, 5, '2026-05-28', '2026-06-07', 'Delivered',  756.00),
(3, 3, '2026-06-01', '2026-06-12', 'Pending',    540.00),
(4, 5, '2026-06-08', '2026-06-20', 'Pending',    920.00),
(1, 5, '2026-06-14', '2026-06-25', 'Pending',    650.00),
(4, 3, '2026-06-17', '2026-06-28', 'Pending',    910.00);
 
insert into PurchaseOrderItem (PONumber, ProductID, QtyOrdered, UnitCost, QtyReceived) values
(1,  1,  200, 2.00,   200),
(1,  7,   80, 3.20,    80),
(2,  3,   60, 8.00,    60),
(2,  8,   50, 4.20,    50),
(3,  9,  100, 5.80,   100),
(3,  29,  80, 3.90,    80),
(4,  4,  100, 6.50,   100),
(4,  11,  60, 7.00,    60),
(5,  14,  75, 4.80,    75),
(5,  16,  30, 7.50,    30),
(6,  1,  100, 2.00,   100),
(6,  2,   50, 5.50,    50),
(7,  3,   60, 8.00,    60),
(7,  10,  60, 4.00,    60),
(8,  13,  40, 6.00,    40),
(8,  17,  20, 15.00,   20),
(9,  12,  60, 13.00,   60),
(9,  18,  45, 1.80,    45),
(10, 5,   80, 7.50,    80),
(10, 15,  70, 8.00,    70),
(11, 19, 100, 6.00,   100),
(11, 20,  85, 9.00,    85),
(12, 21, 110, 2.90,   110),
(12, 22, 105, 5.50,   105),
(13, 6,   60, 4.50,     0),
(13, 9,   80, 5.80,     0),
(14, 24,  40, 13.00,    0),
(14, 26,  30, 6.80,     0),
(15, 1,  100, 2.00,     0),
(15, 7,   60, 3.20,     0),
(16, 12,  40, 13.00,    0),
(16, 17,  60, 15.00,    0);
 
insert into SaleOrder (ClientID, EmployeeID, OrderDate, DeliveryDate, Status, TotalAmount, PaymentStatus) values
(1,  2, '2026-03-10', '2026-03-12', 'Delivered', 308.00, 'Paid'),
(2,  4, '2026-03-18', '2026-03-20', 'Delivered', 176.00, 'Paid'),
(3,  2, '2026-03-25', '2026-03-27', 'Delivered', 540.00, 'Partial'),
(4,  4, '2026-04-05', '2026-04-07', 'Delivered', 264.00, 'Paid'),
(5,  2, '2026-04-12', '2026-04-14', 'Delivered', 189.00, 'Paid'),
(6,  4, '2026-04-20', '2026-04-22', 'Delivered', 422.00, 'Paid'),
(7,  2, '2026-04-28', '2026-04-30', 'Delivered', 315.00, 'Paid'),
(8,  4, '2026-05-06', '2026-05-08', 'Delivered', 198.00, 'Paid'),
(1,  2, '2026-05-12', '2026-05-14', 'Delivered', 350.00, 'Paid'),
(2,  4, '2026-05-18', '2026-05-20', 'Delivered', 476.00, 'Paid'),
(3,  2, '2026-05-24', '2026-05-26', 'Delivered', 243.00, 'Paid'),
(9,  4, '2026-05-29', '2026-05-31', 'Delivered', 361.00, 'Partial'),
(4,  2, '2026-06-03', null,         'Approved',  284.00, 'Paid'),
(5,  4, '2026-06-07', null,         'Approved',  193.00, 'Unpaid'),
(6,  2, '2026-06-13', null,         'Pending',   452.00, 'Unpaid'),
(10, 4, '2026-06-18', null,         'Pending',   127.00, 'Unpaid');
 

insert into SaleOrderItem (SaleOrderID, BatchID, QtyOrdered, UnitPrice, Discount) values
(1,  1,  50, 4.50,  0.00),
(1,  6,  20, 3.50,  0.00),
(2,  7,  20, 7.50,  0.00),
(2,  9,  10, 10.00, 0.00),
(3,  3,  15, 18.00, 0.00),
(3,  10, 10, 16.00, 5.00),
(4,  11, 25,  6.00, 0.00),
(4,  29, 15,  8.00, 0.00),
(5,  1,  30,  4.50, 5.00),
(5,  8,  15,  8.00, 0.00),
(6,  14, 30, 12.00, 0.00),
(6,  12, 10,  9.50, 5.00),
(7,  5,  20, 11.00, 0.00),
(7,  16, 15, 15.00, 0.00),
(8,  36, 20,  9.50, 0.00),
(8,  21, 10, 12.00, 0.00),
(9,  1,  50,  4.50, 0.00),
(9,  6,  25,  3.50, 5.00),
(10, 7,  30,  7.50, 0.00),
(10, 22, 20, 13.00, 0.00),
(11, 9,  15, 10.00, 0.00),
(11, 19, 10,  8.50, 0.00),
(12, 14, 20, 12.00, 0.00),
(12, 30, 15, 14.00, 5.00),
(13, 2,  15, 13.00, 0.00),
(13, 20, 10, 12.50, 0.00),
(14, 11, 20,  6.00, 0.00),
(14, 4,  10,  9.50, 5.00),
(15, 3,  12, 18.00, 0.00),
(15, 13, 15, 11.00, 0.00),
(16, 8,  10,  8.00, 0.00),
(16, 18, 10, 25.00, 10.00);
 
insert into InventoryTransaction (BatchID, EmployeeID, TxnType, Quantity, ReferenceID) values
-- Receipts linked to POs
(1,  3, 'Receipt',  200,  1),
(7,  3, 'Receipt',   80,  1),
(3,  5, 'Receipt',   60,  2),
(8,  5, 'Receipt',   50,  2),
(9,  3, 'Receipt',  100,  3),
(29, 3, 'Receipt',   80,  3),
(4,  5, 'Receipt',  100,  4),
(11, 5, 'Receipt',   60,  4),
(14, 3, 'Receipt',   75,  5),
(16, 3, 'Receipt',   30,  5),
(1,  3, 'Receipt',  100,  6),
(2,  3, 'Receipt',   50,  6),
(3,  5, 'Receipt',   60,  7),
(10, 5, 'Receipt',   60,  7),
(13, 3, 'Receipt',   40,  8),
(17, 3, 'Receipt',   20,  8),
(12, 5, 'Receipt',   60,  9),
(18, 5, 'Receipt',   45,  9),
(5,  3, 'Receipt',   80, 10),
(15, 3, 'Receipt',   70, 10),
(19, 5, 'Receipt',  100, 11),
(20, 5, 'Receipt',   85, 11),
(21, 3, 'Receipt',  110, 12),
(22, 3, 'Receipt',  105, 12),
-- Dispatches linked to Sale Orders
(1,  2, 'Dispatch',  50,  1),
(6,  2, 'Dispatch',  20,  1),
(7,  4, 'Dispatch',  20,  2),
(9,  4, 'Dispatch',  10,  2),
(3,  2, 'Dispatch',  15,  3),
(10, 2, 'Dispatch',  10,  3),
(11, 4, 'Dispatch',  25,  4),
(29, 4, 'Dispatch',  15,  4),
(1,  2, 'Dispatch',  30,  5),
(8,  2, 'Dispatch',  15,  5),
(14, 4, 'Dispatch',  30,  6),
(12, 4, 'Dispatch',  10,  6),
(5,  2, 'Dispatch',  20,  7),
(16, 2, 'Dispatch',  15,  7),
(36, 4, 'Dispatch',  20,  8),
(21, 4, 'Dispatch',  10,  8),
-- Adjustments
(15, 1, 'Adjustment', 5, null),
(24, 1, 'Adjustment', 8, null),
(30, 1, 'Adjustment', 10, null);
 
insert into Payment (SaleOrderID, PONumber, PaymentDate, Amount, PaymentMethod, Direction) values
(1,  null, '2026-03-12', 308.00,  'Cash',         'Incoming'),
(2,  null, '2026-03-20', 176.00,  'BankTransfer', 'Incoming'),
(3,  null, '2026-03-27', 300.00,  'Cheque',       'Incoming'),
(4,  null, '2026-04-07', 264.00,  'Cash',         'Incoming'),
(5,  null, '2026-04-14', 189.00,  'BankTransfer', 'Incoming'),
(6,  null, '2026-04-22', 422.00,  'Cash',         'Incoming'),
(7,  null, '2026-04-30', 315.00,  'Cheque',       'Incoming'),
(8,  null, '2026-05-08', 198.00,  'Cash',         'Incoming'),
(9,  null, '2026-05-14', 350.00,  'BankTransfer', 'Incoming'),
(10, null, '2026-05-20', 476.00,  'Cash',         'Incoming'),
(11, null, '2026-05-26', 243.00,  'Cheque',       'Incoming'),
(12, null, '2026-05-31', 200.00,  'Cash',         'Incoming'),
(13, null, '2026-06-03', 284.00,  'BankTransfer', 'Incoming'),
-- Outgoing to suppliers
(null, 1,  '2026-03-16', 560.00,  'BankTransfer', 'Outgoing'),
(null, 2,  '2026-03-23', 840.00,  'BankTransfer', 'Outgoing'),
(null, 3,  '2026-03-31', 390.00,  'Cheque',       'Outgoing'),
(null, 4,  '2026-04-13', 720.00,  'BankTransfer', 'Outgoing'),
(null, 5,  '2026-05-06', 480.00,  'BankTransfer', 'Outgoing'),
(null, 6,  '2026-05-11', 400.00,  'BankTransfer', 'Outgoing'),
(null, 7,  '2026-05-16', 600.00,  'Cheque',       'Outgoing'),
(null, 8,  '2026-04-21', 870.00,  'BankTransfer', 'Outgoing'),
(null, 9,  '2026-04-29', 1040.00, 'BankTransfer', 'Outgoing'),
(null, 10, '2026-05-13', 480.00,  'Cheque',       'Outgoing'),
(null, 11, '2026-05-26', 630.00,  'BankTransfer', 'Outgoing'),
(null, 12, '2026-06-08', 756.00,  'BankTransfer', 'Outgoing');
 
INSERT INTO UserAccount (Password, Role, EmployeeID, ClientID) VALUES
('demo-only-1', 'Admin', NULL, NULL),
('demo-only-2', 'Employee', 1, NULL),
('demo-only-3', 'Employee', 2, NULL),
('demo-only-4', 'Employee', 3, NULL),
('demo-only-5', 'Employee', 4, NULL),
('demo-only-6', 'Employee', 5, NULL),
('demo-only-7', 'Client', NULL, 1),
('demo-only-8', 'Client', NULL, 2),
('demo-only-9', 'Client', NULL, 3),
('demo-only-10', 'Client', NULL, 4),
('demo-only-11', 'Client', NULL, 5),
('demo-only-12', 'Client', NULL, 6),
('demo-only-13', 'Client', NULL, 7),
('demo-only-14', 'Client', NULL, 8),
('demo-only-15', 'Client', NULL, 9),
('demo-only-16', 'Client', NULL, 10),
('demo-only-17', 'Client', NULL, 11),
('demo-only-18', 'Client', NULL, 12),
('demo-only-19', 'Client', NULL, 13);


-- 2.Retrieve all warehouses located in Demo City 1, along with the total number of product batches currently stored in each.
SELECT W.WarehouseName, W.City, COUNT(B.BatchID) AS TotalBatches
FROM Warehouse W LEFT JOIN Batch B ON W.WarehouseID = B.WarehouseID
WHERE W.City = 'Demo City 1'
GROUP BY W.WarehouseID, W.WarehouseName, W.City;

-- 4.Retrieve all products in a specific warehouse whose batch quantity has fallen below 
-- the product's reorder level, sorted by category name.
SELECT P.ProductName, C.Name AS Category,B.QtyInStock, P.ReorderLevel
FROM Batch B, Product P, Category C
WHERE B.ProductID = P.ProductID
AND P.CategoryID = C.CategoryID
AND B.WarehouseID = 1
AND B.QtyInStock < P.ReorderLevel
ORDER BY C.Name ASC;



SELECT S.SupplierName,
       SUM(POI.QtyReceived * POI.UnitCost) AS TotalValue
FROM Supplier S, PurchaseOrder PO,PurchaseOrderItem POI
WHERE PO.OrderDate BETWEEN '2026-01-01' AND '2026-12-31'AND 
S.SupplierID = PO.SupplierID AND
PO.PONumber = POI.PONumber
GROUP BY S.SupplierID, S.SupplierName
ORDER BY TotalValue DESC;



