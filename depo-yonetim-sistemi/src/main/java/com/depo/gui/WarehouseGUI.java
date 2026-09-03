package com.depo.gui;

import com.depo.model.Product;
import com.depo.DatabaseConfig;
import com.depo.model.Category;
import com.depo.model.Supplier;
import com.depo.model.User;
import com.depo.model.StockMovement;
import com.depo.service.WarehouseService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.chart.*;
import javafx.scene.Node;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.depo.model.CompanyModel;


public class WarehouseGUI extends Application {

    private TableView<Map> tableSystemLogs;

    private final WarehouseService warehouseService = new WarehouseService();
    
    private BorderPane anaDuzen;
    private StackPane icerikAlani;
    private TableView<Product> urunTablosu;
    private TableView<StockMovement> logTablosu;
    
    private Label lblToplamStok;
    private Label lblKritikUrunSayisi;
    private Label lblKategoriSayisi;
    private VBox kartKategoriAlani;
    
    private HBox grafikKonteynir;

    private String aktifKullanici;
    private String aktifRol;
    private int aktifUserId;

    public WarehouseGUI() {
        this.aktifKullanici = "Misafir";
        this.aktifRol = "USER";
        this.aktifUserId = 0;
    }

    public WarehouseGUI(String kullaniciAdi, String rol, int userId) {
        this.aktifKullanici = kullaniciAdi;
        this.aktifRol = rol;
        this.aktifUserId = userId;
    }

    @Override
    public void start(Stage primaryStage) {
        LoginGUI loginGUI = new LoginGUI(primaryStage);
        loginGUI.gosterLoginEkrani();
    }

    public void anaPaneliGoster(Stage primaryStage) {
        primaryStage.setTitle("📦 Depo Yönetim Sistemi v2.0 - Pro");

        anaDuzen = new BorderPane();
        icerikAlani = new StackPane();
        icerikAlani.setPadding(new Insets(20));
        icerikAlani.setStyle("-fx-background-color: #1e1e24;");

        HBox header = olusturHeader();
        anaDuzen.setTop(header);

        VBox sidebar = olusturSidebar();
        anaDuzen.setLeft(sidebar);

        anaDuzen.setCenter(icerikAlani);
        navigasyonDegistir("Dashboard");

        Scene scene = new Scene(anaDuzen, 1150, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //---UI PANELLERİNİN TASARIMI---//

    private HBox olusturHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setStyle("-fx-background-color: #141419; -fx-border-color: #2a2a35; -fx-border-width: 0 0 1 0;");
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblLogo = new Label("📦 DEPOX ERP");
        lblLogo.setFont(Font.font("System", FontWeight.BOLD, 18));
        lblLogo.setTextFill(Color.web("#00adb5"));

        Region separator = new Region();
        HBox.setHgrow(separator, Priority.ALWAYS);

        Label lblUser = new Label("👤 " + aktifKullanici + " (" + aktifRol + ")");
        lblUser.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        lblUser.setTextFill(Color.web("#eeeeee"));

        header.getChildren().addAll(lblLogo, separator, lblUser);
        return header;
    }

   private VBox olusturSidebar() {
    VBox sidebar = new VBox(10);
    sidebar.setPadding(new Insets(20, 15, 20, 15));
    sidebar.setPrefWidth(220);
    sidebar.setStyle("-fx-background-color: #141419;");

    String buttonStyle = "-fx-background-color: transparent; -fx-text-fill: #b2bec3; -fx-alignment: center-left; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 15 10 15; -fx-background-radius: 5;";

    Button btnDashboard = new Button("📊 Kontrol Paneli");
    Button btnStokListesi = new Button("📦 Ürün & Stok Listesi");
    Button btnStokIslemleri = new Button("🔄 Stok Giriş/Çıkış");
    Button btnFirmaBilgileri = new Button("🏢 Firma Bilgileri"); 
    Button btnUrunEkle = new Button("➕ Yeni Ürün Tanımla");
    Button btnUrunGuncelle = new Button("✏️ Ürün Güncelle"); 
    Button btnLoglar = new Button("📜 Sistem Günlükleri");
    Button btnKullaniciYonetimi = new Button("👥 Personel Yönetimi");
    Button btnFirmaYonetimi = new Button("🏢 Firma Yönetimi");

Button btnMaliRapor = new Button("💰 Envanter Mali Raporu");

    Button[] tumButonlar = new Button[]{
        btnDashboard, btnStokListesi, btnStokIslemleri, btnFirmaBilgileri, 
        btnUrunEkle, btnUrunGuncelle, btnLoglar, btnKullaniciYonetimi, 
        btnFirmaYonetimi, btnMaliRapor
    };

    for (Button btn : tumButonlar) {
        btn.setStyle(buttonStyle);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnMouseEntered(e -> btn.setStyle(buttonStyle + "-fx-background-color: #2a2a35; -fx-text-fill: #ffffff;"));
        btn.setOnMouseExited(e -> btn.setStyle(buttonStyle));
    }

    btnDashboard.setOnAction(e -> navigasyonDegistir("Dashboard"));
    btnStokListesi.setOnAction(e -> navigasyonDegistir("StokListesi"));
    btnStokIslemleri.setOnAction(e -> navigasyonDegistir("StokIslemleri"));
    btnFirmaBilgileri.setOnAction(e -> navigasyonDegistir("FirmaBilgileri"));
    btnUrunEkle.setOnAction(e -> navigasyonDegistir("UrunEkle"));
    btnUrunGuncelle.setOnAction(e -> navigasyonDegistir("UrunGuncelle")); 
    btnLoglar.setOnAction(e -> {
        navigasyonDegistir("Loglar");
        sistemGunlukleriniYenileVeGoster();
    });
    btnKullaniciYonetimi.setOnAction(e -> navigasyonDegistir("KullaniciYonetimi"));
    btnFirmaYonetimi.setOnAction(e -> navigasyonDegistir("FirmaYonetimi"));
    btnMaliRapor.setOnAction(e -> navigasyonDegistir("EnvanterMaliRaporu"));

    sidebar.getChildren().clear();

    sidebar.getChildren().addAll(btnDashboard, btnStokListesi);

    if ("AUDITOR".equalsIgnoreCase(aktifRol)) {
        sidebar.getChildren().addAll(btnLoglar, btnMaliRapor);
    } 
    else {
        sidebar.getChildren().add(btnStokIslemleri);

        if ("SUPPLIER".equalsIgnoreCase(aktifRol)) {
            sidebar.getChildren().add(btnFirmaBilgileri);
        }

        if (!"STAFF".equalsIgnoreCase(aktifRol) && !"SUPPLIER".equalsIgnoreCase(aktifRol)) {
            sidebar.getChildren().addAll(btnUrunEkle, btnUrunGuncelle, btnLoglar, btnKullaniciYonetimi);
            
            if ("ADMIN".equalsIgnoreCase(aktifRol)) {
                sidebar.getChildren().add(btnFirmaYonetimi);
            }
        }
    }

    return sidebar;
}
private void navigasyonDegistir(String sayfaAdi) {

    if ("SUPPLIER".equalsIgnoreCase(aktifRol)) {
        if (sayfaAdi.equals("UrunEkle") || sayfaAdi.equals("UrunGuncelle") || 
            sayfaAdi.equals("Loglar") || sayfaAdi.equals("KullaniciYonetimi")) {
            alertGoster(Alert.AlertType.WARNING, "Erişim Engellendi", "Tedarikçi hesabı bu sayfaya erişim yetkisine sahip değildir.");
            return;
        }
    }

    if ("STAFF".equalsIgnoreCase(aktifRol)) {
        if (sayfaAdi.equals("UrunEkle") || sayfaAdi.equals("UrunGuncelle") || 
            sayfaAdi.equals("Loglar") || sayfaAdi.equals("KullaniciYonetimi")) {
            alertGoster(Alert.AlertType.WARNING, "Erişim Engellendi", "Bu sayfaya erişim yetkiniz bulunmamaktadır.");
            return;
        }
    }

    icerikAlani.getChildren().clear();

switch (sayfaAdi) {
        case "Dashboard":
            icerikAlani.getChildren().add(pencereDashboard());
            dashboardVerileriniGuncelle(); 
            break;
            
        case "StokListesi":
            icerikAlani.getChildren().add(pencereStokListesi());
            if (urunTablosu != null) {
                urunTablosu.setItems(FXCollections.observableArrayList(warehouseService.getAllProducts()));
            }
            break;
            
        case "StokIslemleri":
            icerikAlani.getChildren().add(pencereStokIslemleri());
            break;
            
        case "UrunEkle":
            icerikAlani.getChildren().add(pencereUrunEkle());
            break;
            
        case "UrunGuncelle":
            icerikAlani.getChildren().add(pencereUrunGuncellePanel()); 
            break;

        case "FirmaYonetimi":
            icerikAlani.getChildren().add(pencereFirmaYonetimi());
            break;
            
        case "Loglar":
            icerikAlani.getChildren().add(pencereLoglar());
            if (logTablosu != null) {
                logTablosu.setItems(FXCollections.observableArrayList(warehouseService.getMovementHistory()));
            }
            break;
            
        case "KullaniciYonetimi":
            icerikAlani.getChildren().add(pencereKullaniciYonetimi());
            personelListesiniYukle();
            break;

        case "FirmaBilgileri":
            icerikAlani.getChildren().add(createSupplierProfilePanel(1)); 
            break;

        case "EnvanterMaliRaporu":
            icerikAlani.getChildren().add(pencereEnvanterMaliRaporu());
            break;

        default:
            icerikAlani.getChildren().add(pencereDashboard());
            break;
    }
}

    // --- DİNAMİK SEKMELERİN GÖRÜNÜMLERİ ---

    private ScrollPane pencereDashboard() {
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #1e1e24; -fx-border-color: transparent;");

    VBox vbox = new VBox(25);
    vbox.setPadding(new Insets(15, 20, 20, 20));

    if ("SUPPLIER".equalsIgnoreCase(aktifRol)) {
        Label lblBaslik = new Label("🚨 Tedarikçi Mal Takviye Paneli");
        lblBaslik.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblBaslik.setTextFill(Color.WHITE);

        GridPane gridMetrik = new GridPane();
        gridMetrik.setHgap(20); 
        gridMetrik.setVgap(20);

        lblKritikUrunSayisi = new Label("0");
        gridMetrik.add(olusturMetrikKarti("KRİTİK STOK ALARMI", lblKritikUrunSayisi, "#e74c3c"), 0, 0);

        Label lblTabloBaslik = new Label("⚠️ Stok Seviyesi Kritik Düzeyde Olan Ürünler");
        lblTabloBaslik.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblTabloBaslik.setTextFill(Color.web("#e74c3c"));

        TableView<Product> tblKritikUrunler = new TableView<>();
        tblKritikUrunler.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblKritikUrunler.setPrefHeight(380);
        tblKritikUrunler.setStyle("-fx-background-color: #141419; -fx-border-color: #2a2a35; -fx-text-fill: white;");

        TableColumn<Product, String> colCode = new TableColumn<>("Ürün Kodu / ID");
        colCode.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> colName = new TableColumn<>("Ürün Adı");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Integer> colQty = new TableColumn<>("Mevcut Stok");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> colPrice = new TableColumn<>("Birim Fiyat (₺)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, String> colLoc = new TableColumn<>("Depo Raf Konumu");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));

        tblKritikUrunler.getColumns().addAll(colCode, colName, colQty, colPrice, colLoc);

        try {
            List<Product> kritikUrunler;
            
            try {
                kritikUrunler = warehouseService.getLowStockProducts();
            } catch (NoSuchMethodError | Exception ex) {
                List<Product> tumUrunler = warehouseService.getAllProducts();
                if (tumUrunler != null) {
                    kritikUrunler = tumUrunler.stream()
                        .filter(p -> p.getQuantity() <= 10)
                        .toList();
                } else {
                    kritikUrunler = new ArrayList<>();
                }
            }

            if (kritikUrunler != null) {
                tblKritikUrunler.setItems(FXCollections.observableArrayList(kritikUrunler));
                lblKritikUrunSayisi.setText(String.valueOf(kritikUrunler.size()));
            }
        } catch (Exception ignored) {}

        vbox.getChildren().addAll(lblBaslik, gridMetrik, lblTabloBaslik, tblKritikUrunler);

    } else { 
        Label lblBaslik = new Label("Genel Bakış & Analitik");
        lblBaslik.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblBaslik.setTextFill(Color.WHITE);

        GridPane gridMetrik = new GridPane();
        gridMetrik.setHgap(20); 
        gridMetrik.setVgap(20);

        lblToplamStok = new Label("0");
        lblKritikUrunSayisi = new Label("0");
        lblKategoriSayisi = new Label("0");

        gridMetrik.add(olusturMetrikKarti("TOPLAM STOK HACMİ", lblToplamStok, "#3498db"), 0, 0);
        gridMetrik.add(olusturMetrikKarti("KRİTİK STOK ALARMI", lblKritikUrunSayisi, "#e74c3c"), 1, 0);
        gridMetrik.add(olusturMetrikKarti("AKTİF KATEGORİ", lblKategoriSayisi, "#2ecc71"), 2, 0);

        HBox altKisimLayout = new HBox(20);
        altKisimLayout.setAlignment(Pos.TOP_LEFT);

        kartKategoriAlani = new VBox(12);
        kartKategoriAlani.setPadding(new Insets(15));
        kartKategoriAlani.setPrefWidth(280);
        kartKategoriAlani.setStyle("-fx-background-color: #141419; -fx-background-radius: 8; -fx-border-color: #2a2a35;");

        Label lblKatBaslik = new Label("Kategori Bazlı Stok Dağılımı");
        lblKatBaslik.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblKatBaslik.setTextFill(Color.LIGHTGRAY);
        kartKategoriAlani.getChildren().add(lblKatBaslik);

        grafikKonteynir = new HBox(20);
        grafikKonteynir.setAlignment(Pos.CENTER_LEFT);

        altKisimLayout.getChildren().addAll(kartKategoriAlani, grafikKonteynir);
        vbox.getChildren().addAll(lblBaslik, gridMetrik, altKisimLayout);
    }

    scrollPane.setContent(vbox);
    return scrollPane;
}
    private void dashboardVerileriniGuncelle() {
        try {
            if (lblToplamStok == null || kartKategoriAlani == null) return;
            lblToplamStok.setText(String.valueOf(warehouseService.getTotalProductQuantity()));
            
            lblKritikUrunSayisi.setText(String.valueOf(warehouseService.getCriticalStockCount()));
            
            Map<String, Integer> dagilim = warehouseService.getCategoryStockDistribution();
            lblKategoriSayisi.setText(String.valueOf(dagilim.size()));

            kartKategoriAlani.getChildren().removeIf(node -> node instanceof HBox);
            for (Map.Entry<String, Integer> entry : dagilim.entrySet()) {
                HBox row = new HBox();
                row.setPadding(new Insets(2, 0, 2, 0));
                Label name = new Label(entry.getKey()); name.setTextFill(Color.WHITE);
                Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
                Label qty = new Label(entry.getValue() + " Adet"); qty.setTextFill(Color.web("#00adb5"));
                row.getChildren().addAll(name, r, qty);
                kartKategoriAlani.getChildren().add(row);
            }

            olusturGrafikPaneli();

        } catch (Exception e) {
            System.out.println("Dashboard güncellenirken hata: " + e.getMessage());
        }
    }

    public void olusturGrafikPaneli() {
        if (grafikKonteynir == null) return;
        grafikKonteynir.getChildren().clear();

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Kategori Dağılım Oranları");
        pieChart.setPrefSize(380, 290);
        pieChart.setLegendVisible(false); 
        pieChart.setLabelsVisible(true);

        Map<String, Integer> categoryData = warehouseService.getCategoryStockDistribution();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : categoryData.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        Runnable applyDarkThemeStyles = () -> {
            Platform.runLater(() -> {
                Node title = pieChart.lookup(".chart-title");
                if (title != null) {
                    title.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                }
                pieChart.lookupAll(".chart-pie-label").forEach(node -> {
                    node.setStyle("-fx-fill: white !important; -fx-font-size: 11px; -fx-opacity: 1.0 !important; -fx-visibility: visible !important;");
                });
                pieChart.lookupAll(".chart-pie-label-line").forEach(node -> {
                    node.setStyle("-fx-stroke: rgba(255, 255, 255, 0.6) !important; -fx-opacity: 1.0 !important;");
                });
            });
        };

        pieChart.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) { applyDarkThemeStyles.run(); }
        });

