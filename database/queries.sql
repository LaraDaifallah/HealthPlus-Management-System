-- Sample Queries and SQL Solutions

---------
-- Inventory & Stock
-----------

-- Query 1

-- Retrieve each role with the number of employees in it, the minimum, maximum, and average salary, sorted by average salary descending.
SELECT Role, COUNT(EmployeeID) AS TotalEmployees, MIN(Salary) AS MinSalary,
       MAX(Salary) AS MaxSalary, AVG(Salary) AS AvgSalary
FROM Employee
GROUP BY Role
ORDER BY AvgSalary DESC;

-- Query 2

-- Retrieve all warehouses located in Demo City 1, along with the total number of product batches currently stored in each.

-- SQL:
SELECT W.WarehouseName, W.City, COUNT(B.BatchID) AS TotalBatches
FROM Warehouse W
LEFT JOIN Batch B ON W.WarehouseID = B.WarehouseID
WHERE W.City = 'Demo City 1'
GROUP BY W.WarehouseID, W.WarehouseName, W.City;


-- Query 3

-- Retrieve the product name, category, quantity in stock, expiry date, and storage location for all batches currently stored in Demo WarehouseName 1, sorted by expiry date ascending.

-- SQL:
SELECT P.ProductName, C.Name AS Category,
       B.QtyInStock, B.ExpiryDate, B.StorageLocation
FROM Batch B
JOIN Product P ON B.ProductID = P.ProductID
JOIN Category C ON P.CategoryID = C.CategoryID
JOIN Warehouse W ON B.WarehouseID = W.WarehouseID
WHERE W.WarehouseName = 'Demo WarehouseName 1'
ORDER BY B.ExpiryDate ASC;


-- Query 4

-- Retrieve all products in Demo WarehouseName 1 whose batch quantity has fallen below the product's reorder level, sorted by category name.

-- SQL:
SELECT P.ProductName, C.Name AS Category,
       B.QtyInStock, P.ReorderLevel
FROM Batch B, Product P, Category C, Warehouse W
WHERE B.ProductID = P.ProductID
AND P.CategoryID = C.CategoryID
AND B.WarehouseID = W.WarehouseID
AND W.WarehouseName = 'Demo WarehouseName 1'
AND B.QtyInStock < P.ReorderLevel
ORDER BY C.Name ASC;

-- Query 5

-- Retrieve the complete inventory transaction history for Demo Product batch (1), showing transaction type, quantity changed, date, and the name of the employee who performed it, sorted by transaction date ascending.

-- SQL:
SELECT IT.TxnType, IT.Quantity, IT.TxnDate,
       CONCAT(E.FirstName, ' ', E.LastName) AS EmployeeName
FROM InventoryTransaction IT
JOIN Employee E ON IT.EmployeeID = E.EmployeeID
WHERE IT.BatchID = 1
ORDER BY IT.TxnDate ASC;


----------------------
-- Suppliers & Procurement
-------------------------

-- Query 6

-- Retrieve all products supplied by Demo SupplierName 1 that are currently stocked in Demo WarehouseName 1, including the agreed unit cost, batch quantity, expiry date, and storage location, sorted by expiry date ascending.

-- SQL:

SELECT P.ProductName, SP.UnitCost, B.QtyInStock,
       B.ExpiryDate, B.StorageLocation, W.WarehouseName
FROM SupplierProduct SP, Supplier S, Product P, Batch B, Warehouse W
WHERE SP.SupplierID = S.SupplierID
AND SP.ProductID = P.ProductID
AND B.ProductID = P.ProductID
AND B.WarehouseID = W.WarehouseID
AND S.SupplierName = 'Demo SupplierName 1'
AND W.WarehouseName = 'Demo WarehouseName 1'
ORDER BY B.ExpiryDate ASC;

-- Query 7

-- List all suppliers along with the total number of distinct products they supply and the total number of purchase orders issued to each, sorted by total purchase orders descending.

-- SQL:
SELECT S.SupplierName, COUNT(DISTINCT SP.ProductID) AS TotalSuppliedProducts,
       COUNT(DISTINCT PO.PONumber) AS TotalPOs
FROM Supplier S
LEFT JOIN SupplierProduct SP ON S.SupplierID = SP.SupplierID
LEFT JOIN PurchaseOrder PO ON S.SupplierID = PO.SupplierID
GROUP BY S.SupplierID, S.SupplierName
ORDER BY TotalPOs DESC;


