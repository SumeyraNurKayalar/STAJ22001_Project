package com.depo.service;

import com.depo.DatabaseConfig;
import com.depo.model.Category;
import com.depo.model.Product;
import com.depo.model.Supplier;
import com.depo.model.StockMovement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel; // 🎯 Seçim ekranı tablosu için eklendi
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap; 
import java.util.Map;
import java.util.stream.Collectors;

public class WarehouseService {

    // 👑 YENİ METOT: MEVCUT YAPILARI BOZMADAN SEÇİM EKRANI İÇİN TABLO MODELİ ÜRETİR
    public DefaultTableModel getAllProductsForSelection() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hücrelerin el ile düzenlenmesini engeller
            }
        };

        // Seçim ekranı kolon başlıkları
        model.addColumn("Ürün Kodu");
        model.addColumn("Ürün Adı");
        model.addColumn("Kategori");
        model.addColumn("Stok");

        // Sistemdeki mevcut ürünleri çekmek için kendi yazdığınız getAllProducts() metodunu kullanıyoruz 🎯
        List<Product> productList = getAllProducts();
        
        for (Product p : productList) {
            Object[] rowData = {
                p.getId(), // ProductCode
                p.getName(),
                (p.getCategory() != null) ? p.getCategory().getName() : "Genel",
                p.getQuantity()
            };
            model.addRow(rowData);
        }

        return model;
    }

    // 1. Yeni Ürün Tanımlama (Deadlock ve Donma Engellenmiş Güvenli Versiyon)
    public void addProduct(Product product, int aktifUserId) {
    String sqlProduct = "INSERT INTO Products (ProductCode, Name, CategoryId, SupplierId, Quantity, Price, Location) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    // 🎯 Sütunlarımızın SQL sırası: ProductId(1), MovementType(2), Quantity(3), Description(4), UserId(5), Details(6), MovementDate(7)
    String sqlLog = "INSERT INTO dbo.StockMovements (ProductId, MovementType, Quantity, Description, UserId, Details, MovementDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DatabaseConfig.getConnection()) {
        conn.setAutoCommit(false); // Transaction başlat

        // Bağlantıyı yardımcı metotlara paslıyoruz 🎯
        int checkedCategoryId = checkAndInsertCategoryWithConnection(conn, product.getCategory());
        int checkedSupplierId = checkAndInsertSupplierWithConnection(conn, product.getSupplier());

        try (PreparedStatement pstmtProd = conn.prepareStatement(sqlProduct, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement pstmtLog = conn.prepareStatement(sqlLog)) {
            
            pstmtProd.setString(1, product.getId()); 
            pstmtProd.setString(2, product.getName());
            pstmtProd.setInt(3, checkedCategoryId);
            pstmtProd.setInt(4, checkedSupplierId);
            pstmtProd.setInt(5, product.getQuantity());
            pstmtProd.setDouble(6, product.getPrice());
            pstmtProd.setString(7, product.getStorageLocation()); 
            
            int affectedRows = pstmtProd.executeUpdate();
            
            if (affectedRows > 0) {
                int generatedProductId = 1;
                try (ResultSet generatedKeys = pstmtProd.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedProductId = generatedKeys.getInt(1);
                    }
                }

                // 🎯 DOĞRU PARAMETRE EŞLEŞTİRMESİ (Sıralama SQL ile birebir eşitlendi):
                pstmtLog.setInt(1, generatedProductId);                                       // ProductId
                pstmtLog.setString(2, "GİRİŞ");                                               // MovementType
                pstmtLog.setInt(3, product.getQuantity());                                    // Quantity
                pstmtLog.setString(4, "Sisteme yeni ürün eklendi: " + product.getName());      // Description
                pstmtLog.setInt(5, aktifUserId);                                              // UserId
                pstmtLog.setString(6, "İlk Stok Girişi | Miktar: " + product.getQuantity());   // Details
                pstmtLog.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));     // MovementDate
                
                pstmtLog.executeUpdate();
                
                conn.commit(); // Başarılıysa tek seferde diske yaz
                System.out.println("✅ Ürün ve Log başarıyla kaydedildi.");
            } else {
                conn.rollback();
            }
            
        } catch (SQLException e) {
            conn.rollback(); // Hata oluşursa değişiklikleri geri al
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }

    } catch (SQLException e) {
        System.out.println("❌ Veritabanı Hatası: " + e.getMessage());
    }
}