        pieChartData.addListener((ListChangeListener<PieChart.Data>) change -> applyDarkThemeStyles.run());
        pieChart.setData(pieChartData);

        if (pieChart.getScene() != null) { applyDarkThemeStyles.run(); }

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Ürünler");
        xAxis.setTickLabelFill(Color.WHITE);
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("İşlem Miktarı");
        yAxis.setTickLabelFill(Color.WHITE);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Son Stok Hareket Analizi");
        barChart.setPrefSize(420, 290);

        List<StockMovement> recentMovements = warehouseService.getRecentMovementsForChart();
        
        XYChart.Series<String, Number> stockInSeries = new XYChart.Series<>();
        stockInSeries.setName("Giriş");
        
        XYChart.Series<String, Number> stockOutSeries = new XYChart.Series<>();
        stockOutSeries.setName("Çıkış");

        for (int i = recentMovements.size() - 1; i >= 0; i--) {
            StockMovement sm = recentMovements.get(i);
            String urunAdi = sm.getProductName();
            int miktar = sm.getQuantity();

            if ("STOCK_OUT".equalsIgnoreCase(sm.getMovementType())) {
                stockOutSeries.getData().add(new XYChart.Data<>(urunAdi, miktar));
            } else {
                stockInSeries.getData().add(new XYChart.Data<>(urunAdi, miktar));
            }
        }

        barChart.getData().addAll(stockInSeries, stockOutSeries);

        barChart.lookup(".chart-title").setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        xAxis.lookup(".axis-label").setStyle("-fx-text-fill: #b2bec3;");
        yAxis.lookup(".axis-label").setStyle("-fx-text-fill: #b2bec3;");
        barChart.lookup(".chart-legend").setStyle("-fx-background-color: transparent;");

