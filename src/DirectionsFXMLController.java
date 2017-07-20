
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import com.lynden.gmapsfx.service.geocoding.GeocoderAddressComponent;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.service.geocoding.GeocodingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;

public class DirectionsFXMLController implements Initializable, MapComponentInitializedListener, DirectionsServiceCallback {

    private GeocodingService geocodingService;
    private DirectionsService directionsService;
    private DirectionsPane directionsPane;
    private DecimalFormat formatter = new DecimalFormat("###.00000");

    protected StringProperty from = new SimpleStringProperty();
    protected StringProperty to = new SimpleStringProperty();
    protected DirectionsRenderer directionsRenderer;
    protected GoogleMap map;

    @FXML
    protected GoogleMapView mapView;

    @FXML
    protected TextField fromTextField;

    @FXML
    protected TextField toTextField;

    @FXML
    private TextField latitudeText;

    @FXML
    private TextField longitudeText;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private TextField showMe;

    public void showMe(ActionEvent event) {
        geocodingService.geocode(showMe.getText(), (GeocodingResult[] results, GeocoderStatus status) -> {
            GeocodingResult location = results[0];
            double lat, lon;

            if (status == GeocoderStatus.ZERO_RESULTS) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "No matching address found");
                alert.show();
                return;
            } else if (results.length > 1) {
                HashMap<String, GeocodingResult> choices = new HashMap<>();
                for (int i = 0; i < results.length; i++) {
                    choices.put(results[i].getFormattedAddress(), results[i]);
                }
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Choose an address.", choices.keySet());
                dialog.setTitle("Address Choice Dialog");
                dialog.setHeaderText("More Than One ADress Found...");
                dialog.setContentText("Choose the Address you were looking for");

                // Traditional way to get the response value.
                Optional<String> choice = dialog.showAndWait();
                if (choice.isPresent()) {
                    location = choices.get(choice.get());
                }
                else {
                    return;
                }
            }
            lat = location.getGeometry().getLocation().getLatitude();
            lon = location.getGeometry().getLocation().getLongitude();
            latitudeText.setText(formatter.format(lat));
            longitudeText.setText(formatter.format(lon));

            map.setCenter(new LatLong(lat, lon));
            map.setZoom(18);
            map.setMapType(MapTypeIdEnum.SATELLITE);
        });
    }

    @FXML
    public void revGeocode(ActionEvent event) {
        double lat = Double.parseDouble(latitudeText.getText());
        double lon = Double.parseDouble(longitudeText.getText());
        geocodingService.reverseGeocode(lat, lon, (GeocodingResult[] results, GeocoderStatus status) -> {
            GeocodingResult location = results[0];
            List<GeocoderAddressComponent> addressComponents = location.getAddressComponents();
            StringBuilder post = new StringBuilder();
            for (GeocoderAddressComponent partial : addressComponents) {
                post.append(partial.getShortName() + " ");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, post.toString());
            alert.show();
        });
    }

    @FXML
    private void toTextFieldAction(ActionEvent event) {
        enterDirections(event);
    }

    @FXML
    private void enterDirections(ActionEvent event) {
        DirectionsRequest request = new DirectionsRequest(from.get(), to.get(), TravelModes.DRIVING);
        directionsRenderer = new DirectionsRenderer(true, mapView.getMap(), directionsPane);
        directionsService.getRoute(request, this, directionsRenderer);
    }

    @FXML
    private void clearDirections(ActionEvent event) {
        try {
            directionsRenderer.clearDirections();
        } catch (NullPointerException ex) {
            System.out.println("Nothing To Clear");
        }
    }

    @FXML
    private void debugAction(ActionEvent event) {
        toTextField.setText("121 Whittendale Dr. Moorestown NJ");
        fromTextField.setText("201 Mullica Hill Rd. Glassboro NJ");
        showMe.setText("1600 Pennsylvania Ave. NW");

//        map.setMapType(MapTypeIdEnum.ROADMAP);
//        map.setZoom(12);
//        enterDirections(event);
    }

    @Override
    public void directionsReceived(DirectionsResult results, DirectionStatus status) {
        System.out.println(results);
        System.out.println(status);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapView.addMapInializedListener(this);
        to.bindBidirectional(toTextField.textProperty());
        from.bindBidirectional(fromTextField.textProperty());
    }

    @Override
    public void mapInitialized() {
        geocodingService = new GeocodingService();
        MapOptions options = new MapOptions();

        // init underlay map
        options.center(new LatLong(39.709860, -75.118948))
                .zoomControl(true)
                .zoom(18)
                .overviewMapControl(false)
                .mapType(MapTypeIdEnum.SATELLITE)
                .streetViewControl(true);
        map = mapView.createMap(options);
        directionsService = new DirectionsService();
        directionsPane = mapView.getDirec();

        // Style the showMe text field
        showMe.setStyle("-fx-background-color: #a9a9a9 , white , white;\n" +
                "    -fx-background-insets: 0 -1 -1 -1, 0 0 0 0, 0 -1 3 -1;");
        showMe.setStyle(".text-field:focused {\n" +
                "    -fx-background-color: #a9a9a9 , white , white;\n" +
                "    -fx-background-insets: 0 -1 -1 -1, 0 0 0 0, 0 -1 3 -1;\n" +
                "}");
        showMe.setAlignment(Pos.BASELINE_CENTER);

        // init combo box for map type
        typeCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> selected, String oldType, String newType) {
                MapTypeIdEnum ne = null;
                if (newType != null) {
                    switch (newType) {
                        case ("Roadmap"):
                            ne = MapTypeIdEnum.ROADMAP;
                            break;
                        case ("Hybrid"):
                            ne = MapTypeIdEnum.HYBRID;
                            break;
                        case ("Terrain"):
                            ne = MapTypeIdEnum.TERRAIN;
                            break;
                        default:
                            ne = MapTypeIdEnum.SATELLITE;
                            break;
                    }
                    map.setMapType(ne);
                }
            }
        });

        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();
            latitudeText.setText(formatter.format(latLong.getLatitude()));
            longitudeText.setText(formatter.format(latLong.getLongitude()));
        });
    }

}