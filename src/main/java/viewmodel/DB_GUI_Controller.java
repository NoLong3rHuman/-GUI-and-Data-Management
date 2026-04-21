package viewmodel;

import dao.DbConnectivityClass;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import model.Person;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class DB_GUI_Controller implements Initializable {

    @FXML private TableView<Person> tv;
    @FXML private TableColumn<Person, Integer> tv_id;
    @FXML private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;

    @FXML private TextField first_name, last_name, department, email, imageURL;
    @FXML private ComboBox<Major> majorCombo;
    @FXML private Button addBtn, editButton, deleteButton;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;
    @FXML private MenuBar menuBar;

    private final ObservableList<Person> data = FXCollections.observableArrayList();
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();

    public enum Major { Business, CSC, CPIS }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Table setup
        tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
        tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
        tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));

        tv.setItems(cnUtil.getData());

        // Disable buttons unless selected
        editButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

        // Form validation binding
        BooleanBinding validForm = Bindings.createBooleanBinding(
                this::isFormValid,
                first_name.textProperty(),
                last_name.textProperty(),
                department.textProperty(),
                email.textProperty(),
                majorCombo.valueProperty()
        );

        addBtn.disableProperty().bind(validForm.not());

        // ComboBox values
        majorCombo.getItems().setAll(Major.values());

        // Tooltips
        first_name.setTooltip(new Tooltip("Enter first name"));
        last_name.setTooltip(new Tooltip("Enter last name"));
        department.setTooltip(new Tooltip("Enter department"));
        email.setTooltip(new Tooltip("Farmingdale email"));
        imageURL.setTooltip(new Tooltip("Image URL"));
        majorCombo.setTooltip(new Tooltip("Select major"));
    }

    private boolean isFormValid() {
        return !first_name.getText().trim().isEmpty()
                && !last_name.getText().trim().isEmpty()
                && !department.getText().trim().isEmpty()
                && !email.getText().trim().isEmpty()
                && majorCombo.getValue() != null;
    }

    @FXML
    protected void addNewRecord() {
        Person p = new Person(
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                majorCombo.getValue().toString(),
                email.getText(),
                imageURL.getText()
        );

        cnUtil.insertUser(p);
        p.setId(cnUtil.retrieveId(p));
        tv.getItems().add(p);

        clearForm();
        setStatus("Record added successfully.");
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p == null) {
            setStatus("Select a record to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Student Record");
        alert.setContentText("Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            cnUtil.deleteRecord(p);
            tv.getItems().remove(p);
            setStatus("Record deleted.");
        }
    }

    @FXML
    protected void selectedItemTV(MouseEvent e) {
        Person p = tv.getSelectionModel().getSelectedItem();

        if (p == null) return;

        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
        majorCombo.setValue(Major.valueOf(p.getMajor()));
    }

    @FXML
    protected void clearForm() {
        first_name.clear();
        last_name.clear();
        department.clear();
        email.clear();
        imageURL.clear();
        majorCombo.setValue(null);
    }

    @FXML
    protected void searchByLastName() {
        String search = searchField.getText().toLowerCase();

        ObservableList<Person> filtered = FXCollections.observableArrayList();

        for (Person p : tv.getItems()) {
            if (p.getLastName().toLowerCase().contains(search)) {
                filtered.add(p);
            }
        }

        tv.setItems(filtered);
        setStatus("Search completed.");
    }

    @FXML
    protected void reloadData() {
        tv.setItems(cnUtil.getData());
        setStatus("Table reloaded.");
    }

    @FXML
    protected void exportCsv() throws IOException {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(menuBar.getScene().getWindow());

        if (file == null) return;

        try (PrintWriter pw = new PrintWriter(file)) {
            for (Person p : tv.getItems()) {
                pw.println(p.getFirstName() + "," + p.getLastName());
            }
        }

        setStatus("CSV exported.");
    }

    @FXML
    protected void importCsv() throws IOException {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(menuBar.getScene().getWindow());

        if (file == null) return;

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            Person p = new Person(parts[0], parts[1], "", "", "", "");
            tv.getItems().add(p);
        }

        setStatus("CSV imported.");
    }

    private void setStatus(String msg) {
        statusLabel.setText(msg);
    }
}