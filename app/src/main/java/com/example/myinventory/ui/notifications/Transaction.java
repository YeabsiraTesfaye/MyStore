package com.example.myinventory.ui.notifications;

import com.google.firebase.Timestamp;

public class Transaction {

    String item;
    String description;

    public String getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(String soldBy) {
        this.soldBy = soldBy;
    }

    String soldBy;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id, itemID, shopId;
    Timestamp date;
    int bought,sold,quantity,total, paid, unpaid;

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public int getUnpaid() {
        return unpaid;
    }

    public void setUnpaid(int unpaid) {
        this.unpaid = unpaid;
    }

    public Transaction() {
    }

    public Transaction(String item, String description, String soldBy, Timestamp date, int bought, int sold, int quantity, int total, int paid, int unpaid, String id, String itemID, String shopId) {
        this.item = item;
        this.description = description;
        this.soldBy = soldBy;
        this.date = date;
        this.bought = bought;
        this.sold = sold;
        this.quantity = quantity;
        this.total = total;
        this.paid = paid;
        this.unpaid = unpaid;
        this.id = id;
        this.itemID = itemID;
        this.shopId = shopId;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public int getBought() {
        return bought;
    }

    public void setBought(int bought) {
        this.bought = bought;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
}