-- Query 8

-- Retrieve all purchase orders issued in 2026, showing supplier name, order status, expected delivery date, and total order amount, sorted by expected delivery date ascending.

-- SQL:
SELECT S.SupplierName, PO.PONumber,PO.Status, PO.ExpDeliveryDate, PO.TotalAmount
FROM PurchaseOrder PO,Supplier S
WHERE S.SupplierID = PO.SupplierID
AND PO.OrderDate BETWEEN '2026-01-01' AND '2026-12-31'
ORDER BY PO.ExpDeliveryDate ASC;


-- Query 9

-- Find the total value of products received from each supplier in 2026, sorted by total value descending.

-- SQL:
SELECT S.SupplierName,
       SUM(POI.QtyReceived * POI.UnitCost) AS TotalValue
FROM Supplier S, PurchaseOrder PO,PurchaseOrderItem POI
WHERE PO.OrderDate BETWEEN '2026-01-01' AND '2026-12-31'AND 
S.SupplierID = PO.SupplierID AND
PO.PONumber = POI.PONumber
GROUP BY S.SupplierID, S.SupplierName
ORDER BY TotalValue DESC;


-- Query 10

-- Retrieve all purchase orders with a status of Pending whose expected delivery date has already passed 1-6-2026, indicating potential supply delays, sorted by expected delivery date ascending.

-- SQL:
SELECT PO.PONumber, S.SupplierName, PO.OrderDate,
       PO.ExpDeliveryDate, PO.Status, PO.TotalAmount
FROM PurchaseOrder PO JOIN Supplier S ON PO.SupplierID = S.SupplierID
WHERE PO.Status = 'Pending'
AND PO.ExpDeliveryDate < '2026-06-01'
ORDER BY PO.ExpDeliveryDate ASC;


---------------
-- Clients & Sales
-------------

-- Query 11

-- List the name, type, city, and total number of orders placed by each client, including clients who have not placed any orders yet, sorted by total orders descending.

-- SQL:
SELECT CL.ClientName, CL.ClientType, CL.City, COUNT(SO.SaleOrderID) AS TotalOrders
FROM Client CL LEFT JOIN SaleOrder SO ON CL.ClientID = SO.ClientID
GROUP BY CL.ClientID, CL.ClientName, CL.ClientType, CL.City
ORDER BY TotalOrders DESC;


-- Query 12

-- Retrieve all sale orders placed by Demo ClientName 1, showing order date, delivery date, status, total amount, and payment status, sorted from newest to latest.

-- SQL:
SELECT SO.SaleOrderID, SO.OrderDate, SO.DeliveryDate, SO.Status, SO.TotalAmount, SO.PaymentStatus
FROM SaleOrder SO, Client CL 
WHERE CL.ClientName = 'Demo ClientName 1'AND
 		SO.ClientID = CL.ClientID
ORDER BY SO.OrderDate DESC;


-- Query 13

-- Find the top five best-selling products by total quantity sold in May 2026, along with the warehouse each batch originated from.

-- SQL:
SELECT P.ProductName,W.WarehouseName,SUM(SOI.QtyOrdered) AS TotalQtySold
FROM SaleOrderItem SOI, SaleOrder SO, Batch B, Product P, Warehouse W
WHERE SOI.SaleOrderID = SO.SaleOrderID
AND SOI.BatchID = B.BatchID
AND B.ProductID = P.ProductID
AND B.WarehouseID = W.WarehouseID
AND SO.OrderDate BETWEEN '2026-05-01' AND '2026-05-31'
GROUP BY P.ProductID, P.ProductName, W.WarehouseID, W.WarehouseName
ORDER BY TotalQtySold DESC
LIMIT 5;

----------
-- Employees
---------

-- Query 14
-- List the name, role, hire date, phone, and salary of all employees whose role contains the word manager, salesperson, or Officer, sorted by role then by hire date ascending.

-- SQL:

SELECT FirstName, LastName, Role, HireDate, Phone, Salary
FROM Employee
WHERE Role LIKE '%Manager%'
   OR Role LIKE '%Sales%'
   OR Role LIKE '%Officer%'
ORDER BY Role, HireDate ASC;


-- Query 15

-- Retrieve the full name, phone, and hire date of all sales officer, sorted by hire date ascending.

-- SQL:
SELECT CONCAT(FirstName, ' ', LastName) AS EmployeeName, Phone, HireDate
FROM Employee
WHERE Role = 'Sales Officer'
ORDER BY HireDate ASC;




