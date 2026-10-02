package application;

import java.sql.*;
import java.util.ArrayList;

public class SupplierProductDAO {

    public static ArrayList<SupplierProduct> getAllSupplierProducts() {

        ArrayList<SupplierProduct> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM SupplierProduct";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                SupplierProduct sp = new SupplierProduct(
                        rs.getInt("SupplierID"),
                        rs.getInt("ProductID"),
                        rs.getDouble("UnitCost")
                );

                list.add(sp);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addSupplierProduct(SupplierProduct sp) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "INSERT INTO SupplierProduct " +
                            "(SupplierID, ProductID, UnitCost) " +
                            "VALUES (?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, sp.getSupplierId());
            stmt.setInt(2, sp.getProductId());
            stmt.setDouble(3, sp.getUnitCost());

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteSupplierProduct(int supplierId, int productId) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "DELETE FROM SupplierProduct " +
                            "WHERE SupplierID=? AND ProductID=?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, supplierId);
            stmt.setInt(2, productId);

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateSupplierProduct(SupplierProduct sp) {
        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "UPDATE SupplierProduct SET UnitCost = ? " +
                            "WHERE SupplierID = ? AND ProductID = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setDouble(1, sp.getUnitCost());
            stmt.setInt(2, sp.getSupplierId());
            stmt.setInt(3, sp.getProductId());

            int rows = stmt.executeUpdate();
            conn.close();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}