public void addProduct(Product product) {
    // Eğer userId belirtilmemişse varsayılan olarak 1 (Admin/Sistem) id'si ile kaydeder
    addProduct(product, 1); 
}

    // --- DEADLOCK ENGELLEYEN BAĞLANTI PAYLAŞIMLI YARDIMCI METOTLAR ---

    private int checkAndInsertCategoryWithConnection(Connection conn, Category cat) throws SQLException {
        String checkSql = "SELECT Id FROM dbo.Categories WHERE Name = ?";
        String insertSql = "INSERT INTO dbo.Categories (Name) VALUES (?)"; 
        String categoryName = (cat != null && cat.getName() != null) ? cat.getName().trim() : "Genel";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, categoryName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Id");
                }
            }
        }

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, categoryName);
            insertStmt.executeUpdate();
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return 1;
    }

    private int checkAndInsertSupplierWithConnection(Connection conn, Supplier sup) throws SQLException {
        String checkSql = "SELECT Id FROM dbo.Suppliers WHERE Name = ?";
        String insertSql = "INSERT INTO dbo.Suppliers (Name, Phone) VALUES (?, ?)"; 
        String supplierName = (sup != null && sup.getName() != null) ? sup.getName().trim() : "Genel Tedarikçi";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, supplierName);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Id");
                }
            }
        }

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, supplierName);
            insertStmt.setString(2, (sup != null && sup.getPhoneNumber() != null) ? sup.getPhoneNumber().trim() : "000");
            insertStmt.executeUpdate();
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return 1;
    }

    // --- DİĞER STANDART METOTLAR (Donma Yapmayanlar) ---

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.Name as CatName, s.Name as SupName, s.Phone as SupPhone " +
                     "FROM Products p " +
                     "JOIN Categories c ON p.CategoryId = c.Id " +
                     "JOIN Suppliers s ON p.SupplierId = s.Id";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category cat = new Category(rs.getInt("CategoryId"), rs.getString("CatName"));
                Supplier sup = new Supplier(rs.getInt("SupplierId"), rs.getString("SupName"), rs.getString("SupPhone"));
                
                Product p = new Product(
                    rs.getString("ProductCode"), 
                    rs.getString("Name"),
                    cat, sup,
                    rs.getInt("Quantity"),
                    rs.getDouble("Price"),
                    rs.getString("Location") 
                );
                products.add(p);
            }
        } catch (SQLException e) {
            System.out.println("❌ Liste çekilirken DB hatası: " + e.getMessage());
        }
        return products;
    }

    public Product getProductById(String productCode) {
        String sql = "SELECT p.*, c.Name as CatName, s.Name as SupName, s.Phone as SupPhone " +
                     "FROM Products p " +
                     "JOIN Categories c ON p.CategoryId = c.Id " +
                     "JOIN Suppliers s ON p.SupplierId = s.Id WHERE p.ProductCode = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Category cat = new Category(rs.getInt("CategoryId"), rs.getString("CatName"));
                    Supplier sup = new Supplier(rs.getInt("SupplierId"), rs.getString("SupName"), rs.getString("SupPhone"));
                    return new Product(rs.getString("ProductCode"), rs.getString("Name"), cat, sup, 
                                       rs.getInt("Quantity"), rs.getDouble("Price"), rs.getString("Location"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Ürün aranırken DB hatası: " + e.getMessage());
        }
        return null;
    }

    public int getLatestProductIdByCode(String productCode) {
    // Sütun adı tam olarak veritabanındaki gibi 'Id' yapıldı 🎯
    String sql = "SELECT Id FROM Products WHERE ProductCode = ?";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, productCode);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("Id"); 
            }
        }
    } catch (SQLException e) {
        System.out.println("❌ Product ID çekilirken hata oluştu: " + e.getMessage());
    }
    return -1; // Hatalı durumu yakalamak için -1 dönüyoruz
}

    public boolean updateProduct(String productCode, Product newProduct, int aktifUserId) {
    Product oldProduct = getProductById(productCode);
    if (oldProduct == null) {
        System.out.println("⚠️ Güncellenmek istenen ürün bulunamadı: " + productCode);
        return false;
    }

    int realProductId = getLatestProductIdByCode(productCode);

    try (Connection conn = DatabaseConfig.getConnection()) {
        conn.setAutoCommit(false); // Transaction başlat 🎯

        // Bağlantıyı kaybetmeden kategori ve tedarikçi kontrolünü yapıyoruz
        int checkedCategoryId = checkAndInsertCategoryWithConnection(conn, newProduct.getCategory());
        int checkedSupplierId = checkAndInsertSupplierWithConnection(conn, newProduct.getSupplier());

        String sqlUpdate = "UPDATE Products SET Name = ?, CategoryId = ?, SupplierId = ?, Quantity = ?, Price = ?, Location = ? WHERE ProductCode = ?";
        String sqlLog = "INSERT INTO dbo.StockMovements (ProductId, MovementType, Quantity, Description, UserId, Details, MovementDate) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate);
             PreparedStatement pstmtLog = conn.prepareStatement(sqlLog)) {
            
            // 1. Ürün Güncelleme Parametreleri
            pstmt.setString(1, newProduct.getName());
            pstmt.setInt(2, checkedCategoryId);
            pstmt.setInt(3, checkedSupplierId);
            pstmt.setInt(4, newProduct.getQuantity());
            pstmt.setDouble(5, newProduct.getPrice());
            pstmt.setString(6, newProduct.getStorageLocation());
            pstmt.setString(7, productCode);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Değişim log detaylarını güvenli string formatı ile hazırlıyoruz (NullPointerException korumalı)
                String oldName = oldProduct.getName() != null ? oldProduct.getName() : "Belirsiz";
                String newName = newProduct.getName() != null ? newProduct.getName() : "Belirsiz";
                String oldLoc = oldProduct.getStorageLocation() != null ? oldProduct.getStorageLocation() : "Yok";
                String newLoc = newProduct.getStorageLocation() != null ? newProduct.getStorageLocation() : "Yok";

                String detaylar = String.format(
                    "İsim: [%s -> %s] | Miktar: [%d -> %d] | Fiyat: [%.2f -> %.2f] | Konum: [%s -> %s]", 
                    oldName, newName,
                    oldProduct.getQuantity(), newProduct.getQuantity(),
                    oldProduct.getPrice(), newProduct.getPrice(),
                    oldLoc, newLoc
                );
                
                // Stok miktarındaki değişimi hesaplıyoruz (Mevcut stok - Eski stok)
                int miktarFarki = newProduct.getQuantity() - oldProduct.getQuantity();

                // 2. Log Ekleme Parametreleri (Parametreler SQL sırasıyla birebir eşlendi)
                pstmtLog.setInt(1, realProductId);                                              // ProductId
                pstmtLog.setString(2, "GÜNCELLEME");                                            // MovementType
                pstmtLog.setInt(3, miktarFarki);                                                // Quantity (Yapılan net stok değişimi)
                pstmtLog.setString(4, "Ürün bilgileri güncellendi: " + newName);                // Description
                pstmtLog.setInt(5, aktifUserId);                                                // UserId
                pstmtLog.setString(6, detaylar);                                                // Details
                pstmtLog.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));       // MovementDate
                
                pstmtLog.executeUpdate();

                conn.commit(); // Her iki işlem de başarılıysa veritabanına tek seferde yaz 🚀
                System.out.println("✅ Ürün başarıyla güncellendi ve sistem günlüğüne kaydedildi.");
                return true;
            } else {
                conn.rollback(); // Güncelleme başarısızsa değişiklikleri geri al
            }
            
        } catch (SQLException e) {
            conn.rollback(); // Herhangi bir SQL hatasında işlemi güvenle geri sar
            System.out.println("❌ Güncelleme sırasında SQL Hatası: " + e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (SQLException e) {
        System.out.println("❌ Güncelleme bağlantı hatası: " + e.getMessage());
    }
    return false;
}


    public boolean stockIn(String productCode, int quantity, int aktifUserId) {
    Product p = getProductById(productCode);
    if (p == null || quantity <= 0) return false;
    String updateSql = "UPDATE Products SET Quantity = Quantity + ? WHERE ProductCode = ?";
    try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
        pstmt.setInt(1, quantity); 
        pstmt.setString(2, productCode); 
        pstmt.executeUpdate();
        
        // Buranın tam doğru ID'yi ve dbo.StockMovements tablosunu tetiklediğinden emin oluyoruz
        saveMovement(getLatestProductIdByCode(productCode), "GİRİŞ", quantity, "Manuel giriş.", aktifUserId, "Stok arttı.");
        return true;
    } catch (SQLException e) {
        System.out.println("❌ Stok Giriş Hatası: " + e.getMessage());
    }
    return false;
}

    public boolean stockOut(String productCode, int quantity, int aktifUserId) {
        Product p = getProductById(productCode);
        if (p == null || quantity <= 0 || p.getQuantity() < quantity) return false;

        String updateSql = "UPDATE Products SET Quantity = Quantity - ? WHERE ProductCode = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            
            pstmt.setInt(1, quantity);
            pstmt.setString(2, productCode);
            pstmt.executeUpdate();

            String detaylar = "Eski Stok: " + p.getQuantity() + " -> Yeni Stok: " + (p.getQuantity() - quantity);
            int intProdId = getLatestProductIdByCode(productCode);
            saveMovement(intProdId, "ÇIKIŞ", quantity, "Manuel çıkış.", aktifUserId, detaylar);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Stok Çıkış hatası: " + e.getMessage());
        }
        return false;
    }

    public List<Product> getCriticalStockProducts() {
        List<Product> criticalList = new ArrayList<>();
        for (Product p : getAllProducts()) {
            if (p.getQuantity() < 5) {
                criticalList.add(p);
            }
        }
        return criticalList;
    }

    public List<StockMovement> getMovementHistory() {
    List<StockMovement> list = new ArrayList<>();
    
    // 🎯 SSMS'ten doğruladığımız gerçek kolon adlarıyla yazılmış pürüzsüz SQL sorgusu
    String sql = "SELECT sm.Id, sm.ProductId, sm.MovementType, sm.Quantity, sm.Description, sm.UserId, sm.Details, sm.MovementDate, " +
                 "       p.Name AS productName, " +
                 "       u.username AS dbUserName, " +
                 "       u.role AS dbUserRole " +
                 "FROM dbo.StockMovements sm " +
                 "LEFT JOIN dbo.Products p ON sm.ProductId = p.Id " +
                 "LEFT JOIN dbo.users u ON sm.UserId = u.id " + 
                 "ORDER BY sm.MovementDate DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            String descriptionAsReason = rs.getString("Description");
            
            StockMovement sm = new StockMovement(
                String.valueOf(rs.getInt("Id")), 
                String.valueOf(rs.getInt("ProductId")),
                rs.getString("MovementType"), 
                rs.getInt("Quantity"),
                descriptionAsReason != null ? descriptionAsReason : "", 
                rs.getInt("UserId"),
                rs.getString("Details"), 
                rs.getTimestamp("MovementDate") != null ? rs.getTimestamp("MovementDate").toLocalDateTime() : java.time.LocalDateTime.now()
            );
            
            // Veritabanından gelen gerçek isimleri modele dolduruyoruz
            String pName = rs.getString("productName");
            sm.setProductName(pName != null && !pName.isEmpty() ? pName : "Ürün ID: " + sm.getProductId()); 

            String uName = rs.getString("dbUserName");
            sm.setUserName(uName != null && !uName.isEmpty() ? uName : "Kullanıcı ID: " + sm.getUserId());

            String uRole = rs.getString("dbUserRole");
            sm.setUserRole(uRole != null && !uRole.isEmpty() ? uRole : "USER");
            
            list.add(sm);
        }
        System.out.println("🚀 [MÜKEMMEL] Sistem günlükleri ANA sorguyla tamamen hatasız yüklendi. Kayıt: " + list.size());
        
    } catch (SQLException e) {
        // Artık DatabaseName düzgün ayarlandığı için bu catch bloğuna neredeyse hiç düşmeyeceksin!
        System.out.println("❌ Kritik SQL Hatası: " + e.getMessage());
    }
    return list;
}

    public String exportDailyMovementReportToCSV() throws Exception {
        String tarihDamgasi = new SimpleDateFormat("yyyy_MM_dd").format(new Date());
        String dosyaAdi = "sistem_detayli_denetim_raporu_" + tarihDamgasi + ".csv";

        String sql = "SELECT sm.Id, sm.ProductId, sm.MovementType, sm.Quantity, sm.Description, " +
                     "u.username, u.role, sm.Details, sm.MovementDate " +
                     "FROM StockMovements sm " +
                     "LEFT JOIN users u ON sm.UserId = u.id " +
                     "ORDER BY sm.MovementDate DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery();
             BufferedWriter writer = new BufferedWriter(new FileWriter(dosyaAdi))) {

            writer.write("Log ID;Ürün ID;İşlem Tipi;Miktar;Açıklama;İşlemi Yapan;Kullanıcı Rolü;Değişim Detayları;Tarih/Saat");
            writer.newLine();

            while (rs.next()) {
                String id = String.valueOf(rs.getInt("Id"));
                String prodId = String.valueOf(rs.getInt("ProductId"));
                String rawType = rs.getString("MovementType"); 
                int qty = rs.getInt("Quantity");
                String reason = rs.getString("Description");
                String username = rs.getString("username");
                String role = rs.getString("role");
                String details = rs.getString("Details");
                Timestamp ts = rs.getTimestamp("MovementDate");

                writer.write(String.format("%s;%s;%s;%d;%s;%s;%s;%s;%s", 
                        id, prodId, rawType, qty, reason, username, role, details, ts));
                writer.newLine();
            }
            writer.flush();
        }
        return dosyaAdi;
    }

    public int getTotalProductQuantity() {
        String sql = "SELECT SUM(Quantity) AS Total FROM Products";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt("Total");
        } catch (SQLException e) {/**/}
        return 0;
    }

    public Map<String, Integer> getCategoryStockDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT c.Name AS KategoriAdi, SUM(p.Quantity) AS ToplamStok FROM Products p JOIN Categories c ON p.CategoryId = c.Id GROUP BY c.Name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                distribution.put(rs.getString("KategoriAdi"), rs.getInt("ToplamStok"));
            }
        } catch (SQLException e) {/**/}
        return distribution;
    }

    public List<StockMovement> getRecentMovementsForChart() {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT TOP 5 sm.MovementType, sm.Quantity, p.Name AS productName FROM dbo.StockMovements sm LEFT JOIN Products p ON sm.ProductId = p.Id ORDER BY sm.MovementDate DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                StockMovement sm = new StockMovement(null, null, rs.getString("MovementType"), rs.getInt("Quantity"), null, 0, null, null);
                sm.setProductName(rs.getString("productName"));
                list.add(sm);
            }
        } catch (SQLException e) {/**/}
        return list;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> results = new ArrayList<>();
        String sql = "SELECT p.*, c.Name as CatName, s.Name as SupName, s.Phone as SupPhone " +
                     "FROM Products p " +
                     "JOIN Categories c ON p.CategoryId = c.Id " +
                     "JOIN Suppliers s ON p.SupplierId = s.Id " +
                     "WHERE p.Name LIKE ? OR p.ProductCode LIKE ? OR p.Location LIKE ? OR c.Name LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String wildCardKeyword = "%" + keyword + "%";
            pstmt.setString(1, wildCardKeyword);
            pstmt.setString(2, wildCardKeyword);
            pstmt.setString(3, wildCardKeyword);
            pstmt.setString(4, wildCardKeyword);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category cat = new Category(rs.getInt("CategoryId"), rs.getString("CatName"));
                    Supplier sup = new Supplier(rs.getInt("SupplierId"), rs.getString("SupName"), rs.getString("SupPhone"));
                    results.add(new Product(rs.getString("ProductCode"), rs.getString("Name"), cat, sup, rs.getInt("Quantity"), rs.getDouble("Price"), rs.getString("Location")));
                }
            }
        } catch (SQLException e) {/**/}
        return results;
    }

    private void saveMovement(int prodId, String type, int qty, String reason, int userId, String details) {
    if (prodId == -1) {
        System.out.println("❌ Hatalı Product ID (-1) nedeniyle log kaydedilmedi.");
        return;
    }
    String sql = "INSERT INTO dbo.StockMovements (ProductId, MovementType, Quantity, Description, UserId, Details, MovementDate) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, prodId);
        pstmt.setString(2, type); 
        pstmt.setInt(3, qty);
        pstmt.setString(4, reason);
        pstmt.setInt(5, userId);
        pstmt.setString(6, details);
        pstmt.setTimestamp(7, java.sql.Timestamp.valueOf(LocalDateTime.now()));
        pstmt.executeUpdate();
        System.out.println("✅ Hareket kaydı başarıyla eklendi.");
    } catch (SQLException e) {
        System.out.println("❌ Log kaydedilirken hata oluştu: " + e.getMessage());
    }
}

    public Category getOrCreateCategoryByName(String categoryName) {
        String selectSql = "SELECT Id FROM Categories WHERE Name = ?";
        
        try (Connection conn = DatabaseConfig.getConnection(); 
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            
            selectStmt.setString(1, categoryName);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("Id"), categoryName);
                }
            }
        } catch (SQLException e) { 
            System.out.println("🚨 Categories tablosu sorgulanırken hata oluştu: " + e.getMessage());
        }

        String insertSql = "INSERT INTO Categories (Name) VALUES (?)";
        try (Connection conn = DatabaseConfig.getConnection(); 
             PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            
            insertStmt.setString(1, categoryName);
            insertStmt.executeUpdate();
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return new Category(generatedKeys.getInt(1), categoryName);
                }
            }
        } catch (SQLException e) { 
            System.out.println("🚨 Categories tablosuna ekleme yapılırken hata oluştu: " + e.getMessage());
        }

        return new Category(1, "Genel"); 
    }

    // 🎯 YENİ EKLENEN VE HALIHAZIRDAKİ "DatabaseConfig" YAPISINA TAM UYUMLU METOTLAR:
   public void addStockMovement(String productCode, String movementType, int quantity, String description, int userId) throws Exception {
    // 1. Ürünü buluyoruz
    Product product = getProductById(productCode); 
    if (product == null) {
        throw new Exception("Ürün bulunamadı!");
    }

    // 2. Miktar ve Stok Hesaplaması
    int mutlakMiktar = Math.abs(quantity); 
    int yeniStok = product.getQuantity();

    if ("STOCK_IN".equalsIgnoreCase(movementType) || "STOK_GİRİŞ".equalsIgnoreCase(movementType)) {
        yeniStok += mutlakMiktar; 
    } else if ("STOCK_OUT".equalsIgnoreCase(movementType) || "STOK_ÇIKIŞ".equalsIgnoreCase(movementType)) {
        if (yeniStok < mutlakMiktar) {
            throw new Exception("Yetersiz stok miktarı! Mevcut stok: " + yeniStok);
        }
        yeniStok -= mutlakMiktar; 
    }
    
    // 3. Ürün Stok Adedini Güncelleme
    product.setQuantity(yeniStok);
    updateProduct(productCode, product, userId); 

    // 🎯 4. PRODUCTS TABLOSUNDAN GERÇEK SAYISAL ID'Yİ ALMA (Gerçek Sütun Adı: ProductCode)
    int gercekSayisalId = -1;
    String idSorgusu = "SELECT Id FROM dbo.Products WHERE ProductCode = ? OR CAST(Id AS VARCHAR) = ?"; 

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(idSorgusu)) {
        
        pstmt.setString(1, productCode);
        pstmt.setString(2, productCode);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                gercekSayisalId = rs.getInt("Id");
            }
        }
    }

    if (gercekSayisalId == -1) {
        throw new Exception("Ürüne ait veritabanı referans ID'si bulunamadı!");
    }

    // 🎯 5. STOCKMOVEMENTS TABLOSUNA LOG KAYDI (Birebir Veritabanı Sütun İsimleri)
    String insertQuery = "INSERT INTO dbo.StockMovements (ProductId, UserId, MovementType, Quantity, MovementDate, Description, Details) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    java.time.LocalDateTime suAnkiZaman = java.time.LocalDateTime.now();

    try (Connection conn = DatabaseConfig.getConnection(); 
         PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
        
        stmt.setInt(1, gercekSayisalId);
        stmt.setInt(2, userId);
        
        if ("STOCK_IN".equalsIgnoreCase(movementType) || "STOK_GİRİŞ".equalsIgnoreCase(movementType)) {
            stmt.setString(3, "STOK_GİRİŞ");
            stmt.setInt(4, mutlakMiktar); 
        } else {
            stmt.setString(3, "STOK_ÇIKIŞ");
            stmt.setInt(4, -mutlakMiktar); 
        }
        
        stmt.setTimestamp(5, java.sql.Timestamp.valueOf(suAnkiZaman));
        stmt.setString(6, (description == null || description.trim().isEmpty()) ? "Stok Hareketi" : description);
        stmt.setString(7, "Mevcut Stok Değişimi");
        
        stmt.executeUpdate();
    }
}

    // 2. DÜZELTİLMİŞ LOG LİSTELEME METODU
    

        public int getCriticalStockCount() {
        // 🎯 Veritabanındaki Products tablosunda stok miktarı (Quantity) 10 veya daha az olan ürünleri sayar.
        // Eğer projendeki kritik sınır farklıysa '10' sayısını değiştirebilirsin.
        String sql = "SELECT COUNT(*) AS kritikAdet FROM dbo.Products WHERE Quantity <= 10";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("kritikAdet");
            }
        } catch (SQLException e) {
            System.out.println("❌ Kritik stok sayısı veritabanından çekilemedi: " + e.getMessage());
        }
        return 0; // Hata durumunda sistemi çökertmemek için 0 dönüyoruz
    }

    public boolean saveStockMovement(int productId, String movementType, int quantity, String description, int userId) {
    // Sütun ve tablo isimlerini veritabanı şemana (Id ve StockMovements) göre eşliyoruz:
    String sql = "INSERT INTO dbo.StockMovements (ProductId, MovementType, Quantity, Description, UserId, MovementDate) " +
                 "VALUES (?, ?, ?, ?, ?, GETDATE())";
                 
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
        pstmt.setInt(1, productId);
        pstmt.setString(2, movementType);
        pstmt.setInt(3, quantity);
        pstmt.setString(4, description);
        pstmt.setInt(5, userId);
        
        int affectedRows = pstmt.executeUpdate();
        return affectedRows > 0;
    } catch (SQLException e) {
        System.out.println("❌ Stok hareketi kaydedilirken hata oluştu: " + e.getMessage());
        return false;
    }
}

