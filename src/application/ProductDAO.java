package application;

import java.sql.*;
import java.util.ArrayList;

public class ProductDAO {

    public static ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM Product";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("ProductID"),
                        rs.getString("ProductName"),
                        rs.getString("Description"),
                        rs.getDouble("UnitPrice"),
                        rs.getInt("ReorderLevel"),
                        rs.getInt("CategoryID")
                );

                list.add(p);
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addProduct(Product p) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO Product "
                    + "(ProductName, Description, UnitPrice, ReorderLevel, CategoryID) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getName());
            stmt.setString(2, p.getDescription());
            stmt.setDouble(3, p.getPrice());
            stmt.setInt(4, p.getReorderLevel());
            stmt.setInt(5, p.getCategoryId());

            int rowsAffected = stmt.executeUpdate();
            conn.close();

            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateProduct(Product p) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "UPDATE Product SET ProductName = ?, Description = ?, UnitPrice = ?, "
                    + "ReorderLevel = ?, CategoryID = ? WHERE ProductID = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, p.getName());
            stmt.setString(2, p.getDescription());
            stmt.setDouble(3, p.getPrice());
            stmt.setInt(4, p.getReorderLevel());
            stmt.setInt(5, p.getCategoryId());
            stmt.setInt(6, p.getId());

            stmt.executeUpdate();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteProduct(int id) {
        try {
            Connection conn = DBConnection.connect();

            String deleteSup = "DELETE FROM SupplierProduct WHERE ProductID = ?";
            PreparedStatement s1 = conn.prepareStatement(deleteSup);
            s1.setInt(1, id);
            s1.executeUpdate();

            String deleteBatch = "DELETE FROM Batch WHERE ProductID = ?";
            PreparedStatement s2 = conn.prepareStatement(deleteBatch);
            s2.setInt(1, id);
            s2.executeUpdate();

            String sql = "DELETE FROM Product WHERE ProductID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            conn.close();
            return rowsAffected > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Product getProductById(int id) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Product WHERE ProductID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Product p = new Product(
                        rs.getInt("ProductID"),
                        rs.getString("ProductName"),
                        rs.getString("Description"),
                        rs.getDouble("UnitPrice"),
                        rs.getInt("ReorderLevel"),
                        rs.getInt("CategoryID")
                );

                conn.close();
                return p;
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static boolean productExists(int id) {
        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT ProductID FROM Product WHERE ProductID = ?";
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