package application;

import java.sql.*;
import java.util.ArrayList;

public class CategoryDAO {

    public static ArrayList<Category> getAllCategories(){

        ArrayList<Category> list = new ArrayList<>();

        try{
            Connection conn = DBConnection.connect();
            String sql ="SELECT * FROM Category";
            Statement stmt= conn.createStatement();
            ResultSet rs =stmt.executeQuery(sql);
            while (rs.next()){
                Category c= new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
                list.add(c);
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public static void addCategory(Category c) {
        try {
            Connection conn=DBConnection.connect();

            String sql ="INSERT INTO Category (Name, Description) VALUES (?, ?)";

            PreparedStatement stmt =conn.prepareStatement(sql);

            stmt.setString(1,c.getName());
            stmt.setString(2,c.getDescription());

            stmt.executeUpdate();

            conn.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void updateCategory(Category c){
        try {
            Connection conn = DBConnection.connect();
            String sql ="UPDATE Category SET Name = ?, Description = ? WHERE CategoryID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,c.getName());
            stmt.setString(2,c.getDescription());
            stmt.setInt(3,c.getId());

            stmt.executeUpdate();
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteCategory(int id) {
        System.out.println("deleteCategory called with id: " + id);
        try {
            Connection conn = DBConnection.connect();

            // collect product ID
            ArrayList<Integer> productIds = new ArrayList<>();
            PreparedStatement s1 = conn.prepareStatement(
                    "SELECT ProductID FROM Product WHERE CategoryID = ?");
            s1.setInt(1, id);
            ResultSet rs = s1.executeQuery();
            while (rs.next()) {
                productIds.add(rs.getInt("ProductID"));
            }
            rs.close();
            s1.close();

            // now delete supplier links
            for (int pid : productIds) {
                PreparedStatement s2 = conn.prepareStatement(
                        "DELETE FROM SupplierProduct WHERE ProductID = ?");
                s2.setInt(1, pid);
                s2.executeUpdate();
                s2.close();

                PreparedStatement s3 = conn.prepareStatement(
                        "DELETE FROM Batch WHERE ProductID = ?");
                s3.setInt(1, pid);
                s3.executeUpdate();
                s3.close();
            }

            // now delete the category
            PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM Category WHERE CategoryID = ?");
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            stmt.close();
            conn.close();

            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Category getCategoryById(int id) {
        try {
            Connection conn= DBConnection.connect();
            String sql ="SELECT * FROM Category WHERE CategoryID = ?";
            PreparedStatement stmt =conn.prepareStatement(sql);
            stmt.setInt(1,id);
            ResultSet rs =stmt.executeQuery();
            if (rs.next()){
                Category c =new Category(
                        rs.getInt("CategoryID"),
                        rs.getString("Name"),
                        rs.getString("Description")
                );
                conn.close();
                return c;
            }
            conn.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}