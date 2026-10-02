package application;

import java.sql.*;

public class UserAccountDAO {

    public static UserAccount login(int userID, String password) {
        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM UserAccount WHERE UserID = ? AND Password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("Role");

                int empVal = rs.getInt("EmployeeID");
                Integer employeeID = rs.wasNull() ? null : empVal;

                int cliVal = rs.getInt("ClientID");
                Integer clientID = rs.wasNull() ? null : cliVal;

                conn.close();
                return new UserAccount(userID, password, role, employeeID, clientID);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean addUser(String password, String role,
                                  Integer employeeID, Integer clientID) {
        try {
            Connection conn = DBConnection.connect();
            String sql = "INSERT INTO UserAccount (Password, Role, EmployeeID, ClientID) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, password);
            ps.setString(2, role);
            if (employeeID == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, employeeID);
            if (clientID == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, clientID);
            int rows = ps.executeUpdate();
            conn.close();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getLastUserID() {
        try {
            Connection conn = DBConnection.connect();
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT LAST_INSERT_ID()");
            if (rs.next()) {
                int id = rs.getInt(1);
                conn.close();
                return id;
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}