public List<StockMovement> getAllStockMovements() {
    List<StockMovement> list = new ArrayList<>();
    
    // 🟢 Görsellerdeki MSSQL şemasıyla %100 birebir uyumlu hale getirildi:
    // - StockMovements: sm.Id, sm.ProductId, sm.UserId, sm.MovementType, sm.Quantity, sm.Description, sm.Details, sm.MovementDate
    // - Products: p.Id, p.Name
    // - users: u.id, u.username, u.role (Küçük harflere dikkat edildi)
    String sql = "SELECT sm.Id AS log_id, sm.ProductId, p.Name AS ProdName, sm.MovementType, " +
                 "sm.Quantity, sm.Description AS Reason, sm.UserId, u.username AS OpName, " +
                 "u.role AS OpRole, sm.Details AS Details, sm.MovementDate " +
                 "FROM dbo.StockMovements sm " +
                 "LEFT JOIN dbo.Products p ON sm.ProductId = p.Id " +
                 "LEFT JOIN dbo.users u ON sm.UserId = u.id " +
                 "ORDER BY sm.MovementDate DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            // Gerçek log ID'si veritabanından çekiliyor
            String id = String.valueOf(rs.getInt("log_id")); 
            
            String productId = String.valueOf(rs.getInt("ProductId"));
            String movementType = rs.getString("MovementType");
            int quantity = rs.getInt("Quantity");
            String reason = rs.getString("Reason");
            int userId = rs.getInt("UserId");
            String details = rs.getString("Details");
            
            LocalDateTime timestamp = null;
            if (rs.getTimestamp("MovementDate") != null) {
                timestamp = rs.getTimestamp("MovementDate").toLocalDateTime();
            }

            // Model nesnemizi orijinal yapısıyla ayağa kaldırıyoruz
            StockMovement sm = new StockMovement(id, productId, movementType, quantity, reason, userId, details, timestamp);
            
            String pName = rs.getString("ProdName");
            sm.setProductName(pName != null ? pName : "Bilinmeyen Ürün");
            
            String uName = rs.getString("OpName");
            sm.setUserName(uName != null ? uName : "Sistem");
            
            String uRole = rs.getString("OpRole");
            sm.setUserRole(uRole != null ? uRole : "USER");

            list.add(sm);
        }
        System.out.println("🚀 [BAŞARILI] Loglar ana sorguyla çekildi. Toplam kayıt: " + list.size());

    } catch (SQLException e) {
        System.out.println("❌ Ana sorgu başarısız oldu! Hata: " + e.getMessage());
        System.out.println("⚠️ Güvenli yedek kurtarma sorgusu doğrudan devreye alınıyor...");
        
        // Users tablosuna dokunmayan, sadece StockMovements ve Products tablolarını bağlayan yedek sorgu:
        String fallbackSql = "SELECT sm.Id AS fallback_id, sm.ProductId, sm.MovementType, sm.Quantity, sm.Description, sm.UserId, sm.Details, sm.MovementDate, " +
                             "       p.Name AS productName " +
                             "FROM dbo.StockMovements sm " +
                             "LEFT JOIN dbo.Products p ON sm.ProductId = p.Id " +
                             "ORDER BY sm.MovementDate DESC";
                             
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(fallbackSql);
             ResultSet rs = pstmt.executeQuery()) {
             
            list.clear(); // Listeyi sıfırla ki mükerrer kayıt olmasın
            int sanalId = 1;

            while (rs.next()) {
                String descriptionAsReason = rs.getString("Description");
                
                StockMovement sm = new StockMovement(
                    String.valueOf(sanalId++), 
                    String.valueOf(rs.getInt("ProductId")), 
                    rs.getString("MovementType"), 
                    rs.getInt("Quantity"), 
                    descriptionAsReason != null ? descriptionAsReason : "",
                    rs.getInt("UserId"), 
                    rs.getString("Details"), 
                    rs.getTimestamp("MovementDate") != null ? rs.getTimestamp("MovementDate").toLocalDateTime() : java.time.LocalDateTime.now()
                );
                
                // Ürün ismini gerçek isimle dolduruyoruz
                String pName = rs.getString("productName");
                sm.setProductName(pName != null && !pName.isEmpty() ? pName : "Ürün ID: " + sm.getProductId());
                sm.setUserName("Sistem");
                sm.setUserRole("USER");
                
                list.add(sm);
            }
            System.out.println("✔ [YEDEK BAŞARILI] Tablo maskelenmiş isimlerle kurtarıldı. Kayıt: " + list.size());
        } catch (SQLException fallbackEx) {
            System.out.println("❌ Kritik Hata: Yedek sorgu da çalışamadı: " + fallbackEx.getMessage());
        }
    }
    return list;
}
// 📌 YEDEK SORGU: Eğer yukarıdaki sorguda Products tablosundaki 'p.id' veya Users tablosundaki 'u.id' kısmından dolayı
// yine bir 'id' uyuşmazlığı hatası yaşanırsa, veritabanındaki TÜM ilişkileri kesip doğrudan veriyi getiren ZIRHLI sorgu:
private List<StockMovement> getAllStockMovementsGarantiliYedekSorgu() {
    List<StockMovement> list = new ArrayList<>();
    
    // Sadece StockMovements tablosunun kendi kolonlarını çeker, hiçbir JOIN yapmaz!
    String sql = "SELECT ProductId, MovementType, Quantity, description AS Reason, UserId, details AS Details, MovementDate " +
                 "FROM dbo.StockMovements " +
                 "ORDER BY MovementDate DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        int sanalId = 1;
        while (rs.next()) {
            String id = String.valueOf(sanalId++);
            String productId = String.valueOf(rs.getInt("ProductId"));
            String movementType = rs.getString("MovementType");
            int quantity = rs.getInt("Quantity");
            String reason = rs.getString("Reason");
            int userId = rs.getInt("UserId");
            String details = rs.getString("Details");
            
            LocalDateTime timestamp = null;
            if (rs.getTimestamp("MovementDate") != null) {
                timestamp = rs.getTimestamp("MovementDate").toLocalDateTime();
            }

            StockMovement sm = new StockMovement(id, productId, movementType, quantity, reason, userId, details, timestamp);
            sm.setProductName("Ürün ID: " + productId); 
            sm.setUserName("Kullanıcı ID: " + userId);
            sm.setUserRole("USER");

            list.add(sm);
        }
        System.out.println("🚀 [YEDEK BAŞARILI] Kurtarma modunda loglar başarıyla çekildi. Kayıt: " + list.size());
    } catch (SQLException ex) {
        System.out.println("❌ KRİTİK HATA: Veritabanından hiçbir şekilde veri çekilemiyor! Hata: " + ex.getMessage());
    }
    return list;
}
// 📌 ALTERNATİF ŞEMA: Eğer Products tablosunda birincil anahtar küçük harfli 'id' değil de büyük harfli 'Id' ise:
private List<StockMovement> getAllStockMovementsAlternatifSorgu() {
    List<StockMovement> list = new ArrayList<>();
    
    String sql = "SELECT sm.id AS LogId, sm.ProductId, p.Id AS ProdId, p.Name AS ProdName, sm.MovementType, " +
                 "sm.Quantity, sm.description AS Reason, sm.UserId, u.username AS OpName, " +
                 "u.role AS OpRole, sm.details AS Details, sm.MovementDate " +
                 "FROM dbo.StockMovements sm " +
                 "LEFT JOIN Products p ON sm.ProductId = p.Id " +
                 "LEFT JOIN Users u ON sm.UserId = u.id " +
                 "ORDER BY sm.MovementDate DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            list.add(extractStockMovement(rs));
        }
        System.out.println("✅ Alternatif Şema Başarıyla Çalıştı. Çekilen kayıt: " + list.size());
        return list;
    } catch (SQLException ex) {
        System.out.println("⚠️ İkinci Şema da Başarısız. Kurtarma Sorgusu Deneniyor... Hata: " + ex.getMessage());
        return getAllStockMovementsKurtarmaSorgusu();
    }
}

