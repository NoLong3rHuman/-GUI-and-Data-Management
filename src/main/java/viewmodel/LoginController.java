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
import service.UserSession;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label loginStatusLabel;


    @FXML
    protected void login(ActionEvent actionEvent) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        String savedUsername = java.util.prefs.Preferences
                .userNodeForPackage(SignUpController.class)
                .get("registeredUsername", "");

        String savedPassword = java.util.prefs.Preferences
                .userNodeForPackage(SignUpController.class)
                .get("registeredPassword", "");

        if (!username.equals(savedUsername) || !password.equals(savedPassword)) {
            loginStatusLabel.setText("Invalid username or password.");
            return;
        }

        UserSession.createSession(username, password, "USER");

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/css/lightTheme.css").toExternalForm()
            );

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();

        } catch (Exception e) {
            loginStatusLabel.setText("Login failed.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void goToSignUp(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signup.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/css/lightTheme.css").toExternalForm()
            );

            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();

        } catch (Exception e) {
            loginStatusLabel.setText("Could not open signup page.");
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        usernameField.setText(UserSession.getSavedUsername());
        passwordField.setText(UserSession.getSavedPassword());
    }
}

