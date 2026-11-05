package com.ecomm.controller;

import com.ecomm.model.CarListing;
import com.ecomm.model.CarMake;
import com.ecomm.model.CarModel;
import com.ecomm.model.ListingOptions;
import com.ecomm.model.User;
import com.ecomm.repository.CarListingRepository;
import com.ecomm.repository.CarMakeRepository;
import com.ecomm.repository.CarModelRepository;
import com.ecomm.util.Navigator;
import com.ecomm.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class MainController {

    private static final String ALL_BODY_TYPES = "All types";
    private static final String ALL_FUELS = "All fuels";

    @FXML private TextField keywordField;
    @FXML private Label welcomeLabel;
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<CarListing> listingsTable;
    @FXML private TableColumn<CarListing, String> titleColumn;
    @FXML private TableColumn<CarListing, String> makeColumn;
    @FXML private TableColumn<CarListing, String> modelColumn;
    @FXML private TableColumn<CarListing, String> bodyTypeColumn;
    @FXML private TableColumn<CarListing, Integer> yearColumn;
    @FXML private TableColumn<CarListing, String> mileageColumn;
    @FXML private TableColumn<CarListing, String> fuelColumn;
    @FXML private TableColumn<CarListing, BigDecimal> priceColumn;
    @FXML private TableColumn<CarListing, String> locationColumn;

    @FXML private ComboBox<CarMake> makeFilterComboBox;
    @FXML private ComboBox<CarModel> modelFilterComboBox;
    @FXML private ComboBox<String> bodyTypeFilterComboBox;
    @FXML private ComboBox<String> fuelFilterComboBox;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private TextField minYearField;
    @FXML private TextField maxYearField;
    @FXML private TextField locationSearchField;

    @FXML private Button addListingButton;
    @FXML private Button removeListingButton;
    @FXML private Button logoutButton;

    private User currentUser;
    private final CarListingRepository listingRepository = new CarListingRepository();
    private final CarMakeRepository makeRepository = new CarMakeRepository();
    private final CarModelRepository modelRepository = new CarModelRepository();
    private final ObservableList<CarListing> listingsData = FXCollections.observableArrayList();

    /** What "Refresh" re-runs: the current search, or the user's own listings. */
    private Runnable reload = this::runSearch;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        listingsTable.setItems(listingsData);
        removeListingButton.setDisable(true);
        statusLabel.setText("Ready");
    }

    // ---------- setup ----------

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        makeColumn.setCellValueFactory(new PropertyValueFactory<>("makeName"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("modelName"));
        bodyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("bodyType"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        mileageColumn.setCellValueFactory(new PropertyValueFactory<>("formattedMileage"));
        fuelColumn.setCellValueFactory(new PropertyValueFactory<>("fuelType"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("€%,.0f", price));
            }
        });
        for (TableColumn<CarListing, ?> numeric : List.of(yearColumn, mileageColumn, priceColumn)) {
            numeric.getStyleClass().add("numeric");
        }

        listingsTable.setPlaceholder(new Label("No listings found"));
        listingsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        listingsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleViewDetails(null);
            }
        });
        listingsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) ->
                removeListingButton.setDisable(selected == null || !isOwnListing(selected)));
    }

    private void setupFilters() {
        makeFilterComboBox.getItems().add(null);
        makeFilterComboBox.getItems().addAll(makeRepository.findAll());
        makeFilterComboBox.setPromptText("All makes");
        makeFilterComboBox.setCellFactory(lv -> nullAwareCell("All makes"));
        makeFilterComboBox.setButtonCell(nullAwareCell("All makes"));
        makeFilterComboBox.setOnAction(e -> reloadModelFilter());

        modelFilterComboBox.setPromptText("All models");
        modelFilterComboBox.setCellFactory(lv -> nullAwareCell("All models"));
        modelFilterComboBox.setButtonCell(nullAwareCell("All models"));

        bodyTypeFilterComboBox.getItems().add(ALL_BODY_TYPES);
        bodyTypeFilterComboBox.getItems().addAll(ListingOptions.BODY_TYPES);
        bodyTypeFilterComboBox.setValue(ALL_BODY_TYPES);

        fuelFilterComboBox.getItems().add(ALL_FUELS);
        fuelFilterComboBox.getItems().addAll(ListingOptions.FUEL_TYPES);
        fuelFilterComboBox.setValue(ALL_FUELS);
    }

    /** A cell that shows placeholder text for the "no selection" (null) entry of a combo box. */
    private static <T> ListCell<T> nullAwareCell(String nullText) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : (item == null ? nullText : item.toString()));
            }
        };
    }

    private void reloadModelFilter() {
        CarMake make = makeFilterComboBox.getValue();
        modelFilterComboBox.getItems().clear();
        modelFilterComboBox.getItems().add(null);
        if (make != null) {
            modelFilterComboBox.getItems().addAll(modelRepository.findByMakeId(make.getMakeId()));
        }
        modelFilterComboBox.setValue(null);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Signed in as " + user.getDisplayName());
    }

    public void loadInitialData() {
        runSearch();
    }

    public void showStatus(String message) {
        statusLabel.setText(message);
    }

    // ---------- listing views ----------

    private void showListings(List<CarListing> listings, String title, String status) {
        listingsData.setAll(listings);
        titleLabel.setText(title);
        statusLabel.setText(status);
        listingsTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        runSearch();
    }

    private void runSearch() {
        reload = this::runSearch;
        try {
            CarMake make = makeFilterComboBox.getValue();
            CarModel model = modelFilterComboBox.getValue();
            Integer minYear = parseInteger(minYearField, "Year");
            Integer maxYear = parseInteger(maxYearField, "Year");
            BigDecimal minPrice = parseDecimal(minPriceField, "Price");
            BigDecimal maxPrice = parseDecimal(maxPriceField, "Price");

            if (minYear != null && maxYear != null && minYear > maxYear
                    || minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
                statusLabel.setText("The minimum value is larger than the maximum. Please check the ranges.");
                return;
            }

            List<CarListing> results = listingRepository.searchWithFilters(
                    keywordField.getText().trim(),
                    make != null ? make.getMakeName() : null,
                    model != null ? model.getModelName() : null,
                    minYear, maxYear, minPrice, maxPrice,
                    ALL_FUELS.equals(fuelFilterComboBox.getValue()) ? null : fuelFilterComboBox.getValue(),
                    null, null,
                    locationSearchField.getText().trim(),
                    ALL_BODY_TYPES.equals(bodyTypeFilterComboBox.getValue()) ? null : bodyTypeFilterComboBox.getValue());

            boolean filtered = hasActiveFilters();
            showListings(results, filtered ? "Search results" : "All listings",
                    results.size() + (results.size() == 1 ? " listing" : " listings"));
        } catch (IllegalArgumentException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private boolean hasActiveFilters() {
        return !keywordField.getText().isBlank()
                || makeFilterComboBox.getValue() != null
                || modelFilterComboBox.getValue() != null
                || !ALL_BODY_TYPES.equals(bodyTypeFilterComboBox.getValue())
                || !ALL_FUELS.equals(fuelFilterComboBox.getValue())
                || !minPriceField.getText().isBlank() || !maxPriceField.getText().isBlank()
                || !minYearField.getText().isBlank() || !maxYearField.getText().isBlank()
                || !locationSearchField.getText().isBlank();
    }

    @FXML
    public void handleClearFilters(ActionEvent event) {
        keywordField.clear();
        makeFilterComboBox.setValue(null);
        modelFilterComboBox.getItems().clear();
        modelFilterComboBox.setValue(null);
        bodyTypeFilterComboBox.setValue(ALL_BODY_TYPES);
        fuelFilterComboBox.setValue(ALL_FUELS);
        minPriceField.clear();
        maxPriceField.clear();
        minYearField.clear();
        maxYearField.clear();
        locationSearchField.clear();
        runSearch();
    }

    @FXML
    public void handleShowAllListings(ActionEvent event) {
        handleClearFilters(event);
    }

    @FXML
    public void handleMyListings(ActionEvent event) {
        if (currentUser == null) return;
        reload = () -> handleMyListings(null);

        List<CarListing> mine = listingRepository.findActiveByUserId(currentUser.getUserId());
        showListings(mine, "My listings", mine.isEmpty()
                ? "You have no active listings yet. Use \"Add listing\" to create one."
                : "You have " + mine.size() + (mine.size() == 1 ? " active listing" : " active listings"));
    }

    @FXML
    public void handleRefresh(ActionEvent event) {
        reload.run();
    }

    // ---------- actions ----------

    @FXML
    public void handleAddListing(ActionEvent event) {
        try {
            AddListingController controller = Navigator.show((Stage) addListingButton.getScene().getWindow(),
                    Navigator.ADD_LISTING, "Add listing - E-Commerce Auto", 900, 800);
            controller.setCurrentUser(currentUser);
        } catch (IOException e) {
            UiUtil.showAlert("Error", "Could not open the add listing screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleViewDetails(ActionEvent event) {
        CarListing selected = listingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a listing first, then click \"View details\" (or double-click a row).");
            return;
        }
        showListingDetails(selected);
    }

    @FXML
    public void handleRemoveListing(ActionEvent event) {
        CarListing selected = listingsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !isOwnListing(selected)) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remove \"" + selected.getTitle() + "\"? It will no longer appear in search results.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Remove listing");
        confirm.setHeaderText(null);
        confirm.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (listingRepository.deactivate(selected.getListingId())) {
                reload.run();
                statusLabel.setText("Listing removed.");
            } else {
                statusLabel.setText("Could not remove the listing. Please try again.");
            }
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Navigator.show((Stage) logoutButton.getScene().getWindow(),
                    Navigator.LOGIN, "Login - E-Commerce Auto", 460, 560);
        } catch (IOException e) {
            UiUtil.showAlert("Error", "Could not log out: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean isOwnListing(CarListing listing) {
        return currentUser != null && listing.getUserId() == currentUser.getUserId();
    }

    // ---------- details dialog ----------

    private void showListingDetails(CarListing listing) {
        Label title = new Label(listing.getTitle());
        title.getStyleClass().add("detail-title");
        title.setWrapText(true);

        Label price = new Label(listing.getFormattedPrice());
        price.getStyleClass().add("detail-price");

        HBox badges = new HBox(8);
        if (listing.getBodyType() != null) badges.getChildren().add(badge(listing.getBodyType()));
        if (listing.getFuelType() != null) badges.getChildren().add(badge(listing.getFuelType()));
        if (listing.isNew()) badges.getChildren().add(badge("New"));

        GridPane specs = new GridPane();
        specs.setHgap(18);
        specs.setVgap(8);
        String[][] rows = {
                {"Car", listing.getFullCarName()},
                {"Mileage", listing.getFormattedMileage()},
                {"Transmission", orNa(listing.getTransmission())},
                {"Color", orNa(listing.getColor())},
                {"Location", orNa(listing.getLocation())},
                {"Seller", listing.getSellerEmail()},
                {"Posted", listing.getCreatedAt() != null ? listing.getCreatedAt().toLocalDate().toString() : "N/A"},
        };
        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0]);
            key.getStyleClass().add("spec-key");
            Label value = new Label(rows[i][1]);
            value.getStyleClass().add("spec-value");
            specs.addRow(i, key, value);
        }

        Label descriptionHeading = new Label("Description");
        descriptionHeading.getStyleClass().add("section-title");
        Label description = new Label(listing.getDescription() != null && !listing.getDescription().isBlank()
                ? listing.getDescription() : "No description provided.");
        description.setWrapText(true);
        description.setMaxWidth(440);

        VBox content = new VBox(12, title, price, badges, specs, descriptionHeading, description);
        content.setPadding(new Insets(8, 4, 4, 4));
        content.setPrefWidth(460);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Listing details");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        dialog.showAndWait();
    }

    private static Label badge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("badge");
        return label;
    }

    private static String orNa(String value) {
        return value != null && !value.isBlank() ? value : "N/A";
    }

    // ---------- input parsing ----------

    private Integer parseInteger(TextField field, String what) {
        String text = field.getText().trim();
        if (text.isEmpty()) return null;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(what + " filters must be whole numbers.");
        }
    }

    private BigDecimal parseDecimal(TextField field, String what) {
        String text = field.getText().trim();
        if (text.isEmpty()) return null;
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(what + " filters must be numbers.");
        }
    }
}
