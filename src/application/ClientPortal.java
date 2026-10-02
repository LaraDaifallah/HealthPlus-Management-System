package application;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientPortal extends Application {

    private final UserAccount account;
    private final int clientID;

    public static class CartItem {
        private final IntegerProperty batchID = new SimpleIntegerProperty();
        private final StringProperty productName = new SimpleStringProperty();
        private final IntegerProperty qty = new SimpleIntegerProperty();
        private final DoubleProperty unitPrice = new SimpleDoubleProperty();
        private final DoubleProperty subtotal = new SimpleDoubleProperty();

        public CartItem(int batchID, String productName, int qty, double unitPrice) {
            this.batchID.set(batchID);
            this.productName.set(productName);
            this.qty.set(qty);
            this.unitPrice.set(unitPrice);
            this.subtotal.set(qty * unitPrice);
        }

        public int getBatchID() { return batchID.get(); }
        public String getProductName() { return productName.get(); }
        public int getQty() { return qty.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getSubtotal() { return subtotal.get(); }

    }

    public static class CatalogItem {
        private final IntegerProperty batchID = new SimpleIntegerProperty();
        private final StringProperty productName = new SimpleStringProperty();
        private final StringProperty category = new SimpleStringProperty();
        private final DoubleProperty price = new SimpleDoubleProperty();
        private final IntegerProperty inStock = new SimpleIntegerProperty();
        private final StringProperty warehouse = new SimpleStringProperty();
        private final StringProperty expiry = new SimpleStringProperty();

        public CatalogItem(int batchID, String productName, String category,
                           double price, int inStock, String warehouse, String expiry) {
            this.batchID.set(batchID);
            this.productName.set(productName);
            this.category.set(category);
            this.price.set(price);
            this.inStock.set(inStock);
            this.warehouse.set(warehouse);
            this.expiry.set(expiry);
        }

        public int getBatchID() { return batchID.get(); }
        public String getProductName() { return productName.get(); }
        public String getCategory() { return category.get(); }
        public double getPrice() { return price.get(); }
        public int getInStock() { return inStock.get(); }
    }

    private static final String DARK = "#1B3A6B";
    private static final String MID = "#2E6DA4";
    private static final String LIGHT = "#E8F2FF";
    private static final String GREEN = "#16A34A";
    private static final String RED = "#DC2626";
    private static final String GRAY = "#F5F7FA";

    private final ObservableList<CartItem> cart = FXCollections.observableArrayList();
    private Label cartTotalLabel;
    private Label cartCountLabel;
    private FlowPane shopGrid;
    private ObservableList<CatalogItem> lastLoadedCatalog = FXCollections.observableArrayList();

    public ClientPortal(UserAccount account) {
        this.account = account;
        this.clientID = account.getClientID();
    }

    public ClientPortal() {
        this.account = null;
        this.clientID = 0;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Health Plus --> Client Portal");

        HBox header = buildHeader(stage);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab shopTab     = new Tab("🛍  Shop");
        Tab cartTab     = new Tab(" cart");
        Tab ordersTab   = new Tab("  my orders");
        Tab paymentsTab = new Tab("$  My Payments");

        shopTab.setContent(buildShopTabCards());
        cartTab.setContent(buildCartTab());
        ordersTab.setContent(buildOrdersTab());
        paymentsTab.setContent(buildPaymentsTab());

        tabs.getTabs().addAll(shopTab, cartTab, ordersTab, paymentsTab);

        cart.addListener((ListChangeListener<CartItem>) c -> {
            int n = cart.size();
            cartTab.setText(n > 0 ? "🛒  Cart (" + n + ")" : " Cart");
            if (cartCountLabel != null)
                cartCountLabel.setText("🛒  " + n + " item(s) in cart");
        });

        VBox root = new VBox(0, header, tabs);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        Scene scene = new Scene(root, 1050, 720);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    private HBox buildHeader(Stage stage) {
        HBox h = new HBox(12);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(14, 24, 14, 24));
        h.setStyle("-fx-background-color: " + DARK + ";");

        Label logo = new Label("🏥");
        logo.setFont(Font.font(24));

        VBox tb = new VBox(2);
        Label t = new Label("Health Plus");
        t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        t.setTextFill(Color.WHITE);
        Label s = new Label("Welcome, " + getClientName());
        s.setFont(Font.font("Arial", 12));
        s.setTextFill(Color.web("#A8C4E0"));
        tb.getChildren().addAll(t, s);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        cartCountLabel = new Label("🛒  0 item(s) in cart");
        cartCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        cartCountLabel.setTextFill(Color.web("#A8C4E0"));

        Button logout = new Button("Logout");
        logout.setStyle("-fx-background-color:transparent; -fx-text-fill:#A8C4E0; -fx-border-color:#A8C4E0;" +
                " -fx-border-radius:6; -fx-background-radius:6; -fx-cursor:hand; -fx-padding:6 14;");
        logout.setOnAction(e -> {
            stage.close();
            try {
                new LoginScreen().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        h.getChildren().addAll(logo, tb, sp, cartCountLabel, logout);
        return h;
    }

    private ScrollPane buildShopTabCards() {
        VBox outer = pane();

        Label title = new Label("🛍 Browse Products");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(DARK));

        TextField searchField = new TextField();
        searchField.setPromptText("🔍  Search by product name...");
        searchField.setPrefWidth(280);
        searchField.setStyle("-fx-background-radius:20; -fx-border-radius:20; " +
                "-fx-border-color:#CBD5E1; -fx-padding:8 14; -fx-font-size:13px;");

        ComboBox<String> catBox = new ComboBox<>();
        catBox.setPromptText("All Categories");
        catBox.setStyle("-fx-font-size:13px;");
        loadCategories(catBox);

        Button searchBtn = actionBtn("Search", MID);
        Button clearBtn  = actionBtn("Clear", "#64748B");

        HBox searchBar = new HBox(10, searchField, catBox, searchBtn, clearBtn);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        outer.getChildren().addAll(title, searchBar, new Separator());

        shopGrid = new FlowPane();
        shopGrid.setHgap(16);
        shopGrid.setVgap(16);
        shopGrid.setPadding(new Insets(8, 0, 8, 0));

        outer.getChildren().add(shopGrid);

        HBox legend = new HBox(16,
                legendItem("#FEF9C3","Low stock (< 20)"),
                legendItem("#FEE2E2", "Out of stock"));
        outer.getChildren().add(legend);

        populateShopGrid(loadCatalog("", ""));

        searchBtn.setOnAction(e -> {
            String kw  = searchField.getText().trim();
            String cat = (catBox.getValue() == null || catBox.getValue().equals("All Categories")) ? "" : catBox.getValue();
            populateShopGrid(loadCatalog(kw, cat));
        });
        searchField.setOnAction(e -> searchBtn.fire());

        catBox.setOnAction(e -> {
            String kw  = searchField.getText().trim();
            String cat = (catBox.getValue() == null || catBox.getValue().equals("All Categories")) ? "" : catBox.getValue();
            populateShopGrid(loadCatalog(kw, cat));
        });
        clearBtn.setOnAction(e -> {
            searchField.clear();
            catBox.setOnAction(null);
            catBox.setValue("All Categories");
            catBox.setOnAction(ev -> {
                String kw  = searchField.getText().trim();
                String cat = (catBox.getValue() == null || catBox.getValue().equals("All Categories")) ? "" : catBox.getValue();
                populateShopGrid(loadCatalog(kw, cat));
            });
            populateShopGrid(loadCatalog("", ""));
        });

        ScrollPane sp = new ScrollPane(outer);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + GRAY + ";");
        return sp;
    }

    private void populateShopGrid(ObservableList<CatalogItem> items) {
        lastLoadedCatalog = items;
        shopGrid.getChildren().clear();
        if (items.isEmpty()) {
            Label empty = new Label("No products found.");
            empty.setStyle("-fx-text-fill:#94A3B8; -fx-font-size:14px; -fx-padding:24;");
            shopGrid.getChildren().add(empty);
            return;
        }
        for (CatalogItem item : items) {
            shopGrid.getChildren().add(buildProductCard(item));
        }
    }

    private VBox buildProductCard(CatalogItem item) {
        VBox card = new VBox(8);
        card.setPrefWidth(220);
        card.setPadding(new Insets(14));
        card.setStyle(
                "-fx-background-color:white; -fx-background-radius:12;" +
                        "-fx-border-color:#E2E8F0; -fx-border-radius:12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(80);
        imgBox.setStyle("-fx-background-color:" + LIGHT + "; -fx-background-radius:8;");
        Label icon = new Label(getCategoryIcon(item.getCategory()));
        icon.setFont(Font.font(32));
        imgBox.getChildren().add(icon);

        Label catLbl = new Label(item.getCategory().toUpperCase());
        catLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        catLbl.setTextFill(Color.web(MID));

        Label nameLbl = new Label(item.getProductName());
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        nameLbl.setTextFill(Color.web(DARK));
        nameLbl.setWrapText(true);

        Label priceLbl = new Label("₪ " + String.format("%.2f", item.getPrice()));
        priceLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        priceLbl.setTextFill(Color.web(GREEN));

        Label stockLbl;
        if (item.getInStock() == 0) {
            stockLbl = new Label("Out of stock");
            stockLbl.setStyle("-fx-text-fill:" + RED + "; -fx-font-size:11px; -fx-font-weight:bold;");
        } else if (item.getInStock() < 20) {
            stockLbl = new Label("Only " + item.getInStock() + " left!");
            stockLbl.setStyle("-fx-text-fill:#D97706; -fx-font-size:11px; -fx-font-weight:bold;");
        } else {
            stockLbl = new Label("✓ In stock (" + item.getInStock() + " units)");
            stockLbl.setStyle("-fx-text-fill:" + GREEN + "; -fx-font-size:11px; -fx-font-weight:bold;");}

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_LEFT);

        boolean alreadyInCart = cart.stream()
                .anyMatch(ci -> ci.getProductName().equals(item.getProductName()));
        boolean outOfStock = item.getInStock() == 0;

        if (alreadyInCart) {
            Label inCartLbl = new Label("✓ In Cart");
            inCartLbl.setStyle(
                    "-fx-background-color:" + LIGHT + "; -fx-text-fill:" + DARK + ";" +
                            "-fx-font-weight:bold; -fx-font-size:12px;" +
                            "-fx-padding:6 14; -fx-background-radius:6;");
            controls.getChildren().add(inCartLbl);
        } else {
            Spinner<Integer> qtySpinner = new Spinner<>(1, Math.max(1, item.getInStock()), 1);
            qtySpinner.setPrefWidth(72);
            qtySpinner.setDisable(outOfStock);

            Button addBtn = new Button("＋ Add");
            addBtn.setStyle(
                    "-fx-background-color:" + (outOfStock ? "#CBD5E1" : GREEN) + ";" +
                            "-fx-text-fill:white; -fx-background-radius:6;" +
                            "-fx-cursor:" + (outOfStock ? "default" : "hand") + ";" +
                            "-fx-font-size:12px; -fx-font-weight:bold; -fx-padding:6 14;");
            addBtn.setDisable(outOfStock);
            addBtn.setOnAction(e -> {
                int qty = qtySpinner.getValue();
                cart.add(new CartItem(item.getBatchID(), item.getProductName(), qty, item.getPrice()));

                updateTotal();
                populateShopGrid(lastLoadedCatalog);});
            controls.getChildren().addAll(qtySpinner, addBtn);
        }

        card.getChildren().addAll(imgBox, catLbl, nameLbl, priceLbl, stockLbl, grow, controls);
        return card;
    }

    private String getCategoryIcon(String category) {
        if (category == null) {
            return "📦";
        }
        String cat = category.toLowerCase();
        if (cat.contains("medic") || cat.contains("pharma")) {
            return "💊";
        }
        if (cat.contains("skin") || cat.contains("cream")) {
            return "🧴";}
        if (cat.contains("child") || cat.contains("baby")) {
            return "🍼";
        }
        if (cat.contains("shampoo") || cat.contains("hair")) {
            return "🧴";
        }
        if (cat.contains("personal") || cat.contains("care")) {
            return "🧼";
        }
        return "📦";
    }

    private ScrollPane buildCartTab() {
        VBox pane = pane();

        Label title = new Label(" Your Cart");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(DARK));
        pane.getChildren().addAll(title, new Separator());

        TableView<CartItem> cartTable = new TableView<>(cart);
        cartTable.setPrefHeight(280);
        cartTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        cartTable.setPlaceholder(new Label("Your cart is empty -> go to Shop to add products!"));

        TableColumn<CartItem, String>  cn = col("Product","productName", 240);
        TableColumn<CartItem, Integer> cq = col("Qty","qty", 60);
        TableColumn<CartItem, Double>  cp = col("Unit Price (₪)","unitPrice",   110);
        TableColumn<CartItem, Double>  cs = col("Subtotal (₪)",  "subtotal",    110);

        TableColumn<CartItem, Void> cr = new TableColumn<>("Remove");
        cr.setPrefWidth(90);
        cr.setCellFactory(c -> new TableCell<>() {
            final Button btn = new Button("🗑 Remove");
            {
                btn.setStyle("-fx-background-color:" + RED + "; -fx-text-fill:white;" +
                        " -fx-background-radius:6;" +
                        " -fx-cursor:hand; -fx-font-size:11px; -fx-padding:5 10;");
                btn.setOnAction(e -> {
                    cart.remove(getTableView().getItems().get(getIndex()));
                    updateTotal();
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        cartTable.getColumns().addAll(cn, cq, cp, cs, cr);
        pane.getChildren().add(cartTable);

        cartTotalLabel = new Label("Total: ₪0.00");
        cartTotalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        cartTotalLabel.setTextFill(Color.web(DARK));
        HBox totalRow = new HBox(cartTotalLabel);
        totalRow.setAlignment(Pos.CENTER_RIGHT);
        pane.getChildren().addAll(totalRow, new Separator());

        pane.getChildren().add(new Separator());

        Label payTitle = new Label("💳 Payment -> Required to Place Order");
        payTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        payTitle.setTextFill(Color.web(DARK));

        Label payNote = new Label("You must pay upfront to confirm your order. Choose full or partial advance payment.");
        payNote.setStyle("-fx-text-fill:#64748B; -fx-font-size:12px; -fx-font-style:italic;");
        payNote.setWrapText(true);

        ToggleGroup payToggle = new ToggleGroup();
        RadioButton fullPayBtn = new RadioButton("Pay in Full");
        RadioButton partialPayBtn = new RadioButton("Pay Partial (advance)");
        fullPayBtn.setToggleGroup(payToggle);
        partialPayBtn.setToggleGroup(payToggle);
        fullPayBtn.setSelected(true);
        fullPayBtn.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");
        partialPayBtn.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");
        HBox toggleRow = new HBox(20, fullPayBtn, partialPayBtn);
        toggleRow.setAlignment(Pos.CENTER_LEFT);

        Label amtLabel = new Label("Advance amount (₪):");
        amtLabel.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        TextField advanceField = new TextField();
        advanceField.setPromptText("Enter amount you are paying now");
        advanceField.setStyle("-fx-background-radius:6; -fx-border-radius:6; -fx-border-color:#CBD5E1; -fx-padding:8 12; -fx-font-size:13px; -fx-background-color:white;");
        advanceField.setPrefWidth(220);
        HBox amtRow = new HBox(10, amtLabel, advanceField);
        amtRow.setAlignment(Pos.CENTER_LEFT);
        amtRow.setVisible(false);
        amtRow.setManaged(false);

        Label methodLabel = new Label("Payment Method:");
        methodLabel.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Cash", "BankTransfer", "Cheque");
        methodBox.setValue("Cash");
        methodBox.setStyle("-fx-font-size:13px;");
        HBox methodRow = new HBox(10, methodLabel, methodBox);
        methodRow.setAlignment(Pos.CENTER_LEFT);

        partialPayBtn.setOnAction(e -> {
            amtRow.setVisible(true);
            amtRow.setManaged(true);
        });
        fullPayBtn.setOnAction(e -> {
            amtRow.setVisible(false);
            amtRow.setManaged(false);
        });

        Label dlabel = new Label("Estimated Delivery:");
        dlabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        dlabel.setTextFill(Color.web("#64748B"));
        Label estimatedDateLbl = new Label(LocalDate.now().plusDays(3).format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")));
        estimatedDateLbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        estimatedDateLbl.setTextFill(Color.web(DARK));
        Label estimateNote = new Label("(our team will confirm the exact date)");
        estimateNote.setStyle("-fx-text-fill:#94A3B8; -fx-font-size:11px; -fx-font-style:italic;");
        VBox deliveryBox = new VBox(2, estimatedDateLbl, estimateNote);

        pane.getChildren().addAll(payTitle, payNote, toggleRow, amtRow, methodRow,
                new HBox(12, dlabel, deliveryBox) {{
                    setAlignment(Pos.CENTER_LEFT);
                }});

        Button placeBtn = new Button("  Place Order & Pay");
        placeBtn.setStyle("-fx-background-color:" + GREEN + "; -fx-text-fill:white; -fx-font-size:15px; " +
                "-fx-font-weight:bold; -fx-background-radius:8; -fx-cursor:hand; -fx-padding:12 32;");

        Button clearBtn = new Button("🗑  Clear Cart");
        clearBtn.setStyle("-fx-background-color:" + RED + "; -fx-text-fill:white; -fx-font-size:13px; " +
                "-fx-background-radius:8; -fx-cursor:hand; -fx-padding:10 20;");

        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        statusLabel.setWrapText(true);

        pane.getChildren().addAll(new HBox(12, placeBtn, clearBtn), statusLabel);

        clearBtn.setOnAction(e -> {
            cart.clear();
            updateTotal();
            statusLabel.setText("");
        });

        placeBtn.setOnAction(e -> {
            if (cart.isEmpty()) {
                statusLabel.setTextFill(Color.web(RED));
                statusLabel.setText("X  Cart is empty!");
                return;
            }

            double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();
            double payAmount;
            String payStatus;

            if (fullPayBtn.isSelected()) {
                payAmount = total;
                payStatus = "Paid";
            } else {
                String advTxt = advanceField.getText().trim();
                if (advTxt.isEmpty()) {
                    statusLabel.setTextFill(Color.web(RED));
                    statusLabel.setText("Enter the advance amount.");
                    return;
                }
                try {
                    payAmount = Double.parseDouble(advTxt);
                } catch (NumberFormatException ex) {
                    statusLabel.setTextFill(Color.web(RED));
                    statusLabel.setText("X Invalid amount.");
                    return;
                }
                if (payAmount <= 0) {
                    statusLabel.setTextFill(Color.web(RED));
                    statusLabel.setText("X Amount must be greater than zero.");
                    return;
                }
                if (payAmount >= total) {
                    statusLabel.setTextFill(Color.web(RED));
                    statusLabel.setText("X For full payment select 'Pay in Full'.");
                    return;
                }
                payStatus = "Partial";
            }

            String method = methodBox.getValue();

            // Check stock again before saving
            for (CartItem item : cart) {
                if (getBatchStock(item.getBatchID()) < item.getQty()) {
                    statusLabel.setTextFill(Color.web(RED));
                    statusLabel.setText(item.getProductName() + " no longer has enough stock. Please update your cart.");
                    return;
                }
            }

            LocalDate now = LocalDate.now();
            String today = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String delivery = now.plusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int empID = getAnyEmployee();
            if (empID == -1) {
                statusLabel.setTextFill(Color.web(RED));
                statusLabel.setText("X No employees available.");
                return;
            }

            try {
                SaleOrder so = new SaleOrder(clientID, empID, today, delivery, "Pending", total, payStatus);
                boolean ok = SaleOrderDAO.addSaleOrder(so);
                if (!ok) {
                    statusLabel.setTextFill(Color.web(RED));
                    statusLabel.setText("X  Failed to place order.");
                    return;
                }
                int newID = getLastSaleOrderID();

                PaymentDAO.addPayment(new Payment(newID, null, today, payAmount, method, "Incoming"));

                for (CartItem item : cart) {
                    SaleOrderDAO.addSaleOrderItem(new SaleOrderItem(
                            newID, item.getBatchID(), item.getQty(), item.getUnitPrice(), 0.0));
                    deductStock(item.getBatchID(), item.getQty());
                }

                statusLabel.setTextFill(Color.web(GREEN));
                statusLabel.setText(
                        "Order #" + newID + " placed! " +
                                "Total: " + String.format("%.2f", total) +
                                " | Paid now: " + String.format("%.2f", payAmount) + " (" + method + ")." +
                                (payStatus.equals("Partial") ? " Remaining: " + String.format("%.2f", total - payAmount) + " to be settled." : "") +
                                " Our team will prepare your order shortly.");
                cart.clear();
                updateTotal();
                advanceField.clear();
                fullPayBtn.setSelected(true);
                amtRow.setVisible(false);
                amtRow.setManaged(false);

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setTextFill(Color.web(RED));
                statusLabel.setText("X  Error: " + ex.getMessage());
            }
        });

        ScrollPane sp = new ScrollPane(pane);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:" + GRAY + ";");
        return sp;
    }

    private ScrollPane buildOrdersTab() {
        VBox pane = pane();
        Label title = new Label(" My Orders");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(DARK));
        pane.getChildren().addAll(title, new Separator());

        Button refreshBtn = actionBtn("🔄 Refresh Orders", MID);
        pane.getChildren().add(refreshBtn);

        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No orders yet."));

        String[] cols = {"Order #","Order Date","Delivery Date","Status","Total (₪)","Payment","Handled By"};
        int[] widths  = {70, 100, 110, 120, 90, 90, 130};
        for (int i = 0; i < cols.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> c = new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(idx)));
            if (cols[i].equals("Payment")) {
                c.setCellFactory(cc -> new TableCell<>() {
                    @Override protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty||item==null){ setText(null); setStyle(""); return; }
                        setText(item);
                        setStyle(item.equals("Paid") ? "-fx-text-fill:#16A34A;-fx-font-weight:bold;" :
                                item.equals("Partial") ? "-fx-text-fill:#D97706;-fx-font-weight:bold;" :
                                "-fx-text-fill:#DC2626;-fx-font-weight:bold;");
                    }
                });
            }
            table.getColumns().add(c);
        }
        pane.getChildren().add(table);

        Label summary = new Label("");
        summary.setStyle("-fx-font-size:12px; -fx-text-fill:#64748B;");
        pane.getChildren().add(summary);

        refreshBtn.setOnAction(e -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT so.SaleOrderID, so.OrderDate, so.DeliveryDate, so.Status, " +
                                "so.TotalAmount, so.PaymentStatus, " +
                                "CONCAT(emp.FirstName, ' ', emp.LastName) AS HandledBy " +
                                "FROM SaleOrder so " +
                                "JOIN Employee emp ON so.EmployeeID = emp.EmployeeID " +
                                "WHERE so.ClientID=? ORDER BY so.OrderDate DESC");
                ps.setInt(1, clientID);
                ResultSet rs = ps.executeQuery();
                int count=0;
                double total=0;
                while (rs.next()) {
                    count++;
                    double amt=rs.getDouble("TotalAmount");
                    total+=amt;
                    String status = rs.getString("Status");
                    String statusDisplay = status.equals("Delivered") ? " Delivered" :
                            status.equals("Approved")  ? " Being Prepared" :
                            status.equals("Pending")   ? " Awaiting Approval" :
                            "X Cancelled";
                    table.getItems().add(FXCollections.observableArrayList(
                            String.valueOf(rs.getInt("SaleOrderID")),
                            rs.getString("OrderDate"),
                            rs.getString("DeliveryDate")!=null?rs.getString("DeliveryDate"):"->",
                            statusDisplay,
                            String.format("%.2f",amt),
                            rs.getString("PaymentStatus"),
                            rs.getString("HandledBy")));
                }
                conn.close();
                summary.setText("Showing "+count+" order(s)  |  Total spent: ₪"+String.format("%.2f",total));
            } catch(Exception ex){ex.printStackTrace();}
        });
        refreshBtn.fire();

        ScrollPane sp = new ScrollPane(pane);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:"+GRAY+";");
        return sp;
    }

    private ScrollPane buildPaymentsTab() {
        VBox pane = pane();
        Label title = new Label("💳 My Payments");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(DARK));
        pane.getChildren().addAll(title, new Separator());

        Button refreshBtn = actionBtn("🔄 Refresh", MID);
        pane.getChildren().add(refreshBtn);

        TableView<ObservableList<String>> table = new TableView<>();
        table.setPrefHeight(380);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No payments yet."));

        String[] cols  = {"Payment #","Order #","Date","Amount (₪)","Method"};
        int[]    widths= {100,90,110,110,140};
        for (int i=0; i<cols.length; i++) {
            final int idx=i;
            TableColumn<ObservableList<String>,String> c=new TableColumn<>(cols[i]);
            c.setPrefWidth(widths[i]);
            c.setCellValueFactory(d->new SimpleStringProperty(d.getValue().get(idx)));
            table.getColumns().add(c);
        }
        pane.getChildren().add(table);

        Label summary = new Label("");
        summary.setStyle("-fx-font-size:12px; -fx-text-fill:#64748B;");
        pane.getChildren().add(summary);

        refreshBtn.setOnAction(e -> {
            table.getItems().clear();
            try {
                Connection conn = DBConnection.connect();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT p.PaymentID,p.SaleOrderID,p.PaymentDate,p.Amount,p.PaymentMethod " +
                                "FROM Payment p JOIN SaleOrder so ON p.SaleOrderID=so.SaleOrderID " +
                                "WHERE so.ClientID=? ORDER BY p.PaymentDate DESC");
                ps.setInt(1, clientID);
                ResultSet rs = ps.executeQuery();
                int count=0;
                double total=0;
                while (rs.next()) {
                    count++;
                    double amt=rs.getDouble("Amount");
                    total+=amt;
                    table.getItems().add(FXCollections.observableArrayList(String.valueOf(rs.getInt("PaymentID")),
                            String.valueOf(rs.getInt("SaleOrderID")),
                            rs.getString("PaymentDate"), String.format("%.2f",amt),
                            rs.getString("PaymentMethod")));}
                conn.close();
                summary.setText("Showing "+count+" payment(s) | Total paid: ₪"+String.format("%.2f",total));
            } catch(Exception ex){ex.printStackTrace();}
        });
        refreshBtn.fire();

        ScrollPane sp = new ScrollPane(pane);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color:"+GRAY+";");
        return sp;
    }

    private void showAddToCartDialog(CatalogItem item) {
        if (item.getInStock() == 0) {
            new Alert(Alert.AlertType.WARNING, "This product is out of stock!", ButtonType.OK).showAndWait();
            return;
        }
        for (CartItem ci : cart) {
            if (ci.getBatchID() == item.getBatchID()) {
                new Alert(Alert.AlertType.INFORMATION, item.getProductName() + " is already in your cart!", ButtonType.OK).showAndWait();
                return;
            }
        }
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Adding: " + item.getProductName());

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        Label info = new Label("Price: ₪" + String.format("%.2f", item.getPrice()) + "  |  Available: " + item.getInStock() + " units");
        info.setStyle("-fx-font-size:13px;");
        Label ql = new Label("Quantity:");
        ql.setStyle("-fx-font-weight:bold;");
        Spinner<Integer> spinner = new Spinner<>(1, item.getInStock(), 1);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        Label subLabel = new Label("Subtotal: ₪" + String.format("%.2f", item.getPrice()));
        subLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px; -fx-text-fill:" + GREEN + ";");
        spinner.valueProperty().addListener((o,old,qty) -> subLabel.setText("Subtotal: ₪" + String.format("%.2f", qty * item.getPrice())));
        content.getChildren().addAll(info, ql, spinner, subLabel);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(b -> b == ButtonType.OK ? spinner.getValue() : null);
        dialog.showAndWait().ifPresent(qty -> {
            cart.add(new CartItem(item.getBatchID(), item.getProductName(), qty, item.getPrice()));
            updateTotal();
            new Alert(Alert.AlertType.INFORMATION, qty + "x " + item.getProductName() + " added to cart! ", ButtonType.OK).showAndWait();
        });
    }

    private void updateTotal() {
        double total = cart.stream().mapToDouble(CartItem::getSubtotal).sum();
        if (cartTotalLabel != null) {
            cartTotalLabel.setText("Total: ₪" + String.format("%.2f", total));
        }
    }

    private ObservableList<CatalogItem> loadCatalog(String keyword, String category) {
        ObservableList<CatalogItem> list = FXCollections.observableArrayList();
        try {
            Connection conn = DBConnection.connect();
            StringBuilder sql = new StringBuilder(
                    "SELECT b.BatchID, p.ProductName, c.Name AS Category, p.UnitPrice, b.QtyInStock, w.WarehouseName, b.ExpiryDate " +
                            "FROM Batch b JOIN Product p ON b.ProductID=p.ProductID " +
                            "JOIN Category c ON p.CategoryID=c.CategoryID " +
                            "JOIN Warehouse w ON b.WarehouseID=w.WarehouseID WHERE 1=1");
            List<String> params = new ArrayList<>();
            if (!keyword.isEmpty()) {
                sql.append(" AND p.ProductName LIKE ?");
                params.add("%" + keyword + "%");
            }
            if (!category.isEmpty() && !category.equals("All Categories")) {
                sql.append(" AND c.Name=?");
                params.add(category);
            }
            sql.append(" ORDER BY c.Name, p.ProductName");
            PreparedStatement ps = conn.prepareStatement(sql.toString());
            for (int i=0; i<params.size(); i++) {
                ps.setString(i+1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new CatalogItem(
                        rs.getInt("BatchID"), rs.getString("ProductName"), rs.getString("Category"),
                        rs.getDouble("UnitPrice"), rs.getInt("QtyInStock"), rs.getString("WarehouseName"),
                        rs.getString("ExpiryDate")!=null?rs.getString("ExpiryDate"):"->"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void loadCategories(ComboBox<String> cb) {
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery("SELECT Name FROM Category ORDER BY Name");
            cb.getItems().add("All Categories");
            while (rs.next()) cb.getItems().add(rs.getString("Name"));
            cb.setValue("All Categories");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getClientName() {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT ClientName FROM Client WHERE ClientID=?");
            ps.setInt(1, clientID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String n=rs.getString("ClientName");
                conn.close();
                return n;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Client";
    }


    private int getAnyEmployee() {
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery("SELECT EmployeeID FROM Employee" +
                    " WHERE Role LIKE '%Sales%' LIMIT 1");
            if (rs.next()) {
                int id=rs.getInt(1);
                conn.close();
                return id;
            }
            rs = conn.createStatement().executeQuery("SELECT EmployeeID FROM Employee LIMIT 1");
            if (rs.next()) {
                int id=rs.getInt(1);
                conn.close();
                return id;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getBatchStock(int batchID) {
        try {
            Connection conn = DBConnection.connect();
            PreparedStatement ps = conn.prepareStatement("SELECT QtyInStock FROM Batch WHERE BatchID=?");
            ps.setInt(1, batchID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stock = rs.getInt("QtyInStock");
                conn.close();
                return stock;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void deductStock(int batchID, int qty) throws SQLException {
        Connection conn = DBConnection.connect();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE Batch SET QtyInStock = QtyInStock - ? " +
                        "WHERE BatchID = ? AND QtyInStock >= ?");
        ps.setInt(1, qty);
        ps.setInt(2, batchID);
        ps.setInt(3, qty);
        int updatedRows = ps.executeUpdate();
        conn.close();
        if (updatedRows == 0) {
            throw new SQLException("Not enough stock for batch #" + batchID);
        }
    }

    private int getLastSaleOrderID() {
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement().executeQuery("SELECT MAX(SaleOrderID) FROM SaleOrder");
            if (rs.next()) {
                int id=rs.getInt(1);
                conn.close();
                return id;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    private <S,T> TableColumn<S,T> col(String title, String prop, int width) {
        TableColumn<S,T> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(width);
        return c;
    }

    private VBox pane() {
        VBox p = new VBox(16);
        p.setPadding(new Insets(24));
        p.setStyle("-fx-background-color:"+GRAY+";");
        return p;
    }

    private Button actionBtn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:"+color+"; -fx-text-fill:white;" +
                " -fx-background-radius:6; -fx-padding:8 18; -fx-cursor:hand;" +
                " -fx-font-weight:bold; -fx-font-size:13px;");
        return b;
    }

    private HBox legendItem(String color, String label) {
        Label sq = new Label("   ");
        sq.setStyle("-fx-background-color:"+color+"; -fx-border-color:#ccc;");
        Label tx = new Label(label);
        tx.setStyle("-fx-font-size:11px; -fx-text-fill:#666;");
        HBox b = new HBox(6, sq, tx);
        b.setAlignment(Pos.CENTER_LEFT);
        return b;
    }

    public static void main(String[] args) {
        launch(args);
    }
}