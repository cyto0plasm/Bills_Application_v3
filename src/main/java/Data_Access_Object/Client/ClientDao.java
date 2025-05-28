
package Data_Access_Object.Client;

import java.util.List;


public interface  ClientDao {
   
    public List <Client> findAll(int userId);
    Client findById(int id,int userId);
    void save (Client client);
    void delete(int id,int userId);

}