        grafikKonteynir.getChildren().addAll(pieChart, barChart);
    }

    private VBox pencereStokListesi() {
        VBox vbox = new VBox(15);

        HBox kontrolBar = new HBox(10);
        kontrolBar.setAlignment(Pos.CENTER_LEFT);

        TextField txtArama = new TextField();
        txtArama.setPromptText("Ürün Adı, ID, Kategori veya Konum ile Akıllı Filtreleme...");
        txtArama.setPrefWidth(350);
        txtArama.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-prompt-text-fill: #777; -fx-background-radius: 5;");

        Button btnAra = new Button("🔍 Filtrele");
        Button btnYenile = new Button("🔄 Listeyi Yenile");
        Button btnExport = new Button("📥 CSV Raporu Al");

        btnAra.setStyle("-fx-background-color: #00adb5; -fx-text-fill: white; -fx-cursor: hand;");
        btnYenile.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-cursor: hand;");
        btnExport.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");

        kontrolBar.getChildren().addAll(txtArama, btnAra, btnYenile, btnExport);

        urunTablosu = new TableView<>();
        urunTablosu.setStyle("-fx-background-color: #141419; -fx-control-inner-background: #141419;");

        TableColumn<Product, String> colId = new TableColumn<>("Ürün ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Product, String> colName = new TableColumn<>("Ürün Adı");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> colCategory = new TableColumn<>("Kategori");
        colCategory.setCellValueFactory(cellData -> {
            Category cat = cellData.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat.getName() : "Belirtilmedi");
        });

        TableColumn<Product, String> colSupplier = new TableColumn<>("Tedarikçi");
        colSupplier.setCellValueFactory(cellData -> {
            Supplier sup = cellData.getValue().getSupplier();
            return new SimpleStringProperty(sup != null ? sup.getName() : "Belirtilmedi");
        });

        TableColumn<Product, Integer> colQty = new TableColumn<>("Stok Miktarı");
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Double> colPrice = new TableColumn<>("Fiyat (TL)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, String> colLoc = new TableColumn<>("Raf Konumu");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));

        urunTablosu.getColumns().addAll(colId, colName, colCategory, colSupplier, colQty, colPrice, colLoc);
        urunTablosu.setItems(FXCollections.observableArrayList(warehouseService.getAllProducts()));

        btnYenile.setOnAction(e -> urunTablosu.setItems(FXCollections.observableArrayList(warehouseService.getAllProducts())));
        
        btnAra.setOnAction(e -> {
            String k = txtArama.getText().trim();
            if(!k.isEmpty()) {
                urunTablosu.setItems(FXCollections.observableArrayList(warehouseService.searchProducts(k)));
            } else {
                urunTablosu.setItems(FXCollections.observableArrayList(warehouseService.getAllProducts()));
            }
        });

        btnExport.setOnAction(e -> {
            try {
                String file = warehouseService.exportDailyMovementReportToCSV();
                alertGoster(Alert.AlertType.INFORMATION, "Başarılı", "Rapor Dışarı Aktarıldı:\n" + file);
            } catch (Exception ex) {
                alertGoster(Alert.AlertType.ERROR, "Hata", ex.getMessage());
            }
        });

        vbox.getChildren().addAll(kontrolBar, urunTablosu);
        return vbox;
    }

    private VBox pencereUrunEkle() {
        VBox vbox = new VBox(15);
        vbox.setMaxWidth(400);

        Label lblTitle = new Label("Yeni Ürün Tanımlama Formu");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.WHITE);

        TextField txtName = new TextField(); txtName.setPromptText("Ürün Adı");
        TextField txtQty = new TextField(); txtQty.setPromptText("Başlangıç Stok Miktarı");
        TextField txtPrice = new TextField(); txtPrice.setPromptText("Birim Fiyat (Örn: 249.90)");
        TextField txtLoc = new TextField(); txtLoc.setPromptText("Depo Raf Konumu (Örn: Raf-B4)");

        ComboBox<Category> comboCategory = new ComboBox<>();
        comboCategory.setPromptText("Kategori Seçin veya Yenisini Yazın...");
        comboCategory.setMaxWidth(Double.MAX_VALUE);
        comboCategory.setEditable(true);
        
        try {
            List<Product> tumUrunler = warehouseService.getAllProducts();
            
            java.util.Set<String> eklenenKategoriIsimleri = new java.util.HashSet<>();
            java.util.List<Category> benzersizKategoriler = new java.util.ArrayList<>();
            
            for (Product p : tumUrunler) {
                Category cat = p.getCategory();
                if (cat != null && cat.getName() != null) {
                    String temizIsim = cat.getName().replaceAll("(\\s*\\(Girilmedi\\))+", " (Girilmedi)").trim();

                    if (eklenenKategoriIsimleri.add(temizIsim.toLowerCase())) {
                        benzersizKategoriler.add(cat);
                    }
                }
            }
            
            comboCategory.setItems(FXCollections.observableArrayList(benzersizKategoriler));
            
            comboCategory.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category object) {
                    if (object == null || object.getName() == null) return "";
                    return object.getName().replaceAll("(\\s*\\(Girilmedi\\))+", " (Girilmedi)").trim();
                }

                @Override
                public Category fromString(String string) {
                    if (string == null || string.trim().isEmpty()) return null;
                    return comboCategory.getItems().stream()
                            .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(string.trim()))
                            .findFirst()
                            .orElse(null);
                }
            });
        } catch (Exception ignored) {}

        ComboBox<Supplier> comboSupplier = new ComboBox<>();
        comboSupplier.setPromptText("Tedarikçi Seçin veya Yenisini Yazın...");
        comboSupplier.setMaxWidth(Double.MAX_VALUE);
        comboSupplier.setEditable(true);
        
        try {
            List<Product> tumUrunler = warehouseService.getAllProducts();
            
            java.util.Set<String> eklenenTedarikciIsimleri = new java.util.HashSet<>();
            java.util.List<Supplier> benzersizTedarikciler = new java.util.ArrayList<>();
            
            for (Product p : tumUrunler) {
                Supplier sup = p.getSupplier();
                if (sup != null && sup.getName() != null) {
                    String temizIsim = sup.getName().replaceAll("(\\s*\\(Girilmedi\\))+", " (Girilmedi)").trim();
                    
                    if (eklenenTedarikciIsimleri.add(temizIsim.toLowerCase())) {
                        benzersizTedarikciler.add(sup);
                    }
                }
            }
            
            comboSupplier.setItems(FXCollections.observableArrayList(benzersizTedarikciler));
            
            comboSupplier.setConverter(new javafx.util.StringConverter<Supplier>() {
                @Override
                public String toString(Supplier object) {
                    if (object == null || object.getName() == null) return "";
                    return object.getName().replaceAll("(\\s*\\(Girilmedi\\))+", " (Girilmedi)").trim();
                }

                @Override
                public Supplier fromString(String string) {
                    if (string == null || string.trim().isEmpty()) return null;
                    return comboSupplier.getItems().stream()
                            .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(string.trim()))
                            .findFirst()
                            .orElse(null);
                }
            });
        } catch (Exception ignored) {}

        String inputStyle = "-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-prompt-text-fill: #777; -fx-padding: 8;";
        for(TextField tf : new TextField[]{txtName, txtQty, txtPrice, txtLoc}) tf.setStyle(inputStyle);
        comboCategory.setStyle("-fx-background-color: #2a2a35;");
        comboSupplier.setStyle("-fx-background-color: #2a2a35;");

        Button btnEkleKaydet = new Button("💾 Ürünü Veritabanına Kaydet");
        btnEkleKaydet.setMaxWidth(Double.MAX_VALUE);
        btnEkleKaydet.setStyle("-fx-background-color: #00adb5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");

        btnEkleKaydet.setOnAction(e -> {
            try {
                Category nihaiKategori = null;
                String katYazi = comboCategory.getEditor().getText() != null ? comboCategory.getEditor().getText().trim() : "";
                
                if (!katYazi.isEmpty()) {
                    final String arananKat = katYazi;
                    nihaiKategori = comboCategory.getItems().stream()
                            .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(arananKat))
                            .findFirst()
                            .orElse(null);
                    
                    if (nihaiKategori == null) {
                        nihaiKategori = warehouseService.getOrCreateCategoryByName(arananKat);
                    }
                }

                Supplier nihaiTedarikci = null;
                String tedYazi = comboSupplier.getEditor().getText() != null ? comboSupplier.getEditor().getText().trim() : "";
                
                if (!tedYazi.isEmpty()) {
                    final String arananTed = tedYazi;
                    nihaiTedarikci = comboSupplier.getItems().stream()
                            .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(arananTed))
                            .findFirst()
                            .orElse(null);
                    
                    if (nihaiTedarikci == null) {
                        nihaiTedarikci = new Supplier(0, arananTed, "Girilmedi");
                    }
                }

                if (txtName.getText().trim().isEmpty() || 
                    txtQty.getText().trim().isEmpty() || 
                    txtPrice.getText().trim().isEmpty() || 
                    nihaiKategori == null || 
                    nihaiTedarikci == null) {
                    
                    alertGoster(Alert.AlertType.ERROR, "Hata", "Lütfen zorunlu tüm alanları doldurun!");
                    return;
                }

                String otomatikBarkodId = "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                Product newProd = new Product(
                        otomatikBarkodId, 
                        txtName.getText().trim(),
                        nihaiKategori,
                        nihaiTedarikci,
                        Integer.parseInt(txtQty.getText().trim()),
                        Double.parseDouble(txtPrice.getText().trim()),
                        txtLoc.getText().trim()
                );
                
                warehouseService.addProduct(newProd, aktifUserId);
                alertGoster(Alert.AlertType.INFORMATION, "Başarılı", "Ürün başarıyla kaydedildi!\nSistemin Atadığı ID: " + otomatikBarkodId);
                
                for(TextField tf : new TextField[]{txtName, txtQty, txtPrice, txtLoc}) tf.clear();
                comboCategory.setValue(null); comboSupplier.setValue(null);
                comboCategory.getEditor().clear(); comboSupplier.getEditor().clear();
                
                navigasyonDegistir("StokListesi");
            } catch (Exception ex) {
                alertGoster(Alert.AlertType.ERROR, "Hata", "Ürün kaydedilemedi:\n" + ex.getMessage());
            }
        });

        vbox.getChildren().addAll(
            lblTitle, 
            new Label("Ürün Adı: *"), txtName, 
            new Label("Kategori: *"), comboCategory, 
            new Label("Tedarikçi: *"), comboSupplier, 
            new Label("Başlangıç Stok Miktarı: *"), txtQty, 
            new Label("Birim Fiyat: *"), txtPrice, 
            new Label("Depo Raf Konumu:"), txtLoc, 
            btnEkleKaydet
        );
        return vbox;
    }

    private VBox pencereUrunGuncellePanel() {
    VBox vbox = new VBox(12);
    vbox.setMaxWidth(420);

    Label lblTitle = new Label("✏️ Ürün Bilgilerini Güncelleme Paneli");
    lblTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
    lblTitle.setTextFill(Color.WHITE);

    TextField txtProductCode = new TextField(); 
    txtProductCode.setPromptText("Aranacak Ürün Kodu (Örn: PRD-1001)");
    
    Button btnGetir = new Button("🔍 Ürünü Sistemden Çağır");
    btnGetir.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

    TextField txtName = new TextField(); txtName.setPromptText("Yeni Ürün Adı");
    TextField txtPrice = new TextField(); txtPrice.setPromptText("Yeni Birim Fiyat (Örn: 45.50)");
    TextField txtLoc = new TextField(); txtLoc.setPromptText("Yeni Raf Konumu (Örn: A-12)");

    ComboBox<Category> comboCategory = new ComboBox<>();
    comboCategory.setPromptText("Kategori Seçin veya Yazın...");
    comboCategory.setMaxWidth(Double.MAX_VALUE);
    comboCategory.setEditable(true);

    ComboBox<Supplier> comboSupplier = new ComboBox<>();
    comboSupplier.setPromptText("Tedarikçi Seçin veya Yazın...");
    comboSupplier.setMaxWidth(Double.MAX_VALUE);
    comboSupplier.setEditable(true);

    String inputStyle = "-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-prompt-text-fill: #777; -fx-padding: 8; -fx-background-radius: 4;";
    for(TextField tf : new TextField[]{txtProductCode, txtName, txtPrice, txtLoc}) {
        tf.setStyle(inputStyle);
    }
    comboCategory.setStyle("-fx-background-color: #2a2a35;");
    comboSupplier.setStyle("-fx-background-color: #2a2a35;");

    try {
        List<Product> tumUrunler = warehouseService.getAllProducts();
        if (tumUrunler != null) {
            comboCategory.setItems(FXCollections.observableArrayList(
                tumUrunler.stream().map(Product::getCategory).filter(Objects::nonNull).distinct().toList()
            ));
            comboSupplier.setItems(FXCollections.observableArrayList(
                tumUrunler.stream().map(Product::getSupplier).filter(Objects::nonNull).distinct().toList()
            ));
        }
    } catch(Exception ignored) {}

    btnGetir.setOnAction(e -> {
        pencereUrunSecPopUp(txtProductCode, txtName, txtPrice, txtLoc, comboCategory, comboSupplier);
    });

    Button btnGuncelleKaydet = new Button("💾 Değişiklikleri Veritabanına İşle");
    btnGuncelleKaydet.setMaxWidth(Double.MAX_VALUE);
    btnGuncelleKaydet.setStyle("-fx-background-color: #00adb5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand; -fx-background-radius: 4;");

    btnGuncelleKaydet.setOnAction(e -> {
        String code = txtProductCode.getText().trim();
        String name = txtName.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String loc = txtLoc.getText().trim();

        if (code.isEmpty() || name.isEmpty() || priceStr.isEmpty()) {
            alertGoster(Alert.AlertType.WARNING, "Eksik Bilgi", "Lütfen ürün kodu, ürün adı ve fiyat alanlarını doldurun!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr.replace(",", "."));
        } catch (NumberFormatException ex) {
            alertGoster(Alert.AlertType.ERROR, "Geçersiz Fiyat", "Girdiğiniz birim fiyat geçerli bir sayı olmalıdır.");
            return;
        }

        Category nihaiKat = null;
        Object katVal = comboCategory.getValue();
        if (katVal instanceof Category c) {
            nihaiKat = c;
        } else if (katVal instanceof String str && !str.trim().isEmpty()) {
            nihaiKat = warehouseService.getOrCreateCategoryByName(str.trim());
        }

        Supplier nihaiTed = null;
        Object tedVal = comboSupplier.getValue();
        if (tedVal instanceof Supplier s) {
            nihaiTed = s;
        } else if (tedVal instanceof String str && !str.trim().isEmpty()) {
            nihaiTed = new Supplier(0, str.trim(), "Girilmedi");
        }

        if (nihaiKat == null || nihaiTed == null) {
            alertGoster(Alert.AlertType.WARNING, "Eksik Kategori/Tedarikçi", "Lütfen geçerli bir kategori ve tedarikçi seçin veya yazın!");
            return;
        }

        Product oldProd = warehouseService.getProductById(code);
        if (oldProd == null) {
            alertGoster(Alert.AlertType.ERROR, "Bulunamadı", "Sistemde '" + code + "' koduna ait bir ürün bulunamadı!");
            return;
        }

        try {
            Product updatedProduct = new Product(code, name, nihaiKat, nihaiTed, oldProd.getQuantity(), price, loc);
            boolean isUpdated = warehouseService.updateProduct(code, updatedProduct, aktifUserId);

            if (isUpdated) {
                alertGoster(Alert.AlertType.INFORMATION, "Başarılı", "Ürün bilgileri başarıyla güncellendi.");
                navigasyonDegistir("StokListesi");
            } else {
                alertGoster(Alert.AlertType.ERROR, "Hata", "Güncelleme işlemi veritabanı tarafından reddedildi.");
            }
        } catch (Exception ex) {
            alertGoster(Alert.AlertType.ERROR, "İşlem Hatalı", "Güncelleme sırasında bir sorun oluştu: " + ex.getMessage());
        }
    });

    vbox.getChildren().addAll(
        lblTitle,
        createFormLabel("Güncellenecek Ürün Kodu: *"), txtProductCode, btnGetir,
        createFormLabel("Yeni Ürün Adı: *"), txtName,
        createFormLabel("Kategori: *"), comboCategory,
        createFormLabel("Tedarikçi: *"), comboSupplier,
        createFormLabel("Birim Fiyat (₺): *"), txtPrice,
        createFormLabel("Depo Raf Konumu:"), txtLoc,
        btnGuncelleKaydet
    );

    return vbox;
}

private Label createFormLabel(String text) {
    Label lbl = new Label(text);
    lbl.setTextFill(Color.LIGHTGRAY);
    lbl.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
    return lbl;
}

    private void pencereUrunSecPopUp(TextField txtProductCode, TextField txtName, TextField txtPrice, TextField txtLoc, ComboBox<Category> comboCategory, ComboBox<Supplier> comboSupplier) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL); 
        popupStage.setTitle("🔍 Sistem Ürün Rehberi");

        VBox layout = new VBox(12);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e1e24;"); 
        layout.setPrefSize(480, 420);

        Label lblTitle = new Label("Sistemden Ürün Seçin");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblTitle.setTextFill(Color.WHITE);

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("Ürün Adı veya Koduna Göre Canlı Ara...");
        txtSearch.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-padding: 8;");

        TableView<Product> tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: #141419;");
        
        TableColumn<Product, String> colCode = new TableColumn<>("Ürün Kodu");
        colCode.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));
        
        TableColumn<Product, String> colName = new TableColumn<>("Ürün Adı");
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        
        TableColumn<Product, Integer> colQty = new TableColumn<>("Stok");
        colQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantity()));

        tableView.getColumns().addAll(colCode, colName, colQty);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        List<Product> tumUrunler = warehouseService.getAllProducts();
        FilteredList<Product> filteredData = new FilteredList<>(FXCollections.observableArrayList(tumUrunler), p -> true);

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (product.getName().toLowerCase().contains(lowerCaseFilter)) return true;
                if (product.getId().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);

        Button btnSec = new Button("✅ Seç ve Aktar");
        btnSec.setMaxWidth(Double.MAX_VALUE);
        btnSec.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");

        Runnable konfirmeEt = () -> {
            Product secilen = tableView.getSelectionModel().getSelectedItem();
            if (secilen != null) {
                txtProductCode.setText(secilen.getId());
                txtName.setText(secilen.getName());
                txtPrice.setText(String.valueOf(secilen.getPrice()));
                txtLoc.setText(secilen.getStorageLocation());
                comboCategory.setValue(secilen.getCategory());
                comboSupplier.setValue(secilen.getSupplier());
                
                popupStage.close();
            } else {
                alertGoster(Alert.AlertType.WARNING, "Seçim Yapılmadı", "Lütfen listeden bir ürün seçin!");
            }
        };

        btnSec.setOnAction(e -> konfirmeEt.run());
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) konfirmeEt.run();
        });

        layout.getChildren().addAll(lblTitle, txtSearch, tableView, btnSec);
        Scene scene = new Scene(layout);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

