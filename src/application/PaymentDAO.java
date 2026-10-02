package application;

import java.sql.*;
import java.util.ArrayList;

public class PaymentDAO {

    public static boolean addPayment(Payment p) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "INSERT INTO Payment "
                    + "(SaleOrderID, PONumber, PaymentDate, Amount, PaymentMethod, Direction) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            if (p.getSaleOrderId() == null) {
                stmt.setNull(1, Types.INTEGER);
            } else {
                stmt.setInt(1, p.getSaleOrderId());
            }

            if (p.getPoNumber() == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, p.getPoNumber());
            }

            stmt.setString(3, p.getPaymentDate());
            stmt.setDouble(4, p.getAmount());
            stmt.setString(5, p.getPaymentMethod());
            stmt.setString(6, p.getDirection());

            int rows = stmt.executeUpdate();
            conn.close();

            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Payment> getAllPayments() {
        ArrayList<Payment> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql = "SELECT * FROM Payment";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Integer saleOrderId = rs.getObject("SaleOrderID") == null ? null : rs.getInt("SaleOrderID");
                Integer poNumber = rs.getObject("PONumber") == null ? null : rs.getInt("PONumber");

                Payment p = new Payment(
                        rs.getInt("PaymentID"),
                        saleOrderId,
                        poNumber,
                        rs.getString("PaymentDate"),
                        rs.getDouble("Amount"),
                        rs.getString("PaymentMethod"),
                        rs.getString("Direction")
                );

                list.add(p);
            }

            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public static boolean deletePayment(int id) {
        try {
            Connection conn = DBConnection.connect();

            String sql = "DELETE FROM Payment WHERE PaymentID = ?";
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
}