// 📌 KURTARMA SORGUSU: Hiçbir join bağlantısı kurmadan, sadece StockMovements tablosunun kendisini çeker. 
// Bu sayede Products ve Users tablolarındaki hiçbir uyuşmazlıktan etkilenmez ve tabloyu kesinlikle doldurur!
private List<StockMovement> getAllStockMovementsKurtarmaSorgusu() {
    List<StockMovement> list = new ArrayList<>();
    
    String sql = "SELECT id AS LogId, ProductId, MovementType, Quantity, description AS Reason, UserId, details AS Details, MovementDate " +
                 "FROM dbo.StockMovements " +
                 "ORDER BY MovementDate DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            String id = String.valueOf(rs.getInt("LogId"));
            String productId = String.valueOf(rs.getInt("ProductId"));
            String movementType = rs.getString("MovementType");
            int quantity = rs.getInt("Quantity");
            String reason = rs.getString("Reason");
            int userId = rs.getInt("UserId");
            String details = rs.getString("Details");
            
            LocalDateTime timestamp = null;
            if (rs.getTimestamp("MovementDate") != null) {
                timestamp = rs.getTimestamp("MovementDate").toLocalDateTime();
            }

            StockMovement sm = new StockMovement(id, productId, movementType, quantity, reason, userId, details, timestamp);
            sm.setProductName("Ürün ID: " + productId); // Join yapamadığımız için geçici olarak ID yazarız
            sm.setUserName("Kullanıcı ID: " + userId);
            sm.setUserRole("USER");

            list.add(sm);
        }
        System.out.println("✅ Kurtarma Sorgusu Başarıyla Çalıştı. En azından temel veriler yüklendi! Kayıt: " + list.size());
    } catch (SQLException e) {
        System.out.println("❌ TÜM SORGULAR BAŞARISIZ. Lütfen veritabanı bağlantınızı veya tablo yapınızı kontrol edin: " + e.getMessage());
    }
    return list;
}

