package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class Controller {

    // -------------------- PATIENT UI ELEMENTS --------------------
    @FXML private TextField patientNameField;
    @FXML private TextField patientAgeField;
    @FXML private ComboBox<String> patientGenderBox;

    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Integer> colId;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, Integer> colAge;
    @FXML private TableColumn<Patient, String> colGender;

    // -------------------- VACCINE UI ELEMENTS --------------------
    @FXML private TextField vaccineNameField;
    @FXML private TextField manufacturerField;
    @FXML private DatePicker boosterDatePicker;

    // -------------------- PATIENT DATA LIST --------------------
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    // -------------------- INITIALIZE --------------------
    @FXML
    private void initialize() {
        // Populate gender options
        patientGenderBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));

        // Link columns with Patient getters
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colAge.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getAge()).asObject());
        colGender.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGender()));

        // Load all patients at startup
        loadPatients();
    }

    // -------------------- ADD PATIENT --------------------
    @FXML
    private void addPatient() {
        String name = patientNameField.getText();
        String ageText = patientAgeField.getText();
        String gender = patientGenderBox.getValue();

        if (name.isEmpty() || ageText.isEmpty() || gender == null) {
            showAlert(Alert.AlertType.WARNING, "Please fill all fields before adding a patient!");
            return;
        }

        try {
            int age = Integer.parseInt(ageText);
            String sql = "INSERT INTO patient (name, age, gender) VALUES (?, ?, ?)";

            try (Connection conn = DBconnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt(2, age);
                ps.setString(3, gender);
                ps.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "✅ Patient added successfully!");
                loadPatients();
                clearPatientFields();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Age must be a number!");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Failed to add patient!");
        }
    }

    // -------------------- VIEW PATIENTS --------------------
    @FXML
    private void viewPatients() {
        loadPatients();
        showAlert(Alert.AlertType.INFORMATION, "✅ Patient list refreshed!");
    }

    // -------------------- DELETE PATIENT --------------------
    @FXML
    private void deletePatient() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Please select a patient to delete!");
            return;
        }

        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM patient WHERE patient_id = ?")) {
            ps.setInt(1, selected.getId());
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "🗑️ Patient deleted successfully!");
            loadPatients();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Failed to delete patient!");
        }
    }

    // -------------------- ADD VACCINE --------------------
    @FXML
    private void addVaccine() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Select a patient first!");
            return;
        }

        String vaccine = vaccineNameField.getText();
        String manufacturer = manufacturerField.getText();
        String date = boosterDatePicker.getValue() == null ? null : boosterDatePicker.getValue().toString();

        if (vaccine.isEmpty() || manufacturer.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, "Please fill all vaccine fields!");
            return;
        }

        String sql = "INSERT INTO vaccination_record (patient_id, vaccine_name, manufacturer, booster_due_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            ps.setString(2, vaccine);
            ps.setString(3, manufacturer);
            ps.setString(4, date);
            ps.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "💉 Vaccine added for patient ID: " + selected.getId());
            clearVaccineFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "❌ Failed to add vaccine!");
        }
    }

    // -------------------- HELPER: LOAD PATIENTS --------------------
    private void loadPatients() {
        patientList.clear();
        try (Connection conn = DBconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM patient")) {

            while (rs.next()) {
                patientList.add(new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender")));
            }
            patientTable.setItems(patientList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------- HELPER: CLEAR INPUT FIELDS --------------------
    private void clearPatientFields() {
        patientNameField.clear();
        patientAgeField.clear();
        patientGenderBox.setValue(null);
    }

    private void clearVaccineFields() {
        vaccineNameField.clear();
        manufacturerField.clear();
        boosterDatePicker.setValue(null);
    }

    // -------------------- HELPER: ALERT --------------------
    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }
}