------------------
-- Payments & Finance
-----------------

-- Query 16

-- Retrieve all payments received from clients in june 2026, showing client name, payment date, amount paid, and payment method, sorted by payment date descending.

-- SQL:
SELECT CL.ClientName, PAY.PaymentDate, PAY.Amount, PAY.PaymentMethod
FROM Payment PAY, SaleOrder SO, Client CL
WHERE PAY.SaleOrderID = SO.SaleOrderID
AND SO.ClientID = CL.ClientID
AND PAY.Direction = 'Incoming'
AND PAY.PaymentDate BETWEEN '2026-06-01' AND '2026-06-30'
ORDER BY PAY.PaymentDate DESC;

-------------------
-- Complex queries
-------------------

-- Query 17

-- Calculate the total number of product batches per category at Demo WarehouseName 1, along with the combined stock value per category, sorted by total stock value descending.

-- SQL:
SELECT C.Name AS CategoryName, COUNT(B.BatchID) AS TotalBatches,
       SUM(B.QtyInStock * P.UnitPrice) AS TotalStockValue
FROM Batch B, Product P, Category C, Warehouse W
WHERE B.ProductID = P.ProductID
AND P.CategoryID = C.CategoryID
AND B.WarehouseID = W.WarehouseID
AND W.WarehouseName = 'Demo WarehouseName 1'
GROUP BY C.CategoryID, C.Name
ORDER BY TotalStockValue DESC;


-- Query 18

-- Calculate the total revenue generated per product category for 2026, sorted by revenue descending.

-- SQL:
SELECT C.Name AS Category,
       SUM(SOI.QtyOrdered * SOI.UnitPrice * (1 - SOI.Discount / 100)) AS TotalRevenue
FROM SaleOrderItem SOI
JOIN SaleOrder SO ON SOI.SaleOrderID = SO.SaleOrderID
JOIN Batch B ON SOI.BatchID = B.BatchID
JOIN Product P ON B.ProductID = P.ProductID
JOIN Category C ON P.CategoryID = C.CategoryID
WHERE SO.OrderDate BETWEEN '2026-01-01' AND '2026-12-31'
GROUP BY C.CategoryID, C.Name
ORDER BY TotalRevenue DESC;


-- Query 19

-- Identify all sale orders that are fully or partially unpaid and are more than 30 days old, showing client name, order date, total amount, and payment status, sorted by order date ascending.

-- SQL:
SELECT CL.ClientName, SO.OrderDate,
       SO.TotalAmount, SO.PaymentStatus
FROM SaleOrder SO
JOIN Client CL ON SO.ClientID = CL.ClientID
WHERE SO.PaymentStatus IN ('Unpaid', 'Partial')
AND SO.OrderDate < DATE_SUB(CURDATE(), INTERVAL 30 DAY)
ORDER BY SO.OrderDate ASC;

-- Query 20

-- Find the employee who processed the highest number of sale orders in the January 2025, along with the total value of those orders.

-- SQL:
SELECT E.FirstName,E.LastName,COUNT(SO.SaleOrderID) AS TotalOrders,
       SUM(SO.TotalAmount) AS TotalOrderValue
FROM Employee E JOIN SaleOrder SO ON E.EmployeeID = SO.EmployeeID
WHERE SO.OrderDate BETWEEN '2025-01-01' AND '2025-01-31'
GROUP BY E.EmployeeID, E.FirstName, E.LastName
ORDER BY TotalOrders DESC
LIMIT 1; -- doesn't handle more than one emp with the same totalOrder

--------------------

SELECT E.FirstName, E.LastName,
       COUNT(SO.SaleOrderID) AS TotalOrders,
       SUM(SO.TotalAmount) AS TotalOrderValue
FROM Employee E JOIN SaleOrder SO ON E.EmployeeID = SO.EmployeeID
WHERE SO.OrderDate BETWEEN '2025-01-01' AND '2025-01-31'
GROUP BY E.EmployeeID, E.FirstName, E.LastName
HAVING COUNT(SO.SaleOrderID) = (
    SELECT MAX(cnt) FROM (
        SELECT COUNT(SaleOrderID) AS cnt
        FROM SaleOrder
        WHERE OrderDate BETWEEN '2026-06-01' AND '2026-06-30'
        GROUP BY EmployeeID
    ) AS counts
);



