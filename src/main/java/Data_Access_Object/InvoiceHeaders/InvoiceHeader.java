package Data_Access_Object.InvoiceHeaders;

import java.sql.Timestamp;
import java.text.DecimalFormat;

public class InvoiceHeader {

    private int id;
    private int clientId;
    private float total_amount;
    private float income;
     private float firstIncome;
    private float remaining;
    private float discount;
    private String PaymentMethod;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public void setPaymentMethod(String PaymentMethod) {
        this.PaymentMethod = PaymentMethod;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }
    
    public InvoiceHeader() {
        // Initialize with default values
        this.id = 0;
        this.clientId = 0;
        this.total_amount = 0 ;
        this.income = 0;
              this.firstIncome = 0;
        this.remaining = 0;
        this.discount=0;
        this.PaymentMethod="cash";
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public InvoiceHeader(int id, int clientId, float total_amount, float income,float firstIncome, float remaining,float discount,String PaymentMethod, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.total_amount = total_amount;
        this.income = income;
        this.firstIncome = firstIncome;
        this.discount=discount;
        this.remaining = remaining;
        this.PaymentMethod=PaymentMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "InvoiceHeader{" + "id=" + id + ", clientId=" + clientId + ", totalAmount=" + total_amount + ", income=" + income + ", remaining=" + remaining + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setTotalAmount(float total_amount) {
        this.total_amount = total_amount;
    }

  
     public void setIncome(float income) {
        this.income = income;
        this.remaining = this.total_amount - this.income; 
    }
       public void setFirstIncome(float firstIncome) {
        this.firstIncome = firstIncome;
    }
 
    public void setRemaining(float remaining) {
        this.remaining = remaining;
    }
    public void setDiscount(float discount){
    this.discount=discount;
    }
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getClientId() {
        return clientId;
    }

    public float getTotalAmount() {
        return total_amount;
    }

    public float getIncome() {
        return income;
    }
       public float getFirstIncome() {
        return firstIncome;
    }

    public float getRemaining() {
        return remaining;
    }
    public float getDiscount(){
    return discount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

  

}
