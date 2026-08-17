package com.depo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockMovement {
    private String id;              // Veritabanındaki benzersiz Log ID'si için
    private String productId;
    private String productName;     // Arayüzde ürün ismini gösterebilmek için
    private String movementType;    // "GİRİŞ", "GÜNCELLEME", "ÇIKIŞ" vb.
    private int quantity;
    private String reason;          // İşlemin kısa açıklaması (Description)
    private int userId;             // İşlemi yapan kullanıcının ID'si
    private String userName;        // İşlemi yapan kişinin adı/kullanıcı adı
    private String userRole;        // İşlemi yapan kişinin görevi/rolü
    private String details;         // Eski/Yeni değer değişim detayları
    private LocalDateTime timestamp;

    // 🎯 0. BOŞ CONSTRUCTOR (Framework'ler, Servisler ve Manuel Mapping için ŞART)
    public StockMovement() {
    }

    // 1. ESKİ CONSTRUCTOR (Mevcut diğer kodların patlamaması için korundu)
    public StockMovement(String productId, String movementType, int quantity) {
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now(); 
    }
    
    // 2. YENİ CONSTRUCTOR (Veritabanından tüm audit log geçmişini eksiksiz çekmek için)
    public StockMovement(String id, String productId, String movementType, int quantity, String reason, int userId, String details, LocalDateTime timestamp) {
        this.id = id;
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.reason = reason;
        this.userId = userId;
        this.details = details;
        this.timestamp = timestamp;
    }

    // ==========================================
    // ENCAPSULATION (GETTERS & SETTERS)
    // ==========================================
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    // 🎯 AKILLI GETTER: ID veya herhangi başka bir bilgi yerine kesinlikle isim almasını sağlıyoruz.
    public String getProductName() { 
        if (productName != null && !productName.trim().isEmpty()) {
            return productName;
        }
        return "Bilinmeyen Ürün"; // ID basmak yerine metinsel isim veriyoruz
    }
    public void setProductName(String productName) { this.productName = productName; }

    // 🎯 AKILLI HAREKET TÜRÜ GETTER'I: 
    // GÜNCELLEME veya GİRİŞ yazan yerleri miktar pozitifse STOCK IN, negatifse STOCK OUT yapar.
    public String getMovementType() { 
        if (this.movementType != null) {
            String typeUpper = this.movementType.toUpperCase().trim();
            if (typeUpper.equals("GÜNCELLEME") || typeUpper.equals("GİRİŞ")) {
                if (this.quantity > 0) {
                    return "STOCK IN";
                } else if (this.quantity < 0) {
                    return "STOCK OUT";
                } else {
                    return "GÜNCELLEME"; // Değişim miktarı 0 ise sabit kalır
                }
            }
        }
        return movementType; 
    }
    public void setMovementType(String movementType) { this.movementType = movementType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /**
     * 🎯 SİHİRLİ METOT: TableView sütununun doğrudan "islemiYapan" adıyla bağlanabilmesi için.
     * Kullanıcı adı ve rolü birleştirerek "Nur (ADMIN)" formatında çıktı verir.
     */
    public String getIslemiYapan() {
        if (userName != null && !userName.trim().isEmpty()) {
            String rolFormatli = (userRole != null && !userRole.trim().isEmpty()) ? " (" + userRole.toUpperCase() + ")" : "";
            return userName + rolFormatli;
        }
        return "Sistem Kullanıcısı"; 
    }

    /**
     * 🎯 SETTER: Servis katmanından tek parça olarak işlem yapan verisi gönderilirse ayrıştırmak için.
     */
    public void setIslemiYapan(String islemiYapan) {
        if (islemiYapan != null && !islemiYapan.trim().isEmpty()) {
            this.userName = islemiYapan;
        }
    }

    // Ekranda ve konsolda tüm detayları basan güncel toString()
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String zamanMetni = (timestamp != null) ? timestamp.format(formatter) : "-";
        
        String detayMetni = (details != null && !details.isEmpty()) ? details : "Yok";
        String urunIsimMetni = getProductName(); 
        String personelMetni = getIslemiYapan(); 
        String akilliHareketTuru = getMovementType(); 

        return String.format("[%s] Ürün: %s (%s) | İşlem: %s | Miktar: %d | Yapan: %s\n" +
                             " └─ Açıklama: %s\n" +
                             " └─ Değişim Detayı: %s\n", 
                             zamanMetni, productId, urunIsimMetni, akilliHareketTuru, quantity, personelMetni, reason, detayMetni);
    }
}