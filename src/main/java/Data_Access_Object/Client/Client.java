package Data_Access_Object.Client;

import java.sql.Timestamp;

public class Client {

    private int id;
    private int userId;
    private String name;
    private String phone;
    private Timestamp created_at;
    private Timestamp updated_at;

    public Client(int id, int userId, String name, String phone, Timestamp created_at, Timestamp updated_at) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

}
