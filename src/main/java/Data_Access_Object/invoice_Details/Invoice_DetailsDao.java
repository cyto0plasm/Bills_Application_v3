
package Data_Access_Object.invoice_Details;

import Data_Access_Object.Client.*;
import java.util.List;


public interface  Invoice_DetailsDao {
   
    public List <Invoice_Details> findAll();
    Invoice_Details findById(int id);
    boolean save (Invoice_Details invoice);
    void delete(int id);

}