// Ortak veri dönüştürme yardımcısı
private StockMovement extractStockMovement(ResultSet rs) throws SQLException {
    String id = String.valueOf(rs.getInt("LogId"));
    String productId = String.valueOf(rs.getInt("ProductId"));
    String movementType = rs.getString("MovementType");
    int quantity = rs.getInt("Quantity");
    String reason = rs.getString("Reason");
    int userId = rs.getInt("UserId");
    String details = rs.getString("Details");
    
    LocalDateTime timestamp = null;
    if (rs.getTimestamp("MovementDate") != null) {
        timestamp = rs.getTimestamp("MovementDate").toLocalDateTime();
    }

    StockMovement sm = new StockMovement(id, productId, movementType, quantity, reason, userId, details, timestamp);
    
    String pName = rs.getString("ProdName");
    sm.setProductName(pName != null ? pName : "Bilinmeyen Ürün");
    
    String uName = rs.getString("OpName");
    sm.setUserName(uName != null ? uName : "Sistem");
    
    String uRole = rs.getString("OpRole");
    sm.setUserRole(uRole != null ? uRole : "USER");

    return sm;
}

/**
 * Stok miktarı belirlenen kritik eşiğin (örneğin 10) altında olan ürünleri getirir.
 */
public List<Product> getLowStockProducts() {
    int kritikEsik = 10; // İsteğe göre kritik stok sınırını değiştirebilirsiniz
    
    List<Product> tumUrunler = getAllProducts();
    if (tumUrunler == null) {
        return new ArrayList<>();
    }
    
    return tumUrunler.stream()
            .filter(p -> p.getQuantity() <= kritikEsik)
            .collect(Collectors.toList());
}