private VBox pencereStokIslemleri() {
    VBox anaKonteynir = new VBox(20);
    anaKonteynir.setMaxWidth(450);
    anaKonteynir.setPadding(new Insets(10));

    Label lblTitle = new Label("🔄 Hızlı Stok Giriş / Çıkış Paneli");
    lblTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
    lblTitle.setTextFill(Color.WHITE);

    GridPane formGrid = new GridPane();
    formGrid.setHgap(10);
    formGrid.setVgap(15);
    formGrid.setAlignment(Pos.TOP_LEFT);

    Label lblId = createFormLabel("Ürün ID:");
    HBox idLayout = new HBox(8);
    idLayout.setAlignment(Pos.CENTER_LEFT);

    TextField txtId = new TextField(); 
    txtId.setPromptText("Ürün ID Girin veya Sağdan Çağırın");
    txtId.setPrefWidth(240);

    Button btnUrunSec = new Button("🔍 Çağır");
    btnUrunSec.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 7 12 7 12;");

    idLayout.getChildren().addAll(txtId, btnUrunSec);

    btnUrunSec.setOnAction(e -> {
        pencereUrunSecPopUp(txtId, new TextField(), new TextField(), new TextField(), new ComboBox<>(), new ComboBox<>());
    });
    txtId.setOnMouseClicked(e -> {
        if (e.getClickCount() == 2) {
            pencereUrunSecPopUp(txtId, new TextField(), new TextField(), new TextField(), new ComboBox<>(), new ComboBox<>());
        }
    });

    Label lblType = createFormLabel("İşlem Türü:");
    ComboBox<String> comboType = new ComboBox<>();
    comboType.getItems().addAll("STOCK_IN", "STOCK_OUT");
    comboType.setValue("STOCK_IN");
    comboType.setPrefWidth(320);
    comboType.setMouseTransparent(false);
    comboType.setEditable(false);

    if ("SUPPLIER".equalsIgnoreCase(aktifRol)) {
        comboType.setValue("STOCK_IN");
        comboType.setDisable(true);
    }

    Label lblAmount = createFormLabel("Miktar:");
    TextField txtAmount = new TextField(); 
    txtAmount.setPromptText("Miktar (Örn: 50)");
    txtAmount.setPrefWidth(320);

    String inputStyle = "-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 4;";
    txtId.setStyle(inputStyle);
    txtAmount.setStyle(inputStyle);

    comboType.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-background-radius: 4;");
    comboType.setCellFactory(lv -> new ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: #2a2a35; -fx-padding: 8;");
            }
        }
    });

    comboType.setButtonCell(new ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item);
                setTextFill(Color.WHITE);
            }
        }
    });

    formGrid.add(lblId, 0, 0);
    formGrid.add(idLayout, 0, 1);
    
    formGrid.add(lblType, 0, 2);
    formGrid.add(comboType, 0, 3);
    
    formGrid.add(lblAmount, 0, 4);
    formGrid.add(txtAmount, 0, 5);

    Button btnUygula = new Button("🚀 Stok Hareketi Kaydet");
    btnUygula.setMaxWidth(320);
    btnUygula.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 4;");

    btnUygula.setOnAction(e -> {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            alertGoster(Alert.AlertType.WARNING, "Eksik Alan", "Lütfen bir Ürün ID seçin veya girin!");
            return;
        }

        int miktar;
        try {
            miktar = Math.abs(Integer.parseInt(txtAmount.getText().trim()));
            if (miktar == 0) {
                alertGoster(Alert.AlertType.WARNING, "Geçersiz Miktar", "Stok işlem miktarı 0'dan büyük olmalıdır!");
                return;
            }
        } catch (NumberFormatException nfe) {
            alertGoster(Alert.AlertType.ERROR, "Format Hatası", "Miktar alanına geçerli bir tam sayı girmelisiniz!");
            return;
        }

        String secilenTip = comboType.getValue();
    
        if ("SUPPLIER".equalsIgnoreCase(aktifRol) && ("STOCK_OUT".equalsIgnoreCase(secilenTip) || "STOK_ÇIKIŞ".equalsIgnoreCase(secilenTip))) {
            alertGoster(Alert.AlertType.ERROR, "Yetkisiz İşlem", "Tedarikçi (SUPPLIER) rolü depodan mal çıkışı (Stock Out) yapamaz!");
            return;
        }

        String type = "GÜNCELLEME";
        String desc = ""; 

        if ("STOCK_IN".equalsIgnoreCase(secilenTip) || "STOK_GİRİŞ".equalsIgnoreCase(secilenTip)) {
            type = "STOK_GİRİŞ";
        } else if ("STOCK_OUT".equalsIgnoreCase(secilenTip) || "STOK_ÇIKIŞ".equalsIgnoreCase(secilenTip)) {
            miktar = -miktar;
            type = "STOK_ÇIKIŞ";
        }

        try {
            warehouseService.addStockMovement(id, type, miktar, desc, aktifUserId);
            
            dashboardVerileriniGuncelle();
            
            alertGoster(Alert.AlertType.INFORMATION, "Başarılı", "Stok hareketi başarıyla veritabanına işlendi.");

            txtId.clear();
            txtAmount.clear();
            if (!"SUPPLIER".equalsIgnoreCase(aktifRol)) {
                comboType.setValue("STOCK_IN");
            }
        } catch (Exception ex) {
            alertGoster(Alert.AlertType.ERROR, "İşlem Başarısız", "Stok hareketi kaydedilirken bir hata oluştu: " + ex.getMessage());
        }
    });

    anaKonteynir.getChildren().addAll(lblTitle, formGrid, btnUygula);
    return anaKonteynir;
}

private VBox pencereLoglar() {
    VBox vbox = new VBox(15);
    vbox.setStyle("-fx-padding: 15; -fx-background-color: #1e1e2e;");

    Label lblTitle = new Label("📜 Sistem Denetim Günlükleri & Log Geçmişi");
    lblTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
    lblTitle.setTextFill(Color.WHITE);

    HBox filterBox = new HBox(12);
    filterBox.setAlignment(Pos.CENTER_LEFT);

    Label lblBaslangic = new Label("Başlangıç:");
    lblBaslangic.setTextFill(Color.web("#b2bec3"));
    DatePicker dpBaslangic = new DatePicker();

    Label lblBitis = new Label("Bitiş:");
    lblBitis.setTextFill(Color.web("#b2bec3"));
    DatePicker dpBitis = new DatePicker();

    Button btnFiltrele = new Button("🔍 Filtrele");
    btnFiltrele.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5;");

    Button btnSifirla = new Button("🔄 Sıfırla");
    btnSifirla.setStyle("-fx-background-color: #45475a; -fx-text-fill: #ffffff; -fx-cursor: hand; -fx-background-radius: 5;");

    filterBox.getChildren().addAll(lblBaslangic, dpBaslangic, lblBitis, dpBitis, btnFiltrele, btnSifirla);

    logTablosu = new TableView<>();
    logTablosu.setStyle("-fx-background-color: #141419; -fx-control-inner-background: #141419;");

    TableColumn<StockMovement, String> colLogId = new TableColumn<>("Log ID");
    colLogId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colLogId.setPrefWidth(70);

    TableColumn<StockMovement, String> colProdName = new TableColumn<>("Ürün Adı");
    colProdName.setCellValueFactory(new PropertyValueFactory<>("productName"));
    colProdName.setPrefWidth(160);

    TableColumn<StockMovement, String> colMoveType = new TableColumn<>("İşlem Türü");
    colMoveType.setCellValueFactory(new PropertyValueFactory<>("movementType"));
    colMoveType.setPrefWidth(110);

    TableColumn<StockMovement, Integer> colQty = new TableColumn<>("Miktar");
    colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
    colQty.setPrefWidth(80);

    TableColumn<StockMovement, String> colDesc = new TableColumn<>("Açıklama");
    colDesc.setCellValueFactory(new PropertyValueFactory<>("reason")); 
    colDesc.setPrefWidth(220);

    TableColumn<StockMovement, String> colUser = new TableColumn<>("İşlemi Yapan");
    colUser.setCellValueFactory(new PropertyValueFactory<>("islemiYapan"));
    colUser.setPrefWidth(150);

    TableColumn<StockMovement, LocalDateTime> colDate = new TableColumn<>("Tarih");
    colDate.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
    colDate.setPrefWidth(160);

    logTablosu.getColumns().clear();
    logTablosu.getColumns().addAll(colLogId, colProdName, colMoveType, colQty, colDesc, colUser, colDate);
    logTablosu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    Runnable tumLoglariYukle = () -> {
        try {
            logTablosu.getItems().clear();
            List<StockMovement> tumHareketler = warehouseService.getMovementHistory(); 
            logTablosu.setItems(FXCollections.observableArrayList(tumHareketler));
            System.out.println("🚀 [BAŞARILI] Log tablosu dolduruldu. Kayıt sayısı: " + tumHareketler.size());
        } catch (Exception e) {
            System.out.println("❌ Log listesi yüklenirken hata oluştu: " + e.getMessage());
        }
    };

    tumLoglariYukle.run();

    btnFiltrele.setOnAction(e -> {
        LocalDate baslangic = dpBaslangic.getValue();
        LocalDate bitis = dpBitis.getValue();

        if (baslangic == null || bitis == null) {
            alertGoster(Alert.AlertType.WARNING, "Eksik Tarih", "Lütfen başlangıç ve bitiş tarihlerini seçiniz.");
            return;
        }

        if (baslangic.isAfter(bitis)) {
            alertGoster(Alert.AlertType.ERROR, "Tarih Hatası", "Başlangıç tarihi bitiş tarihinden sonra olamaz!");
            return;
        }

        try {
            List<StockMovement> filtrelenmis = warehouseService.getLogsByDateRange(baslangic, bitis);
            logTablosu.setItems(FXCollections.observableArrayList(filtrelenmis));
        } catch (Exception ex) {
            alertGoster(Alert.AlertType.ERROR, "Hata", "Filtreleme esnasında hata oluştu: " + ex.getMessage());
        }
    });

    btnSifirla.setOnAction(e -> {
        dpBaslangic.setValue(null);
        dpBitis.setValue(null);
        tumLoglariYukle.run();
    });

    vbox.getChildren().addAll(lblTitle, filterBox, logTablosu);
    return vbox;
}

private void alertGoster(Alert.AlertType type, String baslik, String icerik) {
    if (icerik != null && (
        icerik.toLowerCase().contains("invalid column name 'id'") || 
        icerik.toLowerCase().contains("stock_movements")
    )) {
        System.out.println("⚠️ Önemsiz veritabanı uyarısı engellendi: " + icerik);
        return;
    }

    Alert alert = new Alert(type);
    alert.setTitle(baslik);
    alert.setHeaderText(null);
    alert.setContentText(icerik);
    alert.showAndWait();
}

