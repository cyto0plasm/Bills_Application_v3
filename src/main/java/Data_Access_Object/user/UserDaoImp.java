package Data_Access_Object.user;

import Data_Access_Object.Client.Client;
import Data_Access_Object.DbConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Cyto
 */
public class UserDaoImp implements UserDao {

   @Override
public List<User> findAll() {
    List<User> userList = new LinkedList<>();
    try (Connection con = DbConnection.getConnection()) {
        if (con == null) {
            System.out.println("Database connection failed.");
            return null;
        }

        String query = "SELECT * FROM user";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                
                // Create user WITHOUT clients initially
                User user = new User(
                    userId,
                    resultSet.getString("name"),
                    resultSet.getString("password"),
                    resultSet.getTimestamp("created_at"),
                    resultSet.getTimestamp("updated_at")
                );

                // Load clients only if needed
                user.setClients(fetchClientsByUserId(userId, con));

                userList.add(user);
            }
        }
    } catch (SQLException e) {
        Logger.getLogger(UserDaoImp.class.getName()).log(Level.SEVERE, null, e);
    }
    return userList;
}

@Override
public User findById(int id) {
    try (Connection con = DbConnection.getConnection()) {
        if (con == null) {
            System.out.println("Database connection failed.");
            return null;
        }

        String query = "SELECT * FROM user WHERE id=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                User user = new User(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("password"),
                    resultSet.getTimestamp("created_at"),
                    resultSet.getTimestamp("updated_at")
                );

                // Load clients only if needed
                user.setClients(fetchClientsByUserId(id, con));

                return user;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}


    /**
     * Fetch clients belonging to a specific user.
     */
    private List<Client> fetchClientsByUserId(int userId, Connection con) {
        List<Client> clients = new LinkedList<>();
        String query = "SELECT * FROM client WHERE user_id=?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Client client = new Client(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("name"),
                        resultSet.getString("phone"),
                        resultSet.getTimestamp("created_at"),
                        resultSet.getTimestamp("updated_at")
                );
                clients.add(client);
            }
        } catch (SQLException e) {
            Logger.getLogger(UserDaoImp.class.getName()).log(Level.SEVERE, null, e);
        }
        return clients;
    }

    public void save(User user) {
        try (Connection con = DbConnection.getConnection()) {
            if (con == null) {
                System.out.println("Database connection failed.");
                return;
            }

            if (user.getName() == null || user.getPassword() == null) {
                System.out.println("User data is incomplete: " + user);
                return;
            }

            if (user.getId() > 0) {
                // Update
                String query = "UPDATE user SET name=?,   password=? WHERE id=?";
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setString(1, user.getName());

                    ps.setString(2, user.getPassword());
                    ps.setInt(3, user.getId());
                    ps.executeUpdate();
                    System.out.println("User UPDATED successfully.");
                }
            } else {
                // Insert
                String query = "INSERT INTO user (name,  password) VALUES (?,  ?)";
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getPassword());
                    System.out.println("Inserting User: " + user);
                    ps.executeUpdate();
                    System.out.println("User INSERTED successfully.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred while saving user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int id) {

        try {
            Connection con = DbConnection.getConnection();
            if (con == null) {
                return;
            }
            String query = "DELETE FROM user WHERE id=?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
