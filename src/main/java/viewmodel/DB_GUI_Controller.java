package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    @FXML
    private TextField first_name, last_name, department, email, imageURL;

    @FXML
    private ImageView img_view;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TableView<Person> tv;

    @FXML
    private TableColumn<Person, Integer> tv_id;

    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    @FXML
    private Button addBtn, editButton, deleteButton;

    @FXML
    private ComboBox<Major> majorCombo;

    @FXML
    private Label statusLabel;

    @FXML
    private MenuItem editItem, deleteItem, importCsvItem, exportCsvItem;

    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();

    private static final String NAME_REGEX = "^[A-Za-z][A-Za-z '-]{1,24}$";
    private static final String DEPARTMENT_REGEX = "^[A-Za-z][A-Za-z &-]{1,29}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@farmingdale\\.edu$";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            majorCombo.getItems().setAll(Major.values());

            editButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            editItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            BooleanBinding validForm = Bindings.createBooleanBinding(
                    this::isFormValid,
                    first_name.textProperty(),
                    last_name.textProperty(),
                    department.textProperty(),
                    majorCombo.valueProperty(),
                    email.textProperty()
            );

            addBtn.disableProperty().bind(validForm.not());

            first_name.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
            last_name.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
            department.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
            email.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
            majorCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateFields());

            validateFields();
            setStatus("Ready");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidName(String text) {
        return text != null && text.trim().matches(NAME_REGEX);
    }

    private boolean isValidDepartment(String text) {
        return text != null && text.trim().matches(DEPARTMENT_REGEX);
    }

    private boolean isValidEmail(String text) {
        return text != null && text.trim().matches(EMAIL_REGEX);
    }

    private boolean isFormValid() {
        return isValidName(first_name.getText())
                && isValidName(last_name.getText())
                && isValidDepartment(department.getText())
                && majorCombo.getValue() != null
                && isValidEmail(email.getText());
    }

    @FXML
    protected void addNewRecord() {
        if (!isFormValid()) {
            setStatus("Please enter valid data.");
            return;
        }

        Person p = new Person(
                first_name.getText().trim(),
                last_name.getText().trim(),
                department.getText().trim(),
                majorCombo.getValue().toString(),
                email.getText().trim(),
                imageURL.getText().trim()
        );

        cnUtil.insertUser(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);
        clearForm();
        setStatus("Student added successfully.");
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null) {
            setStatus("Select a record to edit.");
            return;
        }

        if (!isFormValid()) {
            setStatus("Please enter valid data.");
            return;
        }

        int index = data.indexOf(p);

        Person p2 = new Person(
                p.getId(),
                first_name.getText().trim(),
                last_name.getText().trim(),
                department.getText().trim(),
                majorCombo.getValue().toString(),
                email.getText().trim(),
                imageURL.getText().trim()
        );

        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
        setStatus("Student updated successfully.");
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p == null) {
            setStatus("Select a record to delete.");
            return;
        }

        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);

        if (!data.isEmpty()) {
            tv.getSelectionModel().select(Math.min(index, data.size() - 1));
        }

        clearForm();
        setStatus("Record deleted successfully.");
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        majorCombo.setValue(null);
        email.clear();
        imageURL.clear();
        tv.getSelectionModel().clearSelection();
        validateFields();
        setStatus("Form cleared.");
    }

    @FXML
    protected void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open CSV File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = chooser.showOpenDialog(menuBar.getScene().getWindow());

        if (file == null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length < 7) {
                    continue;
                }

                Person p = new Person(
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim(),
                        parts[6].trim()
                );

                cnUtil.insertUser(p);
                p.setId(cnUtil.retrieveId(p));
                data.add(p);
            }

            setStatus("CSV imported successfully.");

        } catch (Exception e) {
            setStatus("Import failed.");
            e.printStackTrace();
        }
    }

    @FXML
    protected void exportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save CSV File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = chooser.showSaveDialog(menuBar.getScene().getWindow());

        if (file == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("id,firstName,lastName,department,major,email,imageURL");

            for (Person p : data) {
                writer.println(
                        p.getId() + "," +
                                p.getFirstName() + "," +
                                p.getLastName() + "," +
                                p.getDepartment() + "," +
                                p.getMajor() + "," +
                                p.getEmail() + "," +
                                p.getImageURL()
                );
            }

            setStatus("CSV exported successfully.");

        } catch (Exception e) {
            setStatus("Export failed.");
            e.printStackTrace();
        }
    }

    private void markField(TextField field, boolean valid) {
        if (valid) {
            field.setStyle("");
        } else {
            field.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        }
    }

    private void validateFields() {
        markField(first_name, isValidName(first_name.getText()));
        markField(last_name, isValidName(last_name.getText()));
        markField(department, isValidDepartment(department.getText()));
        markField(email, isValidEmail(email.getText()));

        if (majorCombo.getValue() == null) {
            majorCombo.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            majorCombo.setStyle("");
        }
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
            imageURL.setText(file.toURI().toString());
            setStatus("Image selected.");
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p == null) {
            return;
        }

        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        majorCombo.setValue(Major.valueOf(p.getMajor()));
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify...");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email");

        ObservableList<Major> options = FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();

        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);

        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(), textField2.getText(), comboBox.getValue());
            }
            return null;
        });

        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent(results ->
                MyLogger.makeLog(results.fname + " " + results.lname + " " + results.major));
    }

    private enum Major {
        Business, CSC, CPIS
    }

    private static class Results {
        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }
}