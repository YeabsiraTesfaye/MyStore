package com.example.myinventory.ui.home;

public class Items {
 
    // variables for storing our data.
    private String name, Description, ImageUri;
    Integer quantity, buy, sell;
 
    public Items() {
        // empty constructor
        // required for Firebase.
    }
 
    // Constructor for all variables.
    public Items(String name, String Description, Integer quantity, Integer buy, Integer sell, String ImageUri) {
        this.name = name;
        this.Description = Description;
        this.quantity = quantity;
        this.buy = buy;
        this.sell = sell;
        this.ImageUri = ImageUri;
    }
 
    // getter methods for all variables.
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public String getDescription() {
        return Description;
    }
 
    // setter method for all variables.
    public void setDescription(String Description) {
        this.Description = Description;
    }
 
    public Integer getQuantity() {
        return quantity;
    }
 
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getBuy(){return buy;}

    public void setBuy(Integer buy) {
        this.buy = buy;
    }

    public Integer getSell() {
        return sell;
    }
    public void setSell(Integer sell){
        this.sell = sell;
    }

    public String getUri() {
        return ImageUri;
    }

    public void setUri(String ImageUri) {
        this.ImageUri = ImageUri;
    }
}