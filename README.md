<div align="center">

# HealthPlus
### Medical inventory. Orders. Payments. One full desktop GUI.

![Java](https://img.shields.io/badge/Java-17%2B-E76F00?style=for-the-badge)
![JavaFX](https://img.shields.io/badge/GUI-JavaFX-087F8C?style=for-the-badge)
![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge)
[![Build desktop demo](https://github.com/LaraDaifallah/HealthPlus-Management-System/actions/workflows/build.yml/badge.svg)](https://github.com/LaraDaifallah/HealthPlus-Management-System/actions/workflows/build.yml)

**A complete graphical workspace for managing medical supplies from purchasing to delivery.**

[Try the app](#try-healthplus) · [Explore the features](#features) · [Read the report](docs/HealthPlus-Report.pdf) · [View the database design](#database-design)

</div>

**HealthPlus** is a desktop management system built with **JavaFX, Java, JDBC, and MySQL**. It brings medical inventory, suppliers, clients, purchasing, sales, payments, and reporting into a graphical application with separate **Admin, Employee, and Client portals**.

Developed for the Database Systems project at Birzeit University — **Group 27**.

[Project Report](docs/HealthPlus-Report.pdf) · [Database Schema](database/healthplus.sql) · [ER Diagram](design/HealthPlus-ERD-Light.png) · [Java Source](src/application/)

## At a Glance

| 3 role-specific portals | 14 admin tabs | 15 relational tables | 35 Java source files |
| :---: | :---: | :---: | :---: |
| Admin · Employee · Client | Dashboard through Charts & Stats | Catalog, stock, people, orders, payments | Screens, models, and JDBC DAOs |

**Who is it for?** Medical supply operations serving pharmacies and clinics, and anyone exploring a complete JavaFX/MySQL database project.

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
| `docs/HealthPlus-Report.pdf` | Project report with personal identifiers removed |
| `pom.xml` | Maven dependencies and launch/build configuration |
| `launchers/` | Windows and Linux/macOS launch scripts |
| `scripts/package-demo.py` | Desktop ZIP packaging script |
| `.github/workflows/build.yml` | Automated compilation and desktop packages |

## Try HealthPlus

HealthPlus is a **desktop application**. The quickest route is a prebuilt package: it contains the application and JavaFX/JDBC libraries, so you do not need an IDE or Maven to use it.

### Option A — Download a desktop package

1. Open [Build desktop demo](https://github.com/LaraDaifallah/HealthPlus-Management-System/actions/workflows/build.yml).
2. Select a **successful** run and download the artifact for your operating system: **Windows**, **Linux**, **macOS-Intel**, or **macOS-AppleSilicon**. GitHub may require you to sign in to download artifacts.
3. Extract the downloaded artifact, then extract the HealthPlus ZIP inside it. Keep `healthplus.jar`, `lib/`, and the launcher files together.
4. Install **Java 17 or newer** and start your local **MySQL Server**.
5. Import `database/healthplus.sql` using MySQL Workbench. **The script drops and recreates `healthplus`**; use it only for a disposable demo database.
6. On **Windows**, double-click `Start-HealthPlus.bat`. On **Linux/macOS**, open a terminal in the extracted folder and run `bash start-healthplus.sh`.
7. Enter your own MySQL username and password when prompted. Then use a demo account below on the graphical login screen.

Packages become available after the automated build succeeds and remain downloadable for 30 days. A new build can be started from the workflow's **Run workflow** button by a repository maintainer.

### Demo login accounts

For a fresh import of the supplied demo database:

| Portal | User ID | Demo password |
| --- | --- | --- |
| Admin | `1` | `demo-only-1` |
| Employee | `2` | `demo-only-2` |
| Client | `7` | `demo-only-7` |

These are **synthetic application accounts** for trying the GUI. Your **MySQL password** is separate and belongs only in your local launch configuration.

### A suggested first tour

1. Sign in as **Admin** and explore the dashboard and Charts & Stats tab.
2. Open Categories, Products, Warehouses, and Batches to follow stock from the catalog to a warehouse.
3. Explore purchase/sale orders and their line items, then inspect Payments and Inventory.
4. Log out and try the Employee and Client accounts to compare their interfaces.

### Option B — Run or modify the source

Install **JDK 17+**, **Maven**, and **MySQL Server**. Import the demo SQL first, then download the repository or clone it:

```bash
git clone https://github.com/LaraDaifallah/HealthPlus-Management-System.git
cd HealthPlus-Management-System
```

Set your database credentials in the same terminal. For **PowerShell**:

```powershell
$env:HEALTHPLUS_DB_USER = "your_mysql_username"
$env:HEALTHPLUS_DB_PASSWORD = "your_mysql_password"
mvn clean javafx:run
```

For **Linux/macOS**:

```bash
export HEALTHPLUS_DB_USER="your_mysql_username"
export HEALTHPLUS_DB_PASSWORD="your_mysql_password"
mvn clean javafx:run
```

Maven downloads the required JavaFX modules, platform libraries, and MySQL Connector/J. The first run needs internet access. No separate JavaFX SDK setup is required for this route.

| Connection setting | Purpose |
| --- | --- |
| `HEALTHPLUS_DB_USER` | Your MySQL user; defaults to `root` |
| `HEALTHPLUS_DB_PASSWORD` | Your MySQL password; required |
| `HEALTHPLUS_DB_URL` | Optional custom JDBC URL; defaults to `jdbc:mysql://localhost:3306/healthplus?useSSL=false&serverTimezone=UTC` |

For an IDE, import `pom.xml` as a Maven project, set the environment variables in the run configuration, and run `application.Main`.

### Build a local desktop package

```bash
mvn clean package
python scripts/package-demo.py Local
```

The ZIP is created in `target/`. Build on the same operating system and architecture as the machine that will run it, since JavaFX contains platform-specific native libraries. A Java installation and MySQL are still required on the target computer.

### Troubleshooting

| Symptom | What to check |
| --- | --- |
| `java` or `mvn` is not recognized | Install Java/Maven and check PATH; reopen your terminal |
| Connection refused | Start MySQL and confirm the host/port in `HEALTHPLUS_DB_URL` |
| Access denied | Check your own MySQL username/password and database permissions |
| Unknown database or missing tables | Import `database/healthplus.sql` into the same MySQL server used by the app |
| Invalid login | Use the numeric user ID and demo application password, rather than your MySQL password |
| JavaFX/native-library error | Download the package matching your OS and CPU; keep its `lib/` folder intact |
| No downloadable package | Check that the build succeeded; expired artifacts need a new build |

### Demo Scope

Contact records and account passwords in the publication SQL are synthetic demo values. Personal author identifiers were removed from the publication code/report. The account implementation stores passwords as text; production authentication would require password hashing and an access-control review.

## Project Documentation

Read the [project report](docs/HealthPlus-Report.pdf) for the project documentation and explore [the SQL query collection](database/queries.sql) alongside the DAO implementation.

The build workflow compiles the source and packages runtime libraries for each platform. It does not test live database operations or interact with the GUI.
