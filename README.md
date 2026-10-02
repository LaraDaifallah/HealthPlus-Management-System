# HealthPlus Management System
### A full JavaFX GUI for medical supply operations

**HealthPlus** is a desktop management system built with **JavaFX, Java, JDBC, and MySQL**. It brings medical inventory, suppliers, clients, purchasing, sales, payments, and reporting into a graphical application with separate **Admin, Employee, and Client portals**.

Developed for the Database Systems project at Birzeit University — **Group 27**.

[Project Report](docs/HealthPlus-Report.pdf) · [Database Schema](database/healthplus.sql) · [ER Diagram](design/HealthPlus-ERD-Light.png) · [Java Source](src/application/)

## Full Graphical Interface

The application starts with a login screen and routes the authenticated account to its role-specific portal. JavaFX tables, forms, tabs, dashboards, and charts provide the interface for working with the database.

| Portal | Java implementation | Purpose |
| --- | --- | --- |
| Admin | `ProductApp.java` | Central management interface with 14 tabs covering master data, inventory, orders, payments, and statistics |
| Employee | `EmployeePortal.java` | Dedicated workspace for employee operations |
| Client | `ClientPortal.java` | Dedicated workspace for client interactions |

## Features

- **Inventory management:** products, categories, warehouses, stock batches, expiry dates, and inventory transactions.
- **Supplier management:** supplier records and supplier–product relationships.
- **Client and employee management:** records connected to orders and user accounts.
- **Order management:** purchase orders, sale orders, and their line items.
- **Payment tracking:** payment records for business transactions.
- **Dashboard and reporting:** overview metrics, low-stock views, charts, statistics, and SQL-based reports.
- **Role-based navigation:** login selects the appropriate Admin, Employee, or Client interface.
- **Relational database design:** primary keys, foreign keys, and separate tables for order items, batches, and supplier products.

## Application Structure

The JavaFX screens call DAO classes, which use JDBC to query the MySQL database. Model classes represent the application's records.

| Layer | Examples |
| --- | --- |
| User interface | `LoginScreen`, `ProductApp`, `EmployeePortal`, `ClientPortal` |
| Data access | `ProductDAO`, `BatchDAO`, `SaleOrderDAO`, `ReportDAO`, and other DAO classes |
| Models | `Product`, `Batch`, `Client`, `Employee`, `Payment`, and order models |
| Database connection | `DBConnection` |
| Entry point | `application.Main` |

## Database Design

The schema contains **15 tables**:

| Area | Tables |
| --- | --- |
| Catalog and stock | Category, Product, Warehouse, Batch |
| Suppliers | Supplier, SupplierProduct |
| People and accounts | Client, Employee, UserAccount |
| Purchasing | PurchaseOrder, PurchaseOrderItem |
| Sales | SaleOrder, SaleOrderItem |
| Operations | InventoryTransaction, Payment |

![HealthPlus entity relationship diagram](design/HealthPlus-ERD-Light.png)

A [dark ERD image](design/HealthPlus-ERD-Dark.png) and the [editable Draw.io file](design/HealthPlus-ERD.drawio) are also included.

## Repository Guide

| Path | Contents |
| --- | --- |
| `src/application/` | 35 Java files: GUI screens, models, DAOs, and database connection |
| `database/healthplus.sql` | Database creation, tables, and sample data |
| `database/queries.sql` | Project query collection |
| `design/` | Editable ERD and light/dark diagram images |
| `docs/HealthPlus-Report.pdf` | Original project report |

## Getting Started

### Requirements

- A modern JDK; JDK 17 or newer is a suitable starting point.
- A compatible JavaFX SDK with `javafx.controls`.
- MySQL Server and MySQL Connector/J.
- An IDE such as IntelliJ IDEA or Eclipse, or a manually configured Java classpath/module path.

This repository contains the submitted source and database assets. A Maven/Gradle build, packaged JAR, and demonstration video were not present in the uploaded archive.

### 1. Import the database

Open `database/healthplus.sql` in MySQL Workbench and execute it against a local development server. The script creates and selects the **`healthplus`** database and inserts sample data. Use a fresh development database; review the script before running it against an existing database.

### 2. Configure the connection

The connection reads these environment variables:

| Variable | Value |
| --- | --- |
| `HEALTHPLUS_DB_URL` | Optional; defaults to `jdbc:mysql://localhost:3306/healthplus?useSSL=false&serverTimezone=UTC` |
| `HEALTHPLUS_DB_USER` | Optional; defaults to `root` |
| `HEALTHPLUS_DB_PASSWORD` | Required; set to your local MySQL password |

For PowerShell, before launching from the same session:

```powershell
$env:HEALTHPLUS_DB_USER = "root"
$env:HEALTHPLUS_DB_PASSWORD = "your-local-mysql-password"
```

For an IDE, set these variables in the application's run configuration. The original hardcoded database password has been replaced with environment configuration.

### 3. Configure and launch JavaFX

1. Mark `src/` as the source root, preserving the `application` package.
2. Add MySQL Connector/J to the application classpath.
3. Configure the JavaFX SDK, using VM options such as:

```text
--module-path "PATH_TO_JAVAFX_SDK/lib" --add-modules javafx.controls
```

4. Run **`application.Main`**.
5. Sign in using a sample account from the `UserAccount` table. The login expects a numeric user ID and a password; inspect the imported sample rows to obtain the corresponding ID.

### Demo Scope

The publication SQL uses synthetic contact records and demo-only account passwords. Original seed names, contact details, and passwords have been replaced. The submitted account implementation stores passwords as text, so this is an academic application; production authentication would require password hashing and further access-control review.

## Project Documentation

Read the [project report](docs/HealthPlus-Report.pdf) for the original submission and explore [the SQL query collection](database/queries.sql) alongside the DAO implementation.

The publication check verifies file contents and repository organization. The GUI and database have not been run in this publishing environment.
