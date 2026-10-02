package application;

import java.sql.*;
import java.util.ArrayList;

public class PurchaseOrderDAO {

    public static boolean addPurchaseOrder(PurchaseOrder po) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO PurchaseOrder "
                    + "(SupplierID, EmployeeID, OrderDate, ExpDeliveryDate, Status, TotalAmount) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, po.getSupplierId());
            stmt.setInt(2, po.getEmployeeId());
            stmt.setString(3, po.getOrderDate());
            stmt.setString(4, po.getExpDeliveryDate());
            stmt.setString(5, po.getStatus());
            stmt.setDouble(6, po.getTotalAmount());

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<PurchaseOrder> getAllPurchaseOrders() {
        ArrayList<PurchaseOrder> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM PurchaseOrder";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                PurchaseOrder po = new PurchaseOrder(
                        rs.getInt("PONumber"),
                        rs.getInt("SupplierID"),
                        rs.getInt("EmployeeID"),
                        rs.getString("OrderDate"),
                        rs.getString("ExpDeliveryDate"),
                        rs.getString("Status"),
                        rs.getDouble("TotalAmount")
                );

                list.add(po);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addPurchaseOrderItem(PurchaseOrderItem item) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO PurchaseOrderItem "
                    + "(PONumber, ProductID, QtyOrdered, UnitCost, QtyReceived) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, item.getPoNumber());
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQtyOrdered());
            stmt.setDouble(4, item.getUnitCost());
            stmt.setInt(5, item.getQtyReceived());

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<PurchaseOrderItem> getAllPurchaseOrderItems() {
        ArrayList<PurchaseOrderItem> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM PurchaseOrderItem";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                PurchaseOrderItem item = new PurchaseOrderItem(
                        rs.getInt("POItemID"),
                        rs.getInt("PONumber"),
                        rs.getInt("ProductID"),
                        rs.getInt("QtyOrdered"),
                        rs.getDouble("UnitCost"),
                        rs.getInt("QtyReceived")
                );

                list.add(item);
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}