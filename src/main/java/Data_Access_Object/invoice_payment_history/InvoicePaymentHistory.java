package Data_Access_Object.invoice_payment_history;

import Data_Access_Object.InvoiceHeaders.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;

public class InvoicePaymentHistory {

    private int id;
    private int invoice_header_id;
    private float amount_paid;
    private float income;
    private String payment_method;

    
    private Timestamp createdAt;
    private Timestamp updatedAt;
public InvoicePaymentHistory(){
 this.id = 0;
        this.invoice_header_id =0;
        this.amount_paid = 0;
        this.income = 0;
        this.payment_method="cash";
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
}
    public InvoicePaymentHistory(int id, int invoice_header_id, float amount_paid, float income, String payment_method,Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.invoice_header_id = invoice_header_id;
        this.amount_paid = amount_paid;
        this.income = income;
        this.payment_method=payment_method;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

public String getPayment_method() {
        return payment_method;
    }
    public int getId() {
        return id;
    }

    public int getInvoice_header_id() {
        return invoice_header_id;
    }

    public float getAmount_paid() {
        return amount_paid;
    }

    public float getIncome() {
        return income;
    }

   

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInvoice_header_id(int invoice_header_id) {
        this.invoice_header_id = invoice_header_id;
    }

    public void setAmount_paid(float amount_paid) {
        this.amount_paid = amount_paid;
    }

    public void setIncome(float income) {
        this.income = income;
    }

  

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "InvoicePaymentHistory{" + "id=" + id + ", invoice_header_id=" + invoice_header_id + ", amount_paid=" + amount_paid + ", income=" + income   + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }
    

   

}
