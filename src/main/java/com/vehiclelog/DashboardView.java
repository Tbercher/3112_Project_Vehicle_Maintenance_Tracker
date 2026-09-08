package com.vehiclelog;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class DashboardView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private final MaintenanceService maintenanceService;
    private final FileStorageService fileStorageService;
    private final VehicleService vehicleService;
    private final TableView<MaintenanceRecord> historyTable = new TableView<>();
    private final ComboBox<Vehicle> vehicleSelector = new ComboBox<>();
    private final DatePicker serviceDate = new DatePicker(LocalDate.now());
    private final TextField mileage = new TextField();
    private final TextArea description = new TextArea();
    private final Label attachmentLabel = new Label("No document attached");
    private final Label recordCount = new Label("0");
    private final Label formTitle = new Label("Log a service event");
    private final Label statusLabel = new Label();
    private final Label vehicleHeading = new Label("NO VEHICLE SELECTED");
    private MaintenanceRecord selectedRecord;
    private String pendingDocumentPath;

    public DashboardView(MaintenanceService maintenanceService, FileStorageService fileStorageService,
                         VehicleService vehicleService) {
        this.maintenanceService = maintenanceService;
        this.fileStorageService = fileStorageService;
        this.vehicleService = vehicleService;
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setTop(buildHeader());
        root.setCenter(buildContent(stage));

        Scene scene = new Scene(root, 1120, 720);
        scene.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm());
        stage.setTitle("Vehicle Maintenance Log");
        stage.setMinWidth(900);
        stage.setMinHeight(620);
        stage.setScene(scene);
        loadVehicles(stage);
        refreshTable();
        stage.show();
    }

    private VBox buildHeader() {
        vehicleHeading.getStyleClass().add("eyebrow");
        Label title = new Label("Vehicle maintenance log");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("A clear record of every service, receipt, and mile marker.");
        subtitle.getStyleClass().add("subtitle");
        VBox header = new VBox(8, vehicleHeading, title, subtitle);
        header.getStyleClass().add("header");
        return header;
    }

    private VBox buildContent(Stage stage) {
        VBox content = new VBox(18, buildSummary(), buildVehicleBar(stage), buildWorkspace(stage));
        content.setPadding(new Insets(24, 34, 30, 34));
        VBox.setVgrow(content, Priority.ALWAYS);
        return content;
    }

    private HBox buildSummary() {
        Label history = new Label("SERVICE HISTORY");
        history.getStyleClass().add("section-label");
        recordCount.getStyleClass().add("summary-value");
        VBox summary = new VBox(4, history, recordCount);
        summary.getStyleClass().add("summary-block");
        HBox wrapper = new HBox(summary);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    private HBox buildVehicleBar(Stage stage) {
        Label label = new Label("VEHICLE");
        label.getStyleClass().add("field-label");
        vehicleSelector.setPromptText("Select a vehicle");
        vehicleSelector.setMaxWidth(Double.MAX_VALUE);
        vehicleSelector.getStyleClass().add("field-control");
        vehicleSelector.valueProperty().addListener((observable, oldVehicle, newVehicle) -> {
            vehicleHeading.setText(newVehicle == null ? "NO VEHICLE SELECTED" : newVehicle.getName().toUpperCase(Locale.ROOT));
            clearForm();
            refreshTable();
        });

        Button addVehicleButton = primaryButton("Add vehicle");
        addVehicleButton.setOnAction(event -> addVehicle(stage));
        Button deleteVehicleButton = dangerButton("Delete vehicle");
        deleteVehicleButton.setOnAction(event -> deleteVehicle());
        HBox.setHgrow(vehicleSelector, Priority.ALWAYS);
        HBox bar = new HBox(10, label, vehicleSelector, addVehicleButton, deleteVehicleButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private void loadVehicles(Stage stage) {
        vehicleSelector.getItems().setAll(vehicleService.findAll());
        if (vehicleSelector.getItems().isEmpty()) {
            addVehicle(stage);
        } else if (vehicleSelector.getValue() == null) {
            vehicleSelector.getSelectionModel().selectFirst();
        }
    }

    private void addVehicle(Stage stage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add vehicle");
        dialog.setHeaderText("Add a vehicle to your garage");
        dialog.setContentText("Vehicle name (for example, 2024 Kia Sportage Hybrid):");
        dialog.initOwner(stage);
        dialog.showAndWait().ifPresent(name -> {
            try {
                Vehicle vehicle = vehicleService.create(name);
                vehicleSelector.getItems().setAll(vehicleService.findAll());
                vehicleSelector.getSelectionModel().select(vehicle);
                statusLabel.setText("Vehicle added.");
            } catch (IllegalArgumentException exception) {
                showMessage(Alert.AlertType.ERROR, exception.getMessage());
            }
        });
    }

    private void deleteVehicle() {
        Vehicle vehicle = vehicleSelector.getValue();
        if (vehicle == null) {
            showMessage(Alert.AlertType.INFORMATION, "Select a vehicle first.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + vehicle.getName() + " and all of its maintenance records permanently?",
                ButtonType.CANCEL, ButtonType.OK);
        confirmation.setTitle("Delete vehicle");
        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                vehicleService.delete(vehicle.getId());
                vehicleSelector.getItems().setAll(vehicleService.findAll());
                if (vehicleSelector.getItems().isEmpty()) {
                    clearForm();
                    vehicleHeading.setText("NO VEHICLE SELECTED");
                } else {
                    vehicleSelector.getSelectionModel().selectFirst();
                }
                statusLabel.setText("Vehicle and maintenance records deleted.");
            }
        });
    }

    private HBox buildWorkspace(Stage stage) {
        VBox historyPanel = buildHistoryPanel(stage);
        VBox editorPanel = buildEditorPanel(stage);
        HBox workspace = new HBox(20, historyPanel, editorPanel);
        HBox.setHgrow(historyPanel, Priority.ALWAYS);
        workspace.setFillHeight(true);
        VBox.setVgrow(workspace, Priority.ALWAYS);
        return workspace;
    }

    private VBox buildHistoryPanel(Stage stage) {
        Label title = new Label("Maintenance history");
        title.getStyleClass().add("panel-title");
        Label hint = new Label("Select an entry to edit, delete, or open its document.");
        hint.getStyleClass().add("panel-hint");

        TableColumn<MaintenanceRecord, LocalDate> dateColumn = new TableColumn<>("DATE");
        dateColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getServiceDate()));
        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : DATE_FORMAT.format(date));
            }
        });
        dateColumn.setPrefWidth(125);

        TableColumn<MaintenanceRecord, Integer> mileageColumn = new TableColumn<>("MILEAGE");
        mileageColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getMileage()));
        mileageColumn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : NumberFormat.getNumberInstance(Locale.US).format(value) + " mi");
            }
        });
        mileageColumn.setPrefWidth(120);

        TableColumn<MaintenanceRecord, String> descriptionColumn = new TableColumn<>("SERVICE DESCRIPTION");
        descriptionColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescription()));
        descriptionColumn.setPrefWidth(300);

        TableColumn<MaintenanceRecord, String> documentColumn = new TableColumn<>("DOCUMENT");
        documentColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDocumentPath() == null ? "-" : "Attached"));
        documentColumn.setPrefWidth(110);

        historyTable.getColumns().clear();
        historyTable.getColumns().add(dateColumn);
        historyTable.getColumns().add(mileageColumn);
        historyTable.getColumns().add(descriptionColumn);
        historyTable.getColumns().add(documentColumn);
        historyTable.setPlaceholder(new Label("No service records yet. Add your first event on the right."));
        historyTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, record) -> {
            if (record != null) populateForm(record);
        });
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        Button editButton = secondaryButton("Edit selected");
        editButton.setOnAction(event -> {
            MaintenanceRecord record = historyTable.getSelectionModel().getSelectedItem();
            if (record == null) showMessage(Alert.AlertType.INFORMATION, "Select a record first.");
            else populateForm(record);
        });
        Button deleteButton = dangerButton("Delete selected");
        deleteButton.setOnAction(event -> deleteSelected());
        Button openButton = secondaryButton("Open document");
        openButton.setOnAction(event -> openSelectedDocument());
        HBox actions = new HBox(10, editButton, deleteButton, openButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox panel = new VBox(8, title, hint, historyTable, actions);
        panel.getStyleClass().add("panel");
        panel.setMinWidth(560);
        return panel;
    }

    private VBox buildEditorPanel(Stage stage) {
        formTitle.getStyleClass().add("panel-title");
        Label hint = new Label("Keep the important details together while they are fresh.");
        hint.getStyleClass().add("panel-hint");

        serviceDate.setMaxWidth(Double.MAX_VALUE);
        mileage.setPromptText("e.g. 42,500");
        description.setPromptText("Oil change, tire rotation, warranty repair...");
        description.setWrapText(true);
        description.setPrefRowCount(5);
        VBox.setVgrow(description, Priority.NEVER);

        Button attachButton = secondaryButton("Attach receipt");
        attachButton.setTooltip(new Tooltip("Choose a PDF, image, or document from your computer"));
        attachButton.setOnAction(event -> chooseDocument(stage));
        attachmentLabel.getStyleClass().add("attachment-label");
        HBox attachmentRow = new HBox(10, attachButton, attachmentLabel);
        attachmentRow.setAlignment(Pos.CENTER_LEFT);

        Button clearButton = secondaryButton("Clear form");
        clearButton.setOnAction(event -> clearForm());
        Button saveButton = primaryButton("Save service event");
        saveButton.setOnAction(event -> saveRecord());
        HBox formActions = new HBox(10, clearButton, saveButton);
        formActions.setAlignment(Pos.CENTER_RIGHT);

        statusLabel.getStyleClass().add("status-label");
        VBox panel = new VBox(14, formTitle, hint,
                field("SERVICE DATE", serviceDate),
                field("CURRENT MILEAGE", mileage),
                field("SERVICE DESCRIPTION", description),
                field("DOCUMENT", attachmentRow),
                statusLabel, formActions);
        panel.getStyleClass().add("panel");
        panel.setPrefWidth(350);
        return panel;
    }

    private VBox field(String labelText, javafx.scene.Node input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        VBox field = new VBox(6, label, input);
        if (input instanceof javafx.scene.control.Control control) control.getStyleClass().add("field-control");
        return field;
    }

    private void chooseDocument(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Attach service document");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents and images", "*.pdf", "*.doc", "*.docx", "*.png", "*.jpg", "*.jpeg"));
        File selected = chooser.showOpenDialog(stage);
        if (selected == null) return;
        try {
            pendingDocumentPath = fileStorageService.store(new LocalFileMultipartFile(selected));
            attachmentLabel.setText(selected.getName());
            statusLabel.setText("Document copied to the application folder.");
        } catch (IOException exception) {
            showMessage(Alert.AlertType.ERROR, "The document could not be copied: " + exception.getMessage());
        }
    }

    private void saveRecord() {
        try {
            int parsedMileage = Integer.parseInt(mileage.getText().replace(",", "").trim());
            if (selectedRecord == null) {
                maintenanceService.create(vehicleSelector.getValue(), serviceDate.getValue(), parsedMileage,
                        description.getText(), pendingDocumentPath);
                statusLabel.setText("Service event saved.");
            } else {
                maintenanceService.update(selectedRecord.getId(), vehicleSelector.getValue(), serviceDate.getValue(),
                        parsedMileage, description.getText(), pendingDocumentPath);
                statusLabel.setText("Service event updated.");
            }
            refreshTable();
            clearForm();
        } catch (NumberFormatException exception) {
            statusLabel.setText("Enter mileage as a whole number.");
        } catch (IllegalArgumentException exception) {
            statusLabel.setText(exception.getMessage());
        }
    }

    private void deleteSelected() {
        MaintenanceRecord record = historyTable.getSelectionModel().getSelectedItem();
        if (record == null) {
            showMessage(Alert.AlertType.INFORMATION, "Select a record first.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Delete this maintenance record permanently?", ButtonType.CANCEL, ButtonType.OK);
        confirmation.setTitle("Delete maintenance record");
        confirmation.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                maintenanceService.delete(record.getId());
                refreshTable();
                clearForm();
            }
        });
    }

    private void openSelectedDocument() {
        MaintenanceRecord record = historyTable.getSelectionModel().getSelectedItem();
        if (record == null || record.getDocumentPath() == null) {
            showMessage(Alert.AlertType.INFORMATION, "Select a record with an attached document.");
            return;
        }
        Path path = Path.of(record.getDocumentPath());
        if (!Files.exists(path)) {
            showMessage(Alert.AlertType.ERROR, "The attached document is no longer at its saved path.");
            return;
        }
        try {
            openWithSystemViewer(path);
        } catch (IOException | UnsupportedOperationException exception) {
            showMessage(Alert.AlertType.ERROR, "The document could not be opened by the system viewer. Check that Windows has a default app for this file type.");
        }
    }

    private void openWithSystemViewer(Path path) throws IOException {
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")) {
            new ProcessBuilder("explorer.exe", path.toAbsolutePath().toString()).start();
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            throw new UnsupportedOperationException("Opening files is not supported on this desktop.");
        }
        Desktop.getDesktop().open(path.toFile());
    }

    private void populateForm(MaintenanceRecord record) {
        selectedRecord = record;
        formTitle.setText("Edit service event");
        if (record.getVehicle() != null) {
            vehicleSelector.getItems().stream()
                    .filter(vehicle -> vehicle.getId().equals(record.getVehicle().getId()))
                    .findFirst()
                    .ifPresent(vehicleSelector.getSelectionModel()::select);
        }
        serviceDate.setValue(record.getServiceDate());
        mileage.setText(Integer.toString(record.getMileage()));
        description.setText(record.getDescription());
        pendingDocumentPath = record.getDocumentPath();
        attachmentLabel.setText(record.getDocumentPath() == null ? "No document attached" : Path.of(record.getDocumentPath()).getFileName().toString());
        statusLabel.setText("Editing selected record.");
    }

    private void clearForm() {
        selectedRecord = null;
        pendingDocumentPath = null;
        formTitle.setText("Log a service event");
        serviceDate.setValue(LocalDate.now());
        mileage.clear();
        description.clear();
        attachmentLabel.setText("No document attached");
        historyTable.getSelectionModel().clearSelection();
    }

    private void refreshTable() {
        var records = vehicleSelector.getValue() == null
            ? FXCollections.<MaintenanceRecord>observableArrayList()
            : FXCollections.observableArrayList(maintenanceService.findAll(vehicleSelector.getValue()));
        historyTable.setItems(records);
        recordCount.setText(Integer.toString(records.size()));
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        return button;
    }

    private Button dangerButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("danger-button");
        return button;
    }

    private void showMessage(Alert.AlertType type, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }
}
