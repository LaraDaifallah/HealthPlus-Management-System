package application;

import java.sql.*;
import java.util.ArrayList;

public class SupplierDAO {

    public static ArrayList<Supplier> getAllSuppliers() {
        ArrayList<Supplier> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM Supplier";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Supplier s = new Supplier(
                        rs.getInt("SupplierID"),
                        rs.getString("SupplierName"),
                        rs.getString("ContactPerson"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("City"));
                list.add(s);
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addSupplier(Supplier s) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO Supplier (SupplierName, ContactPerson, Phone, Email, City) "
                    + "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, s.getName());
            stmt.setString(2, s.getContactPerson());
            stmt.setString(3, s.getPhone());
            stmt.setString(4, s.getEmail());
            stmt.setString(5, s.getCity());

            int rows = stmt.executeUpdate();
            conn.close();

            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateSupplier(Supplier s) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "UPDATE Supplier SET SupplierName = ?, ContactPerson = ?, Phone = ?, Email = ?, City = ? "
                    + "WHERE SupplierID = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, s.getName());
            stmt.setString(2, s.getContactPerson());
            stmt.setString(3, s.getPhone());
            stmt.setString(4, s.getEmail());
            stmt.setString(5,s.getCity());
            stmt.setInt(6, s.getId());

            stmt.executeUpdate();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteSupplier(int id) {
        try {
            Connection conn = DBConnection.connect();

            String deleteLinks = "DELETE FROM SupplierProduct WHERE SupplierID = ?";
            PreparedStatement s1 = conn.prepareStatement(deleteLinks);
            s1.setInt(1, id);
            s1.executeUpdate();
            s1.close();

            String sql = "DELETE FROM Supplier WHERE SupplierID = ?";
            PreparedStatement stmt =conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            conn.close();

            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Supplier getSupplierById(int id) {
        try  {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM Supplier WHERE SupplierID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Supplier s = new Supplier(
                        rs.getInt("SupplierID"),
                        rs.getString("SupplierName"),
                        rs.getString("ContactPerson"),
                        rs.getString("Phone"),
                        rs.getString("Email"),
                        rs.getString("City")
                );

                conn.close();
                return s;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();}
        return null;
    }
}