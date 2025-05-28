package Data_Access_Object.user;

import Data_Access_Object.Client.Client;
import java.sql.Timestamp;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String password;
    private Timestamp created_at;
    private Timestamp updated_at;
    private List<Client> clients;  // This should be optional

    // Constructor without clients (for new users)
    public User(int id, String name, String password, Timestamp created_at, Timestamp updated_at) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    // Constructor with clients (for fetching existing users)
    public User(int id, String name, String password, Timestamp created_at, Timestamp updated_at, List<Client> clients) {
        this(id, name, password, created_at, updated_at);
        this.clients = clients;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "User{" + "id=" + id + ", name=" + name + ", password=" + password + 
               ", created_at=" + created_at + ", updated_at=" + updated_at + ", clients=" + clients + '}';
    }
}
