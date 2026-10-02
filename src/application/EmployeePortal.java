package application;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeePortal extends Application {

    private final UserAccount account;
    private final int employeeID;
    private Integer employeeWarehouseID;
    private String employeeRole = "";
    private Label statusBar;

    private static final String DARK = "#1B3A6B";
    private static final String MID = "#2E6DA4";
    private static final String LIGHT = "#E8F2FF";
    private static final String GREEN = "#16A34A";
    private static final String RED = "#DC2626";
    private static final String ORANGE = "#D97706";
    private static final String GRAY = "#F5F7FA";

    public EmployeePortal(UserAccount account) {

        this.account = account;
        this.employeeID = account.getEmployeeID() != null ? account.getEmployeeID() : 0;
        this.employeeWarehouseID = getEmployeeWarehouseID();
    }

    public EmployeePortal() {
        this.account = null;
        this.employeeID = 0;
    }

    @Override
    public void start(Stage stage) {
        employeeRole = getEmployeeRole();
        stage.setTitle("Health Plus | Employee Portal (" + employeeRole + ")");
        statusBar = new Label("Ready");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setPadding(new Insets(8, 16, 8, 16));
        statusBar.setFont(Font.font("Arial", 12));
        statusBar.setStyle("-fx-background-color:#F0FDF4;" +
                " -fx-text-fill:#16A34A;" +
                " -fx-border-color:#BBF7D0; " +
                "-fx-border-width:1 0 0 0;");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setStyle("-fx-background-color:" + DARK + ";");
        Label logo = new Label("🏥");
        logo.setFont(Font.font(22));
        VBox titleBox = new VBox(2);
        Label title = new Label("Health Plus | Employee Portal");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);
        Label sub = new Label("Logged in as: " + getEmployeeName() + "  |  Role: " + employeeRole);
        sub.setFont(Font.font("Arial", 12));
        sub.setTextFill(Color.web("#A8C4E0"));
        titleBox.getChildren().addAll(title, sub);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color:transparent;" +
                " -fx-text-fill:#A8C4E0;" +
                " -fx-border-color:#A8C4E0; " +
                "-fx-border-radius:6; " +
                "-fx-background-radius:6; " +
                "-fx-cursor:hand;" +
                " -fx-padding:6 14;");
        logoutBtn.setOnAction(e -> {
            stage.close();
            try {
                new LoginScreen().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        header.getChildren().addAll(logo, titleBox, spacer, logoutBtn);
        HBox roleBadge = new HBox();
        roleBadge.setPadding(new Insets(8, 20, 8, 20));
        String badgeColor = employeeRole.contains("Manager") ? "#064E3B" :
                employeeRole.contains("Sales") ? "#1E3A5F" : "#4C1D95";
        roleBadge.setStyle("-fx-background-color:" + badgeColor + ";");
        Label roleLabel = new Label(getRoleDescription());
        roleLabel.setTextFill(Color.web("#A8C4E0"));
        roleLabel.setFont(Font.font("Arial", 12));
        roleBadge.getChildren().add(roleLabel);
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(buildTab(" Inventory", buildInventoryTab()));
        if (employeeRole.contains("Sales") || employeeRole.contains("Manager")) {
            tabs.getTabs().add(buildTab("$ Sale Orders", buildSaleOrdersTab()));
            tabs.getTabs().add(buildTab(" Clients", buildClientsTab()));
        }

        if (employeeRole.contains("Procurement") || employeeRole.contains("Manager")) {
            tabs.getTabs().add(buildTab("🛒 Purchase Orders", buildPurchaseOrdersTab()));
            tabs.getTabs().add(buildTab(" Suppliers", buildSuppliersTab()));
        }

        if (employeeRole.contains("Manager")) {
            tabs.getTabs().add(buildTab(" " +
                    "Reports", buildReportsTab()));
        }

        VBox root = new VBox(0, header, roleBadge, tabs, statusBar);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        Scene scene = new Scene(root, 980, 720);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildInventoryTab() {
        VBox pane = pane();
        sectionTitle(pane, "Inventory, Batches & Stock Movements");

        TextField searchField = styledField("Search by product name...");
        searchField.setPrefWidth(220);

        ComboBox<String> warehouseFilter = new ComboBox<>();
        warehouseFilter.setPromptText("All Warehouses");
        warehouseFilter.setStyle("-fx-font-size:12px;");
        loadWarehouseFilter(warehouseFilter);
        if (employeeWarehouseID != null) {
            warehouseFilter.setVisible(false);
            warehouseFilter.setManaged(false);
        }

        Button btnAll = filterBtn("All", MID);
        Button btnLowStock = filterBtn("Low Stock", ORANGE);
        Button btnExpiring = filterBtn("Expiring", RED);
        Button btnRefresh = filterBtn("Refresh", "#64748B");

        HBox filterBar = new HBox(10, searchField, warehouseFilter, btnAll, btnLowStock, btnExpiring, btnRefresh);

        filterBar.setAlignment(Pos.CENTER_LEFT);
        pane.getChildren().add(filterBar);

        TableView<ObservableList<String>> batchTable = new TableView<>();
        batchTable.setPrefHeight(300);
        batchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        batchTable.setPlaceholder(new Label("No batches found."));

        String[] cols = {"Batch #", "Product", "Warehouse",
                "Qty in Stock", "Reorder Level", "Status", "Expiry", "Location"};
        int[] widths = {65, 160, 140, 90, 95, 80, 95, 90};

        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            if (cols[i].equals("Status")) {
                c.setCellFactory(cc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);
                        setStyle(item.equals("OK") ? "-fx-text-fill:" + GREEN + "; -fx-font-weight:bold;" :
                                item.equals("Low Stock") ? "-fx-text-fill:" + ORANGE + "; -fx-font-weight:bold;" :
                                "-fx-text-fill:" + RED + "; -fx-font-weight:bold;");
                    }
                });
            }
            batchTable.getColumns().add(c);
        }

        batchTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(ObservableList<String> row, boolean empty) {
                super.updateItem(row, empty);
                if (row == null || empty) {
                    setStyle("");
                    return;
                }
                String status = row.get(5);
                if (status.equals("Expiring")) {
                    setStyle("-fx-background-color:#FEE2E2;");
                } else if (status.equals("Low Stock")) {
                    setStyle("-fx-background-color:#FEF9C3;");
                } else {
                    setStyle("");
                }
            }
        });
        HBox legend = new HBox(16,
                legendChip("#FEF9C3", "Below reorder level"),
                legendChip("#FEE2E2", "Expiring within 60 days"));
        pane.getChildren().addAll(batchTable, legend, new Separator());
        VBox txnCard = card("#FFFFFF");
        Label txnTitle = new Label("Record a Stock Movement");
        txnTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        txnTitle.setTextFill(Color.web(DARK));
        txnCard.getChildren().add(txnTitle);
        TextField batchField = styledField("Batch ID  (which batch is this movement for?)");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Receipt", "Dispatch", "Adjustment");
        typeBox.setPromptText("Select type...");
        typeBox.setStyle("-fx-font-size:13px;");
        typeBox.setMaxWidth(Double.MAX_VALUE);
        TextField qtyField = styledField("Quantity  (how many units?)");
        TextField refField = styledField("Reference ID (PO number for Receipt, SO number for Dispatch)");
        HBox formRow1 = new HBox(10, batchField, typeBox);
        HBox formRow2 = new HBox(10, qtyField, refField);
        HBox.setHgrow(batchField, Priority.ALWAYS);
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        HBox.setHgrow(qtyField, Priority.ALWAYS);
        HBox.setHgrow(refField, Priority.ALWAYS);
        Button btnAddTxn = actionBtn("+ Record Movement", GREEN);
        if (!employeeRole.contains("Manager")) {
            batchField.setDisable(true);
            typeBox.setDisable(true);
            qtyField.setDisable(true);
            refField.setDisable(true);
            btnAddTxn.setDisable(true);
            Label noAccessLbl = new Label("⚠  Only Warehouse Managers can record stock movements.");
            noAccessLbl.setStyle("-fx-text-fill:" + ORANGE + "; " +
                    "-fx-font-weight:bold; " +
                    "-fx-font-size:12px;");
            txnCard.getChildren().add(noAccessLbl);
        }

        txnCard.getChildren().addAll(formRow1, formRow2, btnAddTxn);
        pane.getChildren().add(txnCard);
        Runnable loadAll = () -> refreshBatches(batchTable, searchField.getText().trim(), warehouseFilter.getValue(), "all");
        Runnable loadLow = () -> refreshBatches(batchTable, searchField.getText().trim(), warehouseFilter.getValue(), "low");
        Runnable loadExpiring = () -> refreshBatches(batchTable, searchField.getText().trim(), warehouseFilter.getValue(), "expiring");
        btnAll.setOnAction(e -> loadAll.run());
        btnLowStock.setOnAction(e -> loadLow.run());
        btnExpiring.setOnAction(e -> loadExpiring.run());
        btnRefresh.setOnAction(e -> loadAll.run());
        searchField.setOnAction(e -> loadAll.run());
        warehouseFilter.setOnAction(e -> loadAll.run());
        btnAddTxn.setOnAction(e -> {
            try {
                String batchTxt = batchField.getText().trim();
                String typeTxt = typeBox.getValue();
                String qtyTxt = qtyField.getText().trim();
                String refTxt = refField.getText().trim();
                if (batchTxt.isEmpty() || typeTxt == null || qtyTxt.isEmpty()) {
                    showStatus("X Please fill in Batch ID, Type, and Quantity.", false);
                    return;
                }

                InventoryTransaction t = new InventoryTransaction(
                        Integer.parseInt(batchTxt),
                        employeeID,
                        typeTxt,
                        Integer.parseInt(qtyTxt),
                        refTxt.isEmpty() ? 0 : Integer.parseInt(refTxt)
                );
                boolean ok = InventoryTransactionDAO.addTransaction(t);
                if (ok) {
                    showStatus("✓ Stock movement recorded! Batch #" + batchTxt + " updated.", true);
                    batchField.clear();
                    typeBox.setValue(null);
                    qtyField.clear();
                    refField.clear();
                    loadAll.run();
                } else {
                    showStatus("X Failed. check that Batch ID exists and quantity is valid.", false);
                }
            } catch (NumberFormatException ex) {
                showStatus("X Batch ID, Quantity, and Reference must be numbers.", false);
            }
        });
        loadAll.run();
        return pane;
    }

    private void refreshBatches(TableView<ObservableList<String>> table, String keyword, String warehouse, String filter) {
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        try {
            Connection conn = DBConnection.connect();
            StringBuilder sql = new StringBuilder(
                    "SELECT b.BatchID, p.ProductName, w.WarehouseName, b.QtyInStock, " +
                            "p.ReorderLevel, b.ExpiryDate, b.StorageLocation " +
                            "FROM Batch b JOIN Product p ON b.ProductID=p.ProductID " +
                            "JOIN Warehouse w ON b.WarehouseID=w.WarehouseID WHERE 1=1");
            List<String> params = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND p.ProductName LIKE ?");
                params.add("%" + keyword + "%");
            }
            if (warehouse != null && !warehouse.equals("All Warehouses") && !warehouse.isEmpty()) {
                sql.append(" AND w.WarehouseName = ?");
                params.add(warehouse);
            }
            if (employeeWarehouseID != null
                    && (warehouse == null || warehouse.equals("All Warehouses"))) {
                sql.append(" AND b.WarehouseID = ?");
                params.add(String.valueOf(employeeWarehouseID));
            }
            if (filter.equals("low")) {
                sql.append(" AND b.QtyInStock < p.ReorderLevel");
            } else if (filter.equals("expiring")) {
                sql.append(" AND b.ExpiryDate IS NOT NULL AND b.ExpiryDate <= DATE_ADD(CURDATE(), INTERVAL 60 DAY)");
            }
            sql.append(filter.equals("expiring") ? " ORDER BY b.ExpiryDate ASC" : " ORDER BY p.ProductName");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt("QtyInStock");
                int reorder = rs.getInt("ReorderLevel");
                String expiryStr = rs.getString("ExpiryDate");
                boolean expiringSoon = expiryStr != null &&
                        LocalDate.parse(expiryStr).isBefore(LocalDate.now().plusDays(60));
                String status = expiringSoon ? "Expiring" : (qty < reorder ? "Low Stock" : "OK");
                rows.add(FXCollections.observableArrayList(
                        String.valueOf(rs.getInt("BatchID")),
                        rs.getString("ProductName"),
                        rs.getString("WarehouseName"),
                        String.valueOf(qty),
                        String.valueOf(reorder),
                        status,
                        expiryStr != null ? expiryStr : "—",
                        rs.getString("StorageLocation") != null ? rs.getString("StorageLocation") : "—"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        table.setItems(rows);
    }

    private void loadWarehouseFilter(ComboBox<String> cb) {
        try {
            Connection conn = DBConnection.connect();

            if (employeeWarehouseID != null) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT WarehouseName FROM Warehouse WHERE WarehouseID = ?");
                ps.setInt(1, employeeWarehouseID);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String assignedWarehouse = rs.getString("WarehouseName");
                    cb.getItems().add(assignedWarehouse);
                    cb.setValue(assignedWarehouse);
                }
            } else {
                ResultSet rs = conn.createStatement().executeQuery(
                        "SELECT WarehouseName FROM Warehouse ORDER BY WarehouseName");
                cb.getItems().add("All Warehouses");

                while (rs.next()) {
                    cb.getItems().add(rs.getString("WarehouseName"));
                }
                cb.setValue("All Warehouses");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox buildSaleOrdersTab() {
        VBox pane = pane();
        sectionTitle(pane, "$ Sale Orders ");
        Button refreshBtn = actionBtn(" ⟳ Refresh Orders", MID);
        pane.getChildren().add(refreshBtn);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(320);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No sale orders yet."));
        String[] cols = {
                "SO #", "Client Name", "Handled By", "Order Date", "Delivery Date", "Status", "Payment", "Total (₪)"
        }
                ;
        int[] widths = {55, 140, 120, 90, 90, 85, 80, 85};

        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            if (cols[i].equals("Status") || cols[i].equals("Payment")) {
                c.setCellFactory(cc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);
                        String color = (item.equals("Delivered") || item.equals("Paid")) ? GREEN :
                                (item.equals("Partial")) ? ORANGE : RED;
                        setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
                    }
                });
            }
            table.getColumns().add(c);
        }

        TableColumn<ObservableList<String>, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(220);
        actionCol.setCellFactory(c -> new TableCell<>() {
            final Button approveBtn = new Button("✓ Approve Order");
            final Button deliverBtn = new Button("☐ Mark Delivered");
            final Button partialBtn = new Button("$ Record Remaining Payment");
            {

                approveBtn.setStyle("-fx-background-color:" + GREEN + ";" +
                        " -fx-text-fill:white;" +
                        " -fx-background-radius:6;" +
                        " -fx-cursor:hand;" +
                        " -fx-font-size:11px;" +
                        " -fx-padding:5 10;");
                approveBtn.setOnAction(e -> {
                    ObservableList<String> row = getTableView().getItems().get(getIndex());
                    int soId = Integer.parseInt(row.get(0));
                    String clientName = row.get(1);
                    double total = Double.parseDouble(row.get(7));
                    showApproveDialog(soId, clientName, total, () -> refreshSaleOrders(table));
                });
                deliverBtn.setStyle("-fx-background-color:" + MID + "; " +
                        "-fx-text-fill:white; " +
                        "-fx-background-radius:6;" +
                        " -fx-cursor:hand;" +
                        " -fx-font-size:11px;" +
                        " -fx-padding:5 10;");
                deliverBtn.setOnAction(e -> {
                    ObservableList<String> row = getTableView().getItems().get(getIndex());
                    int soId = Integer.parseInt(row.get(0));
                    String payment = row.get(6);
                    try {
                        Connection conn = DBConnection.connect();
                        PreparedStatement ps = conn.prepareStatement(
                                "UPDATE SaleOrder SET Status='Delivered' WHERE SaleOrderID=?");
                        ps.setInt(1, soId);
                        ps.executeUpdate();
                        conn.close();
                        showStatus("✓ Order #" + soId + " marked Delivered! Payment status: " + payment, true);
                        refreshSaleOrders(table);
                    } catch (Exception ex) {
                        showStatus("X Failed to mark delivered.", false);
                    }
                });
                partialBtn.setStyle("-fx-background-color:" + ORANGE + "; -fx-text-fill:white; -fx-background-radius:6;" +
                        " -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10;");
                partialBtn.setOnAction(e -> {
                    ObservableList<String> row = getTableView().getItems().get(getIndex());
                    int soId = Integer.parseInt(row.get(0));
                    String clientName = row.get(1);
                    double total = Double.parseDouble(row.get(7));
                    showConfirmOrderDialog(soId, clientName, total, true, () -> refreshSaleOrders(table));
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                ObservableList<String> row = getTableView().getItems().get(getIndex());
                String status = row.get(5);
                String payment = row.get(6);
                VBox btns = new VBox(4);
                if (status.equals("Pending")) {
                    btns.getChildren().add(approveBtn);
                    if (payment.equals("Partial")) {
                        btns.getChildren().add(partialBtn);
                    }
                } else if (status.equals("Approved")) {
                    btns.getChildren().add(deliverBtn);
                    if (payment.equals("Partial")) {
                        btns.getChildren().add(partialBtn);
                    }
                } else if (status.equals("Delivered") && payment.equals("Partial")) {
                    btns.getChildren().add(partialBtn);
                }
                setGraphic(btns.getChildren().isEmpty() ? null : btns);
            }
        });
        table.getColumns().add(actionCol);
        pane.getChildren().add(table);
        pane.getChildren().add(new Separator());
        VBox createCard = card("#FFFFFF");
        Label createTitle = new Label("Create a Sale Order Manually");
        createTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        createTitle.setTextFill(Color.web(MID));
        TextField clientField = styledField("Client ID  (look it up in the clients tab)");
        TextField dateField = styledField("Order Date (yyyy-mm-dd)");
        TextField delivField = styledField("Expected Delivery Date (yyyy-mm-dd)");
        TextField amtField = styledField("Total Amount (₪)");
        HBox row1 = new HBox(10, clientField, dateField);
        HBox row2 = new HBox(10, delivField, amtField);
        HBox.setHgrow(clientField, Priority.ALWAYS);
        HBox.setHgrow(dateField, Priority.ALWAYS);
        HBox.setHgrow(delivField, Priority.ALWAYS);
        HBox.setHgrow(amtField, Priority.ALWAYS);
        Button btnCreate = actionBtn("＋ create Sale Order", GREEN);
        createCard.getChildren().addAll(createTitle, row1, row2, btnCreate);
        pane.getChildren().add(createCard);
        refreshBtn.setOnAction(e -> refreshSaleOrders(table));
        btnCreate.setOnAction(e -> {
            try {
                SaleOrder so = new SaleOrder(
                        Integer.parseInt(clientField.getText().trim()),
                        employeeID,
                        dateField.getText().trim(),
                        delivField.getText().trim(),
                        "Pending",
                        Double.parseDouble(amtField.getText().trim()),
                        "Unpaid"
                );
                boolean ok = SaleOrderDAO.addSaleOrder(so);
                if (ok) {
                    showStatus(" Sale Order created | status Pending. waiting for the client payment.", true);
                    clientField.clear();
                    dateField.clear();
                    delivField.clear();
                    amtField.clear();
                    refreshSaleOrders(table);
                } else {
                    showStatus("X Could not create Sale Order. Check Client ID is valid.", false);
                }
            } catch (Exception ex) {
                showStatus("X", false);
            }
        });
        refreshSaleOrders(table);
        return pane;
    }

    private void refreshSaleOrders(TableView<ObservableList<String>> table) {
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        try {
            Connection conn = DBConnection.connect();
            StringBuilder sql = new StringBuilder(
                    "SELECT so.SaleOrderID, cl.ClientName, " +
                            "CONCAT(e.FirstName, ' ', e.LastName) AS EmpName, " +
                            "so.OrderDate, so.DeliveryDate, so.Status, so.PaymentStatus, so.TotalAmount " +
                            "FROM SaleOrder so " +
                            "JOIN Client cl ON so.ClientID=cl.ClientID " +
                            "JOIN Employee e ON so.EmployeeID=e.EmployeeID " +
                            "JOIN SaleOrderItem soi ON so.SaleOrderID=soi.SaleOrderID " +
                            "JOIN Batch b ON soi.BatchID=b.BatchID WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (employeeWarehouseID != null) {
                sql.append(" AND b.WarehouseID = ?");
                params.add(employeeWarehouseID);
            }
            sql.append(" GROUP BY so.SaleOrderID ORDER BY so.OrderDate DESC");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(FXCollections.observableArrayList(
                        String.valueOf(rs.getInt("SaleOrderID")),
                        rs.getString("ClientName"),
                        rs.getString("EmpName"),
                        rs.getString("OrderDate"),
                        rs.getString("DeliveryDate") != null ? rs.getString("DeliveryDate") : "—",
                        rs.getString("Status"),
                        rs.getString("PaymentStatus"),
                        String.format("%.2f", rs.getDouble("TotalAmount"))
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        table.setItems(rows);
    }

    private void showApproveDialog(int soId, String clientName, double total, Runnable onDone) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Approve Order #" + soId);
        dialog.setHeaderText("Record payment received from: " + clientName);
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setPrefWidth(380);
        Label totalLbl = new Label("Order Total: " + String.format("%.2f", total));
        totalLbl.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:#1B3A6B;");
        Label methodLbl = new Label("Payment Method received:");
        methodLbl.setStyle("-fx-font-weight:bold;");
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Cash", "BankTransfer", "Cheque");
        methodBox.setValue("Cash");
        methodBox.setMaxWidth(Double.MAX_VALUE);
        Label amtLbl = new Label("Amount received (leave 0 if not yet paid):");
        amtLbl.setStyle("-fx-font-weight:bold;");
        TextField amtField = new TextField(String.format("%.2f", total));
        content.getChildren().addAll(totalLbl, methodLbl, methodBox, amtLbl, amtField);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) {
                return;
            }
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                String payStatus = amt >= total ? "Paid" : (amt > 0 ? "Partial" : "Unpaid");
                String today = LocalDate.now().toString();
                if (amt > 0) {
                    PaymentDAO.addPayment(new Payment(soId, null, today, amt, methodBox.getValue(), "Incoming"));
                }

                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE SaleOrder SET Status='Approved', PaymentStatus=? WHERE SaleOrderID=?");
                ps.setString(1, payStatus);
                ps.setInt(2, soId);
                ps.executeUpdate();
                conn.close();
                showStatus("Order #" + soId + " approved. Payment: " + payStatus +
                        (amt > 0 ? " (" + String.format("%.2f", amt) + " received)" : " (not yet received)"), true);
                onDone.run();
            } catch (Exception ex) {
                showStatus("Invalid amount entered.", false);
            }
        });
    }

    private void showConfirmOrderDialog(int soId, String clientName, double total,
                                        boolean isAdditionalPayment, Runnable onDone) {

        double alreadyPaid = getAlreadyPaidAmount(soId);
        double remaining = Math.max(total - alreadyPaid, 0);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isAdditionalPayment ? "Record Payment - Order #" + soId : "Mark Order #" + soId + " as Delivered");
        dialog.setHeaderText((isAdditionalPayment ? "Additional payment from: " : "Recording payment from: ") + clientName);
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setPrefWidth(380);

        Label totalLbl = new Label("Order Total: ₪" + String.format("%.2f", total));
        totalLbl.setStyle("-fx-font-weight:bold; -fx-font-size:13px; -fx-text-fill:" + DARK + ";");
        Label paidLbl = new Label("Already Paid: ₪" + String.format("%.2f", alreadyPaid));
        paidLbl.setStyle("-fx-font-size:13px; -fx-text-fill:" + GREEN + "; -fx-font-weight:bold;");
        Label remainLbl = new Label("Remaining: ₪" + String.format("%.2f", remaining));
        remainLbl.setStyle("-fx-font-size:13px; -fx-text-fill:" + (remaining > 0 ? RED : GREEN) + "; -fx-font-weight:bold;");
        Label methodLbl = new Label("How did they pay?");

        methodLbl.setStyle("-fx-font-weight:bold;");
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Cash", "BankTransfer", "Cheque");
        methodBox.setValue("Cash");
        methodBox.setMaxWidth(Double.MAX_VALUE);
        Label amtLbl = new Label("Amount received now (₪):");
        amtLbl.setStyle("-fx-font-weight:bold;");
        TextField amtField = new TextField(String.format("%.2f", remaining));
        content.getChildren().addAll(totalLbl, paidLbl, remainLbl, methodLbl, methodBox, amtLbl, amtField);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) {
                return;
            }
            try {
                double amt = Double.parseDouble(amtField.getText().trim());
                if (amt <= 0) {
                    showStatus(" Amount must be greater than zero.", false);
                    return;
                }

                double totalPaidAfter = alreadyPaid + amt;
                String payStatus = totalPaidAfter >= total ? "Paid" :
                        totalPaidAfter > 0 ? "Partial" : "Unpaid";
                String today = LocalDate.now().toString();
                PaymentDAO.addPayment(new Payment(soId, null, today, amt, methodBox.getValue(), "Incoming"));
                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE SaleOrder SET Status='Delivered', PaymentStatus=? WHERE SaleOrderID=?");
                ps.setString(1, payStatus);
                ps.setInt(2, soId);
                ps.executeUpdate();
                conn.close();
                showStatus(" Payment of ₪" + String.format("%.2f", amt) + " recorded for Order #" + soId +
                        " , now " + payStatus + " (total paid: ₪" + String.format("%.2f", totalPaidAfter) + ")", true);
                onDone.run();
            } catch (Exception ex) {
                showStatus(" Invalid amount entered.", false);
            }
        });
    }

    private double getAlreadyPaidAmount(int soId) {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COALESCE(SUM(Amount), 0) AS TotalPaid FROM Payment WHERE SaleOrderID=?");
            ps.setInt(1, soId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double amt = rs.getDouble("TotalPaid");
                conn.close();
                return amt;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private VBox buildClientsTab() {
        VBox pane = pane();
        sectionTitle(pane, "🏥 Clients");
        TextField searchField = styledField("Search by client name...");
        searchField.setPrefWidth(260);
        Button refreshBtn = actionBtn("🔄 Refresh", MID);
        HBox topBar = new HBox(10, searchField, refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        pane.getChildren().add(topBar);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(420);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No clients found."));
        String[] cols = {
                "ID", "Client Name", "Type", "Phone", "City", "Credit Limit (₪)"
        }
                ;
        int[] widths = {
                50, 200, 90, 130, 110, 120
        }
                ;
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            table.getColumns().add(c);
        }
        pane.getChildren().add(table);
        Runnable load = () -> {
            String keyword = searchField.getText().trim();
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                String sql = "SELECT * FROM Client WHERE ClientName LIKE ? ORDER BY ClientName";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "%" + keyword + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    rows.add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("ClientID")),
                            rs.getString("ClientName"),
                            rs.getString("ClientType"),
                            rs.getString("Phone"),
                            rs.getString("City"),
                            String.format("%.2f", rs.getDouble("CreditLimit"))
                    ));
                }
                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            table.setItems(rows);
        }
                ;
        refreshBtn.setOnAction(e -> load.run());
        searchField.setOnAction(e -> load.run());
        load.run();
        return pane;
    }

    private VBox buildPurchaseOrdersTab() {
        VBox pane = pane();
        sectionTitle(pane, "🛒 Purchase Orders");
        Button refreshBtn = actionBtn("🔄 Refresh", MID);
        pane.getChildren().add(refreshBtn);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(320);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No purchase orders yet."));
        String[] cols = {
                "PO #", "Supplier Name", "Order Date", "Exp. Delivery", "Status", "Total (₪)"
        }
                ;
        int[] widths = {
                55, 170, 95, 95, 90, 90
        }
                ;
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            if (cols[i].equals("Status")) {
                c.setCellFactory(cc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);
                        String color = item.equals("Delivered") ? GREEN : item.equals("Pending") ? ORANGE : RED;
                        setStyle("-fx-text-fill:" + color + ";" +
                                " -fx-font-weight:bold;");
                    }
                });
            }
            table.getColumns().add(c);
        }

        TableColumn<ObservableList<String>, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(c -> new TableCell<>() {
            final Button receiveBtn = new Button("📥 Receive Stock");
            {
                receiveBtn.setStyle("-fx-background-color:" + GREEN + ";" +
                        " -fx-text-fill:white;" +
                        " -fx-background-radius:6;" +
                        " -fx-cursor:hand; " +
                        "-fx-font-size:11px;" +
                        " -fx-padding:5 10;");
                receiveBtn.setOnAction(e -> {
                    ObservableList<String> row = getTableView().getItems().get(getIndex());
                    showReceivePODialog(Integer.parseInt(row.get(0)), () -> refreshPurchaseOrders(table));
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                setGraphic(getTableView().getItems().get(getIndex()).get(4).equals("Pending") ? receiveBtn : null);
            }
        });
        table.getColumns().add(actionCol);
        pane.getChildren().add(table);
        pane.getChildren().add(new Separator());
        VBox createCard = card("#FFFFFF");
        Label createTitle = new Label("Create a new purchase order");
        createTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        createTitle.setTextFill(Color.web(MID));
        TextField supField = styledField("Supplier ID ");
        TextField dateField = styledField("Order date (yyyy-mm-dd)");
        TextField delField = styledField("Expected delivery date (yyyy-mm-dd)");
        TextField amtField = styledField("Estimated total amount (₪)");
        HBox row1 = new HBox(10, supField, dateField);
        HBox row2 = new HBox(10, delField, amtField);
        HBox.setHgrow(supField, Priority.ALWAYS);
        HBox.setHgrow(dateField, Priority.ALWAYS);
        HBox.setHgrow(delField, Priority.ALWAYS);
        HBox.setHgrow(amtField, Priority.ALWAYS);
        Button btnCreate = actionBtn("＋ Create Purchase Order", GREEN);
        createCard.getChildren().addAll(createTitle, row1, row2, btnCreate);
        pane.getChildren().add(createCard);
        refreshBtn.setOnAction(e -> refreshPurchaseOrders(table));
        btnCreate.setOnAction(e -> {
            try {
                PurchaseOrder po = new PurchaseOrder(
                        Integer.parseInt(supField.getText().trim()),
                        employeeID,
                        dateField.getText().trim(),
                        delField.getText().trim(),
                        "Pending",
                        Double.parseDouble(amtField.getText().trim())
                );
                boolean ok = PurchaseOrderDAO.addPurchaseOrder(po);
                if (ok) {
                    showStatus(" Purchase Order created! Now add its items in Admin → Purchase tab, then receive here when stock arrives.", true);
                    supField.clear();
                    dateField.clear();
                    delField.clear();
                    amtField.clear();
                    refreshPurchaseOrders(table);
                } else {
                    showStatus(" Could not create PO. Check Supplier ID.", false);
                }
            } catch (Exception ex) {
                showStatus(" Invalid values.", false);
            }
        });
        refreshPurchaseOrders(table);
        return pane;
    }

    private void refreshPurchaseOrders(TableView<ObservableList<String>> table) {
        ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
        try {
            Connection conn = DBConnection.connect();
            StringBuilder sql = new StringBuilder(
                    "SELECT po.PONumber, s.SupplierName, po.OrderDate, po.ExpDeliveryDate, po.Status, po.TotalAmount " +
                            "FROM PurchaseOrder po JOIN Supplier s ON po.SupplierID=s.SupplierID " +
                            "JOIN PurchaseOrderItem poi ON po.PONumber=poi.PONumber " +
                            "JOIN Batch b ON poi.ProductID=b.ProductID WHERE 1=1");
            List<Object> params = new ArrayList<>();
            if (employeeWarehouseID != null) {
                sql.append(" AND b.WarehouseID = ?");
                params.add(employeeWarehouseID);
            }
            sql.append(" GROUP BY po.PONumber ORDER BY po.OrderDate DESC");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(FXCollections.observableArrayList(
                        String.valueOf(rs.getInt("PONumber")),
                        rs.getString("SupplierName"),
                        rs.getString("OrderDate"),
                        rs.getString("ExpDeliveryDate") != null ? rs.getString("ExpDeliveryDate") : "—",
                        rs.getString("Status"),
                        String.format("%.2f", rs.getDouble("TotalAmount"))
                ));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        table.setItems(rows);
    }

    private void showReceivePODialog(int poNumber, Runnable onDone) {
        List<int[]> itemMeta = new ArrayList<>();
        List<TextField> batchFields = new ArrayList<>();
        List<TextField> qtyFields = new ArrayList<>();
        VBox content = new VBox(14);
        content.setPadding(new Insets(16));
        content.setPrefWidth(460);
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT poi.ProductID, p.ProductName, poi.QtyOrdered, poi.QtyReceived " +
                            "FROM PurchaseOrderItem poi JOIN Product p ON poi.ProductID=p.ProductID WHERE poi.PONumber=?");
            ps.setInt(1, poNumber);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int prodId = rs.getInt("ProductID");
                int ordered = rs.getInt("QtyOrdered");
                int received = rs.getInt("QtyReceived");
                int remaining = Math.max(ordered - received, 0);
                Label itemLbl = new Label("📦 " + rs.getString("ProductName") +
                        "  (ordered: " + ordered + "  |  already received: " + received +
                        "  |  still outstanding: " + remaining + ")");
                itemLbl.setStyle("-fx-font-weight:bold;" +
                        " -fx-text-fill:" + DARK + ";");
                TextField batchField = new TextField();
                batchField.setPromptText("Batch ID");
                batchField.setPrefWidth(100);
                batchField.setStyle("-fx-background-radius:6;" +
                        " -fx-border-radius:6; " +
                        "-fx-border-color:#CBD5E1;" +
                        " -fx-padding:6 10;");
                TextField qtyField = new TextField(remaining > 0 ? String.valueOf(remaining) : "");
                qtyField.setPromptText("Qty now");
                qtyField.setPrefWidth(90);
                qtyField.setStyle("-fx-background-radius:6;" +
                        " -fx-border-radius:6; " +
                        "-fx-border-color:#CBD5E1; " +
                        "-fx-padding:6 10;");
                HBox row = new HBox(10, new Label("Batch ID:"), batchField, new Label("Qty received:"), qtyField);
                row.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(itemLbl, row, new Separator());
                itemMeta.add(new int[]{prodId, ordered, received});
                batchFields.add(batchField);
                qtyFields.add(qtyField);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (itemMeta.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "No items found on PO #" + poNumber + ".\nAdd line items first via Admin → Purchase Orders tab.",
                    ButtonType.OK).showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Receive Stock | PO #" + poNumber);
        dialog.setHeaderText("Log what arrived for Purchase Order #" + poNumber);
        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.setPrefHeight(380);
        dialog.getDialogPane().setContent(scroller);
        dialog.getDialogPane().setMinWidth(500);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) {
                return;
            }
            boolean any = false;
            for (int i = 0; i < itemMeta.size(); i++) {
                String bTxt = batchFields.get(i).getText().trim();
                String qTxt = qtyFields.get(i).getText().trim();
                if (bTxt.isEmpty() || qTxt.isEmpty()) {
                    continue;
                }
                try {
                    int batchId = Integer.parseInt(bTxt);
                    int qty = Integer.parseInt(qTxt);
                    int productId = itemMeta.get(i)[0];
                    if (qty <= 0) {
                        continue;
                    }
                    if (!batchBelongsToProduct(batchId, productId)) {
                        showStatus("x Batch #" + batchId + " doesn't match that product - skipped.", false);
                        continue;
                    }
                    InventoryTransaction t = new InventoryTransaction(batchId, employeeID, "Receipt", qty, poNumber);
                    if (InventoryTransactionDAO.addTransaction(t)) {
                        updateQtyReceived(poNumber, productId, qty);
                        any = true;
                    }
                } catch (Exception ex) {
                    showStatus("x Invalid value on one row - skipped.", false);
                }
            }
            if (markPOFullyReceivedIfDone(poNumber)) {
                showStatus(" PO #" + poNumber + " fully received - marked Delivered!", true);
            } else if (any) {
                showStatus(" Partial receipt logged for PO #" + poNumber + " - some items still outstanding.", true);
            }
            onDone.run();
        });
    }

    private boolean batchBelongsToProduct(int batchId, int productId) {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM Batch WHERE BatchID=? AND ProductID=?");
            ps.setInt(1, batchId);
            ps.setInt(2, productId);
            boolean found = ps.executeQuery().next();
            conn.close();
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateQtyReceived(int poNumber, int productId, int qty) {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE PurchaseOrderItem SET QtyReceived=QtyReceived+? WHERE PONumber=? AND ProductID=?");
            ps.setInt(1, qty);
            ps.setInt(2, poNumber);
            ps.setInt(3, productId);
            ps.executeUpdate();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean markPOFullyReceivedIfDone(int poNumber) {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT SUM(QtyOrdered) AS O, SUM(QtyReceived) AS R FROM PurchaseOrderItem WHERE PONumber=?");
            ps.setInt(1, poNumber);
            ResultSet rs = ps.executeQuery();
            boolean done = rs.next() && rs.getInt("O") > 0 && rs.getInt("R") >= rs.getInt("O");
            if (done) {
                PreparedStatement upd = conn.prepareStatement("UPDATE PurchaseOrder SET Status='Delivered' WHERE PONumber=?");
                upd.setInt(1, poNumber);
                upd.executeUpdate();
            }
            conn.close();
            return done;
        } catch (Exception e) {
            return false;
        }
    }

    private VBox buildSuppliersTab() {
        VBox pane = pane();
        sectionTitle(pane, " Suppliers");
        TextField searchField = styledField("Search by supplier name...");
        searchField.setPrefWidth(260);
        Button refreshBtn = actionBtn("🔄 Refresh", MID);
        HBox topBar = new HBox(10, searchField, refreshBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        pane.getChildren().add(topBar);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(420);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No suppliers found!"));
        String[] cols = {
                "ID", "Supplier Name", "Contact Person", "Phone", "Email", "City"
        }
                ;
        int[] widths = {
                50, 170, 130, 120, 170, 100
        }
                ;
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            table.getColumns().add(c);
        }
        pane.getChildren().add(table);
        Runnable load = () -> {
            String keyword = searchField.getText().trim();
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM Supplier WHERE SupplierName LIKE ? ORDER BY SupplierName");
                ps.setString(1, "%" + keyword + "%");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    rows.add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("SupplierID")),
                            rs.getString("SupplierName"),
                            rs.getString("ContactPerson"),
                            rs.getString("Phone"),
                            rs.getString("Email"),
                            rs.getString("City")
                    ));
                }
                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            table.setItems(rows);
        }
                ;
        refreshBtn.setOnAction(e -> load.run());
        searchField.setOnAction(e -> load.run());
        load.run();
        return pane;
    }

    private VBox buildReportsTab() {
        VBox pane = pane();
        sectionTitle(pane, " Reports & Statistics");
        TabPane reportTabs = new TabPane();
        reportTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        reportTabs.setStyle("-fx-tab-min-width:120px;");
        reportTabs.getTabs().addAll(
                buildReportTab("Sales & Profit", this::buildSalesProfitReport),
                buildReportTab(" Inventory", this::buildInventoryReport),
                buildReportTab(" Suppliers & POs", this::buildSupplierReport),
                buildReportTab(" Clients", this::buildClientsReport),
                buildReportTab(" Employee Activity", this::buildEmployeeReport)
        );
        pane.getChildren().add(reportTabs);
        return pane;
    }

    private Tab buildReportTab(String title, java.util.function.Supplier<VBox> builder) {
        Tab tab = new Tab(title);
        VBox content = builder.get();
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + GRAY + ";");
        tab.setContent(scroll);
        return tab;
    }

    private VBox buildSalesProfitReport() {
        VBox pane = pane();
        HBox kpiRow = new HBox(12);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        double[] totalRevenue = {0};
        double[] totalCost = {0};
        int[] totalOrders = {0};
        int[] pendingOrders = {0};

        try {
            Connection conn = DBConnection.connect();
            ResultSet r1 = conn.createStatement().executeQuery(
                    "SELECT COALESCE(SUM(Amount),0) AS Rev FROM Payment WHERE Direction='Incoming'");
            if (r1.next()) {
                totalRevenue[0] = r1.getDouble("Rev");
            }

            ResultSet r2 = conn.createStatement().executeQuery(
                    "SELECT COALESCE(SUM(poi.QtyReceived * poi.UnitCost),0) AS Cost " +
                            "FROM PurchaseOrderItem poi");
            if (r2.next()) {
                totalCost[0] = r2.getDouble("Cost");
            }

            ResultSet r3 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) AS Total, SUM(CASE WHEN Status='Pending' THEN 1 ELSE 0 END) AS Pending FROM SaleOrder");
            if (r3.next()) {
                totalOrders[0] = r3.getInt("Total");
                pendingOrders[0] = r3.getInt("Pending");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        double profit = totalRevenue[0] - totalCost[0];
        kpiRow.getChildren().addAll(
                kpiCard("💰 Total Revenue", "₪" + String.format("%.2f", totalRevenue[0]), GREEN),
                kpiCard("🛒 Total Cost", "₪" + String.format("%.2f", totalCost[0]), RED),
                kpiCard("📈 Gross Profit", "₪" + String.format("%.2f", profit), profit >= 0 ? GREEN : RED),
                kpiCard("📋 Total Orders", String.valueOf(totalOrders[0]), MID),
                kpiCard("⏳ Pending Orders", String.valueOf(pendingOrders[0]), ORANGE)
        );
        pane.getChildren().add(kpiRow);
        pane.getChildren().add(new Separator());
        Label tableTitle = new Label("Sale Orders");
        tableTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        tableTitle.setTextFill(Color.web(DARK));
        TextField searchClient = styledField("Search by client name...");
        searchClient.setPrefWidth(200);
        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All Statuses", "Pending", "Delivered", "Cancelled");
        statusFilter.setValue("All Statuses");
        statusFilter.setStyle("-fx-font-size:12px;");
        ComboBox<String> payFilter = new ComboBox<>();
        payFilter.getItems().addAll("All Payments", "Paid", "Unpaid", "Partial");
        payFilter.setValue("All Payments");
        payFilter.setStyle("-fx-font-size:12px;");
        TextField dateFrom = styledField("From (yyyy-mm-dd)");
        dateFrom.setPrefWidth(140);
        TextField dateTo = styledField("To (yyyy-mm-dd)");
        dateTo.setPrefWidth(140);
        Button searchBtn = filterBtn("🔍 Search", MID);
        Button clearBtn = filterBtn("Clear", "#64748B");
        HBox filterBar = new HBox(8, searchClient, statusFilter, payFilter, dateFrom, dateTo, searchBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        TableView<ObservableList<String>> soTable = new TableView<>();
        soTable.setPrefHeight(280);
        soTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        soTable.setPlaceholder(new Label("No orders found."));
        String[] soCols = {
                "SO #", "Client", "Handled By", "Order Date", "Delivery", "Status", "Payment", "Total (₪)"
        }
                ;
        int[] soWidths = {
                55, 140, 120, 90, 90, 80, 75, 85
        }
                ;
        for (int i = 0; i < soCols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(soCols[i]);
            c.setPrefWidth(soWidths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            if (soCols[i].equals("Status") || soCols[i].equals("Payment")) {
                c.setCellFactory(cc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null){
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);
                        String col = (item.equals("Delivered") || item.equals("Paid")) ? GREEN :
                                item.equals("Partial") ? ORANGE : RED;
                        setStyle("-fx-text-fill:"+col+";" +
                                " -fx-font-weight:bold;");
                    }
                });
            }
            soTable.getColumns().add(c);
        }

        Label soSummary = new Label("");
        soSummary.setStyle("-fx-font-size:12px;" +
                " -fx-text-fill:#64748B;");
        Runnable loadSO = () -> {
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                StringBuilder sql = new StringBuilder(
                        "SELECT so.SaleOrderID, cl.ClientName, CONCAT(e.FirstName,' ',e.LastName) AS Emp, " +
                                "so.OrderDate, so.DeliveryDate, so.Status, so.PaymentStatus, so.TotalAmount " +
                                "FROM SaleOrder so JOIN Client cl ON so.ClientID=cl.ClientID " +
                                "JOIN Employee e ON so.EmployeeID=e.EmployeeID WHERE 1=1");
                List<String> params = new ArrayList<>();
                String kw= searchClient.getText().trim();
                String st = statusFilter.getValue();
                String py = payFilter.getValue();
                String df = dateFrom.getText().trim();
                String dt = dateTo.getText().trim();
                if (!kw.isEmpty()) {
                    sql.append(" AND cl.ClientName LIKE ?");
                    params.add("%"+kw+"%");
                }
                if (!st.equals("All Statuses")) {
                    sql.append(" AND so.Status=?");
                    params.add(st);
                }
                if (!py.equals("All Payments")) {
                    sql.append(" AND so.PaymentStatus=?");
                    params.add(py);
                }
                if (!df.isEmpty()) {
                    sql.append(" AND so.OrderDate >= ?");
                    params.add(df);
                }
                if (!dt.isEmpty()) {
                    sql.append(" AND so.OrderDate <= ?");
                    params.add(dt);
                }
                sql.append(" ORDER BY so.OrderDate DESC");
                PreparedStatement ps = conn.prepareStatement(sql.toString());
                for (int i = 0;i<params.size();i++) {
                    ps.setString(i+1, params.get(i));
                }
                ResultSet rs = ps.executeQuery();
                double sumTotal = 0;
                int cnt = 0;
                while (rs.next()) {
                    cnt++;
                    double amt = rs.getDouble("TotalAmount");
                    sumTotal += amt;
                    rows.add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("SaleOrderID")),
                            rs.getString("ClientName"), rs.getString("Emp"),
                            rs.getString("OrderDate"),
                            rs.getString("DeliveryDate") != null?rs.getString("DeliveryDate"):"-",
                            rs.getString("Status"), rs.getString("PaymentStatus"),
                            String.format("%.2f", amt)));
                }
                conn.close();
                soSummary.setText("Showing " + cnt + " order(s)  |  Total: ₪" + String.format("%.2f", sumTotal));
            } catch (Exception e) {
                e.printStackTrace();
            }
            soTable.setItems(rows);
        }
                ;
        searchBtn.setOnAction(e -> loadSO.run());
        clearBtn.setOnAction(e -> {
            searchClient.clear();
            statusFilter.setValue("All Statuses");
            payFilter.setValue("All Payments");
            dateFrom.clear();
            dateTo.clear();
            loadSO.run();
        });
        searchClient.setOnAction(e -> loadSO.run());
        loadSO.run();
        pane.getChildren().addAll(tableTitle, filterBar, soTable, soSummary);
        return pane;
    }

    private VBox buildInventoryReport() {
        VBox pane = pane();
        TextField searchField = styledField("Search by product name...");
        searchField.setPrefWidth(220);
        ComboBox<String> warehouseBox = new ComboBox<>();
        warehouseBox.setPromptText("All Warehouses");
        loadWarehouseFilter(warehouseBox);
        warehouseBox.setStyle("-fx-font-size:12px;");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("All", "Low Stock", "Expiring", "OK");
        statusBox.setValue("All");
        statusBox.setStyle("-fx-font-size:12px;");
        Button searchBtn = filterBtn("🔍 Search", MID);
        Button clearBtn = filterBtn("Clear", "#64748B");
        HBox filterBar = new HBox(8, searchField, warehouseBox, statusBox, searchBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No batches found."));
        String[] cols = {
                "Batch #", "Product", "Category", "Warehouse", "Qty", "Reorder", "Status", "Expiry", "Location"
        };
        int[] widths = {
                65, 150, 100, 130, 60, 70, 80, 95, 90
        };
        for (int i = 0; i<cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(idx)));
            if (cols[i].equals("Status")) {
                c.setCellFactory(cc->new TableCell<>(){
                    @Override protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);
                        if (empty || item == null){
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);
                        setStyle(item.equals("OK")?"-fx-text-fill:"+GREEN+";-fx-font-weight:bold;":
                                item.equals("Low Stock")?"-fx-text-fill:"+ORANGE+";-fx-font-weight:bold;":
                                "-fx-text-fill:"+RED+";-fx-font-weight:bold;");
                    }
                });
            }
            table.getColumns().add(c);
        }
        table.setRowFactory(tv->new TableRow<>(){
            @Override protected void updateItem(ObservableList<String> row, boolean empty){
                super.updateItem(row, empty);
                if (row == null || empty){
                    setStyle("");
                    return;
                }
                String s = row.get(6);
                setStyle(s.equals("Expiring")?"-fx-background-color:#FEE2E2;":
                        s.equals("Low Stock")?"-fx-background-color:#FEF9C3;":"");
            }
        });
        Label summary = new Label("");
        summary.setStyle("-fx-font-size:12px; -fx-text-fill:#64748B;");
        Runnable load = () -> {
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                StringBuilder sql = new StringBuilder(
                        "SELECT b.BatchID, p.ProductName, c.Name AS Cat, w.WarehouseName, " +
                                "b.QtyInStock, p.ReorderLevel, b.ExpiryDate, b.StorageLocation " +
                                "FROM Batch b JOIN Product p ON b.ProductID=p.ProductID " +
                                "JOIN Category c ON p.CategoryID=c.CategoryID " +
                                "JOIN Warehouse w ON b.WarehouseID=w.WarehouseID WHERE 1=1");
                List<String> params = new ArrayList<>();
                String kw = searchField.getText().trim();
                String wh = warehouseBox.getValue();
                String st = statusBox.getValue();
                if (!kw.isEmpty()) {
                    sql.append(" AND p.ProductName LIKE ?");
                    params.add("%"+kw+"%");
                }
                if (wh != null && !wh.equals("All Warehouses")) {
                    sql.append(" AND w.WarehouseName=?");
                    params.add(wh);
                }
                if (employeeWarehouseID != null
                        && (wh == null || wh.equals("All Warehouses"))) {
                    sql.append(" AND b.WarehouseID = ?");
                    params.add(String.valueOf(employeeWarehouseID));
                }
                sql.append(" ORDER BY p.ProductName");
                PreparedStatement ps = conn.prepareStatement(sql.toString());
                for (int i = 0;i<params.size();i++) {
                    ps.setString(i+1, params.get(i));
                }
                ResultSet rs = ps.executeQuery();
                int cnt = 0;
                int lowCnt = 0;
                while (rs.next()) {
                    int qty = rs.getInt("QtyInStock");
                    int reorder = rs.getInt("ReorderLevel");
                    String exp = rs.getString("ExpiryDate");
                    boolean expiring = exp != null && LocalDate.parse(exp).isBefore(LocalDate.now().plusDays(60));
                    String status = expiring?"Expiring":(qty<reorder?"Low Stock":"OK");
                    if (!st.equals("All") && !status.equals(st)) {
                        continue;
                    }
                    cnt++;
                    if (!status.equals("OK")) {
                        lowCnt++;
                    }
                    rows.add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("BatchID")),
                            rs.getString("ProductName"), rs.getString("Cat"), rs.getString("WarehouseName"),
                            String.valueOf(qty), String.valueOf(reorder), status,
                            exp != null?exp:"—",
                            rs.getString("StorageLocation") != null?rs.getString("StorageLocation"):"—"));
                }
                conn.close();
                summary.setText("Showing "+cnt+" batch(es)  |  "+lowCnt+" need attention");
            } catch (Exception e) {
                e.printStackTrace();
            }
            table.setItems(rows);
        };
        searchBtn.setOnAction(e->load.run());
        clearBtn.setOnAction(e->{
            searchField.clear();
            warehouseBox.setValue("All Warehouses");
            statusBox.setValue("All");
            load.run();
        });
        searchField.setOnAction(e->load.run());
        load.run();
        pane.getChildren().addAll(filterBar, table, summary);
        return pane;
    }

    private VBox buildSupplierReport() {
        VBox pane = pane();
        TextField searchField = styledField("Search by supplier name...");
        searchField.setPrefWidth(200);
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("All Statuses", "Pending", "Delivered", "Cancelled");
        statusBox.setValue("All Statuses");
        statusBox.setStyle("-fx-font-size:12px;");
        TextField dateFrom = styledField("From (yyyy-mm-dd)");
        dateFrom.setPrefWidth(140);
        TextField dateTo = styledField("To (yyyy-mm-dd)");
        dateTo.setPrefWidth(140);
        Button searchBtn = filterBtn("🔍 Search", MID);
        Button clearBtn = filterBtn("Clear", "#64748B");
        HBox filterBar = new HBox(8, searchField, statusBox, dateFrom, dateTo, searchBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No purchase orders found."));
        String[] cols = {
                "PO #", "Supplier", "Handled By", "Order Date", "Exp. Delivery", "Status", "Total (₪)"
        }
                ;
        int[] widths = {
                60, 160, 120, 90, 100, 90, 90
        }
                ;
        for (int i = 0;i<cols.length;i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(idx)));
            if (cols[i].equals("Status")) {
                c.setCellFactory(cc->new TableCell<>(){
                    @Override protected void updateItem(String item, boolean empty){
                        super.updateItem(item, empty);
                        if (empty || item == null){
                            setText(null);
                            setStyle("");
                            return;
                        }
                        setText(item);
                        setStyle(item.equals("Delivered")?"-fx-text-fill:"+GREEN+";-fx-font-weight:bold;":
                                item.equals("Pending")?"-fx-text-fill:"+ORANGE+";-fx-font-weight:bold;":
                                "-fx-text-fill:"+RED+";-fx-font-weight:bold;");
                    }
                });
            }
            table.getColumns().add(c);
        }

        Label summary = new Label("");
        summary.setStyle("-fx-font-size:12px; -fx-text-fill:#64748B;");
        Runnable load = () -> {
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                StringBuilder sql = new StringBuilder(
                        "SELECT po.PONumber, s.SupplierName, CONCAT(e.FirstName,' ',e.LastName) AS Emp, " +
                                "po.OrderDate, po.ExpDeliveryDate, po.Status, po.TotalAmount " +
                                "FROM PurchaseOrder po JOIN Supplier s ON po.SupplierID=s.SupplierID " +
                                "JOIN Employee e ON po.EmployeeID=e.EmployeeID WHERE 1=1");
                List<String> params = new ArrayList<>();
                String kw = searchField.getText().trim();
                String st = statusBox.getValue();
                String df = dateFrom.getText().trim();
                String dt = dateTo.getText().trim();
                if (!kw.isEmpty()){
                    sql.append(" AND s.SupplierName LIKE ?");
                    params.add("%"+kw+"%");
                }
                if (!st.equals("All Statuses")){
                    sql.append(" AND po.Status=?");
                    params.add(st);
                }
                if (!df.isEmpty()){
                    sql.append(" AND po.OrderDate >= ?");
                    params.add(df);
                }
                if (!dt.isEmpty()){
                    sql.append(" AND po.OrderDate <= ?");
                    params.add(dt);
                }
                sql.append(" ORDER BY po.OrderDate DESC");
                PreparedStatement ps = conn.prepareStatement(sql.toString());
                for (int i = 0;i<params.size();i++) {
                    ps.setString(i+1, params.get(i));
                }
                ResultSet rs = ps.executeQuery();
                int cnt = 0;
                double total = 0;
                while (rs.next()){
                    cnt++;
                    double amt = rs.getDouble("TotalAmount");
                    total+=amt;
                    rows.add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("PONumber")),
                            rs.getString("SupplierName"), rs.getString("Emp"),
                            rs.getString("OrderDate"),
                            rs.getString("ExpDeliveryDate") != null?rs.getString("ExpDeliveryDate"):"-",
                            rs.getString("Status"),
                            String.format("%.2f", amt)));
                }
                conn.close();
                summary.setText("Showing "+cnt+" PO(s)  |  Total: ₪"+String.format("%.2f", total));
            } catch (Exception e){
                e.printStackTrace();
            }
            table.setItems(rows);
        }
                ;
        searchBtn.setOnAction(e->load.run());
        clearBtn.setOnAction(e->{
            searchField.clear();
            statusBox.setValue("All Statuses");
            dateFrom.clear();
            dateTo.clear();
            load.run();
        });
        load.run();
        pane.getChildren().addAll(filterBar, table, summary);
        return pane;
    }

    private VBox buildClientsReport() {
        VBox pane = pane();
        TextField searchField = styledField("Search by client name or city...");
        searchField.setPrefWidth(260);
        Button searchBtn = filterBtn("🔍 Search", MID);
        Button clearBtn = filterBtn("Clear", "#64748B");
        HBox filterBar = new HBox(8, searchField, searchBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(350);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No clients found."));
        String[] cols = {
                "ID", "Client Name", "Type", "City", "Credit Limit (₪)", "Total Orders", "Total Paid (₪)"
        }
                ;
        int[] widths = {
                50, 180, 80, 100, 110, 90, 110
        }
                ;
        for (int i = 0;i<cols.length;i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(idx)));
            table.getColumns().add(c);
        }

        Label summary = new Label("");
        summary.setStyle("-fx-font-size:12px; -fx-text-fill:#64748B;");
        Runnable load = () -> {
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                String kw = searchField.getText().trim();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT cl.ClientID, cl.ClientName, cl.ClientType, cl.City, cl.CreditLimit, " +
                                "COUNT(DISTINCT so.SaleOrderID) AS TotalOrders, " +
                                "COALESCE(SUM(p.Amount),0) AS TotalPaid " +
                                "FROM Client cl " +
                                "LEFT JOIN SaleOrder so ON cl.ClientID=so.ClientID " +
                                "LEFT JOIN Payment p ON so.SaleOrderID=p.SaleOrderID " +
                                "WHERE (cl.ClientName LIKE ? OR cl.City LIKE ?) " +
                                "GROUP BY cl.ClientID ORDER BY TotalOrders DESC");
                ps.setString(1, "%"+kw+"%");
                ps.setString(2, "%"+kw+"%");
                ResultSet rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()){
                    cnt++;
                    rows.add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("ClientID")),
                            rs.getString("ClientName"), rs.getString("ClientType"),
                            rs.getString("City"),
                            String.format("%.2f", rs.getDouble("CreditLimit")),
                            String.valueOf(rs.getInt("TotalOrders")),
                            String.format("%.2f", rs.getDouble("TotalPaid"))));
                }
                conn.close();
                summary.setText("Showing "+cnt+" client(s)");
            } catch (Exception e){
                e.printStackTrace();
            }
            table.setItems(rows);
        }
                ;
        searchBtn.setOnAction(e->load.run());
        clearBtn.setOnAction(e->{
            searchField.clear();
            load.run();
        });
        searchField.setOnAction(e->load.run());
        load.run();
        pane.getChildren().addAll(filterBar, table, summary);
        return pane;
    }

    private VBox buildEmployeeReport() {
        VBox pane = pane();
        TextField searchField = styledField("Search by employee name...");
        searchField.setPrefWidth(220);
        TextField dateFrom = styledField("From (yyyy-mm-dd)");
        dateFrom.setPrefWidth(140);
        TextField dateTo = styledField("To (yyyy-mm-dd)");
        dateTo.setPrefWidth(140);
        Button searchBtn = filterBtn("🔍 Search", MID);
        Button clearBtn = filterBtn("Clear", "#64748B");
        HBox filterBar = new HBox(8, searchField, dateFrom, dateTo, searchBtn, clearBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(350);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No data found."));
        String[] cols = {
                "Employee", "Role", "Sale Orders", "Revenue Generated (₪)", "Purchase rders", "Total Purchased (₪)"
        }
                ;
        int[] widths = {
                140, 130, 90, 140, 110, 140
        }
                ;
        for (int i = 0;i<cols.length;i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(idx)));
            table.getColumns().add(c);
        }

        Label summary = new Label("");
        summary.setStyle("-fx-font-size:12px;" +
                " -fx-text-fill:#64748B;");
        Runnable load = () -> {
            ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
            try {
                Connection conn = DBConnection.connect();
                String kw = searchField.getText().trim();
                String df = dateFrom.getText().trim();
                String dt = dateTo.getText().trim();
                StringBuilder sql = new StringBuilder(
                        "SELECT e.EmployeeID, CONCAT(e.FirstName,' ',e.LastName) AS EmpName, e.Role, " +
                                "COUNT(DISTINCT so.SaleOrderID) AS SaleOrders, " +
                                "COALESCE(SUM(DISTINCT pay.Amount),0) AS Revenue, " +
                                "COUNT(DISTINCT po.PONumber) AS PurchaseOrders, " +
                                "COALESCE(SUM(DISTINCT po.TotalAmount),0) AS Purchased " +
                                "FROM Employee e " +
                                "LEFT JOIN SaleOrder so ON e.EmployeeID=so.EmployeeID");
                if (!df.isEmpty() || !dt.isEmpty()) {
                    sql.append(" AND so.OrderDate BETWEEN '"+(df.isEmpty()?"2000-01-01":df)+"' AND '"+(dt.isEmpty()?"2099-12-31":dt)+"'");
                }
                sql.append(" LEFT JOIN Payment pay ON so.SaleOrderID=pay.SaleOrderID AND pay.Direction='Incoming'");
                sql.append(" LEFT JOIN PurchaseOrder po ON e.EmployeeID=po.EmployeeID");
                sql.append(" WHERE (e.FirstName LIKE ? OR e.LastName LIKE ?)");
                sql.append(" GROUP BY e.EmployeeID ORDER BY SaleOrders DESC");
                PreparedStatement ps = conn.prepareStatement(sql.toString());
                ps.setString(1, "%"+kw+"%");
                ps.setString(2, "%"+kw+"%");
                ResultSet rs = ps.executeQuery();
                int cnt = 0;
                while (rs.next()){
                    cnt++;
                    rows.add(FXCollections.observableArrayList(
                            rs.getString("EmpName"), rs.getString("Role"),
                            String.valueOf(rs.getInt("SaleOrders")),
                            String.format("%.2f", rs.getDouble("Revenue")),
                            String.valueOf(rs.getInt("PurchaseOrders")),
                            String.format("%.2f", rs.getDouble("Purchased"))));
                }
                conn.close();
                summary.setText("Showing "+cnt+" employee(s)");
            } catch (Exception e){
                e.printStackTrace();
            }
            table.setItems(rows);
        }
                ;
        searchBtn.setOnAction(e->load.run());
        clearBtn.setOnAction(e->{
            searchField.clear();
            dateFrom.clear();
            dateTo.clear();
            load.run();
        });
        load.run();
        pane.getChildren().addAll(filterBar, table, summary);
        return pane;
    }

    private VBox kpiCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14));
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color:white;" +
                " -fx-background-radius:10; " +
                "-fx-border-color:#E2E8F0;" +
                " -fx-border-radius:10;");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size:11px;" +
                " -fx-text-fill:#64748B;");
        Label val = new Label(value);
        val.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        val.setTextFill(Color.web(color));
        card.getChildren().addAll(lbl, val);
        return card;
    }

    private void showStatus(String msg, boolean success) {
        statusBar.setText(msg);
        statusBar.setStyle(
                success
                        ? "-fx-background-color:#F0FDF4;" +
                          " -fx-text-fill:#16A34A;" +
                          " -fx-border-color:#BBF7D0;" +
                          " -fx-border-width:1 0 0 0;" +
                          " -fx-padding:8 16;"
                        : "-fx-background-color:#FEF2F2;" +
                          " -fx-text-fill:#DC2626;" +
                          " -fx-border-color:#FECACA;" +
                          " -fx-border-width:1 0 0 0;" +
                          " -fx-padding:8 16;"
        );
    }

    private String getEmployeeRole() {
        if (employeeID <= 0) {
            return "Employee";
        }
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT Role FROM Employee WHERE EmployeeID=?");
            ps.setInt(1, employeeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String r = rs.getString("Role");
                conn.close();
                return r;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Employee";
    }
    private Integer getEmployeeWarehouseID() {
        if (employeeID <= 0) return null;
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT WarehouseID FROM Employee WHERE EmployeeID=?");
            ps.setInt(1, employeeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int wid = rs.getInt("WarehouseID");
                Integer result = rs.wasNull() ? null : wid;
                conn.close();
                return result;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getEmployeeName() {
        if (employeeID <= 0) {
            return "Employee";
        }
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT FirstName, LastName " +
                    "FROM Employee " +
                    "WHERE EmployeeID=?");
            ps.setInt(1, employeeID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String n = rs.getString("FirstName") + " " + rs.getString("LastName");
                conn.close();
                return n;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Employee";
    }

    private String getRoleDescription() {
        if (employeeRole.contains("Manager")) {
            return " Warehouse Manager : Full access: inventory, sales, purchases, reports";
        }
        if (employeeRole.contains("Sales")) {
            return " Sales Officer : Handles client orders, delivery confirmation, and payment recording";
        }
        if (employeeRole.contains("Procurement")) {
            return "🛒 Procurement Officer :Creates purchase orders and receives incoming stock from suppliers";
        }
        return " Employee - Inventory view";
    }

    private Tab buildTab(String title, VBox content) {
        Tab tab = new Tab(title);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + GRAY + ";");
        tab.setContent(scroll);
        return tab;
    }

    private VBox pane() {
        VBox p = new VBox(16);
        p.setPadding(new Insets(24));
        p.setStyle("-fx-background-color:" + GRAY + ";");
        return p;
    }

    private VBox card(String bgColor) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(14));
        c.setStyle("-fx-background-color:" + bgColor + ";" +
                " -fx-background-radius:10; " +
                "-fx-border-color:#E2E8F0;" +
                " -fx-border-radius:10;");
        return c;
    }

    private void sectionTitle(VBox pane, String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lbl.setTextFill(Color.web(DARK));
        lbl.setStyle("-fx-padding:0 0 8 0;" +
                " -fx-border-color:" + MID + ";" +
                " -fx-border-width:0 0 2 0;");
        pane.getChildren().add(lbl);
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-radius:6;" +
                " -fx-border-radius:6;" +
                " -fx-border-color:#CBD5E1;" +
                " -fx-padding:8 12;" +
                " -fx-font-size:13px;" +
                " -fx-background-color:white;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private Button actionBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:" + color + ";" +
                " -fx-text-fill:white;" +
                " -fx-background-radius:6; " +
                "-fx-padding:8 18; " +
                "-fx-cursor:hand;" +
                " -fx-font-weight:bold; " +
                "-fx-font-size:13px;");
        return btn;
    }

    private Button filterBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:" + color + "; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:6 14; -fx-cursor:hand; -fx-font-size:12px;");
        return btn;
    }

    private Button reportBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:white;" +
                " -fx-text-fill:" + DARK + ";" +
                " -fx-border-color:" + MID + ";" +
                " -fx-border-radius:8;" +
                " -fx-background-radius:8;" +
                " -fx-padding:12 20;" +
                " -fx-cursor:hand;" +
                " -fx-font-weight:bold;");
        return btn;
    }

    private HBox legendChip(String color, String label) {
        Label sq = new Label("   ");
        sq.setStyle("-fx-background-color:" + color + ";" +
                " -fx-border-color:#ccc;" +
                " -fx-background-radius:3;");
        Label tx = new Label(label);
        tx.setStyle("-fx-font-size:11px;" +
                " -fx-text-fill:#666;");
        HBox b = new HBox(6, sq, tx);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
