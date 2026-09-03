package com.depo.model;

public class Product {
    private String id;
    private String name;
    private Category category;
    private Supplier supplier;
    private int quantity;
    private double price;
    private String storageLocation;

    public Product(String id, String name, Category category, Supplier supplier, int quantity, double price, String storageLocation) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.supplier = supplier;
        this.quantity = quantity;
        this.price = price;
        this.storageLocation = storageLocation;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }

    @Override
    public String toString() {
        return "ID: " + id + " | Ad: " + name + " | Kategori: " + category + 
               " | Tedarikçi: " + supplier + " | Adet: " + quantity + 
               " | Fiyat: " + price + " TL | Konum: " + storageLocation;
    }

    public boolean isStockCritical() {
        return this.quantity <= 10;
    }
}
