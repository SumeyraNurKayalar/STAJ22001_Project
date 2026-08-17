package com.depo.model;

public class Supplier {
    private int id;
    private String name;
    private String phoneNumber;

    public Supplier(int id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }

    public String getPhone() {
    return this.phoneNumber;
}

    @Override
    public String toString() { return name + " (" + phoneNumber + ")"; }
}