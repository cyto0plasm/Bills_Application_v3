package Data_Access_Object.invoice_payment_history;

import Data_Access_Object.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoicePaymentHistoryDaoImp implements InvoicePaymentHistoryDao {

    // Method to fetch InvoicePaymentHistory based on multiple filters
    public List<InvoicePaymentHistory> findByFilters(Date startDate, Date endDate, double minAmount, double maxAmount) {
        List<InvoicePaymentHistory> filteredPayments = new ArrayList<>();
        String query = "SELECT * FROM invoice_payment_history WHERE 1=1";

        if (startDate != null) {
            query += " AND created_at >= ?";
        }
        if (endDate != null) {
            query += " AND created_at <= ?";
        }
        if (minAmount != Double.MIN_VALUE) {
            query += " AND amount_paid >= ?";
        }
        if (maxAmount != Double.MAX_VALUE) {
            query += " AND amount_paid <= ?";
        }

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int paramIndex = 1;
            if (startDate != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(startDate.getTime()));
            }
            if (endDate != null) {
                stmt.setTimestamp(paramIndex++, new Timestamp(endDate.getTime()));
            }
            if (minAmount != Double.MIN_VALUE) {
                stmt.setDouble(paramIndex++, minAmount);
            }
            if (maxAmount != Double.MAX_VALUE) {
                stmt.setDouble(paramIndex++, maxAmount);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                filteredPayments.add(extractInvoicePaymentHistoryFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return filteredPayments;
    }

    // Extract a single InvoicePaymentHistory from ResultSet
    private InvoicePaymentHistory extractInvoicePaymentHistoryFromResultSet(ResultSet rs) throws SQLException {
        return new InvoicePaymentHistory(
                rs.getInt("id"),
                rs.getInt("invoice_header_id"),
                rs.getFloat("amount_paid"),
                rs.getFloat("income"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at")
        );
    }

    // Method to fetch all InvoicePaymentHistory
    @Override
    public List<InvoicePaymentHistory> findAll() {
        List<InvoicePaymentHistory> paymentList = new ArrayList<>();
        try (Connection con = DbConnection.getConnection()) {
            String query = "SELECT * FROM invoice_payment_history";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    paymentList.add(extractInvoicePaymentHistoryFromResultSet(resultSet));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoicePaymentHistoryDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return paymentList;
    }

    // Method to fetch a single InvoicePaymentHistory by its ID
    @Override
    public InvoicePaymentHistory findById(int id) {
        try (Connection con = DbConnection.getConnection()) {
            String query = "SELECT * FROM invoice_payment_history WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ResultSet resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    return extractInvoicePaymentHistoryFromResultSet(resultSet);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoicePaymentHistoryDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // Method to save (insert or update) InvoicePaymentHistory
    @Override
    public int save(InvoicePaymentHistory paymentHistory) {
        try (Connection con = DbConnection.getConnection()) {
            if (paymentHistory.getId() > 0) {
                // Update existing payment history (without payment_method)
                String query = "UPDATE invoice_payment_history SET invoice_header_id=?, amount_paid=?, income=?, updated_at=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setInt(1, paymentHistory.getInvoice_header_id());
                    ps.setFloat(2, paymentHistory.getAmount_paid());
                    ps.setFloat(3, paymentHistory.getIncome());
                    ps.setTimestamp(4, paymentHistory.getUpdatedAt());
                    ps.setInt(5, paymentHistory.getId());

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        return paymentHistory.getId();
                    }
                }
            } else {
                // Insert new payment history (without payment_method)
                String query = "INSERT INTO invoice_payment_history (invoice_header_id, amount_paid, income, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, paymentHistory.getInvoice_header_id());
                    ps.setFloat(2, paymentHistory.getAmount_paid());
                    ps.setFloat(3, paymentHistory.getIncome());
                    ps.setTimestamp(4, paymentHistory.getCreatedAt());
                    ps.setTimestamp(5, paymentHistory.getUpdatedAt());

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        ResultSet generatedKeys = ps.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoicePaymentHistoryDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    // Method to delete an InvoicePaymentHistory by its ID
    @Override
    public boolean delete(int id) {
        try (Connection con = DbConnection.getConnection()) {
            String query = "DELETE FROM invoice_payment_history WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                int affectedRows = ps.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<InvoicePaymentHistory> findByClientId(int clientId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateTotals(InvoicePaymentHistory header) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<InvoicePaymentHistory> findByInvoiceHeaderId(int invoiceHeaderId) {
        List<InvoicePaymentHistory> paymentHistories = new ArrayList<>();
        String query = "SELECT * FROM invoice_payment_history WHERE invoice_header_id = ? ORDER BY created_at ASC";
        
        try (Connection con = DbConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, invoiceHeaderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                paymentHistories.add(extractInvoicePaymentHistoryFromResultSet(rs));
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoicePaymentHistoryDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return paymentHistories;
    }
}
