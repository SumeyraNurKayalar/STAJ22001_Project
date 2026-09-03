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
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DepoYonetimDB;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa"; 
    private static final String PASSWORD = "boolean1213"; 

    public static String getUrl() {
        return URL + "user=" + USER + ";password=" + PASSWORD + ";";
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("🚨 [KRİTİK HATA] SQL JDBC Sürücüsü (Driver) sınıf yolunda bulunamadı!");
            throw new SQLException("JDBC Driver eksikliği nedeniyle veritabanı bağlantısı kurulamadı.", e);
        }
    }

    public static List<Product> getLowStockProducts() {
        List<Product> lowStockList = new ArrayList<>();
        String query = "SELECT Id, Name, Quantity, Price, StorageLocation FROM dbo.Products WHERE Quantity <= 10";

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
            System.err.println("\n❌ [VERİTABANI HATASI] Kritik stok verileri işlenirken bir sorun oluştu!");
            System.err.println("⚠️ SQL State Kodu : " + e.getSQLState());
            System.err.println("🔍 Detaylı SQL Mesajı: " + e.getMessage());
            
        } catch (Exception e) {
            System.err.println("🚨 [BEKLENMEDİK SİSTEM HATASI]: " + e.getMessage());
        }
        
        return lowStockList;
    }
}