private VBox olusturMetrikKarti(String baslik, Label lblDeger, String renkKodu) {
    VBox kart = new VBox(10);
    kart.setPadding(new Insets(20));
    kart.setPrefWidth(260);
    kart.setStyle("-fx-background-color: #141419; -fx-background-radius: 8; -fx-border-color: " + renkKodu + "; -fx-border-width: 0 0 0 5;");

    Label lblBaslik = new Label(baslik);
    lblBaslik.setFont(Font.font("System", FontWeight.BOLD, 12));
    lblBaslik.setTextFill(Color.web("#b2bec3"));

    lblDeger.setFont(Font.font("System", FontWeight.BOLD, 32));
    lblDeger.setTextFill(Color.WHITE);

    kart.getChildren().addAll(lblBaslik, lblDeger);
    return kart;
}

private void initializeSystemLogsTable() {
    if (tableSystemLogs == null) return;

    tableSystemLogs.getColumns().clear();

    TableColumn<Map, Integer> colId = new TableColumn<>("Log ID");
    colId.setCellValueFactory(new MapValueFactory<>("id"));

    TableColumn<Map, String> colProdName = new TableColumn<>("Ürün Adı");
    colProdName.setCellValueFactory(new MapValueFactory<>("productName"));

    TableColumn<Map, String> colType = new TableColumn<>("İşlem Türü");
    colType.setCellValueFactory(new MapValueFactory<>("movementType"));

    TableColumn<Map, Integer> colQty = new TableColumn<>("Miktar");
    colQty.setCellValueFactory(new MapValueFactory<>("quantity"));

    TableColumn<Map, String> colDesc = new TableColumn<>("Açıklama");
    colDesc.setCellValueFactory(new MapValueFactory<>("description"));

    TableColumn<Map, String> colOperator = new TableColumn<>("İşlemi Yapan");
    colOperator.setCellValueFactory(new MapValueFactory<>("operator"));

    TableColumn<Map, String> colDate = new TableColumn<>("Tarih");
    colDate.setCellValueFactory(new MapValueFactory<>("movementDate"));

    tableSystemLogs.getColumns().addAll(colId, colProdName, colType, colQty, colDesc, colOperator, colDate);
}

    public static void main(String[] args) {
        launch(args);
    }

private void sistemGunlukleriniYenileVeGoster() {
    try {
        List<StockMovement> güncelLoglar = warehouseService.getAllStockMovements();
        
        if (logTablosu != null) {
            logTablosu.setItems(FXCollections.observableArrayList(güncelLoglar));
            
            logTablosu.refresh(); 
            
            System.out.println("✅ Sistem Günlükleri tablosu tetiklendi. Kayıt Sayısı: " + güncelLoglar.size());
        }
    } catch (Exception e) {
        System.out.println("❌ Loglar yüklenirken hata oluştu: " + e.getMessage());
    }
}

// =================================================
// GÜNCELLENEN PERSONEL YÖNETİM PANELİ VE METOTLARI
// =================================================

private TableView<User> kullaniciTablosu;
private TableView<StockMovement> secilenKullaniciLogTablosu;

private VBox pencereKullaniciYonetimi() {
    VBox vbox = new VBox(15);
    vbox.setStyle("-fx-padding: 15;");

    Label lblTitle = new Label("👥 Personel Yönetimi & Yetki Kontrol Paneli");
    lblTitle.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 18));
    lblTitle.setTextFill(javafx.scene.paint.Color.WHITE);

    SplitPane splitPane = new SplitPane();
    splitPane.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");

    VBox solBolum = new VBox(10);
    solBolum.setPrefWidth(450);

    kullaniciTablosu = new TableView<>();
    kullaniciTablosu.setStyle("-fx-background-color: #141419; -fx-control-inner-background: #141419;");

    TableColumn<User, Integer> colUserId = new TableColumn<>("ID");
    colUserId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
    colUserId.setPrefWidth(50);

    TableColumn<User, String> colUsername = new TableColumn<>("Kullanıcı Adı");
    colUsername.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));
    colUsername.setPrefWidth(150);

    TableColumn<User, String> colRole = new TableColumn<>("Rol / Yetki");
    colRole.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("role"));
    colRole.setPrefWidth(100);

    kullaniciTablosu.getColumns().addAll(colUserId, colUsername, colRole);

    HBox butonlar = new HBox(10);
    Button btnSil = new Button("🗑️ Personeli Sil");
    btnSil.setStyle("-fx-background-color: #ff7675; -fx-text-fill: white; -fx-font-weight: bold;");
    
    Button btnRolDegistir = new Button("🛡️ Rolünü Değiştir");
    btnRolDegistir.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-font-weight: bold;");

    butonlar.getChildren().addAll(btnSil, btnRolDegistir);
    solBolum.getChildren().addAll(new Label("👤 Kayıtlı Personeller"), kullaniciTablosu, butonlar);

    VBox sagBolum = new VBox(10);
    secilenKullaniciLogTablosu = new TableView<>();
    secilenKullaniciLogTablosu.setStyle("-fx-background-color: #141419; -fx-control-inner-background: #141419;");

    TableColumn<StockMovement, String> colLogProd = new TableColumn<>("Ürün Adı");
    colLogProd.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
    
    TableColumn<StockMovement, String> colLogType = new TableColumn<>("İşlem");
    colLogType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("movementType"));
    
    TableColumn<StockMovement, Integer> colLogQty = new TableColumn<>("Miktar");
    colLogQty.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));

    secilenKullaniciLogTablosu.getColumns().addAll(colLogProd, colLogType, colLogQty);
    sagBolum.getChildren().addAll(new Label("📜 Seçilen Personelin İşlem Geçmişi"), secilenKullaniciLogTablosu);

    splitPane.getItems().addAll(solBolum, sagBolum);

    kullaniciTablosu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
            personelLoglariniYukle(newSelection.getId());
        }
    });

    btnSil.setOnAction(e -> {
    User secilen = kullaniciTablosu.getSelectionModel().getSelectedItem();
    if (secilen == null) {
        alertGoster(Alert.AlertType.WARNING, "Seçim Yapılmadı", "Lütfen silmek istediğiniz kullanıcıyı seçin!");
        return;
    }
    
    String oturumAcanKullanici = (this.aktifKullanici != null) ? this.aktifKullanici : "";
    
    if (secilen.getUsername().equalsIgnoreCase(oturumAcanKullanici)) {
        alertGoster(Alert.AlertType.ERROR, "İşlem Engellendi", "Şu an açık olan kendi oturumunuzu silemezsiniz!");
        return;
    }
    
    personelSil(secilen.getId(), secilen.getUsername());
});

    btnRolDegistir.setOnAction(e -> {
        User secilen = kullaniciTablosu.getSelectionModel().getSelectedItem();
        if (secilen == null) return;
        
        if (secilen.getUsername().equalsIgnoreCase("Nur")) {
            System.out.println("⚠️ Kendi yöneticilik rolünüzü değiştiremezsiniz!");
            return;
        }

        java.util.List<com.depo.model.Role> rolSecenekleri = java.util.Arrays.asList(com.depo.model.Role.values());

        javafx.scene.control.ChoiceDialog<com.depo.model.Role> dialog = new javafx.scene.control.ChoiceDialog<>(secilen.getRole(), rolSecenekleri);
        dialog.setTitle("🛡️ Yetki Düzenleme");
        dialog.setHeaderText("Personel: " + secilen.getUsername());
        dialog.setContentText("Lütfen yeni yetki seviyesini seçin:");
        
        dialog.getDialogPane().setStyle("-fx-background-color: #1e1e24; -fx-text-fill: white;");
        dialog.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: white;");

        java.util.Optional<com.depo.model.Role> sonuc = dialog.showAndWait();
        
        sonuc.ifPresent(yeniRol -> {
            personelRolGuncelle(secilen.getId(), yeniRol.toString());
        });
    });
    vbox.getChildren().addAll(lblTitle, splitPane);

    personelListesiniYukle(); 

    return vbox;
}

public void personelListesiniYukle() {
    List<User> kullanicilar = new ArrayList<>();
    
    String sql = "SELECT id, username, password_hash, role FROM dbo.users ORDER BY id ASC";
    
    try (java.sql.Connection conn = DatabaseConfig.getConnection();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
         java.sql.ResultSet rs = pstmt.executeQuery()) {
         
        while (rs.next()) {
            int uId = rs.getInt("id");
            String uName = rs.getString("username");
            String uPass = rs.getString("password_hash"); 
            String uRoleStr = rs.getString("role");

            com.depo.model.Role roleEnum = null;
            if (uRoleStr != null && !uRoleStr.trim().isEmpty()) {
                try {
                    roleEnum = com.depo.model.Role.valueOf(uRoleStr.toUpperCase().trim());
                } catch (IllegalArgumentException e) {
                    roleEnum = com.depo.model.Role.values()[0];
                }
            } else {
                roleEnum = com.depo.model.Role.values()[0];
            }

            User userInstance = new User(uId, uName, uPass != null ? uPass : "", roleEnum);

            kullanicilar.add(userInstance);
        }
        
        if (kullaniciTablosu != null) {
            kullaniciTablosu.setItems(javafx.collections.FXCollections.observableArrayList(kullanicilar));
            kullaniciTablosu.refresh();
            System.out.println("🚀 [BAŞARILI] Gerçek personel listesi yüklendi. Toplam Personel: " + kullanicilar.size());
        }
        
    } catch (Exception e) {
        System.out.println("❌ Personel listesi yüklenirken hata oluştu: " + e.getMessage());
        e.printStackTrace();
    }
}

private void personelLoglariniYukle(int userId) {
    List<StockMovement> kullaniciLoglari = new ArrayList<>();
    
    String sql = "SELECT sm.Id, sm.ProductId, sm.Quantity, sm.MovementType, sm.Description, sm.UserId, sm.Details, sm.MovementDate, " +
                 "p.Name AS productName, u.username AS gercekKullaniciAdi, u.role AS gercekKullaniciRolu " +
                 "FROM dbo.StockMovements sm " +
                 "LEFT JOIN dbo.Products p ON sm.ProductId = p.Id " +
                 "LEFT JOIN dbo.users u ON sm.UserId = u.id " + 
                 "WHERE sm.UserId = ? ORDER BY sm.MovementDate DESC";
                 
    try (java.sql.Connection conn = DatabaseConfig.getConnection();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        
        try (java.sql.ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                StockMovement sm = new StockMovement(
                    String.valueOf(rs.getInt("Id")), String.valueOf(rs.getInt("ProductId")),
                    rs.getString("MovementType"), rs.getInt("Quantity"),
                    rs.getString("Description"), rs.getInt("UserId"),
                    rs.getString("Details"), rs.getTimestamp("MovementDate").toLocalDateTime()
                );
                
                sm.setProductName(rs.getString("productName"));
                
                String dbUser = rs.getString("gercekKullaniciAdi");
                String dbRole = rs.getString("gercekKullaniciRolu");
                
                sm.setUserName(dbUser != null ? dbUser : "Bilinmeyen Personel");
                sm.setUserRole(dbRole != null ? dbRole : "USER");
                
                kullaniciLoglari.add(sm);
            }
        }
        
        if (secilenKullaniciLogTablosu != null) {
            secilenKullaniciLogTablosu.setItems(javafx.collections.FXCollections.observableArrayList(kullaniciLoglari));
        }
    } catch (java.sql.SQLException e) {
        System.out.println("❌ Kullanıcı logları yüklenirken hata: " + e.getMessage());
    }
}

private void personelSil(int id, String username) {
    String sql = "DELETE FROM dbo.users WHERE id = ?";
    try (java.sql.Connection conn = DatabaseConfig.getConnection();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setInt(1, id);
        int etkilenenSatir = pstmt.executeUpdate();
        
        if (etkilenenSatir > 0) {
            System.out.println("🗑️ [BAŞARILI] Personel veritabanından silindi: " + username);
            personelListesiniYukle();
            if (secilenKullaniciLogTablosu != null) {
                secilenKullaniciLogTablosu.getItems().clear();
            }
        }
    } catch (java.sql.SQLException e) {
        System.out.println("❌ Personel silinirken SQL hatası: " + e.getMessage());
        e.printStackTrace();
    }
}

