package application;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class ProductApp extends Application {

    private static final String DARK = "#1B3A6B";
    private static final String MID = "#2E6DA4";
    private static final String GREEN = "#16A34A";
    private static final String RED = "#DC2626";
    private static final String ORANGE = "#D97706";
    private static final String GRAY = "#F5F7FA";

    private Label statusBar;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Health Plus - Admin Portal");
        statusBar = new Label("Ready");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setPadding(new Insets(8, 16, 8, 16));
        statusBar.setFont(Font.font("Arial", 12));
        statusBar.setStyle("-fx-background-color:#F0FDF4; -fx-text-fill:#16A34A;" +
                " -fx-border-color:#BBF7D0; -fx-border-width:1 0 0 0;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color:" + DARK + ";");
        Label logo = new Label("HP");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        logo.setTextFill(Color.WHITE);
        VBox titleBox = new VBox(2);
        Label title = new Label("Health Plus - Admin Portal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.WHITE);
        Label subtitle = new Label("Full system access | Group 27");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#A8C4E0"));
        titleBox.getChildren().addAll(title, subtitle);
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:#A8C4E0; -fx-border-color:#A8C4E0;" +
                " -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand; -fx-padding:6 14;");
        logoutBtn.setOnAction(e -> {
            stage.close();
            try {
                new LoginScreen().start(new Stage());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        header.getChildren().addAll(logo, titleBox, sp, logoutBtn);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tab("Dashboard", buildDashboard()), tab("Categories", buildCategoryTab()), tab("Products", buildProductTab()),
                tab("Warehouses", buildWarehouseTab()), tab("Batches", buildBatchTab()), tab("Suppliers", buildSupplierTab()),
                tab("Clients", buildClientTab()), tab("Employees", buildEmployeeTab()), tab("Sup-Product", buildSupplierProductTab()),
                tab("Purchase Orders", buildPurchaseTab()), tab("Sale Orders", buildSaleTab()), tab("Payments", buildPaymentTab()),
                tab("Inventory", buildInventoryTab()), tab("Charts & Stats", buildChartsTab())
        );
        VBox root = new VBox(0, header, tabs, statusBar);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        Scene scene = new Scene(root, 1050, 750);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildDashboard() {
        VBox pane = pane();
        sectionTitle(pane, "Dashboard - Overview");

        HBox kpiRow = new HBox(12);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        try {
            Connection conn = DBConnection.connect();
            ResultSet r1 = conn.createStatement().executeQuery(
                    "SELECT COALESCE(SUM(Amount),0) AS Rev FROM Payment" +
                            " WHERE Direction='Incoming'");
            double revenue = r1.next() ? r1.getDouble("Rev") : 0;
            ResultSet r2 = conn.createStatement().executeQuery(
                    "SELECT COALESCE(SUM(poi.QtyReceived*poi.UnitCost),0) AS Cost " +
                            "FROM PurchaseOrderItem poi");
            double cost = r2.next() ? r2.getDouble("Cost") : 0;
            ResultSet r3 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) AS C FROM SaleOrder" +
                            " WHERE Status='Pending'");
            int pending = r3.next() ? r3.getInt("C") : 0;
            ResultSet r4 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) AS C FROM Batch" +
                            " WHERE QtyInStock < (SELECT ReorderLevel " +
                            "FROM Product p" +
                            " WHERE p.ProductID=Batch.ProductID)");
            int lowStock = r4.next() ? r4.getInt("C") : 0;
            ResultSet r5 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) AS C FROM Client");
            int clients = r5.next() ? r5.getInt("C") : 0;
            ResultSet r6 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) AS C FROM SaleOrder" +
                            " WHERE PaymentStatus='Unpaid' OR PaymentStatus='Partial'");
            int unpaid = r6.next() ? r6.getInt("C") : 0;
            conn.close();
            kpiRow.getChildren().addAll(
                    kpi("Total Revenue", String.format("%.0f NIS", revenue), GREEN),
                    kpi("Total Cost", String.format("%.0f NIS", cost), RED),
                    kpi("Gross Profit", String.format("%.0f NIS", revenue - cost), revenue >= cost ? GREEN : RED),
                    kpi("Pending Orders", String.valueOf(pending), ORANGE),
                    kpi("Low Stock", String.valueOf(lowStock), lowStock > 0 ? RED : GREEN),
                    kpi("Unpaid Orders", String.valueOf(unpaid), unpaid > 0 ? ORANGE : GREEN),
                    kpi("Clients", String.valueOf(clients), MID)
            );
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        pane.getChildren().add(kpiRow);
        pane.getChildren().add(new Separator());

        Label soTitle = new Label("Recent Sale Orders");
        soTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        soTitle.setTextFill(Color.web(DARK));
        pane.getChildren().add(soTitle);
        TableView<ObservableList<String>> soTable = new TableView<>();
        soTable.setPrefHeight(180);
        soTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        soTable.setPlaceholder(new Label("No orders."));
        addCols(soTable, new String[]{"SO#", "Client", "Date", "Status", "Payment", "Total (NIS)"}, new int[]{55, 150, 90, 90, 80, 90});

        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT so.SaleOrderID, cl.ClientName, so.OrderDate, so.Status, so.PaymentStatus, so.TotalAmount " +
                            "FROM SaleOrder so JOIN Client cl ON so.ClientID=cl.ClientID ORDER BY so.OrderDate DESC LIMIT 10");
            while (rs.next()) {
                soTable.getItems().add(FXCollections.observableArrayList(
                        String.valueOf(rs.getInt("SaleOrderID")),
                        rs.getString("ClientName"), rs.getString("OrderDate"),
                        rs.getString("Status"), rs.getString("PaymentStatus"),
                        String.format("%.2f", rs.getDouble("TotalAmount"))));
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        pane.getChildren().add(soTable);
        pane.getChildren().add(new Separator());

        HBox midRow = new HBox(16);

        VBox bestBox = new VBox(8);
        bestBox.setPrefWidth(460);
        Label bestTitle = new Label("Top 5 Best-Selling Products");
        bestTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        bestTitle.setTextFill(Color.web(DARK));
        TableView<ObservableList<String>> bestTable = new TableView<>();
        bestTable.setPrefHeight(160);
        bestTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        addCols(bestTable, new String[]{"Product", "Category", "Total Sold"}, new int[]{200, 120, 90});
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT p.ProductName, c.Name AS Cat, SUM(soi.QtyOrdered) AS TotalSold " +
                            "FROM SaleOrderItem soi" +
                            " JOIN Batch b ON soi.BatchID=b.BatchID " +
                            "JOIN Product p ON b.ProductID=p.ProductID" +
                            " JOIN Category c ON p.CategoryID=c.CategoryID " +
                            "GROUP BY p.ProductID" +
                            " ORDER BY TotalSold DESC LIMIT 5");
            while (rs.next()) {
                bestTable.getItems().add(FXCollections.observableArrayList(
                        rs.getString("ProductName"), rs.getString("Cat"), String.valueOf(rs.getInt("TotalSold"))));
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        bestBox.getChildren().addAll(bestTitle, bestTable);

        VBox lowBox = new VBox(8);
        lowBox.setPrefWidth(460);
        Label lowTitle = new Label("Low Stock Alerts");
        lowTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lowTitle.setTextFill(Color.web(RED));
        TableView<ObservableList<String>> lowTable = new TableView<>();
        lowTable.setPrefHeight(160);
        lowTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        addCols(lowTable, new String[]{"Product", "Warehouse", "Qty", "Reorder"}, new int[]{160, 140, 60, 70});
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT p.ProductName, w.WarehouseName, b.QtyInStock, p.ReorderLevel " +
                            "FROM Batch b JOIN Product p ON b.ProductID=p.ProductID " +
                            "JOIN Warehouse w ON b.WarehouseID=w.WarehouseID " +
                            "WHERE b.QtyInStock < p.ReorderLevel " +
                            "ORDER BY b.QtyInStock ASC");
            while (rs.next()) {
                lowTable.getItems().add(FXCollections.observableArrayList(
                        rs.getString("ProductName"), rs.getString("WarehouseName"),
                        String.valueOf(rs.getInt("QtyInStock")), String.valueOf(rs.getInt("ReorderLevel"))));
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        lowTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(ObservableList<String> row, boolean empty) {
                super.updateItem(row, empty);
                setStyle(row != null && !empty ? "-fx-background-color:#FEE2E2;" : "");
            }
        });
        lowBox.getChildren().addAll(lowTitle, lowTable);
        midRow.getChildren().addAll(bestBox, lowBox);
        HBox.setHgrow(bestBox, Priority.ALWAYS);
        HBox.setHgrow(lowBox, Priority.ALWAYS);
        pane.getChildren().add(midRow);

        pane.getChildren().add(new Separator());
        Label whTitle = new Label("Warehouse Capacity");
        whTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        whTitle.setTextFill(Color.web(DARK));
        pane.getChildren().add(whTitle);
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT w.WarehouseName, w.Capacity, COALESCE(SUM(b.QtyInStock),0) AS Used " +
                            "FROM Warehouse w LEFT JOIN Batch b ON w.WarehouseID=b.WarehouseID " +
                            "GROUP BY w.WarehouseID");
            while (rs.next()) {
                String name = rs.getString("WarehouseName");
                int cap = rs.getInt("Capacity");
                int used = rs.getInt("Used");
                double pct = cap > 0 ? (double)used/cap*100 : 0;
                HBox whRow = new HBox(12);
                whRow.setAlignment(Pos.CENTER_LEFT);
                Label nameLbl = new Label(name);
                nameLbl.setPrefWidth(240);
                nameLbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");
                ProgressBar bar = new ProgressBar(pct / 100.0);
                bar.setPrefWidth(300);
                bar.setPrefHeight(18);
                bar.setStyle(pct > 80 ? "-fx-accent:" + RED + ";" : pct > 50 ? "-fx-accent:" + ORANGE + ";" : "-fx-accent:" + GREEN + ";");
                Label pctLbl = new Label(String.format("%.1f%% (%d / %d)", pct, used, cap));
                pctLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#64748B;");
                whRow.getChildren().addAll(nameLbl, bar, pctLbl);
                pane.getChildren().add(whRow);
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return pane;
    }

    private VBox buildCategoryTab() {
        VBox pane = pane();
        sectionTitle(pane, "Categories");
        TextField nameField = field("Category Name");
        TextField descField = field("Description");
        TextField idField = field("ID (for update/delete)");
        HBox row1 = row(nameField, descField);
        pane.getChildren().addAll(row1, idField);
        Button btnAdd = btn("Add", GREEN);
        Button btnUpd = btn("Update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Name", "Description"}, new int[]{60, 180, 320});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Category ORDER BY Name");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("CategoryID")), rs.getString("Name"), rs.getString("Description")));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();

        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                nameField.setText(row.get(1));
                descField.setText(row.get(2));
            }
        });
        btnAdd.setOnAction(e -> {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            if (name.isEmpty()) {
                err("Name required.");
                return;
            }
            CategoryDAO.addCategory(new Category(name, desc));
            ok("Category added!");
            nameField.clear();
            descField.clear();
            refresh.run();
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Category ex = CategoryDAO.getCategoryById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                String name = nameField.getText().trim().isEmpty() ? ex.getName() : nameField.getText().trim();
                String desc = descField.getText().trim().isEmpty() ? ex.getDescription() : descField.getText().trim();
                CategoryDAO.updateCategory(new Category(id, name, desc));
                ok("Category updated!");
                idField.clear();
                nameField.clear();
                descField.clear();
                refresh.run();}

            catch (NumberFormatException ex) {
                err("Enter a valid ID.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("category")) {
                return;
            }
            try {
                int id = Integer.parseInt(idField.getText().trim());
                boolean ok = CategoryDAO.deleteCategory(id);
                if (ok) {
                    ok("Category deleted!");
                    idField.clear();
                    nameField.clear();
                    descField.clear();
                    refresh.run();}
                else {
                    err("Not found or has linked products.");
                }
            }
            catch (NumberFormatException ex) {
                err("Enter a valid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            descField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildProductTab() {
        VBox pane = pane();
        sectionTitle(pane, "Products");
        TextField nameField = field("Product Name");
        TextField descField = field("Description");
        TextField priceField = field("Unit Price");
        TextField reordField = field("Reorder Level");
        TextField catField = field("Category ID");
        TextField idField = field("ID (for update/delete)");
        pane.getChildren().addAll(row(nameField, descField), row(priceField, reordField, catField), idField);
        Button btnAdd = btn("Add", GREEN);
        Button btnUpd = btn("Update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Name", "Description", "Price", "Reorder", "Category"}, new int[]{55, 160, 200, 70, 75, 110});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT p.ProductID, p.ProductName, p.Description, p.UnitPrice, p.ReorderLevel, c.Name AS Cat " +
                                "FROM Product p" +
                                " JOIN Category c ON p.CategoryID=c.CategoryID " +
                                "ORDER BY p.ProductName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("ProductID")), rs.getString("ProductName"),
                            rs.getString("Description"), String.format("%.2f", rs.getDouble("UnitPrice")),
                            String.valueOf(rs.getInt("ReorderLevel")), rs.getString("Cat")));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                nameField.setText(row.get(1));
                descField.setText(row.get(2));
                priceField.setText(row.get(3));
                reordField.setText(row.get(4));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                boolean ok = ProductDAO.addProduct(new Product(
                        nameField.getText().trim(), descField.getText().trim(),
                        Double.parseDouble(priceField.getText().trim()),
                        Integer.parseInt(reordField.getText().trim()),
                        Integer.parseInt(catField.getText().trim())));
                if (ok) {
                    ok("Product added!");
                    nameField.clear();
                    descField.clear();
                    priceField.clear();
                    reordField.clear();
                    catField.clear();
                    refresh.run();
                }
                else {
                    err("Failed. Check Category ID.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Product ex = ProductDAO.getProductById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                ProductDAO.updateProduct(new Product(id,
                        nameField.getText().trim().isEmpty() ? ex.getName() : nameField.getText().trim(),
                        descField.getText().trim().isEmpty() ? ex.getDescription() : descField.getText().trim(),
                        priceField.getText().trim().isEmpty() ? ex.getPrice() : Double.parseDouble(priceField.getText().trim()),
                        reordField.getText().trim().isEmpty() ? ex.getReorderLevel() : Integer.parseInt(reordField.getText().trim()),
                        catField.getText().trim().isEmpty() ? ex.getCategoryId() : Integer.parseInt(catField.getText().trim())));
                ok("Product updated!");
                idField.clear();
                nameField.clear();
                descField.clear();
                priceField.clear();
                reordField.clear();
                catField.clear();
                refresh.run();
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("product")) {
                return;
            }
            try {
                boolean ok = ProductDAO.deleteProduct(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Product deleted!");
                    idField.clear();
                    refresh.run();
                }
                else {
                    err("Not found.");}
            }
            catch (Exception ex) {
                err("Invalid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            descField.clear();
            priceField.clear();
            reordField.clear();
            catField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildWarehouseTab() {
        VBox pane = pane();
        sectionTitle(pane, "Warehouses");
        TextField nameField = field("Warehouse Name");
        TextField addrField = field("Address");
        TextField cityField = field("City");
        TextField phoneField = field("Phone");
        TextField capField = field("Capacity");
        TextField idField = field("ID (for update/delete)");
        pane.getChildren().addAll(row(nameField, addrField), row(cityField, phoneField, capField), idField);
        Button btnAdd = btn("Add", GREEN);
        Button btnUpd = btn("update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Name", "Address", "City", "Phone", "Capacity"}, new int[]{55, 180, 160, 100, 120, 80});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Warehouse ORDER BY WarehouseName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("WarehouseID")), rs.getString("WarehouseName"),
                            rs.getString("Address"), rs.getString("City"), rs.getString("Phone"),
                            String.valueOf(rs.getInt("Capacity"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                nameField.setText(row.get(1));
                addrField.setText(row.get(2));
                cityField.setText(row.get(3));
                phoneField.setText(row.get(4));
                capField.setText(row.get(5));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                boolean ok = WarehouseDAO.addWarehouse(new Warehouse(
                        nameField.getText().trim(), addrField.getText().trim(), cityField.getText().trim(),
                        phoneField.getText().trim(), Integer.parseInt(capField.getText().trim())));
                if (ok) {
                    ok("warehouse added!");
                    nameField.clear();
                    addrField.clear();
                    cityField.clear();
                    phoneField.clear();
                    capField.clear();
                    refresh.run();
                }
                else {
                    err("Failed.");
                }
            }
            catch (Exception ex) {
                err("Invalid capacity.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Warehouse ex = WarehouseDAO.getWarehouseById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                WarehouseDAO.updateWarehouse(new Warehouse(id,
                        nameField.getText().trim().isEmpty() ? ex.getName() : nameField.getText().trim(),
                        addrField.getText().trim().isEmpty() ? ex.getAddress() : addrField.getText().trim(),
                        cityField.getText().trim().isEmpty() ? ex.getCity() : cityField.getText().trim(),
                        phoneField.getText().trim().isEmpty() ? ex.getPhone() : phoneField.getText().trim(),
                        capField.getText().trim().isEmpty() ? ex.getCapacity() : Integer.parseInt(capField.getText().trim())));
                ok("Warehouse updated!");
                idField.clear();
                nameField.clear();
                addrField.clear();
                cityField.clear();
                phoneField.clear();
                capField.clear();
                refresh.run();
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("warehouse")) {
                return;
            }
            try {
                boolean ok = WarehouseDAO.deleteWarehouse(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Warehouse deleted!");
                    idField.clear();
                    refresh.run();
                }
                else {
                    err("Not found.");
                }
            }
            catch (Exception ex) {
                err("Invalid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            addrField.clear();
            cityField.clear();
            phoneField.clear();
            capField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildBatchTab() {
        VBox pane = pane();
        sectionTitle(pane, "Batches");
        TextField prodField = field("Product ID");
        TextField whField = field("Warehouse ID");
        TextField qtyField = field("Qty");
        TextField expField = field("Expiry (yyyy-mm-dd)");
        TextField locField = field("Location (e.g. A-1)");
        TextField idField = field("ID (for update/delete)");
        pane.getChildren().addAll(row(prodField, whField, qtyField), row(expField, locField), idField);
        Button btnAdd = btn("Add", GREEN);
        Button btnUpd = btn("Update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Product", "Warehouse", "Qty", "Expiry", "Location"}, new int[]{55, 170, 160, 60, 95, 90});
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(ObservableList<String> row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                    return;
                }
                try {
                    String exp = row.get(4);
                    if (!exp.equals("-") && LocalDate.parse(exp).isBefore(LocalDate.now().plusDays(60))) {
                        setStyle("-fx-background-color:#FEE2E2;");
                    }
                    else {
                        setStyle("");
                    }
                }
                catch (Exception ex) {
                    setStyle("");
                }
            }
        });
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT b.BatchID, p.ProductName, w.WarehouseName, b.QtyInStock, b.ExpiryDate, b.StorageLocation " +
                                "FROM Batch b JOIN Product p ON b.ProductID=p.ProductID " +
                                "JOIN Warehouse w ON b.WarehouseID=w.WarehouseID " +
                                "ORDER BY p.ProductName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("BatchID")), rs.getString("ProductName"), rs.getString("WarehouseName"),
                            String.valueOf(rs.getInt("QtyInStock")),
                            rs.getString("ExpiryDate") != null ? rs.getString("ExpiryDate") : "-",
                            rs.getString("StorageLocation") != null ? rs.getString("StorageLocation") : "-"));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                qtyField.setText(row.get(3));
                expField.setText(row.get(4));
                locField.setText(row.get(5));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                int pId = Integer.parseInt(prodField.getText().trim());
                int wId = Integer.parseInt(whField.getText().trim());
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (!WarehouseDAO.warehouseExists(wId)) {
                    err("warehouse ID not found.");
                    return;
                }
                if (!WarehouseDAO.hasEnoughCapacity(wId, qty)) {
                    err("Not enough warehouse capacity.");
                    return;
                }
                boolean ok = BatchDAO.addBatch(new Batch(pId, wId, qty, expField.getText().trim(), locField.getText().trim()));
                if (ok) {
                    ok("Batch added!");
                    prodField.clear();
                    whField.clear();
                    qtyField.clear();
                    expField.clear();
                    locField.clear();
                    refresh.run();
                }
                else {
                    err("Failed. Check Product/Warehouse IDs.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Batch ex = BatchDAO.getBatchById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                BatchDAO.updateBatch(new Batch(id,
                        prodField.getText().trim().isEmpty() ?
                                ex.getProductId() : Integer.parseInt(prodField.getText().trim()),
                        whField.getText().trim().isEmpty() ?
                                ex.getWarehouseId() : Integer.parseInt(whField.getText().trim()),
                        qtyField.getText().trim().isEmpty() ?
                                ex.getQuantity() : Integer.parseInt(qtyField.getText().trim()),
                        expField.getText().trim().isEmpty() ?
                                ex.getExpiryDate() : expField.getText().trim(),
                        locField.getText().trim().isEmpty() ?
                                ex.getStorageLocation() : locField.getText().trim()));
                ok("Batch updated!");
                idField.clear();
                prodField.clear();
                whField.clear();
                qtyField.clear();
                expField.clear();
                locField.clear();
                refresh.run();
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("batch")) {
                return;
            }
            try {
                boolean ok = BatchDAO.deleteBatch(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Batch deleted!");
                    idField.clear();
                    refresh.run();
                }
                else {
                    err("Not found.");
                }
            }
            catch (Exception ex) {
                err("Invalid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            prodField.clear();
            whField.clear();
            qtyField.clear();
            expField.clear();
            locField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildSupplierTab() {
        VBox pane = pane();
        sectionTitle(pane, "Suppliers");
        TextField nameField = field("Supplier Name");
        TextField contField = field("Contact Person");
        TextField phoneField = field("Phone");
        TextField emailField = field("Email");
        TextField cityField = field("City");
        TextField idField = field("ID (for update/delete)");
        pane.getChildren().addAll(row(nameField, contField), row(phoneField, emailField, cityField), idField);
        Button btnAdd = btn("Add", GREEN);
        Button btnUpd = btn("Update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Name", "Contact", "Phone", "Email", "City"}, new int[]{55, 150, 120, 110, 160, 100});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Supplier " +
                        "ORDER BY SupplierName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("SupplierID")), rs.getString("SupplierName"),
                            rs.getString("ContactPerson"), rs.getString("Phone"),
                            rs.getString("Email"), rs.getString("City")));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                nameField.setText(row.get(1));
                contField.setText(row.get(2));
                phoneField.setText(row.get(3));
                emailField.setText(row.get(4));
                cityField.setText(row.get(5));
            }
        });
        btnAdd.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                err("Name required.");
                return;
            }
            boolean ok = SupplierDAO.addSupplier(new Supplier(name, contField.getText().trim(), phoneField.getText().trim(), emailField.getText().trim(), cityField.getText().trim()));
            if (ok) {
                ok("Supplier added!");
                nameField.clear();
                contField.clear();
                phoneField.clear();
                emailField.clear();
                cityField.clear();
                refresh.run();
            }
            else {
                err("Failed.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Supplier ex = SupplierDAO.getSupplierById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                SupplierDAO.updateSupplier(new Supplier(id,
                        nameField.getText().trim().isEmpty() ?
                                ex.getName() : nameField.getText().trim(),
                        contField.getText().trim().isEmpty() ?
                                ex.getContactPerson() : contField.getText().trim(),
                        phoneField.getText().trim().isEmpty() ?
                                ex.getPhone() : phoneField.getText().trim(),
                        emailField.getText().trim().isEmpty() ?
                                ex.getEmail() : emailField.getText().trim(),
                        cityField.getText().trim().isEmpty() ?
                                ex.getCity() : cityField.getText().trim()));
                ok("Supplier updated!");
                idField.clear();
                nameField.clear();
                contField.clear();
                phoneField.clear();
                emailField.clear();
                cityField.clear();
                refresh.run();
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("supplier")) {
                return;
            }
            try {
                boolean ok = SupplierDAO.deleteSupplier(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Supplier deleted!");
                    idField.clear();
                    refresh.run();
                }
                else {
                    err("Not found.");
                }
            }
            catch (Exception ex) {
                err("Invalid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            contField.clear();
            phoneField.clear();
            emailField.clear();
            cityField.clear();
            refresh.run();});
        return pane;
    }

    private VBox buildClientTab() {
        VBox pane = pane();
        sectionTitle(pane, "Clients");
        TextField nameField = field("Client Name");
        TextField typeField = field("Type: Pharmacy or Clinic");
        TextField phoneField = field("Phone");
        TextField cityField = field("City");
        TextField creditField = field("Credit Limit");
        TextField idField = field("ID (for update/delete)");
        TextField passField = field("Password (for new account)");
        pane.getChildren().addAll(row(nameField, typeField), row(phoneField, cityField, creditField), passField, idField);
        Button btnAdd = btn("Add + Create Account", GREEN);
        Button btnUpd = btn("Update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Name", "Type", "Phone", "City", "Credit Limit (NIS)"}, new int[]{55, 180, 80, 120, 100, 120});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Client ORDER BY ClientName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("ClientID")), rs.getString("ClientName"),
                            rs.getString("ClientType"), rs.getString("Phone"), rs.getString("City"),
                            String.format("%.2f", rs.getDouble("CreditLimit"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                nameField.setText(row.get(1));
                typeField.setText(row.get(2));
                phoneField.setText(row.get(3));
                cityField.setText(row.get(4));
                creditField.setText(row.get(5));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                String pass = passField.getText().trim();
                if (pass.isEmpty()) {
                    err("Password required to create account.");
                    return;
                }
                boolean ok = ClientDAO.addClient(new Client(nameField.getText().trim(), typeField.getText().trim(),
                        phoneField.getText().trim(), cityField.getText().trim(), Double.parseDouble(creditField.getText().trim())));
                if (ok) {
                    int newId = getLastId("SELECT MAX(ClientID) FROM Client");
                    UserAccountDAO.addUser(pass, "Client", null, newId);
                    int uid = getLastId("SELECT MAX(UserID) FROM UserAccount");
                    ok("Client added! Login User ID: " + uid);
                    nameField.clear();
                    typeField.clear();
                    phoneField.clear();
                    cityField.clear();
                    creditField.clear();
                    passField.clear();
                    refresh.run();
                }
                else {
                    err("Failed. Type must be Pharmacy or Clinic.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Client ex = ClientDAO.getClientById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                ClientDAO.updateClient(new Client(id,
                        nameField.getText().trim().isEmpty() ?
                                ex.getName() : nameField.getText().trim(),
                        typeField.getText().trim().isEmpty() ?
                                ex.getType() : typeField.getText().trim(),
                        phoneField.getText().trim().isEmpty() ?
                                ex.getPhone() : phoneField.getText().trim(),
                        cityField.getText().trim().isEmpty() ?
                                ex.getCity() : cityField.getText().trim(),
                        creditField.getText().trim().isEmpty() ?
                                ex.getCreditLimit() : Double.parseDouble(creditField.getText().trim())));
                ok("Client updated!");
                idField.clear();
                nameField.clear();
                typeField.clear();
                phoneField.clear();
                cityField.clear();
                creditField.clear();
                passField.clear();
                refresh.run();
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("client")) {
                return;
            }
            try {
                boolean ok = ClientDAO.deleteClient(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Client deleted!");
                    idField.clear();
                    refresh.run();
                }
                else { err("Not found or has orders.");}
            }
            catch (Exception ex) {
                err("Invalid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            nameField.clear();
            typeField.clear();
            phoneField.clear();
            cityField.clear();
            creditField.clear();
            passField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildEmployeeTab() {
        VBox pane = pane();
        sectionTitle(pane, "Employees");
        TextField firstField = field("First Name");
        TextField lastField = field("Last Name");
        TextField roleField = field("Role");
        TextField hireField = field("Hire date (yyyy-mm-dd)");
        TextField phoneField = field("phone");
        TextField salField = field("salary");
        TextField idField = field("ID (for update/delete)");
        TextField passField = field("Password (for new account)");
        pane.getChildren().addAll(row(firstField, lastField, roleField), row(hireField, phoneField, salField), passField, idField);
        Button btnAdd = btn("Add + Create Account", GREEN);
        Button btnUpd = btn("Update", MID);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "First Name", "Last Name", "Role", "Hire Date", "Phone", "Salary"}, new int[]{55, 100, 100, 140, 95, 120, 90});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Employee ORDER BY FirstName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("EmployeeID")), rs.getString("FirstName"), rs.getString("LastName"),
                            rs.getString("Role"), rs.getString("HireDate"), rs.getString("Phone"),
                            String.format("%.2f", rs.getDouble("Salary"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
                firstField.setText(row.get(1));
                lastField.setText(row.get(2));
                roleField.setText(row.get(3));
                hireField.setText(row.get(4));
                phoneField.setText(row.get(5));
                salField.setText(row.get(6));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                String pass = passField.getText().trim();
                if (pass.isEmpty()) {
                    err("Password required.");
                    return;
                }
                boolean ok = EmployeeDAO.addEmployee(new Employee(firstField.getText().trim(), lastField.getText().trim(), roleField.getText().trim(), hireField.getText().trim(), phoneField.getText().trim(), Double.parseDouble(salField.getText().trim()), null));
                if (ok) {
                    int newId = getLastId("SELECT MAX(EmployeeID) FROM Employee");
                    UserAccountDAO.addUser(pass, "Employee", newId, null);
                    int uid = getLastId("SELECT MAX(UserID) FROM UserAccount");
                    ok("Employee added! Login User ID: " + uid);
                    firstField.clear();
                    lastField.clear();
                    roleField.clear();
                    hireField.clear();
                    phoneField.clear();
                    salField.clear();
                    passField.clear();
                    refresh.run();
                }
                else {
                    err("failed.");
                }
            }
            catch (Exception ex) {
                err("Invalid salary.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                Employee ex = EmployeeDAO.getEmployeeById(id);
                if (ex == null) {
                    err("ID not found.");
                    return;
                }
                EmployeeDAO.updateEmployee(new Employee(id,
                        firstField.getText().trim().isEmpty() ?
                                ex.getFirstName() : firstField.getText().trim(),
                        lastField.getText().trim().isEmpty() ?
                                ex.getLastName() : lastField.getText().trim(),
                        roleField.getText().trim().isEmpty() ?
                                ex.getRole() : roleField.getText().trim(),
                        hireField.getText().trim().isEmpty() ?
                                ex.getHireDate() : hireField.getText().trim(),
                        phoneField.getText().trim().isEmpty() ?
                                ex.getPhone() : phoneField.getText().trim(),
                        salField.getText().trim().isEmpty() ?
                                ex.getSalary() : Double.parseDouble(salField.getText().trim()),
                        ex.getWarehouseID()));
                ok("Employee updated!");
                idField.clear();
                firstField.clear();
                lastField.clear();
                roleField.clear();
                hireField.clear();
                phoneField.clear();
                salField.clear();
                passField.clear();
                refresh.run();
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("employee")) {
                return;
            }
            try {
                boolean ok = EmployeeDAO.deleteEmployee(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Employee deleted!");
                    idField.clear();
                    refresh.run();
                }
                else {
                    err("Not found or has linked orders.");
                }
            }
            catch (Exception ex) {
                err("Invalid ID.");
            }
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            firstField.clear();
            lastField.clear();
            roleField.clear();
            hireField.clear();
            phoneField.clear();
            salField.clear();
            passField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildSupplierProductTab() {
        VBox pane = pane();
        sectionTitle(pane, "Supplier - Product Links");
        TextField supField = field("Supplier ID");
        TextField prodField = field("Product ID");
        TextField costField = field("Unit Cost");
        pane.getChildren().add(row(supField, prodField, costField));
        Button btnAdd = btn("Link", GREEN);
        Button btnUpd = btn("Update Cost", MID);
        Button btnDel = btn("Remove Link", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnUpd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"Supplier", "Product", "Unit Cost (NIS)"}, new int[]{200, 200, 120});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT s.SupplierName, p.ProductName, sp.UnitCost, sp.SupplierID, sp.ProductID " +
                                "FROM SupplierProduct sp " +
                                "JOIN Supplier s ON sp.SupplierID=s.SupplierID " +
                                "JOIN Product p ON sp.ProductID=p.ProductID ORDER BY s.SupplierName");
                while (rs.next()) {
                    table.getItems().add(FXCollections.observableArrayList(
                            rs.getString("SupplierName"), rs.getString("ProductName"),
                            String.format("%.2f", rs.getDouble("UnitCost")),
                            String.valueOf(rs.getInt("SupplierID")), String.valueOf(rs.getInt("ProductID"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null && row.size() >= 5) {
                supField.setText(row.get(3));
                prodField.setText(row.get(4));
                costField.setText(row.get(2));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                boolean ok = SupplierProductDAO.addSupplierProduct(new SupplierProduct(Integer.parseInt(supField.getText().trim()),
                        Integer.parseInt(prodField.getText().trim()), Double.parseDouble(costField.getText().trim())));
                if (ok) {
                    ok("Link created!");
                    supField.clear();
                    prodField.clear();
                    costField.clear();
                    refresh.run();
                }
                else {
                    err("Failed or already exists.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnUpd.setOnAction(e -> {
            try {
                boolean ok = SupplierProductDAO.updateSupplierProduct(new SupplierProduct(Integer.parseInt(supField.getText().trim()),
                        Integer.parseInt(prodField.getText().trim()), Double.parseDouble(costField.getText().trim())));
                if (ok) {
                    ok("Cost updated!");
                    supField.clear();
                    prodField.clear();
                    costField.clear();
                    refresh.run();
                }
                else {
                    err("Link not found.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("supplier-product link")) {
                return;
            }
            try {
                boolean ok = SupplierProductDAO.deleteSupplierProduct(Integer.parseInt(supField.getText().trim()), Integer.parseInt(prodField.getText().trim()));
                if (ok) {
                    ok("Link removed!");
                    supField.clear();
                    prodField.clear();
                    costField.clear();
                    refresh.run();
                }
                else {
                    err("Not found.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnRef.setOnAction(e -> {
            supField.clear();
            prodField.clear();
            costField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildPurchaseTab() {
        VBox pane = pane();
        sectionTitle(pane, "Purchase Orders");

        // PO creation
        Label poLabel = sub("Create Purchase Order");
        TextField supField = field("Supplier ID");
        TextField empField = field("Employee ID");
        TextField dateField = field("Order Date (yyyy-mm-dd)");
        TextField delField = field("Expected Delivery (yyyy-mm-dd)");
        TextField statField = field("Status: Pending / Delivered / cancelled");
        TextField amtField = field("Total Amount");
        pane.getChildren().addAll(poLabel, row(supField, empField), row(dateField, delField), row(statField, amtField));
        Button btnAddPO = btn("Create PO", GREEN);
        Button btnShowPO = btn("Refresh POs", DARK);
        pane.getChildren().add(new HBox(10, btnAddPO, btnShowPO));
        TableView<ObservableList<String>> poTable = table();
        addCols(poTable, new String[]{"PO#", "Supplier", "Employee", "Date", "Delivery", "Status", "Total (NIS)"}, new int[]{55, 150, 120, 90, 90, 90, 90});
        pane.getChildren().add(poTable);

        // PO Items
        pane.getChildren().add(new Separator());
        Label itemLabel = sub("Add PO Items");
        TextField poNumField = field("PO Number");
        TextField prodField = field("Product ID");
        TextField qtyOrdField = field("Qty Ordered");
        TextField ucostField = field("Unit Cost");
        TextField qtyRecField = field("Qty Received");
        pane.getChildren().addAll(itemLabel, row(poNumField, prodField), row(qtyOrdField, ucostField, qtyRecField));
        Button btnAddItem = btn("Add Item", GREEN);
        Button btnShowItems = btn("Refresh Items", DARK);
        pane.getChildren().add(new HBox(10, btnAddItem, btnShowItems));
        TableView<ObservableList<String>> itemTable = table();
        addCols(itemTable, new String[]{"PO#", "Product", "Qty Ordered", "Unit Cost", "Qty Received"}, new int[]{60, 200, 90, 90, 90});
        pane.getChildren().add(itemTable);
        Runnable refreshPO = () -> {
            poTable.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT po.PONumber, s.SupplierName, CONCAT(e.FirstName,' ',e.LastName) AS Emp, po.OrderDate, po.ExpDeliveryDate, po.Status, po.TotalAmount " +
                                "FROM PurchaseOrder po JOIN Supplier s ON po.SupplierID=s.SupplierID" +
                                "JOIN Employee e ON po.EmployeeID=e.EmployeeID ORDER BY po.OrderDate DESC");
                while (rs.next()) {
                    poTable.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("PONumber")), rs.getString("SupplierName"), rs.getString("Emp"),
                            rs.getString("OrderDate"), rs.getString("ExpDeliveryDate") != null ? rs.getString("ExpDeliveryDate") : "-",
                            rs.getString("Status"), String.format("%.2f", rs.getDouble("TotalAmount"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        Runnable refreshItems = () -> {
            itemTable.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT poi.PONumber, p.ProductName, poi.QtyOrdered, poi.UnitCost, poi.QtyReceived" +
                                " FROM PurchaseOrderItem poi JOIN Product p ON poi.ProductID=p.ProductID ORDER BY poi.PONumber DESC");
                while (rs.next()) {
                    itemTable.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("PONumber")), rs.getString("ProductName"),
                            String.valueOf(rs.getInt("QtyOrdered")), String.format("%.2f", rs.getDouble("UnitCost")),
                            String.valueOf(rs.getInt("QtyReceived"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refreshPO.run();
        refreshItems.run();
        btnAddPO.setOnAction(e -> {
            try {
                boolean ok = PurchaseOrderDAO.addPurchaseOrder(new PurchaseOrder(Integer.parseInt(supField.getText().trim()), Integer.parseInt(empField.getText().trim()), dateField.getText().trim(), delField.getText().trim(), statField.getText().trim(), Double.parseDouble(amtField.getText().trim())));
                if (ok) {
                    ok("PO created!");
                    supField.clear();
                    empField.clear();
                    dateField.clear();
                    delField.clear();
                    statField.clear();
                    amtField.clear();
                    refreshPO.run();
                }
                else {
                    err("Failed.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnAddItem.setOnAction(e -> {
            try {
                boolean ok = PurchaseOrderDAO.addPurchaseOrderItem(new PurchaseOrderItem(Integer.parseInt(poNumField.getText().trim()), Integer.parseInt(prodField.getText().trim()), Integer.parseInt(qtyOrdField.getText().trim()), Double.parseDouble(ucostField.getText().trim()), Integer.parseInt(qtyRecField.getText().trim())));
                if (ok) {
                    ok("Item added!");
                    poNumField.clear();
                    prodField.clear();
                    qtyOrdField.clear();
                    ucostField.clear();
                    qtyRecField.clear();
                    refreshItems.run();
                }
                else {err("Failed.");}
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnShowPO.setOnAction(e -> refreshPO.run());
        btnShowItems.setOnAction(e -> refreshItems.run());
        return pane;
    }

    private VBox buildSaleTab() {
        VBox pane = pane();
        sectionTitle(pane, "Sale Orders");
        Label soLabel = sub("Create Sale Order");
        TextField clientField = field("Client ID");
        TextField empField = field("Employee ID");
        TextField dateField = field("Order Date (yyyy-mm-dd)");
        TextField delivField = field("Delivery Date (yyyy-mm-dd)");
        TextField statField = field("Status: Pending / Approved / Delivered / Cancelled");
        TextField amtField = field("Total Amount");
        TextField payStatField = field("Payment: Paid / Unpaid / Partial");
        pane.getChildren().addAll(soLabel, row(clientField, empField), row(dateField, delivField), row(statField, amtField, payStatField));
        Button btnAdd = btn("Create SO", GREEN);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnRef));
        TableView<ObservableList<String>> soTable = table();
        addCols(soTable, new String[]{"SO#", "Client", "Employee", "Date", "Delivery", "Status", "Payment", "Total (NIS)"}, new int[]{55, 140, 120, 90, 90, 90, 80, 90});
        soTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(ObservableList<String> row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                    return;
                }
                setStyle(row.get(5).equals("Pending") ? "-fx-background-color:#FEF9C3;" : "");
            }
        });
        pane.getChildren().add(soTable);
        pane.getChildren().add(new Separator());
        Label itemLabel = sub("Add Sale Order Items");
        TextField soIdField = field("Sale Order ID");
        TextField batchField = field("Batch ID");
        TextField qtyField = field("Qty");
        TextField priceField = field("Unit Price");
        TextField discField = field("Discount (0 if none)");
        pane.getChildren().addAll(itemLabel, row(soIdField, batchField), row(qtyField, priceField, discField));
        Button btnAddItem = btn("Add Item", GREEN);
        Button btnRefItems = btn("Refresh Items", DARK);
        pane.getChildren().add(new HBox(10, btnAddItem, btnRefItems));
        TableView<ObservableList<String>> itemTable = table();
        addCols(itemTable, new String[]{"SO#", "Batch", "Product", "Qty", "Price", "Discount"}, new int[]{55, 60, 180, 60, 80, 70});
        pane.getChildren().add(itemTable);
        Runnable refreshSO = () -> {
            soTable.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT so.SaleOrderID, cl.ClientName, CONCAT(e.FirstName,' ',e.LastName) AS Emp, so.OrderDate, so.DeliveryDate, so.Status, so.PaymentStatus, so.TotalAmount " +
                                "FROM SaleOrder so JOIN Client cl ON so.ClientID=cl.ClientID JOIN Employee e ON so.EmployeeID=e.EmployeeID ORDER BY so.OrderDate DESC");
                while (rs.next()) {
                    soTable.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("SaleOrderID")), rs.getString("ClientName"), rs.getString("Emp"),
                            rs.getString("OrderDate"), rs.getString("DeliveryDate") != null ? rs.getString("DeliveryDate") : "-",
                            rs.getString("Status"), rs.getString("PaymentStatus"), String.format("%.2f", rs.getDouble("TotalAmount"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        Runnable refreshItems = () -> {
            itemTable.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT soi.SaleOrderID, soi.BatchID, p.ProductName, soi.QtyOrdered, soi.UnitPrice, soi.Discount " +
                                "FROM SaleOrderItem soi JOIN Batch b ON soi.BatchID=b.BatchID JOIN Product p ON b.ProductID=p.ProductID ORDER BY soi.SaleOrderID DESC");
                while (rs.next()) {
                    itemTable.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("SaleOrderID")), String.valueOf(rs.getInt("BatchID")), rs.getString("ProductName"),
                            String.valueOf(rs.getInt("QtyOrdered")), String.format("%.2f", rs.getDouble("UnitPrice")), String.format("%.2f", rs.getDouble("Discount"))));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refreshSO.run();
        refreshItems.run();
        btnAdd.setOnAction(e -> {
            try {
                boolean ok = SaleOrderDAO.addSaleOrder(new SaleOrder(Integer.parseInt(clientField.getText().trim()), Integer.parseInt(empField.getText().trim()), dateField.getText().trim(), delivField.getText().trim(), statField.getText().trim(), Double.parseDouble(amtField.getText().trim()), payStatField.getText().trim()));
                if (ok) {
                    ok("Sale Order created!");
                    clientField.clear();
                    empField.clear();
                    dateField.clear();
                    delivField.clear();
                    statField.clear();
                    amtField.clear();
                    payStatField.clear();
                    refreshSO.run();
                }
                else {
                    err("Failed.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnAddItem.setOnAction(e -> {
            try {
                boolean ok = SaleOrderDAO.addSaleOrderItem(new SaleOrderItem(Integer.parseInt(soIdField.getText().trim()),
                        Integer.parseInt(batchField.getText().trim()), Integer.parseInt(qtyField.getText().trim()),
                        Double.parseDouble(priceField.getText().trim()), Double.parseDouble(discField.getText().trim())));
                if (ok) {
                    ok("Item added!");
                    soIdField.clear();
                    batchField.clear();
                    qtyField.clear();
                    priceField.clear();
                    discField.clear();
                    refreshItems.run();
                }
                else {
                    err("Failed.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnRef.setOnAction(e -> refreshSO.run());
        btnRefItems.setOnAction(e -> refreshItems.run());
        return pane;
    }

    private VBox buildPaymentTab() {
        VBox pane = pane();
        sectionTitle(pane, "Payments");
        TextField soField = field("Sale Order ID (leave empty for outgoing)");
        TextField poField = field("PO Number (leave empty for incoming)");
        TextField dateField = field("Payment Date (yyyy-mm-dd)");
        TextField amtField = field("Amount");
        TextField methodField = field("Method: Cash / BankTransfer / Cheque");
        TextField dirField = field("Direction: Incoming / Outgoing");
        TextField idField = field("Payment ID (for delete)");
        pane.getChildren().addAll(row(soField, poField), row(dateField, amtField), row(methodField, dirField), idField);
        Button btnAdd = btn("Add", GREEN);
        Button btnDel = btn("Delete", RED);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnDel, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "SO#", "PO#", "Date", "Amount (NIS)", "Method", "Direction"}, new int[]{55, 60, 60, 100, 110, 120, 90});
        table.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(ObservableList<String> row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                    return;
                }
                setStyle(row.get(6).equals("Incoming") ? "-fx-background-color:#F0FDF4;" : "-fx-background-color:#FEF2F2;");
            }
        });
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Payment ORDER BY PaymentDate DESC");
                while (rs.next()) {
                    int so = rs.getInt("SaleOrderID");
                    boolean soNull = rs.wasNull();
                    int po = rs.getInt("PONumber");
                    boolean poNull = rs.wasNull();
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("PaymentID")),
                            soNull ? "-" : String.valueOf(so),
                            poNull ? "-" : String.valueOf(po),
                            rs.getString("PaymentDate"), String.format("%.2f", rs.getDouble("Amount")),
                            rs.getString("PaymentMethod"), rs.getString("Direction")));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        };
        refresh.run();
        table.setOnMouseClicked(e -> {
            ObservableList<String> row = table.getSelectionModel().getSelectedItem();
            if (row != null) {
                idField.setText(row.get(0));
            }
        });
        btnAdd.setOnAction(e -> {
            try {
                Integer soId = soField.getText().trim().isEmpty() ? null : Integer.parseInt(soField.getText().trim());
                Integer poNum = poField.getText().trim().isEmpty() ? null : Integer.parseInt(poField.getText().trim());
                boolean ok = PaymentDAO.addPayment(new Payment(soId, poNum, dateField.getText().trim(), Double.parseDouble(amtField.getText().trim()), methodField.getText().trim(), dirField.getText().trim()));
                if (ok) {
                    ok("Payment added!");
                    soField.clear();
                    poField.clear();
                    dateField.clear();
                    amtField.clear();
                    methodField.clear();
                    dirField.clear();
                    refresh.run();
                }
                else {
                    err("Failed. Check enum values.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnDel.setOnAction(e -> {
            if (!confirmDelete("payment")) {
                return;
            }
            try {
                boolean ok = PaymentDAO.deletePayment(Integer.parseInt(idField.getText().trim()));
                if (ok) {
                    ok("Payment deleted!");
                    idField.clear();
                    refresh.run();
                }
                else {
                    err("Not found.");
                }
            }
            catch (Exception ex) {
                err("Invalid ID.");}
        });
        btnRef.setOnAction(e -> {
            idField.clear();
            soField.clear();
            poField.clear();
            dateField.clear();
            amtField.clear();
            methodField.clear();
            dirField.clear();
            refresh.run();
        });
        return pane;
    }

    private VBox buildInventoryTab() {
        VBox pane = pane();
        sectionTitle(pane, "Inventory Transactions");
        Label hint = new Label("Receipt adds to stock | Dispatch subtracts from stock | Adjustment corrects a count");
        hint.setStyle("-fx-text-fill:#64748B; -fx-font-size:12px; -fx-font-style:italic;");
        pane.getChildren().add(hint);
        TextField batchField = field("Batch ID");
        TextField empField = field("Employee ID");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Receipt", "Dispatch", "Adjustment");
        typeBox.setPromptText("Transaction Type");
        typeBox.setStyle("-fx-font-size:13px;");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        TextField qtyField = field("Quantity");
        TextField refField = field("Reference ID (PO or SO number)");
        HBox row1 = new HBox(10, batchField, empField, typeBox);
        HBox row2 = new HBox(10, qtyField, refField);
        HBox.setHgrow(batchField, Priority.ALWAYS);
        HBox.setHgrow(empField, Priority.ALWAYS);
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        HBox.setHgrow(qtyField, Priority.ALWAYS);
        HBox.setHgrow(refField, Priority.ALWAYS);
        pane.getChildren().addAll(row1, row2);
        Button btnAdd = btn("Record Transaction", GREEN);
        Button btnRef = btn("Refresh", DARK);
        pane.getChildren().add(new HBox(10, btnAdd, btnRef));
        TableView<ObservableList<String>> table = table();
        addCols(table, new String[]{"ID", "Batch", "Product", "Employee", "Type", "Qty", "Date", "Ref"}, new int[]{55, 60, 150, 120, 90, 60, 130, 60});
        pane.getChildren().add(table);
        Runnable refresh = () -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT it.TransactionID, it.BatchID, p.ProductName, CONCAT(e.FirstName,' ',e.LastName) AS Emp, it.TxnType, it.Quantity, it.TxnDate, it.ReferenceID " +
                                "FROM InventoryTransaction it JOIN Batch b ON it.BatchID=b.BatchID " +
                                "JOIN Product p ON b.ProductID=p.ProductID JOIN Employee e ON it.EmployeeID=e.EmployeeID ORDER BY it.TxnDate DESC");
                while (rs.next()) {
                    int ref = rs.getInt("ReferenceID");
                    boolean refNull = rs.wasNull();
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("TransactionID")), String.valueOf(rs.getInt("BatchID")),
                            rs.getString("ProductName"), rs.getString("Emp"), rs.getString("TxnType"),
                            String.valueOf(rs.getInt("Quantity")), rs.getString("TxnDate"),
                            refNull ? "-" : String.valueOf(ref)));
                }
                conn.close();
            }
            catch (Exception e) {
                e.printStackTrace();}
        };
        refresh.run();
        btnAdd.setOnAction(e -> {
            try {
                if (typeBox.getValue() == null) {
                    err("Select transaction type.");
                    return;
                }
                boolean ok = InventoryTransactionDAO.addTransaction(new InventoryTransaction(
                        Integer.parseInt(batchField.getText().trim()), Integer.parseInt(empField.getText().trim()),
                        typeBox.getValue(), Integer.parseInt(qtyField.getText().trim()),
                        refField.getText().trim().isEmpty() ? 0 : Integer.parseInt(refField.getText().trim())));
                if (ok) {
                    ok("Transaction recorded and stock updated!");
                    batchField.clear();
                    empField.clear();
                    typeBox.setValue(null);
                    qtyField.clear();
                    refField.clear();
                    refresh.run();
                }
                else {
                    err("Failed. Check batch ID and quantity.");
                }
            }
            catch (Exception ex) {
                err("Invalid values.");
            }
        });
        btnRef.setOnAction(e -> refresh.run());
        return pane;
    }

    private Tab tab(String title, VBox content) {
        Tab t = new Tab(title);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + GRAY + ";");
        t.setContent(sp);
        return t;
    }

    private VBox pane() {
        VBox p = new VBox(14);
        p.setPadding(new Insets(20));
        p.setStyle("-fx-background-color:" + GRAY + ";");
        return p;
    }

    private void sectionTitle(VBox pane, String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(DARK));
        lbl.setStyle("-fx-padding:0 0 8 0; -fx-border-color:" + MID + "; -fx-border-width:0 0 2 0;");
        pane.getChildren().add(lbl);
    }

    private Label sub(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(Color.web(MID));
        return l;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-radius:6;" +
                " -fx-border-radius:6; -fx-border-color:#CBD5E1; -fx-padding:8 12;" +
                " -fx-font-size:13px; -fx-background-color:white;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white;" +
                " -fx-background-radius:6; -fx-padding:8 18; -fx-cursor:hand;" +
                " -fx-font-weight:bold; -fx-font-size:13px;");
        return b;
    }

    private HBox row(javafx.scene.Node... nodes) {
        HBox h = new HBox(10, nodes);
        for (javafx.scene.Node n : nodes) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        return h;
    }

    private TableView<ObservableList<String>> table() {
        TableView<ObservableList<String>> t = new TableView<>();
        t.setPrefHeight(260);
        t.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No data."));
        return t;
    }

    private void addCols(TableView<ObservableList<String>> table, String[] cols, int[] widths) {
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            table.getColumns().add(c);
        }
    }


    private VBox kpi(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(12));
        card.setPrefWidth(140);
        card.setStyle("-fx-background-color:white; -fx-background-radius:10; -fx-border-color:#E2E8F0; -fx-border-radius:10;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px; -fx-text-fill:#64748B;");
        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        val.setTextFill(Color.web(color));
        val.setWrapText(true);
        card.getChildren().addAll(lbl, val);
        return card;
    }

    private boolean confirmDelete(String item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + item + "?");
        alert.setContentText("This cannot be undone.");
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void ok(String msg) {
        statusBar.setText("  " + msg);
        statusBar.setStyle("-fx-background-color:#F0FDF4; -fx-text-fill:#16A34A; -fx-border-color:#BBF7D0; -fx-border-width:1 0 0 0; -fx-padding:8 16;");
    }

    private void err(String msg) {
        statusBar.setText("  " + msg);
        statusBar.setStyle("-fx-background-color:#FEF2F2; -fx-text-fill:#DC2626; -fx-border-color:#FECACA; -fx-border-width:1 0 0 0; -fx-padding:8 16;");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private int getLastId(String query) {
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                int id = rs.getInt(1);
                conn.close();
                return id;
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private VBox buildChartsTab() {
        VBox pane = pane();
        sectionTitle(pane, "Charts & Statistics");

        Label rev = new Label("Monthly Revenue (NIS) - Incoming Payments");
        rev.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        rev.setTextFill(Color.web(DARK));
        pane.getChildren().add(rev);

        Canvas revCanvas = new Canvas(900, 220);
        GraphicsContext revGc = revCanvas.getGraphicsContext2D();
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT DATE_FORMAT(PaymentDate,'%Y-%m') AS Month, " +
                            "SUM(Amount) AS Total FROM Payment " +
                            "WHERE Direction='Incoming' " +
                            "GROUP BY Month ORDER BY Month LIMIT 9");
            java.util.List<String> months = new java.util.ArrayList<>();
            java.util.List<Double> vals   = new java.util.ArrayList<>();
            while (rs.next()) {
                months.add(rs.getString("Month"));
                vals.add(rs.getDouble("Total"));
            }
            conn.close();
            drawBarChart(revGc, months, vals, 900, 220, "#2E6DA4", "NIS");
        } catch (Exception e) { e.printStackTrace(); }
        pane.getChildren().add(revCanvas);
        pane.getChildren().add(new Separator());

        HBox catRow = new HBox(40);
        catRow.setAlignment(Pos.TOP_LEFT);

        VBox catBox = new VBox(8);
        Label catTitle = new Label("Sales by Category (units sold)");
        catTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        catTitle.setTextFill(Color.web(DARK));
        catBox.getChildren().add(catTitle);

        Canvas catCanvas = new Canvas(440, 260);
        GraphicsContext catGc = catCanvas.getGraphicsContext2D();
        java.util.List<String> catNames = new java.util.ArrayList<>();
        java.util.List<Double> catVals  = new java.util.ArrayList<>();
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT c.Name, COALESCE(SUM(soi.QtyOrdered),0) AS Total " +
                            "FROM Category c " +
                            "LEFT JOIN Product p ON p.CategoryID=c.CategoryID " +
                            "LEFT JOIN Batch b ON b.ProductID=p.ProductID " +
                            "LEFT JOIN SaleOrderItem soi ON soi.BatchID=b.BatchID " +
                            "GROUP BY c.CategoryID ORDER BY Total DESC");
            while (rs.next()) {
                catNames.add(rs.getString("Name"));
                catVals.add(rs.getDouble("Total"));
            }
            conn.close();
        } catch (Exception e) { e.printStackTrace(); }
        drawPieChart(catGc, catNames, catVals, 440, 260);
        catBox.getChildren().add(catCanvas);
        catRow.getChildren().add(catBox);

        VBox whBox = new VBox(8);
        Label whTitle = new Label("Stock Levels per Warehouse (units)");
        whTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        whTitle.setTextFill(Color.web(DARK));
        whBox.getChildren().add(whTitle);

        Canvas whCanvas = new Canvas(380, 260);
        GraphicsContext whGc = whCanvas.getGraphicsContext2D();
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT w.WarehouseName, COALESCE(SUM(b.QtyInStock),0) AS Total " +
                            "FROM Warehouse w LEFT JOIN Batch b ON b.WarehouseID=w.WarehouseID " +
                            "GROUP BY w.WarehouseID ORDER BY Total DESC");
            java.util.List<String> wNames = new java.util.ArrayList<>();
            java.util.List<Double> wVals  = new java.util.ArrayList<>();
            while (rs.next()) {
                wNames.add(rs.getString("WarehouseName"));
                wVals.add(rs.getDouble("Total"));
            }
            conn.close();
            drawBarChart(whGc, wNames, wVals, 380, 260, "#16A34A", "units");
        } catch (Exception e) { e.printStackTrace(); }
        whBox.getChildren().add(whCanvas);
        catRow.getChildren().add(whBox);
        pane.getChildren().add(catRow);
        pane.getChildren().add(new Separator());

        Label topCliTitle = new Label("Top 5 Clients by Total Orders Value (NIS)");
        topCliTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        topCliTitle.setTextFill(Color.web(DARK));
        pane.getChildren().add(topCliTitle);

        Canvas topCliCanvas = new Canvas(900, 200);
        GraphicsContext topCliGc = topCliCanvas.getGraphicsContext2D();
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT cl.ClientName, COALESCE(SUM(so.TotalAmount),0) AS Total " +
                            "FROM Client cl LEFT JOIN SaleOrder so ON so.ClientID=cl.ClientID " +
                            "GROUP BY cl.ClientID ORDER BY Total DESC LIMIT 5");
            java.util.List<String> cNames = new java.util.ArrayList<>();
            java.util.List<Double> cVals  = new java.util.ArrayList<>();
            while (rs.next()) {
                cNames.add(rs.getString("ClientName"));
                cVals.add(rs.getDouble("Total"));
            }
            conn.close();
            drawBarChart(topCliGc, cNames, cVals, 900, 200, "#D97706", "NIS");
        } catch (Exception e) { e.printStackTrace(); }
        pane.getChildren().add(topCliCanvas);
        pane.getChildren().add(new Separator());

        Label topProdTitle = new Label("Top 5 Best-Selling Products (units sold)");
        topProdTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        topProdTitle.setTextFill(Color.web(DARK));
        pane.getChildren().add(topProdTitle);

        Canvas topProdCanvas = new Canvas(900, 200);
        GraphicsContext topProdGc = topProdCanvas.getGraphicsContext2D();
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT p.ProductName, COALESCE(SUM(soi.QtyOrdered),0) AS Total " +
                            "FROM Product p " +
                            "LEFT JOIN Batch b ON b.ProductID=p.ProductID " +
                            "LEFT JOIN SaleOrderItem soi ON soi.BatchID=b.BatchID " +
                            "GROUP BY p.ProductID ORDER BY Total DESC LIMIT 5");
            java.util.List<String> pNames = new java.util.ArrayList<>();
            java.util.List<Double> pVals  = new java.util.ArrayList<>();
            while (rs.next()) {
                pNames.add(rs.getString("ProductName"));
                pVals.add(rs.getDouble("Total"));
            }
            conn.close();
            drawBarChart(topProdGc, pNames, pVals, 900, 200, "#DC2626", "units");
        } catch (Exception e) { e.printStackTrace(); }
        pane.getChildren().add(topProdCanvas);

        return pane;
    }

    private void drawBarChart(GraphicsContext gc, java.util.List<String> labels,
                              java.util.List<Double> values, double W, double H,
                              String hexColor, String unit) {
        double padL = 60, padB = 50, padT = 20, padR = 20;
        double chartW = W - padL - padR;
        double chartH = H - padT - padB;

        gc.setFill(Color.web("#F8FAFC"));
        gc.fillRect(0, 0, W, H);

        if (values.isEmpty()) {
            gc.setFill(Color.web("#94A3B8"));
            gc.fillText("No data", W / 2 - 20, H / 2);
            return;
        }

        double maxVal = values.stream().mapToDouble(d -> d).max().orElse(1);
        if (maxVal == 0) maxVal = 1;

        int n = labels.size();
        double barW = (chartW / n) * 0.6;
        double gap  = (chartW / n) * 0.4;

        gc.setStroke(Color.web("#E2E8F0"));
        gc.setFill(Color.web("#64748B"));
        gc.setFont(Font.font("Arial", 10));
        gc.setLineWidth(1);
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            double y = padT + chartH - (chartH * i / gridLines);
            gc.strokeLine(padL, y, padL + chartW, y);
            double val = maxVal * i / gridLines;
            gc.fillText(String.format("%.0f", val), 2, y + 4);
        }

        gc.setFill(Color.web(hexColor));
        for (int i = 0; i < n; i++) {
            double x   = padL + i * (chartW / n) + gap / 2;
            double barH = (values.get(i) / maxVal) * chartH;
            double y    = padT + chartH - barH;
            gc.fillRoundRect(x, y, barW, barH, 4, 4);

            gc.setFill(Color.web("#1E293B"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            String valStr = values.get(i) > 999
                    ? String.format("%.0fk", values.get(i) / 1000)
                    : String.format("%.0f", values.get(i));
            gc.fillText(valStr, x + barW / 2 - 8, y - 4);

            gc.setFill(Color.web("#475569"));
            gc.setFont(Font.font("Arial", 10));
            String lbl = labels.get(i);
            if (lbl.length() > 12) lbl = lbl.substring(0, 11) + "…";
            gc.fillText(lbl, x, padT + chartH + 14);

            gc.setFill(Color.web(hexColor));
        }

        gc.setStroke(Color.web("#CBD5E1"));
        gc.setLineWidth(1.5);
        gc.strokeLine(padL, padT, padL, padT + chartH);
        gc.strokeLine(padL, padT + chartH, padL + chartW, padT + chartH);
    }

    private void drawPieChart(GraphicsContext gc, java.util.List<String> labels,
                              java.util.List<Double> values, double W, double H) {
        gc.setFill(Color.web("#F8FAFC"));
        gc.fillRect(0, 0, W, H);

        if (values.isEmpty()) {
            gc.setFill(Color.web("#94A3B8"));
            gc.fillText("No data", W / 2 - 20, H / 2);
            return;
        }

        double total = values.stream().mapToDouble(d -> d).sum();
        if (total == 0) total = 1;

        String[] palette = {
                "#2E6DA4","#16A34A","#DC2626","#D97706","#7C3AED",
                "#0891B2","#BE185D","#65A30D","#EA580C","#6366F1"
        };

        double cx = 130, cy = H / 2, r = 110;
        double startAngle = 0;

        for (int i = 0; i < values.size(); i++) {
            double sweep = (values.get(i) / total) * 360.0;
            gc.setFill(Color.web(palette[i % palette.length]));
            gc.fillArc(cx - r, cy - r, r * 2, r * 2, startAngle, sweep,
                    javafx.scene.shape.ArcType.ROUND);
            startAngle += sweep;
        }

        gc.setFill(Color.web("#F8FAFC"));
        gc.fillOval(cx - 55, cy - 55, 110, 110);

        gc.setFont(Font.font("Arial", 11));
        double legendX = cx + r + 20;
        double legendY = 24;
        for (int i = 0; i < labels.size(); i++) {
            gc.setFill(Color.web(palette[i % palette.length]));
            gc.fillRoundRect(legendX, legendY + i * 22, 12, 12, 3, 3);
            gc.setFill(Color.web("#1E293B"));
            String lbl = labels.get(i);
            if (lbl.length() > 14) lbl = lbl.substring(0, 13) + "…";
            double pct = total > 0 ? (values.get(i) / total * 100) : 0;
            gc.fillText(lbl + " (" + String.format("%.0f", pct) + "%)",
                    legendX + 18, legendY + i * 22 + 11);
        }
    }
}
