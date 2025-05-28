package Data_Access_Object.invoice_Details;

import Data_Access_Object.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Invoice_DetailsDaoImp implements Invoice_DetailsDao {
public boolean exists(int invoiceId, String product) {
    String query = "SELECT COUNT(*) FROM invoice_details WHERE invoice_id = ? AND product = ?";
    try (Connection conn = DbConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(query)) {
        stmt.setInt(1, invoiceId);
        stmt.setString(2, product);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

    @Override
    public List<Invoice_Details> findAll() {
        List<Invoice_Details> invoiceList = new LinkedList<>();
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return null;
            }

            String query = "SELECT * FROM invoice_details";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ResultSet resultset = ps.executeQuery();
                while (resultset.next()) {
                    Invoice_Details invoice = new Invoice_Details(
                        resultset.getInt("id"),
                        resultset.getString("product"),
                        resultset.getFloat("quantity"),
                        resultset.getFloat("price"),
                        resultset.getFloat("total"),
                        resultset.getString("payment_method"),
                        resultset.getInt("invoice_id"), // Fix: Reference invoice_id instead of client_id
                        resultset.getTimestamp("created_at"),
                        resultset.getTimestamp("updated_at")
                    );
                    invoiceList.add(invoice);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Invoice_DetailsDaoImp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return invoiceList;
    }
public List<Invoice_Details> findByInvoiceId(int invoiceId) {
    List<Invoice_Details> detailsList = new ArrayList<>();
    try (Connection con = DbConnection.getConnection()) {
        String query = "SELECT * FROM invoice_details WHERE invoice_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, invoiceId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                detailsList.add(new Invoice_Details(
                    rs.getInt("id"),
                    rs.getString("product"),
                    rs.getFloat("quantity"),
                    rs.getFloat("price"),
                    rs.getFloat("total"),
                        rs.getString("payment_method"),
                    rs.getInt("invoice_id"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
                ));
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return detailsList;
}

    @Override
    public Invoice_Details findById(int id) {
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return null;
            }

            String query = "SELECT * FROM invoice_details WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ResultSet resultset = ps.executeQuery();
                if (resultset.next()) {
                    return new Invoice_Details(
                        resultset.getInt("id"),
                        resultset.getString("product"),
                        resultset.getFloat("quantity"),
                        resultset.getFloat("price"),
                        resultset.getFloat("total"),
                            resultset.getString("payment_method"),
                        resultset.getInt("invoice_id"), // Fix
                        resultset.getTimestamp("created_at"),
                        resultset.getTimestamp("updated_at")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    @Override
    public boolean save(Invoice_Details invoice) {
        if (invoice == null) {
            System.out.println("Cannot save null invoice details");
            return false;
        }

        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            // Validate required fields
            if (invoice.getProduct() == null || invoice.getProduct().trim().isEmpty() || 
                invoice.getQuantity() <= 0.0f || invoice.getPrice() <= 0.0f || 
                invoice.getTotal() <= 0.0f || invoice.getInvoiceId() <= 0) {
                System.out.println("Invalid invoice data: " + invoice);
                return false;
            }

            // Start transaction
            con.setAutoCommit(false);
            try {
                boolean success;
                if (invoice.getId() > 0) {
                    // Update existing invoice detail
                    String query = "UPDATE invoice_details SET product=?, quantity=?, price=?, total=?,payment_method=?, invoice_id=?, updated_at=? WHERE id=?";
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, invoice.getProduct().trim());
                        ps.setFloat(2, invoice.getQuantity());
                        ps.setFloat(3, invoice.getPrice());
                        ps.setFloat(4, invoice.getTotal());
                        ps.setString(5, invoice.getPaymentMethod());
                        ps.setInt(6, invoice.getInvoiceId());
                        ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(8, invoice.getId());

                        success = ps.executeUpdate() > 0;
                        if (success) {
                            System.out.println("Invoice details UPDATED successfully for ID: " + invoice.getId());
                        }
                    }
                } else {
                    // Insert new invoice detail
                    String query = "INSERT INTO invoice_details (product, quantity, price, total, payment_method, invoice_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        Timestamp now = new Timestamp(System.currentTimeMillis());
                        ps.setString(1, invoice.getProduct().trim());
                        ps.setFloat(2, invoice.getQuantity());
                        ps.setFloat(3, invoice.getPrice());
                        ps.setFloat(4, invoice.getTotal());
                        ps.setString(5, invoice.getPaymentMethod());
                        ps.setInt(6, invoice.getInvoiceId());
                        ps.setTimestamp(7, now);
                        ps.setTimestamp(8, now);

                        success = ps.executeUpdate() > 0;
                        if (success) {
                            System.out.println("Invoice details INSERTED successfully");
                        }
                    }
                }

                if (success) {
                    con.commit();
                    return true;
                } else {
                    con.rollback();
                    return false;
                }

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Error while saving invoice details: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("Unexpected error while saving invoice details: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void delete(int id) {
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                return;
            }
            String query = "DELETE FROM invoice_details WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
