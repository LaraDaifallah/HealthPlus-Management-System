package application;

import java.sql.*;
import java.util.ArrayList;

public class EmployeeDAO {

    public static ArrayList<Employee> getAllEmployees() {

        ArrayList<Employee> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Employee";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                int wid = rs.getInt("WarehouseID");
                Integer warehouseID = rs.wasNull() ? null : wid;

                Employee e = new Employee(
                        rs.getInt("EmployeeID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Role"),
                        rs.getString("HireDate"),
                        rs.getString("Phone"),
                        rs.getDouble("Salary"),
                        warehouseID
                );

                list.add(e);
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public static boolean addEmployee(Employee e) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "INSERT INTO Employee " +
                            "(FirstName, LastName, Role, HireDate, Phone, Salary, WarehouseID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, e.getFirstName());
            stmt.setString(2, e.getLastName());
            stmt.setString(3, e.getRole());
            stmt.setString(4, e.getHireDate());
            stmt.setString(5, e.getPhone());
            stmt.setDouble(6, e.getSalary());

            if (e.getWarehouseID() == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, e.getWarehouseID());
            }

            int rows = stmt.executeUpdate();

            conn.close();

            return rows > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static void updateEmployee(Employee e) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "UPDATE Employee SET " +
                            "FirstName=?, LastName=?, Role=?, HireDate=?, Phone=?, Salary=?, WarehouseID=? " +
                            "WHERE EmployeeID=?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, e.getFirstName());
            stmt.setString(2, e.getLastName());
            stmt.setString(3, e.getRole());
            stmt.setString(4, e.getHireDate());
            stmt.setString(5, e.getPhone());
            stmt.setDouble(6, e.getSalary());

            if (e.getWarehouseID() == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setInt(7, e.getWarehouseID());
            }

            stmt.setInt(8, e.getId());

            stmt.executeUpdate();

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static boolean deleteEmployee(int id) {

        try {
            Connection conn = DBConnection.connect();

            String sql = "DELETE FROM Employee WHERE EmployeeID=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();

            conn.close();

            return rows > 0;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static Employee getEmployeeById(int id) {

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Employee WHERE EmployeeID=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                int wid = rs.getInt("WarehouseID");
                Integer warehouseID = rs.wasNull() ? null : wid;

                Employee e = new Employee(
                        rs.getInt("EmployeeID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Role"),
                        rs.getString("HireDate"),
                        rs.getString("Phone"),
                        rs.getDouble("Salary"),
                        warehouseID
                );

                conn.close();
                return e;
            }

            conn.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}