package com.depo.model;

public class StorageLocation {
    private String zone;      // Örn: "A-Blok", "Soğuk Hava Deposu"
    private int aisle;        // Koridor Numarası (Örn: 5)
    private String shelf;     // Raf Harfi/Kodu (Örn: "C")
    private int bin;          // Rafın Göz/Kat Numarası (Örn: 2)

    public StorageLocation(String zone, int aisle, String shelf, int bin) {
        this.zone = zone;
        this.aisle = aisle;
        this.shelf = shelf.toUpperCase();
        this.bin = bin;
    }

    // Encapsulation (Getter / Setter)
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public int getAisle() { return aisle; }
    public void setAisle(int aisle) { this.aisle = aisle; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }

    public int getBin() { return bin; }
    public void setBin(int bin) { this.bin = bin; }

    // Kurumsal tam lokasyon kodu üretici (Örn: A-Blok-K5-R-C-G2)
    public String getFullLocationCode() {
        return zone + "-K" + aisle + "-R" + shelf + "-G" + bin;
    }

    @Override
    public String toString() {
        return getFullLocationCode();
    }
}