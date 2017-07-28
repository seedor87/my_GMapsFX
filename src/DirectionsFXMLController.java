
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.JavascriptObject;
import com.lynden.gmapsfx.javascript.event.*;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.*;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import com.lynden.gmapsfx.service.geocoding.GeocoderAddressComponent;
import com.lynden.gmapsfx.service.geocoding.GeocoderStatus;
import com.lynden.gmapsfx.service.geocoding.GeocodingResult;
import com.lynden.gmapsfx.service.geocoding.GeocodingService;
import com.lynden.gmapsfx.shapes.*;
import com.lynden.gmapsfx.shapes.Polygon;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import netscape.javascript.JSObject;

public class DirectionsFXMLController implements Initializable, MapComponentInitializedListener, DirectionsServiceCallback {

    private GeocodingService geocodingService;
    private DirectionsService directionsService;
    private DirectionsPane directionsPane;
    private DecimalFormat formatter = new DecimalFormat("###.00000");

    protected KMLBuilder kmlBuilder = new KMLBuilder();

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

            LatLong latlon = new LatLong(lat, lon);
            map.setCenter(latlon);
            map.setZoom(18);
            map.setMapType(MapTypeIdEnum.SATELLITE);
        });
    }

    @FXML
    public void revGeocode(ActionEvent event) {
        try {
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
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Lat and Long");
            alert.show();
        }
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

    //TODO: Not sure what type of object map is...
    /*
    TODO: write up
     */
    private Polygon makeArray4Shape(String filePath, GoogleMap map){
        ArrayList<ArrayList<double[]>> from_kml = KMLParser.getCoordinateArrayLists(filePath);
        LatLong[] cAry = new LatLong[from_kml.get(0).size()];
        for (int i = 0; i < from_kml.get(0).size(); i ++) {
            cAry[i] = new LatLong(from_kml.get(0).get(i)[1], from_kml.get(0).get(i)[0]); //NOTE, switch lat and long here for proper location
        }

        MVCArray cmvc = new MVCArray(cAry);
        PolygonOptions circleOpts = new PolygonOptions()
                .paths(cmvc)
                .strokeColor("blue")
                .strokeWeight(2)
                .editable(false)
                .fillColor("red")
                .fillOpacity(0.5);
        Polygon c = new Polygon(circleOpts);
        map.addMapShape(c);

        return c;
    }

    @Override
    public void mapInitialized() {
        geocodingService = new GeocodingService();
        MapOptions options = new MapOptions();

        // init underlay map
        options.zoomControl(true)
                .zoom(18)
                .overviewMapControl(false)
                .mapType(MapTypeIdEnum.SATELLITE)
                .streetViewControl(true);
        map = mapView.createMap(options);
        directionsService = new DirectionsService();
        directionsPane = mapView.getDirec();

        geocodingService.geocode("Rowan University, Glassboro NJ", (GeocodingResult[] results, GeocoderStatus status) -> {
                    double lat, lon;
                    GeocodingResult location = results[0];
                    lat = location.getGeometry().getLocation().getLatitude();
                    lon = location.getGeometry().getLocation().getLongitude();
                    LatLong rowan = new LatLong(lat, lon);
                    MarkerOptions markerOptions1 = new MarkerOptions();
                    markerOptions1.position(rowan);
                    Marker mark = new Marker(markerOptions1);
                    map.addMarker(mark);

                    InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
                    infoWindowOptions.content("<h2>Rowan</h2> Current Location: Robinson Hall<br>");
                    InfoWindow fredWilkeInfoWindow = new InfoWindow(infoWindowOptions);
                    fredWilkeInfoWindow.open(map, mark);

                    LatLong latlon = new LatLong(lat + 0.001, lon + 0.001);
                    map.setCenter(latlon);

//                    Circle c = new Circle(new CircleOptions()
//                            .center(latlon)
//                            .radius(50.0)
//                            .fillColor("Red")
//                            .strokeColor("blue")
//                            .strokeWeight(2)
//                            .visible(true)
//                    );
//                    map.addMapShape(c);

            Polygon c = makeArray4Shape("C:\\Users\\Brooke\\IdeaProjects\\my_GMapsFX\\20170727_133342.kml", map);

            ArrayList<double[]> circle_points = CircleMaker.yield_circ(latlon.getLatitude(), latlon.getLongitude(), 0.001);
            ArrayList<LatLong> circle_latlons = new ArrayList<LatLong>();
            for (double[] pair : circle_points) {
                circle_latlons.add(new LatLong(pair[0], pair[1]));
            }

            map.addUIEventHandler(c, UIEventType.click, new UIEventHandler() {
                @Override
                public void handle(JSObject obj) {
                    try {
                        String poly = kmlBuilder.polygon(circle_latlons, new ArrayList<LatLong>(), "Test for Circle");
                        kmlBuilder.createFile(poly);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            map.addStateEventHandler(MapStateEventType.center_changed, new StateEventHandler() {
                @Override
                public void handle() {
                    System.out.println(map.getCenter().toString());
                }
            });

                    LatLong poly1 = new LatLong(lat + .0005, lon - .0005);
                    LatLong poly2 = new LatLong(lat + .0005, lon + .0005);
                    LatLong poly3 = new LatLong(lat - .0005, lon + .0005);
                    LatLong poly4 = new LatLong(lat - .0005, lon - .0005);
                    LatLong[] pAry = new LatLong[]{poly1, poly2, poly3, poly4};
                    MVCArray pmvc = new MVCArray(pAry);

                    //makes the square polygon - CURRENTLY not being made into a kml file tho... weird
                    PolygonOptions polygOpts = new PolygonOptions()
                            .paths(pmvc)
                            .strokeColor("red")
                            .strokeWeight(2)
                            .editable(false)
                            .fillColor("Blue")
                            .fillOpacity(0.5);
                    Polygon pg = new Polygon(polygOpts);
                    map.addMapShape(pg);

                    map.addUIEventHandler(pg, UIEventType.click, new UIEventHandler() {
                        @Override
                        public void handle(JSObject obj) {
                            try {
                                String poly = kmlBuilder.polygon(Arrays.asList(pAry), new ArrayList<LatLong>(), "Test for Square");
                                kmlBuilder.createFile(poly);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                });


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
    } //end of mapInitialized() method

}