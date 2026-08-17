package com.depo.service;

import com.depo.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthService {

    // 💡 Geliştirilmiş Giriş Kontrolü: SQL tarafında LOWER kullanarak harf/büyüklük krizini kökten çözer.
    public boolean login(String username, String password) {
        // Veritabanındaki username alanını ve gelen parametreyi LOWER yaparak büyük-küçük harf duyarlılığını tamamen kaldırıyoruz.
        String query = "SELECT password_hash FROM users WHERE LOWER(username) = LOWER(?)";
        String inputHash = PasswordUtil.hashPassword(password).toLowerCase().trim();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String dbHash = rs.getString("password_hash").toLowerCase().trim();
                return dbHash.equals(inputHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 💡 YENİ YARDIMCI METOT: Giriş yapan kullanıcının detaylarını (id, role vb.) çekmek için şart paşam
    public UserDetails getUserDetails(String username) {
        // Burada da aynı şekilde LOWER(?) kullanarak kullanıcı adı eşleşmesini garanti altına alıyoruz.
        String query = "SELECT id, role FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("role");
                return new UserDetails(id, role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new UserDetails(0, "USER"); // Hata durumunda fallback
    }

    // Yeni Kullanıcı Ekleme (Register)
    public boolean register(String username, String password, String role) {
        String query = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        String passwordHash = PasswordUtil.hashPassword(password).toLowerCase().trim();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username.trim());
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 💡 Kullanıcı rollerini ve ID'sini taşımak için basit bir iç sınıf (Inner Class)
    public static class UserDetails {
        private final int id;
        private final String role;

        public UserDetails(int id, String role) {
            this.id = id;
            this.role = role;
        }

        public int getId() { return id; }
        public String getRole() { return role; }
    }

    // 💡 ŞİFREMİ UNUTTUM ENTEGRASYONU: Kullanıcı adı eşleşirse şifreyi güvenle hashleyip günceller.
    public boolean updatePassword(String username, String newPassword) {
        String query = "UPDATE users SET password_hash = ? WHERE LOWER(username) = LOWER(?)";
        String passwordHash = PasswordUtil.hashPassword(newPassword).toLowerCase().trim();

        try (Connection conn = com.depo.DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, passwordHash);
            stmt.setString(2, username.trim());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Kullanıcı veritabanında varsa ve güncellendiyse true döner
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}