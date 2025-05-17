package org.projeti.controllers;

import com.google.api.services.oauth2.model.Userinfo;
import org.projeti.Service.UserService;
import org.projeti.utils.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.projeti.Service.UserSession;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;
import javafx.scene.input.MouseEvent;

public class SigninController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();
    private Connection cnx = Database.getInstance().getCnx();
    private int failedAttempts = 0; // Counter for failed login attempts

    @FXML
    private void handleSignUp(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Forgin.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the forgot password page.");
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        String role = getUserRole(email, password);

        if (role == null) {
            failedAttempts++;
            showAlert("Login Failed", "Invalid email or password.");

            if (failedAttempts >= 3) {
                sendSecurityAlert(email);
            }
            return;
        }

        failedAttempts = 0; // Reset failed attempts on successful login

        // Set user session data based on the role
        UserSession.setEmail(email);
        UserSession.setRole(role);
        UserSession.setName(getUserName(email));

        String fxmlFile = "admin".equalsIgnoreCase(role) ? "/adminInterface.fxml" : "/Main.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the page.");
        }
    }

    private String getUserRole(String email, String password) {
        String query = "SELECT Role FROM user WHERE Email = ? AND MDP = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
        return null;
    }

    private String getUserName(String email) {
        String query = "SELECT Nom FROM user WHERE Email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nom");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }

    private void sendSecurityAlert(String email) {
        final String senderEmail = "linatekaya00@gmail.com";
        final String senderPassword = "gmre fcoi mlktzbvb";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Security Alert: Suspicious Login Attempts");

            // **Styled Email Template**
            String emailContent = "<html>" +
                    "<head><style>" +
                    "body { margin: 0; padding: 0; font-family: Arial, sans-serif; text-align: center; }" +
                    ".email-container { background: #D32F2F; padding: 40px 0; border-radius: 10px; width: 100%; }" + // Red background
                    ".content { background: #ffffff; padding: 20px; border-radius: 10px; width: 80%; max-width: 400px; margin: auto; }" +
                    ".logo img { max-width: 150px; }" +
                    "h2 { color: #D32F2F; }" + // Title in red
                    ".warning-box { font-size: 18px; font-weight: bold; background: #ffffff; padding: 15px 20px; border-radius: 8px; display: inline-block; border: 2px solid #D32F2F; color: #333; }" +
                    ".footer { font-size: 12px; color: #fff; margin-top: 20px; }" + // Footer text is white
                    "</style></head>" +
                    "<body>" +
                    "<div class='email-container'>" + // This wraps everything in the red background
                    "<div class='content'>" + // White box for content
                    "<div class='logo'><img src='cid:appLogo' alt='App Logo'></div>" +
                    "<h2>Security Alert</h2>" +
                    "<p>Hello,</p>" +
                    "<p>We detected multiple unsuccessful login attempts to your account.</p>" +
                    "<p class='warning-box'>If this wasn't you, please change your password immediately.</p>" +
                    "<p>For security reasons, avoid sharing your login credentials.</p>" +
                    "</div>" + // Close .content (white container)
                    "<div class='footer'>© 2025 Your App Name. Stay Secure.</div>" + // Footer outside the white box
                    "</div>" + // Close .email-container
                    "</body></html>";

            MimeMultipart multipart = new MimeMultipart("related");

            // **HTML Part**
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(emailContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // **Attach Logo**
            String logoPath = "src/main/resources/images/logo.png"; // Adjust path if needed
            File logoFile = new File(logoPath);

            if (logoFile.exists()) {
                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource fds = new FileDataSource(logoFile);
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setHeader("Content-ID", "<appLogo>"); // Matches `cid:appLogo` in HTML
                multipart.addBodyPart(imagePart);
            } else {
                System.err.println("Warning: Logo file not found. Email sent without an image.");
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Security alert email sent to " + email);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoogleLogin(MouseEvent event) {
        GoogleAuth googleAuth = new GoogleAuth();
        String authUrl = googleAuth.getAuthorizationUrl();

        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(authUrl));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open browser.");
            return;
        }

        // Prompt user to enter the authorization code manually
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Google Login");
        dialog.setHeaderText("Paste the authorization code from your browser:");
        dialog.setContentText("Authorization Code:");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/googleAuth.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> processGoogleLogin(code));
    }

    private void processGoogleLogin(String authorizationCode) {
        GoogleAuth googleAuth = new GoogleAuth();
        try {
            Userinfo userInfo = googleAuth.getUserInfo(authorizationCode);

            addUserToDatabase(userInfo);

            showAlert("Login Successful", "Welcome, " + userInfo.getGivenName() + "!");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to authenticate with Google.");
        }
    }

    private void addUserToDatabase(Userinfo userInfo) {
        String email = userInfo.getEmail();
        String firstName = userInfo.getGivenName();
        String lastName = userInfo.getFamilyName();
        String role = "client"; // Default role
        String defaultPassword = "google_user"; // Default password for Google users

        // Check if user exists
        String checkQuery = "SELECT Id FROM user WHERE Email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(checkQuery)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // Insert new user
                    String insertQuery = "INSERT INTO user (Email, Nom, Prenom, Role, MDP) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPs = cnx.prepareStatement(insertQuery)) {
                        insertPs.setString(1, email);
                        insertPs.setString(2, firstName);
                        insertPs.setString(3, lastName);
                        insertPs.setString(4, role);
                        insertPs.setString(5, defaultPassword);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

/*
import com.google.api.services.oauth2.model.Userinfo;
import javafx.scene.control.TextInputDialog;
import org.projeti.Service.UserService;
import org.projeti.utils.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;
import javafx.scene.input.MouseEvent;

public class SigninController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UserService userService = new UserService();
    private Connection cnx = Database.getInstance().getCnx();
    private int failedAttempts = 0; // Counter for failed login attempts

    @FXML
    private void handleSignUp(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterUser.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Forgin.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the forgot password page.");
        }
    }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        String role = getUserRole(email, password);

        if (role == null) {
            failedAttempts++;
            showAlert("Login Failed", "Invalid email or password.");

            if (failedAttempts >= 3) {
                sendSecurityAlert(email);
            }
            return;
        }

        failedAttempts = 0; // Reset failed attempts on successful login

        String fxmlFile = "admin".equalsIgnoreCase(role) ? "/adminInterface.fxml" : "/Main.fxml";

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the page.");
        }
    }

    private String getUserRole(String email, String password) {
        String query = "SELECT Role FROM user WHERE Email = ? AND MDP = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Role");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
        return null;
    }

    private void sendSecurityAlert(String email) {
        final String senderEmail = "linatekaya00@gmail.com";
        final String senderPassword = "gmre fcoi mlktzbvb";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Security Alert: Suspicious Login Attempts");

            // **Styled Email Template**
            String emailContent = "<html>" +
                    "<head><style>" +
                    "body { margin: 0; padding: 0; font-family: Arial, sans-serif; text-align: center; }" +
                    ".email-container { background: #D32F2F; padding: 40px 0; border-radius: 10px; width: 100%; }" + // Red background
                    ".content { background: #ffffff; padding: 20px; border-radius: 10px; width: 80%; max-width: 400px; margin: auto; }" +
                    ".logo img { max-width: 150px; }" +
                    "h2 { color: #D32F2F; }" + // Title in red
                    ".warning-box { font-size: 18px; font-weight: bold; background: #ffffff; padding: 15px 20px; border-radius: 8px; display: inline-block; border: 2px solid #D32F2F; color: #333; }" +
                    ".footer { font-size: 12px; color: #fff; margin-top: 20px; }" + // Footer text is white
                    "</style></head>" +
                    "<body>" +
                    "<div class='email-container'>" + // This wraps everything in the red background
                    "<div class='content'>" + // White box for content
                    "<div class='logo'><img src='cid:appLogo' alt='App Logo'></div>" +
                    "<h2>Security Alert</h2>" +
                    "<p>Hello,</p>" +
                    "<p>We detected multiple unsuccessful login attempts to your account.</p>" +
                    "<p class='warning-box'>If this wasn't you, please change your password immediately.</p>" +
                    "<p>For security reasons, avoid sharing your login credentials.</p>" +
                    "</div>" + // Close .content (white container)
                    "<div class='footer'>© 2025 Your App Name. Stay Secure.</div>" + // Footer outside the white box
                    "</div>" + // Close .email-container
                    "</body></html>";

            MimeMultipart multipart = new MimeMultipart("related");

            // **HTML Part**
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(emailContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // **Attach Logo**
            String logoPath = "src/main/resources/images/logo.png"; // Adjust path if needed
            File logoFile = new File(logoPath);

            if (logoFile.exists()) {
                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource fds = new FileDataSource(logoFile);
                imagePart.setDataHandler(new DataHandler(fds));
                imagePart.setHeader("Content-ID", "<appLogo>"); // Matches `cid:appLogo` in HTML
                multipart.addBodyPart(imagePart);
            } else {
                System.err.println("Warning: Logo file not found. Email sent without an image.");
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Security alert email sent to " + email);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleGoogleLogin(MouseEvent event) {
        GoogleAuth googleAuth = new GoogleAuth();
        String authUrl = googleAuth.getAuthorizationUrl();

        try {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(authUrl));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open browser.");
            return;
        }

        // Prompt user to enter the authorization code manually
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Google Login");
        dialog.setHeaderText("Paste the authorization code from your browser:");
        dialog.setContentText("Authorization Code:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(code -> processGoogleLogin(code));
    }

    private void processGoogleLogin(String authorizationCode) {
        GoogleAuth googleAuth = new GoogleAuth();
        try {
            Userinfo userInfo = googleAuth.getUserInfo(authorizationCode);

            addUserToDatabase(userInfo);

            showAlert("Login Successful", "Welcome, " + userInfo.getGivenName() + "!");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Main.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to authenticate with Google.");
        }
    }

    private void addUserToDatabase(Userinfo userInfo) {
        String email = userInfo.getEmail();
        String firstName = userInfo.getGivenName();
        String lastName = userInfo.getFamilyName();
        String role = "client"; // Default role
        String defaultPassword = "google_user"; // Default password for Google users

        // Check if user exists
        String checkQuery = "SELECT Id FROM user WHERE Email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(checkQuery)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return; // User exists, no need to insert
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new user into database
        String insertQuery = "INSERT INTO user (Nom, Prenom, Num_tel, Email, MDP, Role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertQuery)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, null); // Assuming Num_tel is optional
            ps.setString(4, email);
            ps.setString(5, defaultPassword); // You should hash this in a real application
            ps.setString(6, role);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
*/