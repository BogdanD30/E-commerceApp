package com.ecomm.controller;

import com.ecomm.model.CarListing;
import com.ecomm.model.CarMake;
import com.ecomm.model.CarModel;
import com.ecomm.model.ListingOptions;
import com.ecomm.model.User;
import com.ecomm.repository.CarListingRepository;
import com.ecomm.repository.CarMakeRepository;
import com.ecomm.repository.CarModelRepository;
import com.ecomm.util.DatabaseUtil;
import com.ecomm.util.Navigator;
import com.ecomm.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Year;

public class AddListingController {

    private static final int MIN_TITLE_LENGTH = 10;

    @FXML
    private ComboBox<CarMake> makeComboBox;

    @FXML
    private ComboBox<CarModel> modelComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField priceField;

    @FXML
    private TextField yearField;

    @FXML
    private TextField mileageField;

    @FXML
    private ComboBox<String> fuelTypeComboBox;

    @FXML
    private ComboBox<String> transmissionComboBox;

    @FXML
    private TextField colorField;

    @FXML
    private TextField locationField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label messageLabel;

    private User currentUser;
    private final CarMakeRepository makeRepository = new CarMakeRepository();
    private final CarModelRepository modelRepository = new CarModelRepository();
    private final CarListingRepository listingRepository = new CarListingRepository();

    @FXML
    public void initialize() {
        messageLabel.setText("");

        makeComboBox.setItems(FXCollections.observableArrayList(makeRepository.findAll()));
        makeComboBox.setPromptText("Select make");
        makeComboBox.setOnAction(e -> loadModelsForSelectedMake());

        modelComboBox.setPromptText("Select a make first");

        fuelTypeComboBox.setItems(FXCollections.observableArrayList(ListingOptions.FUEL_TYPES));
        fuelTypeComboBox.setPromptText("Any");
        transmissionComboBox.setItems(FXCollections.observableArrayList(ListingOptions.TRANSMISSIONS));
        transmissionComboBox.setPromptText("Any");
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private void loadModelsForSelectedMake() {
        CarMake make = makeComboBox.getValue();
        if (make == null) {
            modelComboBox.getItems().clear();
            return;
        }
        modelComboBox.setItems(FXCollections.observableArrayList(modelRepository.findByMakeId(make.getMakeId())));
        modelComboBox.setValue(null);
        modelComboBox.setPromptText("Select model");
    }

    @FXML
    public void handleSaveListing(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        CarListing listing = new CarListing();
        listing.setUserId(currentUser.getUserId());
        listing.setModelId(modelComboBox.getValue().getModelId());
        listing.setTitle(titleField.getText().trim());
        listing.setDescription(descriptionArea.getText().trim());
        listing.setPrice(new BigDecimal(priceField.getText().trim()));
        listing.setYear(Integer.parseInt(yearField.getText().trim()));
        if (!mileageField.getText().isBlank()) {
            listing.setMileage(Integer.parseInt(mileageField.getText().trim()));
        }
        listing.setFuelType(fuelTypeComboBox.getValue());
        listing.setTransmission(transmissionComboBox.getValue());
        listing.setColor(blankToNull(colorField.getText()));
        listing.setLocation(blankToNull(locationField.getText()));

        if (listingRepository.create(listing)) {
            returnToDashboard("Listing \"" + listing.getTitle() + "\" published.");
        } else if (!DatabaseUtil.testConnection()) {
            UiUtil.showMessage(messageLabel, UiUtil.DB_DOWN_MESSAGE, false);
        } else {
            UiUtil.showMessage(messageLabel, "Could not save the listing. Please try again.", false);
        }
    }

    private boolean validateForm() {
        if (makeComboBox.getValue() == null) {
            return fail("Please select a car make.", makeComboBox);
        }
        if (modelComboBox.getValue() == null) {
            return fail("Please select a car model.", modelComboBox);
        }
        String title = titleField.getText().trim();
        if (title.length() < MIN_TITLE_LENGTH) {
            return fail("Title must be at least " + MIN_TITLE_LENGTH + " characters long.", titleField);
        }
        if (descriptionArea.getText().isBlank()) {
            return fail("Description is required.", descriptionArea);
        }

        try {
            if (new BigDecimal(priceField.getText().trim()).signum() <= 0) {
                return fail("Price must be greater than 0.", priceField);
            }
        } catch (NumberFormatException e) {
            return fail("Please enter a valid price.", priceField);
        }

        try {
            int year = Integer.parseInt(yearField.getText().trim());
            int maxYear = Year.now().getValue() + 1;
            if (year < 1900 || year > maxYear) {
                return fail("Please enter a year between 1900 and " + maxYear + ".", yearField);
            }
        } catch (NumberFormatException e) {
            return fail("Please enter a valid year.", yearField);
        }

        if (!mileageField.getText().isBlank()) {
            try {
                if (Integer.parseInt(mileageField.getText().trim()) < 0) {
                    return fail("Mileage cannot be negative.", mileageField);
                }
            } catch (NumberFormatException e) {
                return fail("Please enter a valid mileage (whole kilometres).", mileageField);
            }
        }
        return true;
    }

    private boolean fail(String message, Control field) {
        UiUtil.showMessage(messageLabel, message, false);
        field.requestFocus();
        return false;
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        returnToDashboard(null);
    }

    private void returnToDashboard(String status) {
        try {
            MainController main = Navigator.show((Stage) cancelButton.getScene().getWindow(),
                    Navigator.MAIN, "E-Commerce Auto", 1280, 800);
            main.setCurrentUser(currentUser);
            main.loadInitialData();
            if (status != null) {
                main.showStatus(status);
            }
        } catch (IOException e) {
            UiUtil.showMessage(messageLabel, "Could not return to the dashboard: " + e.getMessage(), false);
        }
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
