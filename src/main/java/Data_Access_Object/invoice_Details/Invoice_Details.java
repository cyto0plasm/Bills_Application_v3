package Data_Access_Object.invoice_Details;

import java.sql.Timestamp;

public class Invoice_Details {

    private int id;
    private String product;
    private float quantity;
    private float price;
    private float total;
private String PaymentMethod;
private int invoice_id;
    private Timestamp created_at;
    private Timestamp updated_at;

    public void setPaymentMethod(String PaymentMethod) {
        this.PaymentMethod = PaymentMethod;
    }

    public String getPaymentMethod() {
        return PaymentMethod;
    }
    

    public Invoice_Details() {
        this.id = 0;
        this.product = "";
        this.quantity = 0;
        this.price = 0;
        this.total = 0;
this.PaymentMethod="cash";
        this.created_at = new Timestamp(System.currentTimeMillis());
        this.updated_at = new Timestamp(System.currentTimeMillis());

    }

    public Invoice_Details(int id, String product, float quantity, float price, float total,String PaymentMethod, int invoice_id, Timestamp created_at, Timestamp updated_at) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
        this.PaymentMethod=PaymentMethod;

        this.invoice_id = invoice_id;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public float getQuantity() {
        return quantity;
    }

    public float getPrice() {
        return price;
    }

    public float getTotal() {
        return total;
    }

    public int getInvoiceId() {
        return invoice_id;
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

    public void setProduct(String product) {
        this.product = product;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public void setInvoiceid(int invoice_id) {
        this.invoice_id = invoice_id;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Invoice{" + "id=" + id + ", product=" + product + ", quantity=" + quantity + ", price=" + price + ", total=" + total  + ", created_at=" + created_at + ", updated_at=" + updated_at + '}';
    }

}
