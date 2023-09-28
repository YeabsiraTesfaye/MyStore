package com.example.myinventory.ui.orders;

public class Order {

    public String order;
    public String orderedBy;
    public String date;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String phone;
    public int quantity;

    public Order() {
        // empty constructor
        // required for Firebase.
    }

    public Order(String order, String orderedBy, String phone, String date, int quantity){
        this.order = order;
        this.orderedBy = orderedBy;
        this.date = date;
        this.quantity = quantity;
        this.phone = phone;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderedBy() {
        return orderedBy;
    }

    public void setOrderedBy(String orderedBy) {
        this.orderedBy = orderedBy;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
