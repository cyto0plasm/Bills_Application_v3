/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Access_Object.invoice_payment_history;

import Data_Access_Object.InvoiceHeaders.InvoiceHeader;
import Data_Access_Object.InvoiceHeaders.InvoiceHeaderDao;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Cyto
 */
public class InvoicePaymentService {
     private final InvoiceHeaderDao invoiceHeaderDao;
    private final InvoicePaymentHistoryDao paymentHistoryDao;

    public InvoicePaymentService(InvoiceHeaderDao headerDao, InvoicePaymentHistoryDao paymentDao) {
        this.invoiceHeaderDao = headerDao;
        this.paymentHistoryDao = paymentDao;
    }

public boolean applyPaymentToInvoices(int clientId, float paymentAmount, boolean applyToOldestFirst, String paymentMethod)
 {
    float remaining = paymentAmount;
    Timestamp now = new Timestamp(System.currentTimeMillis());
    boolean paymentApplied = false;

    // Fetch unpaid invoices
    List<InvoiceHeader> unpaidInvoices = invoiceHeaderDao.findByClientId(clientId);

    // Sort invoices based on the flag
    unpaidInvoices.sort(Comparator.comparing(InvoiceHeader::getCreatedAt));
    if (!applyToOldestFirst) {
        Collections.reverse(unpaidInvoices);
    }

    for (InvoiceHeader invoice : unpaidInvoices) {
        if (remaining <= 0) break;

        float invoiceRemaining = invoice.getRemaining();
        float toApply = Math.min(remaining, invoiceRemaining);

        if (toApply > 0) {
            // Save payment history
           InvoicePaymentHistory payment = new InvoicePaymentHistory();
payment.setInvoice_header_id(invoice.getId());
payment.setAmount_paid(toApply);
payment.setIncome(invoice.getIncome() + toApply);
payment.setPayment_method(paymentMethod); 
payment.setCreatedAt(now);
payment.setUpdatedAt(now);
paymentHistoryDao.save(payment);


            // Update invoice header
            invoice.setRemaining(invoiceRemaining - toApply);
            invoice.setIncome(invoice.getIncome() + toApply);
            invoice.setUpdatedAt(now);
            invoiceHeaderDao.save(invoice);

            remaining -= toApply;
            paymentApplied = true;
        }
    }

    return paymentApplied;
}

}
