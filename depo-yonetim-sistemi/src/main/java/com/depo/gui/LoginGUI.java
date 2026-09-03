package com.depo.gui;

import com.depo.service.AuthService;
import com.depo.model.Role; 
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginGUI {

    private final AuthService authService = new AuthService();
    private final Stage stage;

    public LoginGUI(Stage stage) {
        this.stage = stage;
    }

    public void gosterLoginEkrani() {
        stage.setTitle("🔐 DEPOX ERP - Güvenli Giriş");

        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(30));
        vbox.setStyle("-fx-background-color: #1e1e24;");

        Label lblTitle = new Label("DEPOX ERP SİSTEMİ");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#00adb5"));

        Label lblSubTitle = new Label("Lütfen kimlik bilgilerinizi doğrulayın");
        lblSubTitle.setTextFill(Color.LIGHTGRAY);

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Kullanıcı Adı");
        txtUsername.setMaxWidth(280);
        txtUsername.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 10;");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Şifre");
        txtPassword.setMaxWidth(280);
        txtPassword.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 10;");

        Button btnLogin = new Button("Giriş Yap");
        btnLogin.setMaxWidth(280);
        btnLogin.setStyle("-fx-background-color: #00adb5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");

        Hyperlink linkRegister = new Hyperlink("Hesabınız yok mu? Yeni kayıt oluşturun");
        linkRegister.setTextFill(Color.web("#b2bec3"));

        Hyperlink linkForgotPassword = new Hyperlink("Şifremi Unuttum");
        linkForgotPassword.setTextFill(Color.web("#b2bec3"));
        linkForgotPassword.setStyle("-fx-underline: true;");

        Label lblHata = new Label();
        lblHata.setTextFill(Color.web("#e74c3c"));

        btnLogin.setOnAction(e -> {
            String user = txtUsername.getText().trim();
            String pass = txtPassword.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                lblHata.setText("Alanlar boş bırakılamaz!");
            } else if (authService.login(user, pass)) {
                AuthService.UserDetails details = authService.getUserDetails(user);
                WarehouseGUI anaEkran = new WarehouseGUI(user, details.getRole(), details.getId());
                try {
                    anaEkran.anaPaneliGoster(stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                lblHata.setText("Hatalı kullanıcı adı veya şifre!");
            }
        });

        linkRegister.setOnAction(e -> gosterRegisterEkrani());
        linkForgotPassword.setOnAction(e -> sifremiUnuttumPenceresiniAc());

        vbox.getChildren().addAll(lblTitle, lblSubTitle, txtUsername, txtPassword, btnLogin, linkRegister, linkForgotPassword, lblHata);
        
        Scene scene = new Scene(vbox, 400, 480);
        stage.setScene(scene);
        stage.show();
    }

    private void sifremiUnuttumPenceresiniAc() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("🔒 Şifre Sıfırlama");

        VBox layout = new VBox(12);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #1e1e24;");

        Label lblInfo = new Label("Hesap bilgilerinizi girerek şifrenizi sıfırlayın");
        lblInfo.setTextFill(Color.WHITE);
        lblInfo.setFont(Font.font("System", FontWeight.BOLD, 13));

        TextField txtUser = new TextField();
        txtUser.setPromptText("Kullanıcı Adınız");
        txtUser.setMaxWidth(240);
        txtUser.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 8;");

        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Yeni Şifre");
        txtNewPass.setMaxWidth(240);
        txtNewPass.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 8;");

        PasswordField txtNewPassConfirm = new PasswordField();
        txtNewPassConfirm.setPromptText("Yeni Şifre (Tekrar)");
        txtNewPassConfirm.setMaxWidth(240);
        txtNewPassConfirm.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 8;");

        Label lblPopUpMesaj = new Label();
        lblPopUpMesaj.setTextFill(Color.web("#e74c3c"));
        lblPopUpMesaj.setWrapText(true);
        lblPopUpMesaj.setMaxWidth(260);

        Button btnGuncelle = new Button("Şifreyi Sıfırla");
        btnGuncelle.setMaxWidth(240);
        btnGuncelle.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8; -fx-cursor: hand;");

        btnGuncelle.setOnAction(e -> {
            String user = txtUser.getText().trim();
            String pass = txtNewPass.getText();
            String passConf = txtNewPassConfirm.getText();

            if (user.isEmpty() || pass.isEmpty() || passConf.isEmpty()) {
                lblPopUpMesaj.setTextFill(Color.web("#e74c3c"));
                lblPopUpMesaj.setText("Tüm alanları doldurmanız şart!");
                return;
            }

            if (!pass.equals(passConf)) {
                lblPopUpMesaj.setTextFill(Color.web("#e74c3c"));
                lblPopUpMesaj.setText("Şifreler birbiriyle uyuşmuyor!");
                return;
            }

            String hata = sifreKontrolEt(pass);
            if (hata != null) {
                lblPopUpMesaj.setTextFill(Color.web("#e74c3c"));
                lblPopUpMesaj.setText(hata);
                return;
            }

            if (authService.updatePassword(user, pass)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Başarılı");
                alert.setHeaderText(null);
                alert.setContentText("🔒 Şifreniz başarıyla güncellendi! Yeni şifrenizle giriş yapabilirsiniz.");
                alert.showAndWait();
                dialog.close();
            } else {
                lblPopUpMesaj.setTextFill(Color.web("#e74c3c"));
                lblPopUpMesaj.setText("Kullanıcı adı bulunamadı!");
            }
        });

        layout.getChildren().addAll(lblInfo, txtUser, txtNewPass, txtNewPassConfirm, btnGuncelle, lblPopUpMesaj);
        Scene scene = new Scene(layout, 340, 320);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.showAndWait();
    }


    public void gosterRegisterEkrani() {
        stage.setTitle("📝 DEPOX ERP - Yeni Kullanıcı Kaydı");

        VBox vbox = new VBox(15);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(30));
        vbox.setStyle("-fx-background-color: #1e1e24;");

        Label lblTitle = new Label("YENİ HESAP OLUŞTUR");
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web("#2ecc71"));

        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Yeni Kullanıcı Adı");
        txtUsername.setMaxWidth(280);
        txtUsername.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 10;");

        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Şifre (Min 6 hane, harf, sayı ve simge)");
        txtPassword.setMaxWidth(280);
        txtPassword.setStyle("-fx-background-color: #141419; -fx-text-fill: white; -fx-padding: 10;");

        ComboBox<Role> comboRole = new ComboBox<>();
        comboRole.getItems().addAll(Role.values()); 
        comboRole.setValue(Role.STAFF); 
        comboRole.setMaxWidth(280);
        comboRole.setStyle("-fx-background-color: #141419; -fx-text-fill: white;");

        Button btnRegister = new Button("Kayıt Ol ve Veritabanına İşle");
        btnRegister.setMaxWidth(280);
        btnRegister.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");

        Hyperlink linkLogin = new Hyperlink("Zaten hesabınız var mı? Giriş yapın");
        linkLogin.setTextFill(Color.web("#b2bec3"));

        Label lblMesaj = new Label();
        lblMesaj.setWrapText(true);
        lblMesaj.setMaxWidth(280);

        btnRegister.setOnAction(e -> {
            String user = txtUsername.getText().trim();
            String pass = txtPassword.getText();
            Role selectedRole = comboRole.getValue(); 

            if (user.isEmpty() || pass.isEmpty()) {
                lblMesaj.setTextFill(Color.web("#e74c3c"));
                lblMesaj.setText("Lütfen tüm alanları doldurun!");
                return;
            }

            String hata = sifreKontrolEt(pass);
            if (hata != null) {
                lblMesaj.setTextFill(Color.web("#e74c3c"));
                lblMesaj.setText(hata);
                return;
            }

            if (authService.register(user, pass, selectedRole.name())) { 
                lblMesaj.setTextFill(Color.web("#2ecc71"));
                lblMesaj.setText("Kayıt başarılı! Giriş yapabilirsiniz.");
                txtUsername.clear();
                txtPassword.clear();
            } else {
                lblMesaj.setTextFill(Color.web("#e74c3c"));
                lblMesaj.setText("Kullanıcı adı zaten alınmış!");
            }
        });

        linkLogin.setOnAction(e -> gosterLoginEkrani());

        vbox.getChildren().addAll(lblTitle, txtUsername, txtPassword, comboRole, btnRegister, linkLogin, lblMesaj);
        
        Scene scene = new Scene(vbox, 400, 470);
        stage.setScene(scene);
    }


    private String sifreKontrolEt(String sifre) {
        if (sifre == null || sifre.isEmpty()) {
            return "Şifre alanı boş bırakılamaz!";
        }
        
        if (sifre.length() < 6) {
            return "Şifre en az 6 karakter uzunluğunda olmalıdır!";
        }

        boolean harfVar = false;
        boolean rakamVar = false;
        boolean simgeVar = false;

        for (char c : sifre.toCharArray()) {
            if (Character.isLetter(c)) {
                harfVar = true;
            } else if (Character.isDigit(c)) {
                rakamVar = true;
            } else {
                simgeVar = true;
            }
        }

        if (!harfVar) {
            return "Şifre en az 1 adet harf (a-z, A-Z) içermelidir!";
        }

        if (!rakamVar) {
            return "Şifre en az 1 adet sayı (0-9) içermelidir!";
        }

        if (!simgeVar) {
            return "Şifre en az 1 adet özel simge (!, @, #, $, %, vb.) içermelidir!";
        }

        return null;
    }
}