private void personelRolGuncelle(int id, String yeniRol) {
    String sql = "UPDATE dbo.users SET role = ? WHERE id = ?";
    try (java.sql.Connection conn = DatabaseConfig.getConnection();
         java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, yeniRol.toUpperCase().trim());
        pstmt.setInt(2, id);
        int etkilenenSatir = pstmt.executeUpdate();
        
        if (etkilenenSatir > 0) {
            System.out.println("🛡️ [BAŞARILI] Rol güncellendi -> Yeni Rol: " + yeniRol);
            personelListesiniYukle();
        }
    } catch (java.sql.SQLException e) {
        System.out.println("❌ Rol güncellenirken SQL hatası: " + e.getMessage());
        e.printStackTrace();
    }
}

private VBox createSupplierDashboard() {
    VBox mainLayout = new VBox(20);
    mainLayout.setPadding(new Insets(20));

    Label lblHeader = new Label("📦 Tedarikçi Mal Takviye & Yönetim Paneli");
    lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

    HBox cardsContainer = new HBox(15);
    
    VBox cardKritik = createMetricCard("⚠️ KRİTİK STOK ALARMI", "0", "#e74c3c");
    Label lblKritikSayi = (Label) cardKritik.getChildren().get(1);

    VBox cardSonSevkiyat = createMetricCard("🕒 SON SEVKİYAT BİLGİSİ", "Henüz Yok", "#3498db");
    Label lblSonSevkiyat = (Label) cardSonSevkiyat.getChildren().get(1);

    cardsContainer.getChildren().addAll(cardKritik, cardSonSevkiyat);

    Label lblTableTitle = new Label("⚠️ Stok Seviyesi Kritik Düzeyde Olan Ürünler");
    lblTableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");

    TableView<Product> tblKritikUrunler = new TableView<>();
    tblKritikUrunler.setPlaceholder(new Label("🎉 Stok seviyesi kritik düzeyde olan bir ürün bulunmuyor."));

    Button btnHizliTakviye = new Button("⚡ Seçili Ürüne Stok Girişi Yap");
    btnHizliTakviye.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
    
    btnHizliTakviye.setOnAction(e -> {
        Product selected = tblKritikUrunler.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Stok Takviye Bilgisi");
            info.setHeaderText(null);
            info.setContentText("Lütfen 'Stok Giriş/Çıkış' sekmesine giderek " + selected.getId() + " ürün kodunu giriniz.");
            info.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seçim Yapılmadı");
            alert.setHeaderText(null);
            alert.setContentText("Lütfen takviye yapmak istediğiniz ürünü tablodan seçin.");
            alert.showAndWait();
        }
    });

    try {
        List<Product> kritikUrunler = warehouseService.getLowStockProducts();
        if (kritikUrunler != null) {
            tblKritikUrunler.setItems(FXCollections.observableArrayList(kritikUrunler));
            lblKritikSayi.setText(String.valueOf(kritikUrunler.size()));
        }

        int activeUserId = 1; 
        String sonSevkiyatTarihi = warehouseService.getLastSupplierMovementDate(activeUserId);
        lblSonSevkiyat.setText(sonSevkiyatTarihi);
    } catch (Exception ignored) {}

    mainLayout.getChildren().addAll(lblHeader, cardsContainer, lblTableTitle, tblKritikUrunler, btnHizliTakviye);
    return mainLayout;
}

private VBox createMetricCard(String title, String defaultValue, String accentColor) {
    VBox card = new VBox(10);
    card.setPadding(new Insets(15));
    card.setPrefWidth(250);
    card.setStyle("-fx-background-color: #2c3e50; -fx-background-radius: 8px; -fx-border-color: " + accentColor + "; -fx-border-width: 0 0 0 5px;");

    Label lblTitle = new Label(title);
    lblTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;");

    Label lblValue = new Label(defaultValue);
    lblValue.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

    card.getChildren().addAll(lblTitle, lblValue);
    return card;
}