/**
 * Belirli bir tedarikçinin (UserId) yaptığı en son stok giriş tarihini getirir.
 */
public String getLastSupplierMovementDate(int userId) {
    String sql = "SELECT TOP 1 MovementDate FROM dbo.StockMovements " +
                 "WHERE UserId = ? AND MovementType = 'STOK_GİRİŞ' " +
                 "ORDER BY MovementDate DESC";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, userId);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                java.sql.Timestamp timestamp = rs.getTimestamp("MovementDate");
                if (timestamp != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy - HH:mm");
                    return sdf.format(timestamp);
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "Henüz Kayıt Yok";
}

// 🎯 1. Tedarikçinin Tüm Firma Bilgilerini ve Açıklamasını Getirir
public Map<String, String> getSupplierProfile(int userId) {
    Map<String, String> profile = new HashMap<>();
    // 🔴 '=' yerine 'IN' ve TOP 1 eklendi
    String sql = "SELECT Name, ContactPerson, Phone, Email, TaxNumber, Description FROM dbo.Suppliers " +
                 "WHERE ContactPerson IN (SELECT TOP 1 Username FROM dbo.Users WHERE Id = ?) " +
                 "OR Name IN (SELECT TOP 1 Username FROM dbo.Users WHERE Id = ?)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, userId);
        pstmt.setInt(2, userId);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                profile.put("CompanyName", rs.getString("Name") != null ? rs.getString("Name") : "");
                profile.put("ContactPerson", rs.getString("ContactPerson") != null ? rs.getString("ContactPerson") : "");
                profile.put("Phone", rs.getString("Phone") != null ? rs.getString("Phone") : "");
                profile.put("Email", rs.getString("Email") != null ? rs.getString("Email") : "");
                profile.put("TaxInfo", rs.getString("TaxNumber") != null ? rs.getString("TaxNumber") : "");
                profile.put("Description", rs.getString("Description") != null ? rs.getString("Description") : "");
            }
        }
    } catch (Exception e) {
        System.err.println("❌ Profil bilgisi çekilirken hata: " + e.getMessage());
    }

    return profile;
}

