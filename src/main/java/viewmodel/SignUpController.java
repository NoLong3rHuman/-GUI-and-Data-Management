package viewmodel;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class SignUpController {

    @FXML
    private TextField newUsernameField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label signupStatusLabel;

    private static final Preferences prefs =
            Preferences.userNodeForPackage(SignUpController.class);

    private static final String USERNAME_REGEX = "^[A-Za-z][A-Za-z0-9_]{4,19}$";
    private static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,20}$";

    @FXML
    protected void createAccount(ActionEvent actionEvent) {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            signupStatusLabel.setText("All fields are required.");
            return;
        }

        if (!username.matches(USERNAME_REGEX)) {
            signupStatusLabel.setText("Username must be 5-20 chars, start with a letter, and use letters/numbers/_ only.");
            return;
        }

        if (!password.matches(PASSWORD_REGEX)) {
            signupStatusLabel.setText("Password must be 8-20 chars and include upper, lower, digit, and symbol.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            signupStatusLabel.setText("Passwords do not match.");
            return;
        }

        prefs.put("registeredUsername", username);
        prefs.put("registeredPassword", password);

        signupStatusLabel.setText("Account created successfully.");
    }

    @FXML
    protected void backToLogin(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/css/lightTheme.css").toExternalForm()
            );

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();

        } catch (Exception e) {
            signupStatusLabel.setText("Could not return to login.");
            e.printStackTrace();
        }
    }
}