private VBox createSupplierProfilePanel(int currentUserId) {
    VBox mainLayout = new VBox(20);
    mainLayout.setPadding(new Insets(25));
    mainLayout.setStyle("-fx-background-color: #1e1e2e;");

    Label lblHeader = new Label("🏢 Tedarikçi Firma & İletişim Bilgileri");
    lblHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

    Label lblSubHeader = new Label("Sistemde kayıtlı firmanıza ait iletişim, vergi ve açıklama bilgilerini buradan güncelleyebilirsiniz.");
    lblSubHeader.setStyle("-fx-font-size: 12px; -fx-text-fill: #a6adc8;");

    GridPane grid = new GridPane();
    grid.setHgap(15);
    grid.setVgap(15);
    grid.setPadding(new Insets(10, 0, 10, 0));

    TextField txtCompanyName = createStyledTextField("Firma / Şirket Adı");
    TextField txtContactPerson = createStyledTextField("Yetkili Temsilci / Kişi");
    TextField txtPhone = createStyledTextField("Telefon Numaranız");
    TextField txtEmail = createStyledTextField("E-posta Adresiniz");
    TextField txtTaxInfo = createStyledTextField("Vergi Numarası / Daire");
    
    TextArea txtDescription = new TextArea();
    txtDescription.setPromptText("Firma Hakkında Açıklama / Notlar");
    txtDescription.setPrefRowCount(3);
    txtDescription.setStyle("-fx-control-inner-background: #313244; -fx-text-fill: white; -fx-border-color: #45475a; -fx-border-radius: 5;");

    addFormField(grid, "Firma :", txtCompanyName, 0);
    addFormField(grid, "Yetkili Kişi:", txtContactPerson, 1);
    addFormField(grid, "Telefon:", txtPhone, 2);
    addFormField(grid, "E-posta:", txtEmail, 3);
    addFormField(grid, "Vergi No:", txtTaxInfo, 4);

    Label lblDesc = new Label("Açıklama:");
    lblDesc.setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold;");
    grid.add(lblDesc, 0, 5);
    grid.add(txtDescription, 1, 5);

    Button btnSave = new Button("💾 Firma Bilgilerini Kaydet");
    btnSave.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 5;");

    Map<String, String> profileData = warehouseService.getSupplierProfile(currentUserId);
    if (!profileData.isEmpty()) {
        txtCompanyName.setText(profileData.getOrDefault("CompanyName", ""));
        txtContactPerson.setText(profileData.getOrDefault("ContactPerson", ""));
        txtPhone.setText(profileData.getOrDefault("Phone", ""));
        txtEmail.setText(profileData.getOrDefault("Email", ""));
        txtTaxInfo.setText(profileData.getOrDefault("TaxInfo", profileData.getOrDefault("TaxNumber", "")));
        txtDescription.setText(profileData.getOrDefault("Description", ""));
    }

    btnSave.setOnAction(e -> {
        try {
            boolean success = warehouseService.updateSupplierProfile(
                currentUserId,
                txtCompanyName.getText().trim(),
                txtContactPerson.getText().trim(),
                txtPhone.getText().trim(),
                txtEmail.getText().trim(),
                txtTaxInfo.getText().trim(),
                txtDescription.getText().trim()
            );

            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle("Profil Güncelleme");
            alert.setHeaderText(null);
            alert.setContentText(success ? "Firma bilgileri başarıyla kaydedildi!" : "Güncelleme başarısız oldu.");
            alert.showAndWait();

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Hata");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    });

    mainLayout.getChildren().addAll(lblHeader, lblSubHeader, grid, btnSave);
    return mainLayout;
}

private TextField createStyledTextField(String prompt) {
    TextField tf = new TextField();
    tf.setPromptText(prompt);
    tf.setPrefWidth(300);
    tf.setStyle("-fx-background-color: #313244; -fx-text-fill: white; -fx-border-color: #45475a; -fx-border-radius: 5; -fx-padding: 8;");
    return tf;
}

private void addFormField(GridPane grid, String labelText, Control inputControl, int row) {
    Label label = new Label(labelText);
    label.setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold;");
    grid.add(label, 0, row);
    grid.add(inputControl, 1, row);
}

// ==========================================
// AUDITOR YETKİLENDİRME VE ARAYÜZ KONTROLLERİ
// ==========================================
public boolean checkAuditorRestriction(String userRole) {
    if ("AUDITOR".equalsIgnoreCase(userRole)) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Erişim Engellendi");
        alert.setHeaderText("Yetki Sınırı");
        alert.setContentText("AUDITOR (Denetçi) rolündeki kullanıcılar sistem üzerinde değişiklik yapamaz.");
        alert.showAndWait();
        return true;
    }
    return false; 
}

private void showErrorAlert(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Hata");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

/**
 * AUDITOR Özel Yetki : Geçmiş Stok Hareketleri
 */
public void showAuditorLogs() {
    try {
        List<String> logs = warehouseService.getStockMovementLogs();

        ListView<String> listView = new ListView<>();
        if (logs != null && !logs.isEmpty()) {
            listView.getItems().addAll(logs);
        } else {
            listView.getItems().add("Henüz kaydedilmiş bir stok hareketi bulunmuyor.");
        }
        listView.setPrefSize(550, 350);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("AUDITOR - Geçmiş Stok Hareket Logları");
        dialog.setHeaderText("Sistemdeki Tüm Geçmiş Stok Hareketleri");
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();

    } catch (Exception ex) {
        showErrorAlert("Log verileri alınarken hata oluştu: " + ex.getMessage());
    }
}


private VBox olusturEnvanterMaliRaporuSayfasi() {
    VBox mainLayout = new VBox(25);
    mainLayout.setPadding(new Insets(30));
    mainLayout.setStyle("-fx-background-color: #1e1e2e;");

    Label lblTitle = new Label("💰 Envanter Mali Değer & Analiz Raporu");
    lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold;");

    Label lblSubTitle = new Label("Depodaki tüm aktif stokların anlık maliyet analiz raporudur.");
    lblSubTitle.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 14px;");

    VBox headerBox = new VBox(6, lblTitle, lblSubTitle);

    double totalValue = 0.0;
    try {
        if (warehouseService != null) {
            totalValue = warehouseService.getTotalInventoryValue();
        }
    } catch (Exception e) {
        showErrorAlert("Mali rapor hesaplanırken hata oluştu: " + e.getMessage());
    }

    VBox cardTotalValue = new VBox(12);
    cardTotalValue.setPadding(new Insets(25));
    cardTotalValue.setMaxWidth(500);
    cardTotalValue.setStyle(
        "-fx-background-color: #2a2a3c; " +
        "-fx-border-color: #89b4fa; " +
        "-fx-border-width: 1.5; " +
        "-fx-border-radius: 10; " +
        "-fx-background-radius: 10;"
    );

    Label lblCardHeader = new Label("TOPLAM ENVANTER MALİ DEĞERİ");
    lblCardHeader.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 12px; -fx-font-weight: bold; -fx-letter-spacing: 1px;");

    Label lblValue = new Label(String.format("%,.2f TL", totalValue));
    lblValue.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 38px; -fx-font-weight: bold;");

    Label lblFooter = new Label("✔ Veritabanındaki güncel stok miktarları ve birim fiyatlar üzerinden anlık olarak hesaplanmıştır.");
    lblFooter.setStyle("-fx-text-fill: #bac2de; -fx-font-size: 12px; -fx-font-style: italic;");
    lblFooter.setWrapText(true);

    cardTotalValue.getChildren().addAll(lblCardHeader, lblValue, lblFooter);

    Button btnYenile = new Button("🔄 Verileri Yenile");
    btnYenile.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 6;");
    btnYenile.setOnAction(e -> navigasyonDegistir("EnvanterMaliRaporu"));

    mainLayout.getChildren().addAll(headerBox, cardTotalValue, btnYenile);
    return mainLayout;
}

private VBox pencereEnvanterMaliRaporu() {
    VBox mainLayout = new VBox(20);
    mainLayout.setPadding(new Insets(25));
    mainLayout.setStyle("-fx-background-color: #1e1e2e;");

    Label lblTitle = new Label("💰 Envanter Mali Değer & Analiz Raporu");
    lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold;");

    Label lblSubTitle = new Label("Depodaki tüm aktif stokların güncel fiyatlar üzerinden anlık maliyet analizi ve dağılımıdır.");
    lblSubTitle.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 13px;");

    VBox headerBox = new VBox(4, lblTitle, lblSubTitle);

    ComboBox<String> cmbKategori = new ComboBox<>();
    cmbKategori.getItems().add("Tüm Kategoriler");
    cmbKategori.setValue("Tüm Kategoriler");
    
    cmbKategori.setStyle(
        "-fx-background-color: #313244; " +
        "-fx-mark-color: #ffffff; " +
        "-fx-border-color: #89b4fa; " +
        "-fx-border-radius: 5; " +
        "-fx-font-weight: bold;"
    );

    cmbKategori.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item);
                setStyle("-fx-text-fill: #ffffff; -fx-background-color: #313244; -fx-padding: 6 10;");
            }
        }
    });

    cmbKategori.setButtonCell(new javafx.scene.control.ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item);
                setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
            }
        }
    });

    Button btnPDF = new Button("📄 Rapor İndir");
    btnPDF.setStyle("-fx-background-color: #313244; -fx-text-fill: #89b4fa; -fx-font-weight: bold; -fx-border-color: #89b4fa; -fx-border-radius: 5; -fx-cursor: hand; -fx-padding: 8 15;");

    Button btnYenile = new Button("🔄 Verileri Yenile");
    btnYenile.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-background-radius: 5;");
    btnYenile.setOnAction(e -> navigasyonDegistir("EnvanterMaliRaporu"));

    HBox actionBox = new HBox(10, cmbKategori, btnPDF, btnYenile);
    actionBox.setAlignment(Pos.CENTER_RIGHT);

    BorderPane topBar = new BorderPane();
    topBar.setLeft(headerBox);
    topBar.setRight(actionBox);

    double totalValue = 0.0;
    int toplamKalem = 0;
    String enDegerliUrun = "Veri Yok";
    ObservableList<TopProductModel> top5TableData = FXCollections.observableArrayList();
    Map<String, Double> categoryValueMap = new HashMap<>();

    try (Connection conn = DatabaseConfig.getConnection()) {
        
        if (warehouseService != null) {
            totalValue = warehouseService.getTotalInventoryValue();
        }

        String sqlCount = "SELECT COUNT(*) FROM dbo.Products";
        try (PreparedStatement stmt = conn.prepareStatement(sqlCount);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                toplamKalem = rs.getInt(1);
            }
        } catch (Exception ex) {
            System.out.println("⚠️ Count sorgusu uyarısı: " + ex.getMessage());
        }

        String sqlTop5 = "SELECT TOP 5 Name, Quantity, (Quantity * Price) AS total_val " +
                         "FROM dbo.Products " +
                         "ORDER BY total_val DESC";

        try (PreparedStatement stmt = conn.prepareStatement(sqlTop5);
             ResultSet rs = stmt.executeQuery()) {
            
            boolean firstRow = true;
            while (rs.next()) {
                String pName = rs.getString("Name");
                int qty = rs.getInt("Quantity");
                double val = rs.getDouble("total_val");
                String formattedVal = String.format("%,.2f TL", val);

                if (firstRow) {
                    enDegerliUrun = pName + " (" + formattedVal + ")";
                    firstRow = false;
                }

                top5TableData.add(new TopProductModel(pName, qty, formattedVal));
            }
        } catch (Exception ex) {
            System.out.println("❌ Top 5 Ürün Hatası: " + ex.getMessage());
        }

        String sqlCatList = "SELECT Name FROM dbo.Categories ORDER BY Name ASC";
        try (PreparedStatement stmtCat = conn.prepareStatement(sqlCatList);
             ResultSet rsCat = stmtCat.executeQuery()) {
            while (rsCat.next()) {
                String cName = rsCat.getString("Name");
                if (cName != null && !cName.trim().isEmpty()) {
                    cmbKategori.getItems().add(cName);
                }
            }
        } catch (Exception exCat) {
            System.out.println("❌ Kategori listesi çekme hatası: " + exCat.getMessage());
        }

        String sqlCatDist = "SELECT ISNULL(c.Name, 'Kategorisiz') AS CategoryName, " +
                            "SUM(p.Quantity * p.Price) AS CategoryTotal " +
                            "FROM dbo.Products p " +
                            "LEFT JOIN dbo.Categories c ON p.CategoryId = c.Id " +
                            "GROUP BY c.Name";

        try (PreparedStatement stmtDist = conn.prepareStatement(sqlCatDist);
             ResultSet rsDist = stmtDist.executeQuery()) {
            while (rsDist.next()) {
                String catName = rsDist.getString("CategoryName");
                double catTotal = rsDist.getDouble("CategoryTotal");
                if (catTotal > 0) {
                    categoryValueMap.put(catName, catTotal);
                }
            }
        } catch (Exception exDist) {
            System.out.println("❌ Kategori maliyet dağılımı hatası: " + exDist.getMessage());
        }

    } catch (Exception e) {
        System.out.println("❌ Veritabanı bağlantı hatası: " + e.getMessage());
    }

    HBox kpiContainer = new HBox(15);
    kpiContainer.setMaxWidth(Double.MAX_VALUE);

    VBox cardTotalValue = olusturKPICard("TOPLAM ENVANTER MALİ DEĞERİ", String.format("%,.2f TL", totalValue), "#a6e3a1");
    VBox cardTotalCount = olusturKPICard("TOPLAM ÜRÜN KALEMİ", toplamKalem + " Çeşit Ürün", "#89b4fa");
    VBox cardTopProduct = olusturKPICard("EN DEĞERLİ ÜRÜN KALEMİ", enDegerliUrun, "#f9e2af");

    kpiContainer.getChildren().addAll(cardTotalValue, cardTotalCount, cardTopProduct);
    HBox.setHgrow(cardTotalValue, Priority.ALWAYS);
    HBox.setHgrow(cardTotalCount, Priority.ALWAYS);
    HBox.setHgrow(cardTopProduct, Priority.ALWAYS);

    HBox bottomContainer = new HBox(20);
    bottomContainer.setMaxWidth(Double.MAX_VALUE);

    VBox chartBox = new VBox(10);
    chartBox.setPadding(new Insets(15));
    chartBox.setStyle("-fx-background-color: #2a2a3c; -fx-background-radius: 8; -fx-border-color: #45475a; -fx-border-radius: 8;");

    Label lblChartTitle = new Label("📊 Kategori Bazlı Maliyet Dağılımı");
    lblChartTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");

    PieChart pieChart = new PieChart();
    ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
    
    if (!categoryValueMap.isEmpty()) {
        for (Map.Entry<String, Double> entry : categoryValueMap.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    } else {
        pieData.add(new PieChart.Data("Veri Yok", totalValue));
    }
    pieChart.setData(pieData);
    pieChart.setLegendVisible(true);
    pieChart.setPrefHeight(250);

    chartBox.getChildren().addAll(lblChartTitle, pieChart);
    HBox.setHgrow(chartBox, Priority.ALWAYS);

    VBox tableBox = new VBox(10);
    tableBox.setPadding(new Insets(15));
    tableBox.setStyle("-fx-background-color: #2a2a3c; -fx-background-radius: 8; -fx-border-color: #45475a; -fx-border-radius: 8;");

    Label lblTableTitle = new Label("🔝 En Değerli 5 Ürün (Maliyet Bazlı)");
    lblTableTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 14px;");

    TableView<TopProductModel> topProductsTable = new TableView<>();
    topProductsTable.setStyle("-fx-background-color: #1e1e2e; -fx-control-inner-background: #1e1e2e;");
    topProductsTable.setPrefHeight(250);

    TableColumn<TopProductModel, String> colName = new TableColumn<>("Ürün Adı");
    colName.setCellValueFactory(new PropertyValueFactory<>("productName"));

    TableColumn<TopProductModel, Integer> colQty = new TableColumn<>("Miktar");
    colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));

    TableColumn<TopProductModel, String> colVal = new TableColumn<>("Mali Değer");
    colVal.setCellValueFactory(new PropertyValueFactory<>("totalValue"));

    topProductsTable.getColumns().addAll(colName, colQty, colVal);
    topProductsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    topProductsTable.setItems(top5TableData);

    tableBox.getChildren().addAll(lblTableTitle, topProductsTable);
    HBox.setHgrow(tableBox, Priority.ALWAYS);

    bottomContainer.getChildren().addAll(chartBox, tableBox);

    final double fTotalVal = totalValue;
    final int fTotalCount = toplamKalem;
    btnPDF.setOnAction(e -> {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Mali Raporu Kaydet");
        fileChooser.setInitialFileName("Envanter_Mali_Rapor.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Metin Dosyası (*.txt)", "*.txt"));

        File file = fileChooser.showSaveDialog(btnPDF.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("=================================================");
                writer.println("        DEPOX ERP - ENVANTER MALİ RAPORU         ");
                writer.println("=================================================");
                writer.println("Toplam Envanter Değeri : " + String.format("%,.2f TL", fTotalVal));
                writer.println("Toplam Ürün Kalemi     : " + fTotalCount + " Çeşit");
                writer.println("Seçilen Kategori       : " + cmbKategori.getValue());
                writer.println("-------------------------------------------------");
                writer.println("EN DEĞERLİ ÜRÜNLER:");
                for (TopProductModel item : top5TableData) {
                    writer.println("- " + item.getProductName() + " | Adet: " + item.getQuantity() + " | Değer: " + item.getTotalValue());
                }
                writer.println("=================================================");

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Rapor başarıyla kaydedildi:\n" + file.getAbsolutePath());
                alert.show();
            } catch (Exception ex) {
                showErrorAlert("Dosya yazdırılamadı: " + ex.getMessage());
            }
        }
    });

    mainLayout.getChildren().addAll(topBar, kpiContainer, bottomContainer);
    return mainLayout;
}

    public static class TopProductModel {
        private final String productName;
        private final int quantity;
        private final String totalValue;

        public TopProductModel(String productName, int quantity, String totalValue) {
            this.productName = productName;
            this.quantity = quantity;
            this.totalValue = totalValue;
        }

        public String getProductName() { 
            return productName; 
        }
        
        public int getQuantity() { 
            return quantity; 
        }
        
        public String getTotalValue() { 
            return totalValue; 
        }
    }
private VBox olusturKPICard(String baslik, String deger, String renkHex) {
    VBox card = new VBox(8);
    card.setPadding(new Insets(18));
    card.setStyle(
        "-fx-background-color: #2a2a3c; " +
        "-fx-border-color: " + renkHex + "; " +
        "-fx-border-width: 0 0 0 4; " +
        "-fx-border-radius: 6; " +
        "-fx-background-radius: 6;"
    );

    Label lblTitle = new Label(baslik);
    lblTitle.setStyle("-fx-text-fill: #89b4fa; -fx-font-size: 11px; -fx-font-weight: bold;");

    Label lblValue = new Label(deger);
    lblValue.setStyle("-fx-text-fill: " + renkHex + "; -fx-font-size: 20px; -fx-font-weight: bold;");

    card.getChildren().addAll(lblTitle, lblValue);
    return card;
}

