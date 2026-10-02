package application;

import java.sql.*;

public class ReportDAO {

    public static String getBasicStatistics() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();
            Statement stmt = conn.createStatement();

            sb.append("--basic statistics--\n\n");

            ResultSet rs;

            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Product");
            if (rs.next()) sb.append("Total Products: ").append(rs.getInt("total")).append("\n");

            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Batch");
            if (rs.next()) sb.append("Total Batches: ").append(rs.getInt("total")).append("\n");

            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Supplier");
            if (rs.next()) sb.append("Total Suppliers: ").append(rs.getInt("total")).append("\n");

            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM Client");
            if (rs.next()) sb.append("Total Clients: ").append(rs.getInt("total")).append("\n");

            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM SaleOrder");
            if (rs.next()) sb.append("Total Sale Orders: ").append(rs.getInt("total")).append("\n");

            rs = stmt.executeQuery("SELECT COUNT(*) AS total FROM PurchaseOrder");
            if (rs.next()) sb.append("Total Purchase Orders: ").append(rs.getInt("total")).append("\n");

            rs = stmt.executeQuery("SELECT SUM(QtyInStock) AS totalQty FROM Batch");
            if (rs.next()) sb.append("Total Stock Quantity: ").append(rs.getInt("totalQty")).append("\n");

            rs = stmt.executeQuery(
                    "SELECT SUM(b.QtyInStock * p.UnitPrice) AS value " +
                            "FROM Batch b JOIN Product p ON b.ProductID = p.ProductID"
            );
            if (rs.next()) sb.append("Total Inventory Value: ").append(rs.getDouble("value")).append("\n");

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading statistics.");
        }

        return sb.toString();
    }

    public static String getLowStockProducts() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT p.ProductID, p.ProductName, SUM(b.QtyInStock) AS TotalQty, p.ReorderLevel " +
                            "FROM Product p LEFT JOIN Batch b ON p.ProductID = b.ProductID " +
                            "GROUP BY p.ProductID, p.ProductName, p.ReorderLevel " +
                            "HAVING TotalQty < p.ReorderLevel";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sb.append("--- low stock products ---\n\n");

            while (rs.next()) {
                sb.append(rs.getInt("ProductID")).append(" | ")
                        .append(rs.getString("ProductName")).append(" |Qty: ")
                        .append(rs.getInt("TotalQty")).append(" | Reorder Level: ")
                        .append(rs.getInt("ReorderLevel")).append("\n");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading low stock report.");
        }

        return sb.toString();
    }

    public static String getExpiringBatches() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT b.BatchID, p.ProductName, b.ExpiryDate, b.QtyInStock " +
                            "FROM Batch b JOIN Product p ON b.ProductID = p.ProductID " +
                            "WHERE b.ExpiryDate <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) " +
                            "ORDER BY b.ExpiryDate";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sb.append("--- batches expiring in 30 days --\n\n");

            while (rs.next()) {
                sb.append("Batch ID: ").append(rs.getInt("BatchID"))
                        .append(" |Product: ").append(rs.getString("ProductName"))
                        .append(" | Expiry: ").append(rs.getString("ExpiryDate"))
                        .append(" |qty: ").append(rs.getInt("QtyInStock"))
                        .append("\n");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading expiry report.");
        }

        return sb.toString();
    }
    public static String getStockValueByCategory() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT c.Name AS CategoryName, " +
                            "COALESCE(SUM(b.QtyInStock * p.UnitPrice),0) AS StockValue " +
                            "FROM Category c " +
                            "LEFT JOIN Product p ON c.CategoryID = p.CategoryID " +
                            "LEFT JOIN Batch b ON p.ProductID = b.ProductID " +
                            "GROUP BY c.CategoryID, c.Name " +
                            "ORDER BY StockValue DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sb.append("--- Stock value ---\n\n");

            while (rs.next()) {
                sb.append(rs.getString("CategoryName"))
                        .append(" | Value: ")
                        .append(rs.getDouble("StockValue"))
                        .append("\n");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading stock value report.");
        }

        return sb.toString();
    }

    public static String getSalesSummary() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT COUNT(*) AS NumOrders, SUM(TotalAmount) AS TotalRevenue, AVG(TotalAmount) AS AvgOrder " +
                            "FROM SaleOrder";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sb.append("--- sales  ---\n\n");

            if (rs.next()) {
                sb.append("Number of Sale Orders: ").append(rs.getInt("NumOrders")).append("\n");
                sb.append("Total Revenue: ").append(rs.getDouble("TotalRevenue")).append("\n");
                sb.append("Average Order Value: ").append(rs.getDouble("AvgOrder")).append("\n");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading sales summary.");
        }

        return sb.toString();
    }

    public static String getPaymentsSummary() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT Direction, SUM(Amount) AS TotalAmount " +
                            "FROM Payment GROUP BY Direction";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sb.append("---payrment---\n\n");

            while (rs.next()) {
                sb.append(rs.getString("Direction"))
                        .append(" | Total: ")
                        .append(rs.getDouble("TotalAmount"))
                        .append("\n");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading payments summary.");
        }

        return sb.toString();
    }
    public static String getWarehouseCapacityReport() {
        StringBuilder sb = new StringBuilder();

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT w.WarehouseName, w.Capacity, " +
                            "IFNULL(SUM(b.QtyInStock), 0) AS CurrentStock, " +
                            "(w.Capacity - IFNULL(SUM(b.QtyInStock), 0)) AS RemainingCapacity, " +
                            "IF(w.Capacity = 0, 0, (IFNULL(SUM(b.QtyInStock), 0) / w.Capacity) * 100) AS UtilizationPercent " +
                            "FROM Warehouse w " +
                            "LEFT JOIN Batch b ON w.WarehouseID = b.WarehouseID " +
                            "GROUP BY w.WarehouseID, w.WarehouseName, w.Capacity";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            sb.append("---  REPORT ----\n\n");

            while (rs.next()) {
                sb.append(rs.getString("warehouseName"))
                        .append(" | Capacity: ")
                        .append(rs.getInt("Capacity"))
                        .append(" | current Stock: ")
                        .append(rs.getInt("CurrentStock"))
                        .append(" | Remaining: ")
                        .append(rs.getInt("RemainingCapacity"))
                        .append(" | utilization: ")
                        .append(String.format("%.2f", rs.getDouble("utilizationPercent")))
                        .append("%\n");
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error loading warehouse capacity report.");
        }

        return sb.toString();
    }
}