// 🎯 2. Tedarikçinin Firma Bilgilerini Günceller
public boolean updateSupplierProfile(int userId, String companyName, String phone) throws Exception {
    String sql = "UPDATE dbo.Suppliers " +
                 "SET Phone = ? " +
                 "WHERE Name = (SELECT username FROM dbo.Users WHERE id = ?)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, phone);
        pstmt.setInt(2, userId);

        int rows = pstmt.executeUpdate();
        return rows > 0;
    } catch (SQLException e) {
        throw new Exception("Firma bilgileri güncellenirken hata oluştu: " + e.getMessage());
    }
}

// 🎯 Tedarikçinin Firma Bilgilerini Günceller
public boolean updateSupplierProfile(int userId, String companyName, String contactPerson, String phone, String email, String taxInfo, String description) throws Exception {
    // 🔴 'WHERE Id = ?' mantığına geçerek metin eşleşme riskini tamamen sıfırlıyoruz.
    // Eğer userId doğrudan Supplier Id değilse, Users tablosundaki Id eşleşmesini kullanır:
    String sql = "UPDATE dbo.Suppliers SET Name = ?, ContactPerson = ?, Phone = ?, Email = ?, TaxNumber = ?, Description = ? " +
                 "WHERE Id = ? OR ContactPerson = (SELECT TOP 1 Username FROM dbo.Users WHERE Id = ?)";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, companyName);
        pstmt.setString(2, contactPerson);
        pstmt.setString(3, phone);
        pstmt.setString(4, email);
        pstmt.setString(5, taxInfo);
        pstmt.setString(6, description);
        pstmt.setInt(7, userId); // Doğrudan ID üzerinden yakalar
        pstmt.setInt(8, userId);

        int rows = pstmt.executeUpdate();
        
        // Konsoldan kaç satırın güncellendiğini görelim (Hata ayıklama için)
        System.out.println("🔄 Güncellenen satır sayısı: " + rows);
        
        return rows > 0;
    } catch (SQLException e) {
        throw new Exception("Firma bilgileri güncellenirken SQL hatası oluştu: " + e.getMessage());
    }
}

