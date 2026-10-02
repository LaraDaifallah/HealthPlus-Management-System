package application;

import java.sql.*;
import java.util.ArrayList;

public class BatchDAO {

    public static ArrayList<Batch> getAllBatches() {

        ArrayList<Batch> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Batch";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                Batch b = new Batch(
                        rs.getInt("BatchID"), rs.getInt("ProductID"), rs.getInt("WarehouseID"),
                        rs.getInt("QtyInStock"), rs.getString("ExpiryDate"),
                        rs.getString("StorageLocation"));

                list.add(b);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public static boolean addBatch(Batch b) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "INSERT INTO Batch " +
                            "(ProductID, WarehouseID, QtyInStock, ExpiryDate, StorageLocation) " +
                            "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, b.getProductId());
            stmt.setInt(2, b.getWarehouseId());
            stmt.setInt(3, b.getQuantity());
            stmt.setString(4, b.getExpiryDate());
            stmt.setString(5, b.getStorageLocation());

            int rows = stmt.executeUpdate();

            conn.close();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateBatch(Batch b) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "UPDATE Batch SET " + "ProductID = ?, " + "WarehouseID = ?, " + "QtyInStock = ?, " +
                            "ExpiryDate = ?, " + "StorageLocation = ? " + "WHERE BatchID = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, b.getProductId());
            stmt.setInt(2, b.getWarehouseId());
            stmt.setInt(3, b.getQuantity());
            stmt.setString(4, b.getExpiryDate());
            stmt.setString(5, b.getStorageLocation());
            stmt.setInt(6, b.getId());

            stmt.executeUpdate();

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteBatch(int id) {

        try {
            Connection conn = DBConnection.connect();

            String sql = "DELETE FROM Batch WHERE BatchID = ?";

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

    public static Batch getBatchById(int id) {

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Batch WHERE BatchID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                Batch b = new Batch(
                        rs.getInt("BatchID"),
                        rs.getInt("ProductID"),
                        rs.getInt("WarehouseID"),
                        rs.getInt("QtyInStock"),
                        rs.getString("ExpiryDate"),
                        rs.getString("StorageLocation")
                );

                conn.close();
                return b;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}