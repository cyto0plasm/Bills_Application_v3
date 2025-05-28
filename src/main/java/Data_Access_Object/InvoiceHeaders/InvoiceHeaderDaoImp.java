package Data_Access_Object.InvoiceHeaders;

import Data_Access_Object.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InvoiceHeaderDaoImp implements InvoiceHeaderDao {
   public List<InvoiceHeader> findByFilters(Date startDate, Date endDate, double minAmount, double maxAmount, String paymentStatus) {
        List<InvoiceHeader> filteredInvoices = new ArrayList<>();
        String query = "SELECT * FROM invoice_header WHERE 1=1";

        if (startDate != null) {
            query += " AND created_at >= ?";
        }
        if (endDate != null) {
            query += " AND created_at <= ?";
        }
        if (minAmount != Double.MIN_VALUE) {
            query += " AND total_amount >= ?";
        }
        if (maxAmount != Double.MAX_VALUE) {
            query += " AND total_amount <= ?";
        }
        if (!paymentStatus.equals("الكل")) {
            query += " AND payment_status = ?";
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
            if (!paymentStatus.equals("الكل")) {
                stmt.setString(paramIndex, paymentStatus);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                filteredInvoices.add(extractInvoiceHeaderFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return filteredInvoices;
    }
public int getInvoiceItemCount(int invoiceId) {
String query = "SELECT COUNT(*) FROM invoice_details WHERE invoice_id = ?";
    
    try (Connection conn = DbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        
        stmt.setInt(1, invoiceId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

  private InvoiceHeader extractFromResultSet(ResultSet resultSet) throws SQLException {
    return new InvoiceHeader(
        resultSet.getInt("id"),
        resultSet.getInt("client_id"),
        resultSet.getFloat("total_amount"),
        resultSet.getFloat("income"),
        resultSet.getFloat("first_income"), 
        resultSet.getFloat("remaining"),
        resultSet.getFloat("discount"),
        resultSet.getString("payment_method"),
        resultSet.getTimestamp("created_at"),
        resultSet.getTimestamp("updated_at")
    );
}

private InvoiceHeader extractInvoiceHeaderFromResultSet(ResultSet rs) throws SQLException {
    InvoiceHeader invoiceHeader = new InvoiceHeader();
    invoiceHeader.setId(rs.getInt("id"));
    invoiceHeader.setClientId(rs.getInt("client_id"));
    invoiceHeader.setTotalAmount(rs.getFloat("total_amount"));
    invoiceHeader.setIncome(rs.getFloat("income"));
    invoiceHeader.setFirstIncome(rs.getFloat("first_income")); // ✅ added
    invoiceHeader.setRemaining(rs.getFloat("remaining"));
    invoiceHeader.setDiscount(rs.getFloat("discount"));
    invoiceHeader.setPaymentMethod(rs.getString("payment_method"));
    invoiceHeader.setCreatedAt(rs.getTimestamp("created_at"));
    invoiceHeader.setUpdatedAt(rs.getTimestamp("updated_at"));
    return invoiceHeader;
}
    @Override
    public List<InvoiceHeader> findAll() {
        List<InvoiceHeader> headerList = new LinkedList<>();
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return headerList;
            }

            String query = "SELECT * FROM invoice_header";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ResultSet resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    InvoiceHeader header = extractFromResultSet(resultSet);
                    headerList.add(header);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoiceHeaderDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return headerList;
    }

    @Override
    public InvoiceHeader findById(int id) {
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return null;
            }

            String query = "SELECT * FROM invoice_header WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ResultSet resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    return extractFromResultSet(resultSet);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoiceHeaderDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<InvoiceHeader> findByClientId(int clientId) {
        List<InvoiceHeader> invoices = new ArrayList<>();
        try (Connection con = DbConnection.getConnection()) {
            String query = "SELECT * FROM invoice_header WHERE client_id = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, clientId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    invoices.add(new InvoiceHeader(
                            rs.getInt("id"),
                            rs.getInt("client_id"),
                            rs.getFloat("total_amount"),
                            rs.getFloat("income"),
                            rs.getFloat("first_income"),
                            rs.getFloat("remaining"),
                            rs.getFloat("discount"),
                            rs.getString("payment_method"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("updated_at")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return invoices;
    }

   

   @Override
public int save(InvoiceHeader header) {
    try (Connection con = DbConnection.getConnection()) {
        if (con == null) {
            System.out.println("Database connection failed.");
            return -1;
        }

float calculatedRemaining = (header.getTotalAmount() - header.getDiscount()) - header.getIncome();
        header.setRemaining(calculatedRemaining); // ensure always correct

        if (header.getId() > 0) {
            // Existing invoice — check if first_income is already set
            String checkQuery = "SELECT first_income FROM invoice_header WHERE id = ?";
            try (PreparedStatement checkPs = con.prepareStatement(checkQuery)) {
                checkPs.setInt(1, header.getId());
                ResultSet rs = checkPs.executeQuery();

                if (rs.next()) {
                    float existingFirstIncome = rs.getFloat("first_income");

                    if (existingFirstIncome > 0) {
                        // Don't update first_income
                        String update = """
                            UPDATE invoice_header
                            SET client_id=?, total_amount=?, income=?, remaining=?, discount=?, payment_method=?
                            WHERE id=?
                        """;
                        try (PreparedStatement ps = con.prepareStatement(update)) {
                            ps.setInt(1, header.getClientId());
                            ps.setFloat(2, header.getTotalAmount());
                            ps.setFloat(3, header.getIncome());
                            ps.setFloat(4, header.getRemaining());
                            ps.setFloat(5, header.getDiscount());
                            ps.setString(6, header.getPaymentMethod());
                            ps.setInt(7, header.getId());

                            int affected = ps.executeUpdate();
                            if (affected > 0) {
                                return header.getId();
                            }
                        }
                    } else {
                        // Update including first_income
                        header.setFirstIncome(header.getIncome()); // First set income becomes first_income
                        String update = """
                            UPDATE invoice_header
                            SET client_id=?, total_amount=?, income=?, first_income=?, remaining=?, discount=?, payment_method=?
                            WHERE id=?
                        """;
                        try (PreparedStatement ps = con.prepareStatement(update)) {
                            ps.setInt(1, header.getClientId());
                            ps.setFloat(2, header.getTotalAmount());
                            ps.setFloat(3, header.getIncome());
                            ps.setFloat(4, header.getFirstIncome());
                            ps.setFloat(5, header.getRemaining());
                            ps.setFloat(6, header.getDiscount());
                            ps.setString(7, header.getPaymentMethod());
                            ps.setInt(8, header.getId());

                            int affected = ps.executeUpdate();
                            if (affected > 0) {
                                return header.getId();
                            }
                        }
                    }
                }
            }

        } else {
            // Insert new invoice
            header.setFirstIncome(header.getIncome()); // First-time income
            String insert = """
                INSERT INTO invoice_header (client_id, total_amount, income, first_income, remaining, discount, payment_method, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
            try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, header.getClientId());
                ps.setFloat(2, header.getTotalAmount());
                ps.setFloat(3, header.getIncome());
                ps.setFloat(4, header.getFirstIncome());
                ps.setFloat(5, header.getRemaining());
                ps.setFloat(6, header.getDiscount());
                ps.setString(7, header.getPaymentMethod());
ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));

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
        Logger.getLogger(InvoiceHeaderDaoImp.class.getName()).log(Level.SEVERE, null, ex);
    }
    return -1;
}
    @Override
    public void updateTotals(InvoiceHeader header) {
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return;
            }

            String query = "UPDATE invoice_header SET total_amount=?, income=?, remaining=? ,payment_method=? ,discount=? WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setFloat(1, header.getTotalAmount());
                ps.setFloat(2, header.getIncome());
                ps.setFloat(3, header.getRemaining());
                ps.setString(4, header.getPaymentMethod());
                ps.setFloat(5, header.getDiscount());
                ps.setInt(6, header.getId());

                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(InvoiceHeaderDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return false;
            }
            String query = "DELETE FROM invoice_header WHERE id=?";
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

}
