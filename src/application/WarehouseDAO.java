package application;

import java.sql.*;
import java.util.ArrayList;

public class WarehouseDAO {

    public static ArrayList<Warehouse> getAllWarehouses() {
        ArrayList<Warehouse> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM Warehouse";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Warehouse w = new Warehouse(
                        rs.getInt("WarehouseID"),
                        rs.getString("WarehouseName"),
                        rs.getString("Address"),
                        rs.getString("City"),
                        rs.getString("Phone"),
                        rs.getInt("Capacity")
                );

                list.add(w);
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addWarehouse(Warehouse w) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO Warehouse (WarehouseName, Address, City, Phone, Capacity) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, w.getName());
            stmt.setString(2, w.getAddress());
            stmt.setString(3, w.getCity());
            stmt.setString(4, w.getPhone());
            stmt.setInt(5, w.getCapacity());

            int rows = stmt.executeUpdate();
            conn.close();

            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateWarehouse(Warehouse w) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "UPDATE Warehouse SET WarehouseName=?, Address=?, City=?, Phone=?, Capacity=? WHERE WarehouseID=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, w.getName());
            stmt.setString(2, w.getAddress());
            stmt.setString(3, w.getCity());
            stmt.setString(4, w.getPhone());
            stmt.setInt(5, w.getCapacity());
            stmt.setInt(6, w.getId());



            stmt.executeUpdate();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteWarehouse(int id) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "DELETE FROM Warehouse WHERE WarehouseID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            conn.close();

            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Warehouse getWarehouseById(int id) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Warehouse WHERE WarehouseID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Warehouse w = new Warehouse(
                        rs.getInt("WarehouseID"),
                        rs.getString("WarehouseName"),
                        rs.getString("Address"),
                        rs.getString("City"),
                        rs.getString("Phone"),
                        rs.getInt("Capacity")
                );

                conn.close();
                return w;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static boolean hasEnoughCapacity(int warehouseId, int addedQty) {
        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "SELECT w.Capacity, COALESCE(SUM(b.QtyInStock), 0) AS CurrentStock " +
                            "FROM Warehouse w " +
                            "LEFT JOIN Batch b ON w.WarehouseID = b.WarehouseID " +
                            "WHERE w.WarehouseID = ? " +
                            "GROUP BY w.WarehouseID, w.Capacity";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, warehouseId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int capacity = rs.getInt("Capacity");
                int currentStock = rs.getInt("CurrentStock");

                conn.close();

                return currentStock + addedQty <= capacity;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    public static boolean warehouseExists(int id) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT WarehouseID FROM Warehouse WHERE WarehouseID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();

            conn.close();
            return exists;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}