package com.example.myinventory.ui.credits;

import com.google.firebase.Timestamp;

public class Credit {
    String ItemName;
    String PersonName;
    String Phone;
    int ItemPrice;
    int ItemQuantity;
    int PartialPayments;
    Timestamp DueDate;
    String id;
    String itemId;
    int Unpaid;


    String shopId;
    public Credit() {
    }
    public Credit(String itemName, String personName, String phone, int itemPrice, int itemQuantity, int partialPayments, Timestamp dueDate, String id, int unpaid, String itemId, String shopId) {
        this.ItemName = itemName;
        this.PersonName = personName;
        this.Phone = phone;
        this.ItemPrice = itemPrice;
        this.ItemQuantity = itemQuantity;
        this.PartialPayments = partialPayments;
        this.DueDate = dueDate;
        this.id = id;
        this.Unpaid = unpaid;
        this.itemId = itemId;
        this.shopId = shopId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public String getPersonName() {
        return PersonName;
    }

    public void setPersonName(String personName) {
        PersonName = personName;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public int getItemPrice() {
        return ItemPrice;
    }

    public void setItemPrice(int itemPrice) {
        ItemPrice = itemPrice;
    }

    public int getItemQuantity() {
        return ItemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        ItemQuantity = itemQuantity;
    }

    public int getPartialPayments() {
        return PartialPayments;
    }

    public void setPartialPayments(int partialPayments) {
        PartialPayments = partialPayments;
    }

    public Timestamp getDueDate() {
        return DueDate;
    }

    public void setDueDate(Timestamp dueDate) {
        DueDate = dueDate;
    }

    public int getUnpaid() {
        return Unpaid;
    }

    public void setUnpaid(int unpaid) {
        Unpaid = unpaid;
    }


    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

}
