package com.depo;

import com.depo.model.Category;
import com.depo.model.Product;
import com.depo.model.Supplier;
import com.depo.model.StockMovement;
import com.depo.model.Role;
import com.depo.model.User;
import com.depo.service.WarehouseService;

import java.security.MessageDigest; 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;     // GÜN 14 İÇİN EKLENDİ
import java.util.Scanner;
import java.util.UUID;

public class App {
    private static List<Category> kategoriHavuzu = new ArrayList<>();
    private static List<Supplier> tedarikciHavuzu = new ArrayList<>();
    
    // Oturumu açan aktif kullanıcıyı hafızada tutuyoruz
    private static User aktifKullanici = null;

    // SHA-256 Şifre Hashleme Metodu
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString(); 
        } catch (Exception ex) {
            throw new RuntimeException("🚨 Şifre hash'lenirken kriptografik bir hata oluştu!", ex);
        }
    }

    public static void main(String[] args) {
        WarehouseService warehouseService = new WarehouseService();
        Scanner scanner = new Scanner(System.in);

        // Başlangıç Havuz Verileri
        kategoriHavuzu.add(new Category(1, "Elektronik"));
        tedarikciHavuzu.add(new Supplier(101, "Merkez Lojistik", "0555-111-2233"));

        System.out.println("=========================================");
        System.out.println("    DEPO YÖNETİM SİSTEMİ (MSSQL AUTH v3) ");
        System.out.println("=========================================");

        // REAL MSSQL AUTHENTICATION & SECURE HASH CONTROL
         while (aktifKullanici == null) {
            System.out.println("\n--- GİRİŞ PANELİ ---");
            System.out.print("Kullanıcı Adı: ");
            String username = scanner.nextLine().trim();
            System.out.print("Şifre: ");
            String password = scanner.nextLine().trim();

            String hashedInputPassword = hashPassword(password);
            String url = DatabaseConfig.getUrl(); 
            
            String query = "SELECT id, username, password, role, is_active FROM Users WHERE username = ? AND password = ?";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement stmt = conn.prepareStatement(query)) {
                
                stmt.setString(1, username);
                stmt.setString(2, hashedInputPassword); 
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boolean isActive = rs.getBoolean("is_active");
                        
                        if (!isActive) {
                            System.out.println("❌ HATA: Hesabınız dondurulmuştur! Lütfen Sistem Yöneticisi (ADMIN) ile görüşün.");
                            System.out.println("-----------------------------------------");
                            continue; 
                        }

                        int dbId = rs.getInt("id"); 
                        String dbUser = rs.getString("username");
                        String dbPass = rs.getString("password");
                        Role dbRole = Role.valueOf(rs.getString("role").toUpperCase());
                        
                        aktifKullanici = new User(dbId, dbUser, dbPass, dbRole);
                    } else {
                        System.out.println("❌ Hatalı kullanıcı adı veya şifre! (MSSQL Doğrulayamadı)");
                    }
                }
            } catch (Exception e) {
                System.out.println("🚨 Veritabanı bağlantı hatası: " + e.getMessage());
                System.out.println("Lütfen SQL Server'ın açık olduğundan emin olun.");
                break;
            }
        }

        if (aktifKullanici == null) {
            System.out.println("Sistem güvenli modda kapatılıyor.");
            System.exit(0);
        }

        System.out.println("\n✔️ Giriş Başarılı! Oturum: " + aktifKullanici.getUsername().toUpperCase());
        System.out.println("🔑 Rol Yetkisi: [" + aktifKullanici.getRole() + "]");

        // ANA MENÜ DÖNGÜSÜ
        while (true) {
            // KRİTİK STOK UYARISI
            if (aktifKullanici.getRole() != Role.AUDITOR) {
                try {
                    List<Product> kritikUrunler = warehouseService.getCriticalStockProducts();
                    if (kritikUrunler != null && !kritikUrunler.isEmpty()) {
                        System.out.println("\n⚠️  [KRİTİK STOK UYARISI] Tedarik Edilmesi Gereken Eksik Ürünler:");
                        for (Product cp : kritikUrunler) {
                            System.out.println("   -> ID: " + cp.getId() + " | Ad: " + cp.getName() + " | Kalan Stok: " + cp.getQuantity() + " | Fiyat: " + cp.getPrice() + " TL");
                        }
                        System.out.println("-----------------------------------------");
                    }
                } catch (Exception e) {
                    System.out.println("\n⚠️  [UYARI] Şu anda kritik stok analizi yapılamıyor.");
                }
            }

            // --- ROL BAZLI DİNAMİK TERMİNAL MENÜSÜ ---
            System.out.println("\n===== İŞLEM MENÜSÜ =====");
            System.out.println("[1] Tüm Depoyu Listele");

            if (aktifKullanici.getRole() == Role.ADMIN) {
                System.out.println("[2] Yeni Ürün Tanımla");
                System.out.println("[3] Ürün Bilgilerini Güncelle/Düzelt");
            }

            if (aktifKullanici.getRole() != Role.AUDITOR) {
                System.out.println("[4] Depoya Ürün Girişi Yap (Stock In)");
            }

            if (aktifKullanici.getRole() == Role.ADMIN || aktifKullanici.getRole() == Role.STAFF) {
                System.out.println("[5] Depodan Ürün Çıkışı Yap (Stock Out)");
            }

            if (aktifKullanici.getRole() == Role.ADMIN || aktifKullanici.getRole() == Role.AUDITOR) {
                System.out.println("[6] Stok Hareketleri Geçmişini Gör");
                System.out.println("[7] Depo Mali Değer Raporu Oku");
                System.out.println("[10] 📊 Depo Doluluk ve Kategori İstatistikleri (Yeni!)"); // GÜN 14
            }

            if (aktifKullanici.getRole() == Role.ADMIN) {
                System.out.println("[9] Kullanıcı Hesaplarını Yönet (🔒 Admin Özel)");
            }

            System.out.println("[11] 🔍 Gelişmiş Ürün Arama ve Filtreleme (Yeni!)");

            System.out.println("[8] Güvenli Çıkış");
            System.out.println("========================");
            
            int secim = guvenliIntAl(scanner, "Seçiminiz: ");

            switch (secim) {
                case 1:
                    System.out.println("\n--- GÜNCEL STOK LİSTESİ ---");
                    if (warehouseService.getAllProducts().isEmpty()) {
                        System.out.println("Depo şu an tamamen boş.");
                    } else {
                        for (Product p : warehouseService.getAllProducts()) {
                            System.out.println(p); 
                        }
                    }
                    break;

                case 2:
                    if (aktifKullanici.getRole() != Role.ADMIN) { System.out.println("⚠️ Geçersiz seçim!"); break; }
                    String otomatikId = "PRD-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
                    System.out.println("\n--- YENİ ÜRÜN TANIMLAMA (ID: " + otomatikId + ") ---");
                    System.out.print("Ürün Adı: ");
                    String name = scanner.nextLine();
                    int qty = guvenliIntAl(scanner, "Başlangıç Miktarı: ");
                    double price = guvenliDoubleAl(scanner, "Birim Fiyatı: ");
                    System.out.print("Raf Konumu: ");
                    String loc = scanner.nextLine();

                    warehouseService.addProduct(new Product(otomatikId, name, kategoriSecVeyaOlustur(scanner), tedarikciSecVeyaOlustur(scanner), qty, price, loc));
                    System.out.println("✔️ Ürün başarıyla sisteme eklendi!");
                    break;

                case 3:
                    if (aktifKullanici.getRole() != Role.ADMIN) { System.out.println("⚠️ Geçersiz seçim!"); break; }
                    System.out.print("\nDüzenlenecek Ürün ID giriniz: ");
                    String guncelId = scanner.nextLine().toUpperCase();
                    Product eskiUrun = warehouseService.getProductById(guncelId);

                    if (eskiUrun != null) {
                        System.out.println("\n--- ÜRÜN KONTROL VE DÜZELTME PANELİ ---");
                        System.out.print("Yeni Ürün Adı (" + eskiUrun.getName() + "): ");
                        String yeniAd = scanner.nextLine();
                        int yeniAdet = guvenliIntAl(scanner, "Yeni Miktar (" + eskiUrun.getQuantity() + "): ");
                        double yeniFiyat = guvenliDoubleAl(scanner, "Yeni Fiyat (" + eskiUrun.getPrice() + " TL): ");
                        System.out.print("Yeni Raf Konumu (" + eskiUrun.getStorageLocation() + "): ");
                        String yeniKonum = scanner.nextLine();

                        if (warehouseService.updateProduct(guncelId, new Product(guncelId, yeniAd, kategoriSecVeyaOlustur(scanner), tedarikciSecVeyaOlustur(scanner), yeniAdet, yeniFiyat, yeniKonum), aktifKullanici.getId())) {
                            System.out.println("✔️ Ürün verileri ADMIN tarafından başarıyla düzeltildi!");
                        }
                    } else {
                        System.out.println("❌ Hata: Düzenlenecek ID'ye sahip bir ürün bulunamadı.");
                    }
                    break;

                case 4:
                    if (aktifKullanici.getRole() == Role.AUDITOR) { System.out.println("⚠️ Geçersiz seçim!"); break; }
                    System.out.println("\n--- STOK GİRİŞ İŞLEMİ (STOCK IN) ---");
                    System.out.print("Ürün ID Giriniz: ");
                    String inId = scanner.nextLine().toUpperCase();
                    int inQty = guvenliIntAl(scanner, "Eklenecek Miktar: ");

                    if (warehouseService.stockIn(inId, inQty, aktifKullanici.getId())) {
                        System.out.println("✔️ Stok giriş kaydı başarıyla işlendi.");
                    } else {
                        System.out.println("❌ Hata: Ürün ID bulunamadı veya miktar geçersiz.");
                    }
                    break;

                case 5:
                    if (aktifKullanici.getRole() != Role.ADMIN && aktifKullanici.getRole() != Role.STAFF) { System.out.println("⚠️ Geçersiz seçim!"); break; }
                    System.out.println("\n--- STOK ÇIKIŞ İŞLEMİ (STOCK OUT) ---");
                    System.out.print("Ürün ID Giriniz: ");
                    String outId = scanner.nextLine().toUpperCase();
                    int outQty = guvenliIntAl(scanner, "Çıkarılacak Miktar: ");

                    if (warehouseService.stockOut(outId, outQty, aktifKullanici.getId())) {
                        System.out.println("✔️ Stok başarıyla düşüldü.");
                    } else {
                        System.out.println("❌ Hata: Yetersiz stok veya geçersiz ID.");
                    }
                    break;

                case 6:
                    if (aktifKullanici.getRole() != Role.ADMIN && aktifKullanici.getRole() != Role.AUDITOR) { System.out.println("⚠️ Geçersiz seçim!"); break; }
                    System.out.println("\n--- STOK HAREKET LOG LARI ---");
                    if (warehouseService.getMovementHistory().isEmpty()) {
                        System.out.println("Henüz kayıtlı bir stok hareketi bulunmuyor.");
                    } else {
                        for (StockMovement sm : warehouseService.getMovementHistory()) {
                            System.out.println(sm);
                        }
                    }
                    break;

                case 7:
                    if (aktifKullanici.getRole() != Role.AUDITOR && aktifKullanici.getRole() != Role.ADMIN) { 
                        System.out.println("⚠️ Geçersiz seçim!"); 
                        break; 
                    }
                    
                    System.out.println("\n📊 --- DEPO RAPORLAMA MERKEZİ ---");
                    System.out.println("[1] Anlık Envanter Mali Değer Raporu");
                    System.out.println("[2] Günlük Stok Hareket Raporunu Dışarı Aktar (.CSV)");
                    int raporSecim = guvenliIntAl(scanner, "Seçiminiz: ");

                    if (raporSecim == 1) {
                        double toplamMaliyet = 0;
                        for (Product p : warehouseService.getAllProducts()) {
                            toplamMaliyet += (p.getQuantity() * p.getPrice());
                        }
                        System.out.println("\n📌 Toplam Aktif Ürün Çeşidi Sayısı: " + warehouseService.getAllProducts().size());
                        System.out.println("💰 Depodaki Envanterin Toplam Değeri : " + toplamMaliyet + " TL");
                    } 
                    else if (raporSecim == 2) {
                        System.out.println("\n⏳ Günlük hareket verileri sorgulanıyor ve CSV formatına dönüştürülüyor...");
                        try {
                            String olusanDosya = warehouseService.exportDailyMovementReportToCSV();
                            System.out.println("✔️ Rapor başarıyla üretildi!");
                            System.out.println("📂 Dosya Konumu: [ Proje Kök Dizini ] -> " + olusanDosya);
                            System.out.println("💡 Not: Excel ile açtığınızda veriler sütunlara otomatik bölünecektir (Ayırıcı: ';').");
                        } catch (Exception e) {
                            System.out.println("❌ Rapor dışarı aktarılırken kritik hata: " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️ Geçersiz alt seçim!");
                    }
                    break;

                case 8:
                    System.out.println("Oturum sonlandırıldı. Sistem kapatılıyor...");
                    scanner.close();
                    System.exit(0);
                    break;

                case 9: 
                    if (aktifKullanici.getRole() != Role.ADMIN) { System.out.println("⚠️ Geçersiz seçim!"); break; }
                    
                    System.out.println("\n--- 🔒 KULLANICI YÖNETİM MERKEZİ ---");
                    System.out.println("[1] Mevcut Kullanıcı Durumunu Değiştir (Aktif/Pasif)");
                    System.out.println("[2] Yeni Güvenli Kullanıcı Ekle (SHA-256 Otomatik Hash)");
                    int altSecim = guvenliIntAl(scanner, "Seçiminiz: ");

                    if (altSecim == 1) {
                        System.out.print("Durumu değiştirilecek Kullanıcı Adı (Username): ");
                        String targetUser = scanner.nextLine().trim();
                        
                        if (targetUser.equalsIgnoreCase(aktifKullanici.getUsername())) {
                            System.out.println("❌ Kendi hesabınızı pasif duruma getiremezsiniz!");
                            break;
                        }

                        System.out.println("[1] Hesabı AKTİF Yap");
                        System.out.println("[2] Hesabı DONDUR (PASİF Yap)");
                        int durumSecim = guvenliIntAl(scanner, "Seçiminiz: ");
                        int yeniDurum = (durumSecim == 1) ? 1 : 0;

                        String updateQuery = "UPDATE Users SET is_active = ? WHERE username = ?";
                        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl());
                             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                            
                            stmt.setInt(1, yeniDurum);
                            stmt.setString(2, targetUser);
                            
                            int etkilenenSatir = stmt.executeUpdate();
                            if (etkilenenSatir > 0) {
                                String durumMetni = (yeniDurum == 1) ? "AKTİF" : "PASİF (DONDURULDU)";
                                System.out.println("✔️ '" + targetUser + "' isimli kullanıcının durumu başarıyla " + durumMetni + " yapıldı.");
                            } else {
                                System.out.println("❌ Hata: Belirtilen kullanıcı adı veritabanında bulunamadı.");
                            }
                        } catch (Exception e) {
                            System.out.println("🚨 SQL Hatası: " + e.getMessage());
                        }
                    } 
                    else if (altSecim == 2) {
                        System.out.println("\n--- 👤 YENİ GÜVENLİ KULLANICI TANIMLAMA ---");
                        System.out.print("Yeni Kullanıcı Adı: ");
                        String yeniUser = scanner.nextLine().trim();
                        System.out.print("Yeni Kullanıcı Şifresi: ");
                        String yeniPass = scanner.nextLine().trim();
                        
                        System.out.println("Kullanıcı Rolü Seçin:");
                        System.out.println("[1] ADMIN");
                        System.out.println("[2] STAFF");
                        System.out.println("[3] SUPPLIER");
                        System.out.println("[4] AUDITOR");
                        int rolSecim = guvenliIntAl(scanner, "Rol Seçimi: ");
                        
                        String secilenRol = "STAFF";
                        if (rolSecim == 1) secilenRol = "ADMIN";
                        else if (rolSecim == 3) secilenRol = "SUPPLIER";
                        else if (rolSecim == 4) secilenRol = "AUDITOR";

                        String hashliYeniSifre = hashPassword(yeniPass);

                        String insertQuery = "INSERT INTO Users (username, password, role, is_active) VALUES (?, ?, ?, 1)";
                        try (Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl());
                             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                            
                            stmt.setString(1, yeniUser);
                            stmt.setString(2, hashliYeniSifre); 
                            stmt.setString(3, secilenRol);
                            
                            int sonuc = stmt.executeUpdate();
                            if (sonuc > 0) {
                                System.out.println("✔️ Yeni kullanıcı '" + yeniUser + "' terminal üzerinden başarıyla eklendi.");
                                System.out.println("🔒 Şifre veritabanına SHA-256 hash'li olarak yazıldı: " + hashliYeniSifre);
                            }
                        } catch (Exception e) {
                            System.out.println("🚨 Kullanıcı kaydedilemedi! (Kullanıcı adı benzersiz olmalıdır): " + e.getMessage());
                        }
                    } else {
                        System.out.println("⚠️ Geçersiz alt seçim!");
                    }
                    break;

                case 10: // GÜN 14 ENTEGRASYONU: ANALİTİK PANEL METODU TETİKLENİYOR
                    if (aktifKullanici.getRole() != Role.ADMIN && aktifKullanici.getRole() != Role.AUDITOR) {
                        System.out.println("⚠️ Geçersiz seçim! Bu istatistiksel raporlara sadece yetkili kullanıcılar erişebilir.");
                    } else {
                        istatistikDashboarduGoster(warehouseService);
                    }
                    break;

                default:
                    System.out.println("⚠️ Geçersiz seçim! Lütfen menüdeki rakamlardan birini girin.");

                    case 11:
        System.out.println("\n🔍 --- GELİŞMİŞ ÜRÜN ARAMA PANELİ ---");
        System.out.print("Aranacak Kelime (Ürün Adı, ID, Kategori veya Raf Konumu): ");
        String aramaKelimesi = scanner.nextLine().trim();

        if (aramaKelimesi.isEmpty()) {
            System.out.println("⚠️ Arama alanı boş bırakılamaz!");
            break;
        }

        System.out.println("\n⏳ Algoritmalar çalıştırılıyor ve veritabanı taranıyor...");
        List<Product> bulunanUrunler = warehouseService.searchProducts(aramaKelimesi);

        System.out.println("\n🔍 --- ARAMA SONUÇLARI ---");
        if (bulunanUrunler.isEmpty()) {
            System.out.println("❌ '" + aramaKelimesi + "' kriterine uygun hiçbir ürün bulunamadı.");
        } else {
            System.out.println("✔️ Toplam " + bulunanUrunler.size() + " eşleşen kayıt listelendi:\n");
            for (Product p : bulunanUrunler) {
                // Ürünün toString metodu otomatik tetiklenir
                System.out.println(" -> " + p); 
            }
        }
        System.out.println("-----------------------------------------");
        break;
            }
        }
    }

    // GÜN 14 ENTEGRASYONU: ASCII GRAFİK ÇİZEN ANALİTİK DASHBOARD METODU
    // GÜN 14 GELİŞMİŞ ENTEGRASYON: PASTA VE MATRİS GRAFİKLERİ EKLENDİ
    private static void istatistikDashboarduGoster(WarehouseService warehouseService) {
        System.out.println("\n📊 ==================================================");
        System.out.println("      DEPO GELİŞMİŞ ANALİTİK VE GRAFİK PANELİ      ");
        System.out.println("==================================================");

        List<Product> tumUrunler = warehouseService.getAllProducts();
        if (tumUrunler.isEmpty()) {
            System.out.println("❌ Depoda analiz yapılacak yeterli ürün verisi bulunamadı!");
            System.out.println("==================================================");
            return;
        }

        // 1. GENEL DEPO DOLULUK ORANI (ÇUBUK GRAFİK)
        int maksimumKapasite = 5000; 
        int mevcutStok = warehouseService.getTotalProductQuantity();
        double dolulukOrani = ((double) mevcutStok / maksimumKapasite) * 100;
        if (dolulukOrani > 100) dolulukOrani = 100;

        System.out.println("\n📦 [1] Depo Genel Doluluk Durumu:");
        System.out.print("   Grafik: [");
        int barSayisi = (int) (dolulukOrani / 5); 
        for (int i = 0; i < 20; i++) {
            if (i < barSayisi) System.out.print("█");
            else System.out.print(" ");
        }
        System.out.printf("] %s%.2f\n", "%", dolulukOrani);
        System.out.println("   Detay : " + mevcutStok + " / " + maksimumKapasite + " adet ürün aktif.");


        // 2. KATEGORİ BAZLI PASTA GRAFİK SİMÜLASYONU (ORAN DAĞILIMI)
        System.out.println("\n🍕 [2] Ürün Dağılımı - Pasta Grafik Simülasyonu:");
        Map<String, Integer> kategoriVerileri = warehouseService.getCategoryStockDistribution();
        
        if (kategoriVerileri.isEmpty()) {
            System.out.println("   -> Veri bulunamadı.");
        } else {
            // Pasta dilimi renk/karakter eşleştirmesi
            String[] dilimKarakterleri = {"▓▓", "▒▒", "░░", "██", "▒░"};
            int index = 0;

            for (Map.Entry<String, Integer> entry : kategoriVerileri.entrySet()) {
                double oran = mevcutStok > 0 ? ((double) entry.getValue() / mevcutStok) * 100 : 0;
                String karakter = dilimKarakterleri[index % dilimKarakterleri.length];
                index++;

                // Pasta diliminin büyüklüğünü görselleştir
                StringBuilder dilimGosterimi = new StringBuilder();
                int dilimGenisligi = (int) (oran / 4); // her %4 için bir dilim parçası
                for (int i = 0; i < dilimGenisligi; i++) {
                    dilimGosterimi.append(karakter);
                }
                
                System.out.printf("   [%s] %-15s : %-25s (%s%.1f)\n", karakter, entry.getKey(), dilimGosterimi.toString(), "%", oran);
            }
        }


        // 3. FİYAT / STOK SEVİYESİ DEĞİŞİM MATRİSİ (X-Y SCATTER MAP)
        System.out.println("\n📈 [3] Ürün Fiyat Değişimi ve Risk Matrisi (Scatter Plot):");
        System.out.println("   (Y-Ekseni: Fiyat Seviyesi ▲ | X-Ekseni: Stok Miktarı ►)");
        System.out.println("   -------------------------------------------------");

        // Konsol matrisi oluşturma (Yüksek fiyat-Düşük stok analizi için 5x5 sanal matris)
        char[][] matris = new char[5][20];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 20; j++) matris[i][j] = ' ';
        }

        // Ürünleri matrise yerleştir (Örnek eşik değerlerine göre normalize etme)
        for (Product p : tumUrunler) {
            // Stok miktarını 0-19 arasına sıkıştır (Maksimum 500 adet varsayımıyla)
            int x = (int) (p.getQuantity() / 25); 
            if (x > 19) x = 19;

            // Fiyatı 0-4 arasına sıkıştır (Maksimum 10.000 TL varsayımıyla)
            int y = (int) (p.getPrice() / 2000);
            if (y > 4) y = 4;

            // Matriste Y ekseni yukarı doğru olduğu için (4 - y) şeklinde ters yerleştirilir
            matris[4 - y][x] = '*'; 
        }

        // Matrisi Ekrana Bas
        for (int i = 0; i < 5; i++) {
            String seviyeEtiketi = switch (i) {
                case 0 -> "Yüksek f$ ";
                case 2 -> "Orta f$$  ";
                case 4 -> "Düşük f$  ";
                default -> "          ";
            };
            System.out.print(seviyeEtiketi + " |");
            for (int j = 0; j < 20; j++) {
                System.out.print(matris[i][j]);
            }
            System.out.println("|");
        }
        System.out.println("             -------------------------------------");
        System.out.println("             Az Stok ◄─────────────────────────► Çok Stok");
        System.out.println("   💡 Not: Matristeki '*' işaretleri ürünlerin konumunu gösterir.");
        System.out.println("==================================================");
    }

    private static int guvenliIntAl(Scanner scanner, String mesaj) {
        while (true) {
            System.out.print(mesaj);
            String girdi = scanner.nextLine().trim();
            try { return Integer.parseInt(girdi); } 
            catch (NumberFormatException e) { System.out.println("⚠️ Hatalı Girdi! Sadece tam sayı yazın."); }
        }
    }

    private static double guvenliDoubleAl(Scanner scanner, String mesaj) {
        while (true) {
            System.out.print(mesaj);
            String girdi = scanner.nextLine().trim();
            try { return Double.parseDouble(girdi); } 
            catch (NumberFormatException e) { System.out.println("⚠️ Hatalı Girdi! Geçerli ondalıklı fiyat yazın."); }
        }
    }

    private static Category kategoriSecVeyaOlustur(Scanner scanner) {
        if (kategoriHavuzu.isEmpty()) return null;
        return kategoriHavuzu.get(0);
    }

    private static Supplier tedarikciSecVeyaOlustur(Scanner scanner) {
        if (tedarikciHavuzu.isEmpty()) return null;
        return kategoriHavuzu.isEmpty() ? null : tedarikciHavuzu.get(0);
    }

    
}