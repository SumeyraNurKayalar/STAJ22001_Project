package com.depo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.depo.model.Product;

public class DatabaseConfig {
    // DÜZELTME: URL içindeki çift port ve ters eğik çizgi karmaşası düzeltildi.
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DepoYonetimDB;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa"; 
    private static final String PASSWORD = "boolean1213"; 

    // APP.JAVA'NIN ARADIĞI METOT: Hataları tamamen çözen ekleme
    public static String getUrl() {
        return URL + "user=" + USER + ";password=" + PASSWORD + ";";
    }

    // Gelişmiş Bağlantı Yönetimi ve Checked Exception Bildirimi (throws)
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Sürücü kütüphanesi Maven'da eksikse veya yüklenemediyse yakalanır
            System.err.println("🚨 [KRİTİK HATA] SQL JDBC Sürücüsü (Driver) sınıf yolunda bulunamadı!");
            throw new SQLException("JDBC Driver eksikliği nedeniyle veritabanı bağlantısı kurulamadı.", e);
        }
    }

    public static List<Product> getLowStockProducts() {
        List<Product> lowStockList = new ArrayList<>();
        
        // Sütun isimleri MSSQL'deki gerçek PascalCase halleriyle güncellendi: Id, Name, Quantity...
        String query = "SELECT Id, Name, Quantity, Price, StorageLocation FROM dbo.Products WHERE Quantity <= 10";

        // Try-with-resources: conn, stmt ve rs nesnelerini otomatik kapatarak 'Memory Leak' engeller.
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Product product = new Product(
                    rs.getString("Id"),          
                    rs.getString("Name"),        
                    null, 
                    null, 
                    rs.getInt("Quantity"),       
                    rs.getDouble("Price"),       
                    rs.getString("StorageLocation") 
                );
                lowStockList.add(product);
            }
            
        } catch (SQLException e) {
            // Veritabanı katmanında oluşabilecek spesifik hataların yönetimi (Ağ kopması, şema uyuşmazlığı vb.)
            System.err.println("\n❌ [VERİTABANI HATASI] Kritik stok verileri işlenirken bir sorun oluştu!");
            System.err.println("⚠️ SQL State Kodu : " + e.getSQLState());
            System.err.println("🔍 Detaylı SQL Mesajı: " + e.getMessage());
            
        } catch (Exception e) {
            // Öngörülemeyen diğer tüm çalışma zamanı (Runtime) hataları için genel koruma kalkanı
            System.err.println("🚨 [BEKLENMEDİK SİSTEM HATASI]: " + e.getMessage());
        }
        
        return lowStockList; // Hata olsa dahi çökmez, güvenli modda boş liste döner
    }
}