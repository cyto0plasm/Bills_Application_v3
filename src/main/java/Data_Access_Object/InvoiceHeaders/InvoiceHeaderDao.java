package Data_Access_Object.InvoiceHeaders;

import java.util.List;

public interface InvoiceHeaderDao {
    List<InvoiceHeader> findAll();
    List<InvoiceHeader> findByClientId(int clientId);
    InvoiceHeader findById(int id);
    int  save(InvoiceHeader invoiceHeader);
    boolean delete(int id);
    public void updateTotals(InvoiceHeader header);
}
