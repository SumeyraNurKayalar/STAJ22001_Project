package com.depo.model;

public class User {
    private int id; // GÜN 12 ENTEGRASYONU: Veritabanı ID takibi için eklendi
    private String username;
    private String password;
    private Role role;

    // Ana Constructor (App.java içindeki veritabanı girişinde çağrılan)
    public User(int id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Eski kodların veya testlerin patlamaması için Overload Constructor
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- GETTERS & SETTERS ---
    
    public int getId() { 
        return id; 
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() { 
        return username; 
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() { 
        return password; 
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() { 
        return role; 
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}