private VBox pencereFirmaYonetimi() {
    VBox mainLayout = new VBox(20);
    mainLayout.setPadding(new Insets(25));
    mainLayout.setStyle("-fx-background-color: #1e1e2e;");

    if (!"ADMIN".equalsIgnoreCase(aktifRol)) {
        VBox errorBox = new VBox(15);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));
        
        Label lblAccessDenied = new Label("⛔ ERİŞİM ENGELLENDİ");
        lblAccessDenied.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblAccessDenied.setTextFill(Color.web("#f38ba8"));

        Label lblMsg = new Label("Firma bilgilerini görüntüleme ve güncelleme yetkisi sadece ADMIN rolüne aittir.");
        lblMsg.setFont(Font.font("System", FontWeight.NORMAL, 14));
        lblMsg.setTextFill(Color.web("#a6adc8"));

        errorBox.getChildren().addAll(lblAccessDenied, lblMsg);
        return errorBox;
    }

    Label lblTitle = new Label("🏢 Firma ve Tedarikçi Yönetim Paneli");
    lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold;");

    Label lblSubTitle = new Label("Sistemde kayıtlı tüm firma bilgilerini görüntüleyin, arayın, güncelleyin veya silin.");
    lblSubTitle.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 13px;");

    VBox headerBox = new VBox(4, lblTitle, lblSubTitle);

    TextField txtSearch = new TextField();
    txtSearch.setPromptText("🔍 Firma Adı, Yetkili veya Vergi No Ara...");
    txtSearch.setPrefWidth(280);
    txtSearch.setStyle("-fx-background-color: #313244; -fx-text-fill: white; -fx-padding: 8; -fx-background-radius: 5; -fx-border-color: #45475a; -fx-border-radius: 5;");

    Button btnYenile = new Button("🔄 Yenile");
    btnYenile.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 8 15; -fx-cursor: hand; -fx-background-radius: 5;");
    btnYenile.setOnAction(e -> navigasyonDegistir("FirmaYonetimi"));

    HBox actionBox = new HBox(10, txtSearch, btnYenile);
    actionBox.setAlignment(Pos.CENTER_RIGHT);

    BorderPane topBar = new BorderPane();
    topBar.setLeft(headerBox);
    topBar.setRight(actionBox);

    TableView<CompanyModel> tableCompanies = new TableView<>();
    tableCompanies.setStyle("-fx-background-color: #181825; -fx-control-inner-background: #1e1e2e; -fx-table-cell-border-color: #313244;");
    tableCompanies.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(tableCompanies, Priority.ALWAYS);

    TableColumn<CompanyModel, Integer> colId = new TableColumn<>("ID");
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colId.setMaxWidth(60);

    TableColumn<CompanyModel, String> colName = new TableColumn<>("Firma Adı");
    colName.setCellValueFactory(new PropertyValueFactory<>("companyName"));

    TableColumn<CompanyModel, String> colContact = new TableColumn<>("Yetkili Kişi");
    colContact.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));

    TableColumn<CompanyModel, String> colPhone = new TableColumn<>("Telefon");
    colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

    TableColumn<CompanyModel, String> colEmail = new TableColumn<>("E-Posta");
    colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

    TableColumn<CompanyModel, String> colTax = new TableColumn<>("Vergi No / Daire");
    colTax.setCellValueFactory(new PropertyValueFactory<>("taxNumber"));

    TableColumn<CompanyModel, String> colDescription = new TableColumn<>("Açıklama");
    colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

    tableCompanies.getColumns().setAll(
        colId, 
        colName, 
        colContact, 
        colPhone, 
        colEmail, 
        colTax, 
        colDescription
    );

    ObservableList<CompanyModel> companyList = FXCollections.observableArrayList();
    String sqlSelect = "SELECT Id, Name, ContactPerson, Phone, Email, TaxNumber, Description FROM dbo.Suppliers";

    try (java.sql.Connection conn = DatabaseConfig.getConnection();
         java.sql.PreparedStatement stmt = conn.prepareStatement(sqlSelect);
         java.sql.ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            int id = rs.getInt("Id");
            String name = rs.getString("Name");
            String contact = rs.getString("ContactPerson") != null ? rs.getString("ContactPerson") : "Belirtilmedi";
            String phone = rs.getString("Phone") != null ? rs.getString("Phone") : "Girilmedi";
            String email = rs.getString("Email") != null ? rs.getString("Email") : "Belirtilmedi";
            String taxNo = rs.getString("TaxNumber") != null ? rs.getString("TaxNumber") : "-";
            String desc = rs.getString("Description") != null ? rs.getString("Description") : "-";

            companyList.add(new CompanyModel(id, name, contact, phone, email, taxNo, desc));
        }
    } catch (Exception ex) {
        System.out.println("❌ Firma listesi çekme hatası: " + ex.getMessage());
        ex.printStackTrace();
    }

    FilteredList<CompanyModel> filteredData = new FilteredList<>(companyList, p -> true);

    txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
        filteredData.setPredicate(company -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                return true;
            }

            String lowerCaseFilter = newValue.trim().toLowerCase(java.util.Locale.forLanguageTag("tr"));

            if (company == null) return false;

            if (company.getCompanyName() != null && 
                company.getCompanyName().toLowerCase(java.util.Locale.forLanguageTag("tr")).contains(lowerCaseFilter)) {
                return true;
            }
            if (company.getContactPerson() != null && 
                company.getContactPerson().toLowerCase(java.util.Locale.forLanguageTag("tr")).contains(lowerCaseFilter)) {
                return true;
            }
            if (company.getTaxNumber() != null && 
                company.getTaxNumber().toLowerCase(java.util.Locale.forLanguageTag("tr")).contains(lowerCaseFilter)) {
                return true;
            }
            if (company.getDescription() != null && 
                company.getDescription().toLowerCase(java.util.Locale.forLanguageTag("tr")).contains(lowerCaseFilter)) {
                return true;
            }

            return false;
        });
    });

    tableCompanies.setItems(filteredData);

    VBox editCard = new VBox(12);
    editCard.setPadding(new Insets(15));
    editCard.setStyle("-fx-background-color: #2a2a3c; -fx-background-radius: 8; -fx-border-color: #45475a; -fx-border-radius: 8;");

    Label lblEditTitle = new Label("✏️ Seçili Firma Bilgilerini Güncelle / Sil");
    lblEditTitle.setStyle("-fx-text-fill: #f9e2af; -fx-font-weight: bold; -fx-font-size: 14px;");

    GridPane formGrid = new GridPane();
    formGrid.setHgap(15);
    formGrid.setVgap(10);

    TextField txtEditName = createStyledTextField("Firma Adı");
    TextField txtEditContact = createStyledTextField("Yetkili Kişi");
    TextField txtEditPhone = createStyledTextField("Telefon");
    TextField txtEditEmail = createStyledTextField("E-Posta");
    TextField txtEditTax = createStyledTextField("Vergi No");
    TextField txtEditDesc = createStyledTextField("Açıklama");

    formGrid.add(new Label("Firma Adı:"), 0, 0); formGrid.add(txtEditName, 1, 0);
    formGrid.add(new Label("Yetkili Kişi:"), 2, 0); formGrid.add(txtEditContact, 3, 0);
    formGrid.add(new Label("Telefon:"), 0, 1); formGrid.add(txtEditPhone, 1, 1);
    formGrid.add(new Label("E-Posta:"), 2, 1); formGrid.add(txtEditEmail, 3, 1);
    formGrid.add(new Label("Vergi No:"), 0, 2); formGrid.add(txtEditTax, 1, 2);
    formGrid.add(new Label("Açıklama:"), 2, 2); formGrid.add(txtEditDesc, 3, 2);

    formGrid.getChildren().stream()
            .filter(node -> node instanceof Label)
            .forEach(node -> ((Label) node).setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold;"));

    Button btnKaydet = new Button("💾 Değişiklikleri Kaydet");
    btnKaydet.setStyle("-fx-background-color: #a6e3a1; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 5;");

    Button btnSil = new Button("🗑️ Firmayı Sil");
    btnSil.setStyle("-fx-background-color: #f38ba8; -fx-text-fill: #11111b; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 5;");

    final CompanyModel[] selectedCompany = new CompanyModel[1];
    tableCompanies.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        if (newVal != null) {
            selectedCompany[0] = newVal;
            txtEditName.setText(newVal.getCompanyName());
            txtEditContact.setText(newVal.getContactPerson());
            txtEditPhone.setText(newVal.getPhone());
            txtEditEmail.setText(newVal.getEmail());
            txtEditTax.setText(newVal.getTaxNumber());
            txtEditDesc.setText(newVal.getDescription());
        }
    });


btnKaydet.setOnAction(e -> {
    if (selectedCompany[0] == null) {
        alertGoster(Alert.AlertType.WARNING, "Seçim Yapılmadı", "Lütfen tablodan güncellemek istediğiniz firmayı seçin!");
        return;
    }

    String sqlUpdate = "UPDATE dbo.Suppliers SET Name=?, ContactPerson=?, Phone=?, Email=?, TaxNumber=?, Description=? WHERE Id=?";
    
    String sqlLog = "INSERT INTO dbo.StockMovements (MovementType, Description, Quantity, UserId, MovementDate) " +
                    "VALUES (?, ?, ?, (SELECT TOP 1 id FROM dbo.users WHERE username = ?), GETDATE())";

    try (java.sql.Connection conn = DatabaseConfig.getConnection()) {
        conn.setAutoCommit(false);

        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlUpdate);
             java.sql.PreparedStatement stmtLog = conn.prepareStatement(sqlLog)) {

            stmt.setString(1, txtEditName.getText().trim());
            stmt.setString(2, txtEditContact.getText().trim());
            stmt.setString(3, txtEditPhone.getText().trim());
            stmt.setString(4, txtEditEmail.getText().trim());
            stmt.setString(5, txtEditTax.getText().trim());
            stmt.setString(6, txtEditDesc.getText().trim());
            stmt.setInt(7, selectedCompany[0].getId());

            int updatedRows = stmt.executeUpdate();

            if (updatedRows > 0) {
                String logDetay = "Firma ID: " + selectedCompany[0].getId() + 
                                  " | Eski Adı: '" + selectedCompany[0].getCompanyName() + 
                                  "' -> Yeni Adı: '" + txtEditName.getText().trim() + "'";

                stmtLog.setString(1, "FİRMA GÜNCELLEME");
                stmtLog.setString(2, logDetay);
                stmtLog.setInt(3, 0); 
                
                stmtLog.setString(4, this.aktifKullanici); 

                stmtLog.executeUpdate();

                conn.commit(); 

                alertGoster(Alert.AlertType.INFORMATION, "Başarılı", "Firma bilgileri güncellendi.");
                navigasyonDegistir("FirmaYonetimi");
            } else {
                conn.rollback();
            }

        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        }
    } catch (Exception ex) {
        alertGoster(Alert.AlertType.ERROR, "Hata", "Firma güncellenirken hata oluştu: " + ex.getMessage());
    }
});

    btnSil.setOnAction(e -> {
        if (selectedCompany[0] == null) {
            alertGoster(Alert.AlertType.WARNING, "Seçim Yapılmadı", "Lütfen silmek istediğiniz firmayı tablodan seçin!");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Firma Silme Onayı");
        confirmAlert.setHeaderText("Firma Silinecek: " + selectedCompany[0].getCompanyName());
        confirmAlert.setContentText("Bu firmayı silmek istediğinizden emin misiniz? Firma silindiğinde bağlı olan ürünlerin tedarikçi bilgisi kaldırılacaktır (NULL yapılacaktır).");

        java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            String sqlUpdateProducts = "UPDATE dbo.Products SET SupplierId = NULL WHERE SupplierId = ?";
            String sqlDeleteSupplier = "DELETE FROM dbo.Suppliers WHERE Id = ?";

            try (java.sql.Connection conn = DatabaseConfig.getConnection()) {
                conn.setAutoCommit(false);

                try (java.sql.PreparedStatement stmt1 = conn.prepareStatement(sqlUpdateProducts);
                     java.sql.PreparedStatement stmt2 = conn.prepareStatement(sqlDeleteSupplier)) {

                    stmt1.setInt(1, selectedCompany[0].getId());
                    stmt1.executeUpdate();

                    stmt2.setInt(1, selectedCompany[0].getId());
                    int deletedRows = stmt2.executeUpdate();

                    conn.commit(); 

                    try (java.sql.PreparedStatement stmtLog = conn.prepareStatement(
                            "INSERT INTO dbo.SystemLogs (Action, Details, CreatedAt) VALUES (?, ?, GETDATE())")) {
                        stmtLog.setString(1, "FİRMA SİLİNDİ");
                        stmtLog.setString(2, "Silinen Firma ID: " + selectedCompany[0].getId() + " | Adı: " + selectedCompany[0].getCompanyName());
                        stmtLog.executeUpdate();
                    } catch (Exception ignored) {
                    }

                    if (deletedRows > 0) {
                        alertGoster(Alert.AlertType.INFORMATION, "Başarılı", "Firma silindi, bağlı ürünlerin tedarikçi bağı kaldırıldı.");
                        navigasyonDegistir("FirmaYonetimi");
                    }
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            } catch (Exception ex) {
                alertGoster(Alert.AlertType.ERROR, "Silme Hatası", "İşlem sırasında hata oluştu: " + ex.getMessage());
            }
        }
    });

    HBox btnBox = new HBox(10, btnSil, btnKaydet);
    btnBox.setAlignment(Pos.CENTER_RIGHT);

    editCard.getChildren().addAll(lblEditTitle, formGrid, btnBox);

    mainLayout.getChildren().addAll(topBar, tableCompanies, editCard);
    return mainLayout;
}

}
