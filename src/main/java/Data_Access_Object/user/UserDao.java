

package Data_Access_Object.user;

import java.util.List;


public interface UserDao  {
    public List <User> findAll();
    User findById(int id);
    void save (User user);
    void delete(int id);
}