// 🎯 1. Geçmiş Stok Hareketlerini Getirir
public List<String> getStockMovementLogs() throws Exception {
    List<String> logs = new ArrayList<>();
    
    // SQL sorgusu: Ürün adı ile hareket türü, miktar ve tarihi çeker
    String sql = "SELECT sm.id, p.name AS ProductName, sm.movement_type, sm.quantity, sm.created_at " +
                 "FROM dbo.StockMovements sm " +
                 "INNER JOIN dbo.Products p ON p.id = sm.product_id " +
                 "ORDER BY sm.created_at DESC";

    // Tarih formatını daha düzenli (Yıl-Ay-Gün Saat:Dakika:Saniye) göstermek için:
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        while (rs.next()) {
            java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
            String formattedDate = (timestamp != null) ? sdf.format(timestamp) : "Tarih Yok";
            
            String productName = rs.getString("ProductName");
            String movementType = rs.getString("movement_type");
            int quantity = rs.getInt("quantity");

            // Formatlı log metni oluşturma
            String log = String.format("[%s] Ürün: %s | İşlem: %s | Miktar: %d",
                    formattedDate,
                    (productName != null ? productName : "Bilinmeyen Ürün"),
                    (movementType != null ? movementType : "İşlem Belirtilmedi"),
                    quantity);

            logs.add(log);
        }
    } catch (SQLException e) {
        throw new Exception("Stok hareket logları veritabanından çekilirken hata oluştu: " + e.getMessage(), e);
    }

    return logs;
}

// 2. Depodaki Toplam Envanter Mali Değerini Hesaplar (Fiyat * Stok Miktarı)
// 🎯 AUDITOR: Depodaki Toplam Envanter Mali Değerini Hesaplar
public double getTotalInventoryValue() throws Exception {
    // stock_quantity yerine veritabanınızdaki doğru sütun olan Quantity (veya Stock) kullanıldı
    String sql = "SELECT SUM(Price * Quantity) AS TotalValue FROM dbo.Products";
    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

        if (rs.next()) {
            return rs.getDouble("TotalValue");
        }
    } catch (SQLException e) {
        throw new Exception("Envanter değeri hesaplanamadı: " + e.getMessage());
    }
    return 0.0;
}

// 🎯 2. Son 24 Saat Yüksek Miktarlı Çıkış Sayısı (Anomali)
// 1. Kritik Stok Seviyesindeki Ürün Sayısı
// 1. Kritik Stok Seviyesindeki Ürün Sayısı
// 🎯 1. Kritik Stok Altındaki Ürün Sayısı
public int getLowStockCount() {
    int count = 0;
    String sql = "SELECT COUNT(*) FROM dbo.Products WHERE quantity <= min_stock_level";
    
    try (Connection conn = DatabaseConfig.getConnection(); 
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
            count = rs.getInt(1);
        }
    } catch (Exception e) {
        System.out.println("❌ Kritik stok verisi alınamadı: " + e.getMessage());
    }
    return count;
}

// 🎯 2. Son 24 Saat Yüksek Miktarlı Çıkış Sayısı (Anomali)
public int getHighVolumeOutflowCount(int threshold) {
    int count = 0;
    String sql = "SELECT COUNT(*) FROM dbo.StockMovements " +
                 "WHERE movement_type = 'ÇIKIŞ' " +
                 "  AND quantity >= ? " +
                 "  AND created_at >= DATEADD(day, -1, GETDATE())";

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, threshold);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        }
    } catch (Exception e) {
        System.out.println("❌ Anomali verisi alınamadı: " + e.getMessage());
    }
    return count;
}

// 🎯 3. Tarih Aralıklı Log Getirme Metodu
public List<StockMovement> getLogsByDateRange(LocalDate baslangic, LocalDate bitis) {
    List<StockMovement> list = new ArrayList<>();
    
    // 🟢 'u.id', 'u.username', 'u.role' küçük harf yapıldı!
    StringBuilder sql = new StringBuilder(
        "SELECT sm.Id AS log_id, sm.ProductId, p.Name AS ProdName, sm.MovementType, " +
        "sm.Quantity, sm.Description AS Reason, sm.UserId, u.username AS OpName, " +
        "u.role AS OpRole, sm.Details AS Details, sm.MovementDate " +
        "FROM dbo.StockMovements sm " +
        "LEFT JOIN dbo.Products p ON sm.ProductId = p.Id " +
        "LEFT JOIN dbo.users u ON sm.UserId = u.id " +
        "WHERE 1=1 "
    );

    if (baslangic != null) {
        sql.append(" AND CAST(sm.MovementDate AS DATE) >= ? ");
    }
    if (bitis != null) {
        sql.append(" AND CAST(sm.MovementDate AS DATE) <= ? ");
    }

    sql.append(" ORDER BY sm.MovementDate DESC");

    try (Connection conn = DatabaseConfig.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

        int paramIndex = 1;
        if (baslangic != null) {
            pstmt.setDate(paramIndex++, java.sql.Date.valueOf(baslangic));
        }
        if (bitis != null) {
            pstmt.setDate(paramIndex++, java.sql.Date.valueOf(bitis));
        }

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("log_id"));
                String productId = String.valueOf(rs.getInt("ProductId"));
                String movementType = rs.getString("MovementType");
                int quantity = rs.getInt("Quantity");
                String reason = rs.getString("Reason");
                int userId = rs.getInt("UserId");
                String details = rs.getString("Details");
                
                LocalDateTime timestamp = null;
                if (rs.getTimestamp("MovementDate") != null) {
                    timestamp = rs.getTimestamp("MovementDate").toLocalDateTime();
                }

                StockMovement sm = new StockMovement(id, productId, movementType, quantity, reason, userId, details, timestamp);
                
                String pName = rs.getString("ProdName");
                sm.setProductName(pName != null ? pName : "Bilinmeyen Ürün");
                
                String uName = rs.getString("OpName");
                sm.setUserName(uName != null ? uName : "Sistem");
                
                String uRole = rs.getString("OpRole");
                sm.setUserRole(uRole != null ? uRole : "USER");

                list.add(sm);
            }
        }
        System.out.println("🚀 [BAŞARILI] Tarih filtresiyle loglar çekildi. Kayıt sayısı: " + list.size());

    } catch (Exception e) {
        System.out.println("❌ Tarih bazlı log hatası: " + e.getMessage());
    }
    
    return list;
}


}