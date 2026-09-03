package com.depo.model;

public class StorageLocation {
    private String zone;
    private int aisle; 
    private String shelf;
    private int bin; 

    public StorageLocation(String zone, int aisle, String shelf, int bin) {
        this.zone = zone;
        this.aisle = aisle;
        this.shelf = shelf.toUpperCase();
        this.bin = bin;
    }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public int getAisle() { return aisle; }
    public void setAisle(int aisle) { this.aisle = aisle; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }

    public int getBin() { return bin; }
    public void setBin(int bin) { this.bin = bin; }

    public String getFullLocationCode() {
        return zone + "-K" + aisle + "-R" + shelf + "-G" + bin;
    }

    @Override
    public String toString() {
        return getFullLocationCode();
    }
}
