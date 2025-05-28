package Data_Access_Object.Client;

import Data_Access_Object.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ClientDaoImp implements ClientDao {

    //private Connection con;

    public ClientDaoImp() {
//        try {
//            this.con = DbConnection.getConnection(); // Get connection from DbConnection
//        } catch (SQLException e) {
//            Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, null, e);
//            System.out.println("Database connection failed.");
//        }
    }

     @Override
    public List<Client> findAll(int userId) {
        List<Client> clientList = new LinkedList<>();
        String query = "SELECT * FROM client WHERE user_id = ?";
        
        try (Connection con = DbConnection.getConnection();  // Get fresh connection
             PreparedStatement ps = con.prepareStatement(query)) {
             
            ps.setInt(1, userId);
            try (ResultSet resultset = ps.executeQuery()) {
                while (resultset.next()) {
                    Client client = new Client(
                        resultset.getInt("id"),
                        resultset.getInt("user_id"),
                        resultset.getString("name"),
                        resultset.getString("phone"),
                        resultset.getTimestamp("created_at"),
                        resultset.getTimestamp("updated_at")
                    );
                    clientList.add(client);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, null, e);
        }
        return clientList;
    }


    @Override
public Client findById(int id, int userId) {
    String query = "SELECT * FROM client WHERE id=? AND user_id=?";
    try (Connection con = DbConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(query)) {
        
        ps.setInt(1, id);
        ps.setInt(2, userId);
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Client(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("phone"),
                    rs.getTimestamp("created_at"),
                    rs.getTimestamp("updated_at")
                );
            }
        }
    } catch (SQLException e) {
        Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, null, e);
    }

    return null;
}

    @Override
public void save(Client client) {
    // Basic validation with message boxes
    if (client.getName() == null || client.getPhone() == null || 
        client.getName().isBlank() || client.getPhone().isBlank()) {
        JOptionPane.showMessageDialog(null, 
            "يرجى إدخال جميع البيانات المطلوبة!", 
            "خطأ في الإدخال", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (!client.getPhone().matches("\\d{7,15}")) {
        JOptionPane.showMessageDialog(null, 
            "رقم الهاتف يجب أن يحتوي على أرقام فقط ويتراوح بين 7 و 15 رقمًا!", 
            "خطأ في الإدخال", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Get a NEW connection for each operation
    try (Connection con = DbConnection.getConnection()) {
        con.setAutoCommit(false); // Start transaction

        try {
            // Check if phone exists (for new clients)
            if (client.getId() <= 0) {
                String checkQuery = "SELECT id FROM client WHERE phone = ? AND user_id = ?";
                try (PreparedStatement checkStmt = con.prepareStatement(checkQuery)) {
                    checkStmt.setString(1, client.getPhone());
                    checkStmt.setInt(2, client.getUserId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(null, 
                                "رقم الهاتف موجود بالفعل!", 
                                "خطأ", 
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }

            // Prepare the main query
            String query;
            boolean isUpdate = client.getId() > 0;

            if (isUpdate) {
                query = "UPDATE client SET name=?, phone=?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=?";
            } else {
                query = "INSERT INTO client (name, phone, user_id) VALUES (?, ?, ?)";
            }

            // Execute the main operation
            try (PreparedStatement ps = con.prepareStatement(query, 
                    Statement.RETURN_GENERATED_KEYS)) {
                
                ps.setString(1, client.getName());
                ps.setString(2, client.getPhone());

                if (isUpdate) {
                    ps.setInt(3, client.getId());
                    ps.setInt(4, client.getUserId());
                } else {
                    ps.setInt(3, client.getUserId());
                }

                int affectedRows = ps.executeUpdate();
                if (affectedRows == 0) {
                    JOptionPane.showMessageDialog(null, 
                        "لم يتم تعديل أو إدخال أي صفوف.", 
                        "خطأ", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Get generated ID for new clients
                if (!isUpdate) {
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            client.setId(generatedKeys.getInt(1));
                        }
                    }
                }
            }

            con.commit(); // Commit transaction
            JOptionPane.showMessageDialog(null, 
                isUpdate ? "تم تحديث بيانات العميل بنجاح" : "تم إضافة العميل بنجاح", 
                "نجاح", 
                JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            try {
                con.rollback(); // Rollback on error
            } catch (SQLException rollbackEx) {
                Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, "خطأ أثناء التراجع عن العملية", rollbackEx);
            }
            Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, "خطأ أثناء حفظ العميل", e);
            JOptionPane.showMessageDialog(null, 
                "حدث خطأ في قاعدة البيانات أثناء حفظ العميل: " + e.getMessage(), 
                "خطأ في قاعدة البيانات", 
                JOptionPane.ERROR_MESSAGE);
        }
        
    } catch (SQLException e) {
        Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, "خطأ في الاتصال بقاعدة البيانات", e);
        JOptionPane.showMessageDialog(null, 
            "تعذر الاتصال بقاعدة البيانات: " + e.getMessage(), 
            "خطأ في الاتصال", 
            JOptionPane.ERROR_MESSAGE);
    }
}  
@Override
public void delete(int id, int userId) {
    try (Connection con = DbConnection.getConnection()) {
        con.setAutoCommit(false);
        String query = "DELETE FROM client WHERE id=? AND user_id=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                JOptionPane.showMessageDialog(null,
                    "لم يتم العثور على العميل المطلوب حذفه.",
                    "تحذير", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                    "تم حذف العميل بنجاح.",
                    "نجاح", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        con.commit();
    } catch (SQLException e) {
        Logger.getLogger(ClientDaoImp.class.getName()).log(Level.SEVERE, "Error deleting client", e);
        JOptionPane.showMessageDialog(null,
            "حدث خطأ أثناء حذف العميل: " + e.getMessage(),
            "خطأ", JOptionPane.ERROR_MESSAGE);
    }
}
}
