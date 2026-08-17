package com.depo.gui;

public class TopProductModel {
    private final String productName;
    private final int quantity;
    private final String totalValue;

    public TopProductModel(String productName, int quantity, String totalValue) {
        this.productName = productName;
        this.quantity = quantity;
        this.totalValue = totalValue;
    }

    public String getProductName() { 
        return productName; 
    }

    public int getQuantity() { 
        return quantity; 
    }

    public String getTotalValue() { 
        return totalValue; 
    }
}