package com.depo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockMovement {
    private String id;  
    private String productId;
    private String productName;     
    private String movementType;  
    private int quantity;
    private String reason;     
    private int userId;   
    private String userName;      
    private String userRole;   
    private String details; 
    private LocalDateTime timestamp;

    public StockMovement() {
    }

    public StockMovement(String productId, String movementType, int quantity) {
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.timestamp = LocalDateTime.now(); 
    }
    
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
    // GETTERS & SETTERS
    // ==========================================
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getProductName() { 
        if (productName != null && !productName.trim().isEmpty()) {
            return productName;
        }
        return "Bilinmeyen Ürün"; 
    }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMovementType() { 
        if (this.movementType != null) {
            String typeUpper = this.movementType.toUpperCase().trim();
            if (typeUpper.equals("GÜNCELLEME") || typeUpper.equals("GİRİŞ")) {
                if (this.quantity > 0) {
                    return "STOCK IN";
                } else if (this.quantity < 0) {
                    return "STOCK OUT";
                } else {
                    return "GÜNCELLEME";
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

    public String getIslemiYapan() {
        if (userName != null && !userName.trim().isEmpty()) {
            String rolFormatli = (userRole != null && !userRole.trim().isEmpty()) ? " (" + userRole.toUpperCase() + ")" : "";
            return userName + rolFormatli;
        }
        return "Sistem Kullanıcısı"; 
    }

    public void setIslemiYapan(String islemiYapan) {
        if (islemiYapan != null && !islemiYapan.trim().isEmpty()) {
            this.userName = islemiYapan;
        }
    }

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
