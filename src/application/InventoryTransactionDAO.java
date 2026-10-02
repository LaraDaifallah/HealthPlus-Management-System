package application;

import java.sql.*;
import java.util.ArrayList;

public class InventoryTransactionDAO {

    public static boolean addTransaction(InventoryTransaction t) {
        Connection conn = null;

        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);
            int currentQty = 0;
            String selectSql = "SELECT QtyInStock FROM Batch WHERE BatchID = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectSql);
            selectStmt.setInt(1, t.getBatchId());

            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                conn.close();
                return false;
            }

            currentQty = rs.getInt("QtyInStock");

            int newQty;

            if (t.getTxnType().equalsIgnoreCase("Receipt")) {
                newQty = currentQty + t.getQuantity();
            } else if (t.getTxnType().equalsIgnoreCase("Dispatch")) {
                newQty = currentQty - t.getQuantity();

                if (newQty < 0) {
                    conn.rollback();
                    conn.close();
                    return false;
                }

            } else if (t.getTxnType().equalsIgnoreCase("Adjustment")) {
                newQty = t.getQuantity();

                if (newQty < 0) {
                    conn.rollback();
                    conn.close();
                    return false;
                }

            } else {
                conn.rollback();
                conn.close();
                return false;
            }

            String insertSql =
                    "INSERT INTO InventoryTransaction " +
                    "(BatchID, EmployeeID, TxnType, Quantity, ReferenceID) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, t.getBatchId());
            insertStmt.setInt(2, t.getEmployeeId());
            insertStmt.setString(3, t.getTxnType());
            insertStmt.setInt(4, t.getQuantity());
            insertStmt.setInt(5, t.getReferenceId());

            insertStmt.executeUpdate();

            String updateSql =
                    "UPDATE Batch SET QtyInStock = ? WHERE BatchID = ?";

            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, newQty);
            updateStmt.setInt(2, t.getBatchId());

            updateStmt.executeUpdate();

            conn.commit();
            conn.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            try {
                if (conn != null) {
                    conn.rollback();
                    conn.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return false;
        }
    }

    public static ArrayList<InventoryTransaction> getAllTransactions() {
        ArrayList<InventoryTransaction> list = new ArrayList<>();

        try {
            Connection conn = DBConnection.connect();

            String sql ="SELECT * FROM InventoryTransaction";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                InventoryTransaction t = new InventoryTransaction(rs.getInt("TransactionID"), rs.getInt("BatchID"),
                        rs.getInt("EmployeeID"), rs.getString("TxnType"), rs.getInt("Quantity"),
                        rs.getString("TxnDate"), rs.getInt("ReferenceID")
                );

                list.add(t);
            }
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}