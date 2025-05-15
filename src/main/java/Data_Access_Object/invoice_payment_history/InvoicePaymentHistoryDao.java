package Data_Access_Object.invoice_payment_history;

import Data_Access_Object.InvoiceHeaders.*;
import java.util.List;

public interface InvoicePaymentHistoryDao {
    List<InvoicePaymentHistory> findByInvoiceHeaderId(int invoiceHeaderId);

    List<InvoicePaymentHistory> findAll();
    List<InvoicePaymentHistory> findByClientId(int clientId);
    InvoicePaymentHistory findById(int id);
    int  save(InvoicePaymentHistory invoiceHeader);
    boolean delete(int id);
    public void updateTotals(InvoicePaymentHistory header);
}
