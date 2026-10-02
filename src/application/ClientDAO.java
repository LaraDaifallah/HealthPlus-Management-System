package application;

import java.sql.*;
import java.util.ArrayList;

public class ClientDAO {

    public static ArrayList<Client> getAllClients() {

        ArrayList<Client> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Client";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                Client c = new Client(
                        rs.getInt("ClientID"),
                        rs.getString("ClientName"),
                        rs.getString("ClientType"),
                        rs.getString("Phone"),
                        rs.getString("City"),
                        rs.getDouble("CreditLimit")
                );

                list.add(c);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addClient(Client c) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "INSERT INTO Client " +
                            "(ClientName, ClientType, Phone, City, CreditLimit) " +
                            "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, c.getName());
            stmt.setString(2, c.getType());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getCity());
            stmt.setDouble(5, c.getCreditLimit());

            int rows = stmt.executeUpdate();

            conn.close();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void updateClient(Client c) {

        try {
            Connection conn = DBConnection.connect();

            String sql =
                    "UPDATE Client SET " +
                            "ClientName = ?, " +
                            "ClientType = ?, " +
                            "Phone = ?, " +
                            "City = ?, " +
                            "CreditLimit = ? " +
                            "WHERE ClientID = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, c.getName());
            stmt.setString(2, c.getType());
            stmt.setString(3, c.getPhone());
            stmt.setString(4, c.getCity());
            stmt.setDouble(5, c.getCreditLimit());
            stmt.setInt(6, c.getId());

            stmt.executeUpdate();

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteClient(int id) {

        try {
            Connection conn = DBConnection.connect();

            String sql = "DELETE FROM Client WHERE ClientID = ?";

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

    public static Client getClientById(int id) {

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Client WHERE ClientID = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Client c = new Client(
                        rs.getInt("ClientID"),
                        rs.getString("ClientName"),
                        rs.getString("ClientType"),
                        rs.getString("Phone"),
                        rs.getString("City"),
                        rs.getDouble("CreditLimit")
                );

                conn.close();
                return c;
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}