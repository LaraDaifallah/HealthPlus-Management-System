package application;

import java.sql.*;
import java.util.ArrayList;

public class SaleOrderDAO {

    public static boolean addSaleOrder(SaleOrder so) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO SaleOrder "
                    + "(ClientID, EmployeeID, OrderDate, DeliveryDate, Status, TotalAmount, PaymentStatus) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, so.getClientId());
            stmt.setInt(2, so.getEmployeeId());
            stmt.setString(3, so.getOrderDate());
            stmt.setString(4, so.getDeliveryDate());
            stmt.setString(5, so.getStatus());
            stmt.setDouble(6, so.getTotalAmount());
            stmt.setString(7, so.getPaymentStatus());

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<SaleOrder> getAllSaleOrders() {
        ArrayList<SaleOrder> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM SaleOrder";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                SaleOrder so = new SaleOrder(
                        rs.getInt("SaleOrderID"),
                        rs.getInt("ClientID"),
                        rs.getInt("EmployeeID"),
                        rs.getString("OrderDate"),
                        rs.getString("DeliveryDate"),
                        rs.getString("Status"),
                        rs.getDouble("TotalAmount"),
                        rs.getString("PaymentStatus")
                );

                list.add(so);
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addSaleOrderItem(SaleOrderItem item) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO SaleOrderItem "
                    + "(SaleOrderID, BatchID, QtyOrdered, UnitPrice, Discount) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, item.getSaleOrderId());
            stmt.setInt(2, item.getBatchId());
            stmt.setInt(3, item.getQtyOrdered());
            stmt.setDouble(4, item.getUnitPrice());
            stmt.setDouble(5, item.getDiscount());

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<SaleOrderItem> getAllSaleOrderItems() {
        ArrayList<SaleOrderItem> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM SaleOrderItem";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                SaleOrderItem item = new SaleOrderItem(
                        rs.getInt("SaleItemID"),
                        rs.getInt("SaleOrderID"),
                        rs.getInt("BatchID"),
                        rs.getInt("QtyOrdered"),
                        rs.getDouble("UnitPrice"),
                        rs.getDouble("Discount"));

                list.add(item);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}