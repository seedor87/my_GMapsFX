
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.*;
import com.lynden.gmapsfx.javascript.object.*;
import com.lynden.gmapsfx.service.directions.*;
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
import javafx.event.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DirectionsFXMLController implements Initializable, MapComponentInitializedListener, DirectionsServiceCallback {

    private File JNOTE_FILE;
    private File KML_FILE;
    private File JSON_FILE;

    private GeocodingService geocodingService;
    private DirectionsService directionsService;
    private DirectionsPane directionsPane;
    private DirectionsRenderer directionsRenderer;
    private GoogleMap map;
    public LatLong mapCenter;

    private DecimalFormat formatter = new DecimalFormat("###.0000000");
    private KMLBuilder kmlBuilder = new KMLBuilder();

    private StringProperty fromText = new SimpleStringProperty();
    private StringProperty toText = new SimpleStringProperty();
    public List<MapShape> all_map_shapes = new ArrayList<>();
    public ArrayList<LatLong> polygon_coords = new ArrayList<>();
    public List<InteractionWrapper> interactionList = new LinkedList<>();
    public final int MAX_INTERACTION_LIST_CAP = 500;

    public enum drawTypes {
        NONE, CIRCLE, SQUARE, POLYGON
    }
    public drawTypes selctedDrawType = drawTypes.NONE;

    private static int numPins = 0;


    @FXML
    protected GoogleMapView mapView;    //Map View used to render all of shapesMap
    @FXML
    protected Label crossHairs; //The centered focal point to denote the center of the screen
    @FXML
    protected ToolBar toolBarTop;   //The toolbar of functions to use to augment usefulness

    @FXML
    protected TextField fromTextField;
    @FXML
    protected TextField toTextField;
    @FXML
    private TextField findByAddressTextField;
    @FXML
    private TextField latitudeText;
    @FXML
    private TextField longitudeText;
    @FXML
    private ComboBox<String> mapTypeCombo;
    @FXML
    private ComboBox<String> drawTypeCombo;

    public void addToInteractionList(InteractionWrapper iw) {
        if (interactionList.size() + 1 > MAX_INTERACTION_LIST_CAP) {
            interactionList.remove(0);
        }
        interactionList.add(iw);
    }

    public void findByAddress(ActionEvent event) {
        findByAddress(findByAddressTextField.getText());
        InteractionWrapper findByAddressInteraction = new InteractionWrapper(findByAddressTextField.getText(), findByAddressTextField);
        addToInteractionList(findByAddressInteraction);
    }

    public void findByAddress(String givenAddress) {
        geocodingService.geocode(givenAddress, (GeocodingResult[] results, GeocoderStatus status) -> {
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
                dialog.setHeaderText("More Than One Address Found...");
                dialog.setContentText("Choose the Address you were looking for");

                // Traditional way toText get the response value.
                Optional<String> choice = dialog.showAndWait();
                if (choice.isPresent()) {
                    location = choices.get(choice.get());
                } else {
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

//            Used to show alert about info at point
            Platform.runLater( () -> {
                revGeocode(new ActionEvent());
            });
        });
    }

    @FXML
    public void revGeocode(ActionEvent event) {
        double lat = Double.parseDouble(latitudeText.getText());
        double lon = Double.parseDouble(longitudeText.getText());
        revGeocode(lat, lon);
    }

    public void revGeocode(double lat, double lon) {
        try {
            geocodingService.reverseGeocode(lat, lon, (GeocodingResult[] results, GeocoderStatus status) -> {
                GeocodingResult location = results[0];  // Grab just first result in case ther are no more
                List<GeocoderAddressComponent> addressComponents = location.getAddressComponents();
                StringBuilder post = new StringBuilder();
                for (GeocoderAddressComponent partial : addressComponents) {
                    post.append(partial.getShortName() + " ");
                }

                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position(new LatLong(lat, lon));
                Marker mark = new Marker(markerOptions1);
                map.addMarker(mark);

                String infoString = "<h4>Post Num: " + ++numPins + "</h4>" + post.toString();

                InfoWindowOptions infoWindowOptions = new InfoWindowOptions();
                infoWindowOptions.content(infoString);
                InfoWindow infoWindow = new InfoWindow(infoWindowOptions);
                infoWindow.open(map, mark);
            });
            recenterMap(new LatLong(lat, lon));
            setMapZoom(20);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Lat and Long");
            alert.show();
        }
    }

    @FXML
    private void fromTextFieldAction(ActionEvent event) {
        enterDirections(event);
    }

    @FXML
    private void toTextFieldAction(ActionEvent event) {
        enterDirections(event);
    }

    @FXML
    private void enterDirections(ActionEvent event) {
        DirectionsRequest request = new DirectionsRequest(fromText.get(), toText.get(), TravelModes.DRIVING);
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
    private void openFile(ActionEvent event) {
        /**
         * Ex. File Path
         * String defFilePath = "C:\\Users\\Bob S\\IdeaProjects\\my_GMapsFX\\files\\20170815_182137.json";
         * String defFilePath = "C:\\Users\\Bob S\\IdeaProjects\\my_GMapsFX\\files\\20170803_183338.kml"
         */
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"));
        File file = fileChooser.showOpenDialog(DirectionsApiMainApp.getPrimaryStage());
        if(file != null) {
            int i = file.getName().lastIndexOf('.');
            String extension = "";
            if (i >= 0) {
                extension = file.getName().substring(i + 1);
            }

            if (extension.equals("json")) {
                actualizeJSON(file);
            } else if (extension.equals("kml")) {
                deleteAllShapes(new ActionEvent()); // delete all shapes before new kml file is loaded
                actualizeKML(file.getAbsolutePath());
            } else if( extension.equals("jnote")) {
                actualizeKML(FilePacker.retrieveFromZip(file.getAbsolutePath(), "myKml.kml"));
                actualizeJSON(new File(FilePacker.retrieveFromZip(file.getAbsolutePath(), "myJson.json")));
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid FIle Type Chosen");
                alert.setContentText("Please Choose a file with the extension .json or .kml");
                alert.showAndWait();
            }
        } else {
//            throw new RuntimeException("invalid file chosen");
        }
    }

    @Override
    public void directionsReceived(DirectionsResult results, DirectionStatus status) {
        System.out.println(results);
        System.out.println(status);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapView.addMapInializedListener(this);
        toText.bindBidirectional(toTextField.textProperty());
        fromText.bindBidirectional(fromTextField.textProperty());
    }

    public void saveKmlAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "KML files (*.kml)",
                "*.kml");
        fileChooser.getExtensionFilters().add(extFilter);
        File temp = fileChooser.showSaveDialog(DirectionsApiMainApp.getPrimaryStage());
        if (temp != null) {
            KML_FILE = temp;
            saveKml();
        } else {
            System.err.println("Invalid KML File Chosen");
        }
    }

    public void saveKml() {
        if(KML_FILE == null) {
            saveKmlAs();
        } else {
            try {
                kmlBuilder.createFile(KML_FILE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveJsonAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("./"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Json files (*.json)",
                "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        File temp = fileChooser.showSaveDialog(DirectionsApiMainApp.getPrimaryStage());
        if (temp != null) {
            JSON_FILE = temp;
            saveJson();
        } else {
            System.err.println("Invalid Json File Chosen");
        }
    }

    public void saveJson() {
        if(JSON_FILE == null) {
            saveJsonAs();
        } else {
            try {
                FileWriter filewriter = new FileWriter(JSON_FILE.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(filewriter);
                Gson gson = new Gson();
                for(InteractionWrapper interaction : interactionList) {
                    gson.toJson(interaction, bw);
                }
                try {
                    bw.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveJnoteAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("/"));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Jnote files (*.jnote)",
                "*.jnote");
        fileChooser.getExtensionFilters().add(extFilter);
        File temp = fileChooser.showSaveDialog(DirectionsApiMainApp.getPrimaryStage());
        if (temp != null) {
            JNOTE_FILE = temp;
            saveJnote();
        } else {
            System.err.println("Invalid Jnote File Chosen");
        }
    }

    public void saveJnote() {
        if(JNOTE_FILE == null) {
            saveJnoteAs();
        } else {
        	if(System.getProperty("os.name").equals("Linux")) {
        		FilePacker.createZip(JNOTE_FILE.getAbsolutePath(), new ArrayList<>(Arrays.asList(
        				"/home/armstr/git/my_GMapsFX/files/myJson.json",
        				"/home/armstr/git/my_GMapsFX/files/myKml.kml"
        				)));
        	} else {
        		FilePacker.createZip(JNOTE_FILE.getAbsolutePath(), new ArrayList<>(Arrays.asList(
        				"C:\\Users\\Bob S\\IdeaProjects\\my_GMapsFX\\files\\myJson.json",
        				"C:\\Users\\Bob S\\IdeaProjects\\my_GMapsFX\\files\\myKml.kml"
        				)));
        	}
        }
    }



    /**
     * Literally just copied and pasted fromText earlier
     *
     * @param filePath
     * @return Polygon c
     */
    private void actualizeKML(String filePath){
        ArrayList<ArrayList<double[]>> from_kml = KMLParser.getCoordinateArrayLists(filePath);
        for (int i = 0; i < from_kml.size(); i++) {
            ArrayList<double[]> current_polygon = from_kml.get(i);
            LatLong[] array = new LatLong[current_polygon.size()];
            for (int j = 0; j < current_polygon.size(); j++) {
                array[j] = new LatLong(current_polygon.get(j)[1], current_polygon.get(j)[0]); //NOTE, switch lat and long here for proper location
            }
            MVCArray pmvc = new MVCArray(array);
            PolygonOptions shapeOptions = new PolygonOptions()
                    .paths(pmvc)
                    .strokeColor("blue")
                    .strokeWeight(2)
                    .editable(false)
                    .fillColor("red")
                    .fillOpacity(0.5);
            Polygon shape = new Polygon(shapeOptions);
            all_map_shapes.add(shape);
            map.addMapShape(shape);

            map.addUIEventHandler(shape, UIEventType.click, new UIEventHandler() {
                @Override
                public void handle(JSObject obj) {
                    try {
                        String poly = kmlBuilder.polygon(Arrays.asList(array), new ArrayList<LatLong>(), "Test for shape");
                        kmlBuilder.appendTo(poly);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            map.addUIEventHandler(shape, UIEventType.rightclick, new UIEventHandler() {
                @Override
                public void handle(JSObject obj) {
                    map.removeMapShape(shape);
                }
            });
        }
    }

    private void actualizeJSON(File file) {
        try {
            InputStream is = new FileInputStream(file);
            Reader r = new InputStreamReader(is, "UTF-8");
            Gson gson = new GsonBuilder().create();
            JsonStreamParser p = new JsonStreamParser(r);
            while (p.hasNext()) {
                JsonElement e;
                try {
                    e = p.next();
                } catch (Exception ex) {
                            /*
                             *  break case for malformed json exception
                             */
                    break;
                }
                if (e.isJsonObject()) {
                    gson.fromJson(e, Map.class);
                    InteractionWrapper test = gson.fromJson(e, InteractionWrapper.class);
                    EventTarget target;
                    switch (test.getType()) {
                        case ADDRESS_BAR:
                            target = new FindByAddressEventTarget(test.getText(), findByAddressTextField);
                            Event.fireEvent(target, new Event(EventType.ROOT));
                            break;
                        case CENTER_MOVED:
                            target = new CenterChangedEventTarget(test.getLat(), test.getLon(), map);
                            Event.fireEvent(target, new Event(EventType.ROOT));
                            break;
                        case ZOOM_LEVEL:
                            target = new ZoomLevelEventTarget(test.getZoom(), map);
                            Event.fireEvent(target, new Event(EventType.ROOT));
                            break;
                        default:
                            System.err.println("invalid event type encountered");
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("json exception encountered");
        }
    }

    public void recenterMap(double lat, double lon) {
        recenterMap(new LatLong(lat, lon));
    }

    public void recenterMap(LatLong new_center) {
        map.setCenter(new_center);
    }

    public void setMapZoom(int val) {
        String on_error;
        if (val < 0 || val > 20) {
            on_error = "Invalid zoom, must be between 0 and 20";
            Alert alert = new Alert(Alert.AlertType.INFORMATION, on_error);
            alert.show();
            return;
        }
        map.setZoom(val);
    }

    public void deleteAllShapes(Event event) {
        for (MapShape mapShape : all_map_shapes) {
            map.removeMapShape(mapShape);
        }
    }

    private String genDatedName() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
    }


    @Override
    public void mapInitialized() {
        geocodingService = new GeocodingService();
        MapOptions options = new MapOptions();

        File[] files = new File("./tmp").listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                f.delete();
            }
        }

        // init underlay shapesMap
        options.zoomControl(true)
                .zoom(16)
                .overviewMapControl(false)
                .mapType(MapTypeIdEnum.SATELLITE)
                .streetViewControl(true);
        map = mapView.createMap(options);
        directionsService = new DirectionsService();
        directionsPane = mapView.getDirec();

        map.addStateEventHandler(MapStateEventType.center_changed, new StateEventHandler() {
            @Override
            public void handle() {
                double lat = map.getCenter().getLatitude();
                double lon = map.getCenter().getLongitude();
                int zoom = map.getZoom();

                latitudeText.setText(formatter.format(lat));
                longitudeText.setText(formatter.format(lon));
                crossHairs.setLayoutX(crossHairs.getScene().getWindow().getWidth() / 2 - crossHairs.getWidth());
                crossHairs.setLayoutY(crossHairs.getScene().getWindow().getHeight() / 2 - crossHairs.getHeight()+1);

                InteractionWrapper centerLoc = new InteractionWrapper(lat, lon, map);
                addToInteractionList(centerLoc);
                InteractionWrapper zoomLv = new InteractionWrapper(zoom, map);
                addToInteractionList(zoomLv);
            }
        });

        mapCenter = new LatLong(39.70836, -75.11803);
        recenterMap(mapCenter);

        //New code fromText Brooke: grabs the primaryStage field fromText the DirectionsApiMainApp and handles the
        //event "exit the application" field toText carry out the closeField method
        Stage s = DirectionsApiMainApp.getPrimaryStage();
        s.setOnCloseRequest(event -> {
            System.err.println("exiting...");
        });


        /*
        Style the findByAddressTextField text field
        */
//        findByAddressTextField.setStyle("-fx-background-color: #a9a9a9 , white , white;\n" +
//                "    -fx-background-insets: 0 -1 -1 -1, 0 0 0 0, 0 -1 3 -1;");
//        findByAddressTextField.setStyle(".text-field:focused {\n" +
//                "    -fx-background-color: #a9a9a9 , white , white;\n" +
//                "    -fx-background-insets: 0 -1 -1 -1, 0 0 0 0, 0 -1 3 -1;\n" +
//                "}");

        findByAddressTextField.setAlignment(Pos.BASELINE_CENTER);
        toolBarTop.prefWidthProperty().bind((toolBarTop.getScene().getWindow()).widthProperty());
        crossHairs.setStyle("-fx-font-size: 21;");
        crossHairs.setTextFill(Color.RED);

        // init combo box for shapesMap type
        mapTypeCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> selected, String oldType, String newType) {
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

        drawTypeCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> selected, String oldType, String newType) {
                if (newType != null) {
                    switch (newType) {
                        case ("Circle"):
                            selctedDrawType = drawTypes.CIRCLE;
                            break;
                        case ("Square"):
                            selctedDrawType = drawTypes.SQUARE;
                            break;
                        case ("Polygon"):
                            selctedDrawType = drawTypes.POLYGON;
                            break;
                        default:
                            selctedDrawType = drawTypes.NONE;
                            break;
                    }
                }
            }
        });

        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            switch (selctedDrawType) {
                case POLYGON:
                    polygon_coords.add(event.getLatLong());
                    break;
                default:
                    break;
            }
        });

        map.addMouseEventHandler(UIEventType.rightclick, (GMapMouseEvent event) -> {
            switch (selctedDrawType) {
                case CIRCLE:
                    Circle circle = new Circle(new CircleOptions()
                        .center(event.getLatLong())
                        .radius(50.0)
                        .fillColor("Red")
                        .strokeColor("blue")
                        .strokeWeight(2)
                        .visible(true)
                    );

                    // Used to ellipse-ify the circle to match perspective on zoom and un-zoom
                    double radius = 0.0005;
                    double xrad = 0.88 * radius;
                    double yrad = 1.12 * radius;
                    final ArrayList<double[]> c_ps = CircleMaker.yield_ellipse(
                            event.getLatLong().getLatitude(),
                            event.getLatLong().getLongitude(),
                            xrad,
                            yrad);
                    final ArrayList<LatLong> c_ll = new ArrayList<LatLong>();
                    for (double[] pair : c_ps) {
                        c_ll.add(new LatLong(pair[0], pair[1]));
                    }

                    map.addUIEventHandler(circle, UIEventType.click, new UIEventHandler() {
                        @Override
                        public void handle(JSObject obj) {
                            try {
                                String poly = kmlBuilder.polygon(c_ll, new ArrayList<LatLong>(), "Test for Circle");
                                kmlBuilder.appendTo(poly);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    map.addUIEventHandler(circle, UIEventType.rightclick, new UIEventHandler() {
                        @Override
                        public void handle(JSObject obj) {
                            map.removeMapShape(circle);
                        }
                    });

                    all_map_shapes.add(circle);
                    map.addMapShape(circle);
                    break;

                case SQUARE:
                    double clat = event.getLatLong().getLatitude();
                    double clon = event.getLatLong().getLongitude();
                    LatLong ne = new LatLong(clat + 0.001, clon - 0.001);
                    LatLong sw = new LatLong(clat - 0.001, clon + 0.001);
                    Rectangle rectangle = new Rectangle(new RectangleOptions()
                            .bounds(new LatLongBounds(ne, sw))
                            .fillColor("Red")
                            .strokeColor("blue")
                            .strokeWeight(2)
                            .visible(true)
                    );
                    ArrayList<LatLong> s_ll = new ArrayList<LatLong>();
                    s_ll.add(new LatLong(clat + 0.001, clon - 0.001));
                    s_ll.add(new LatLong(clat + 0.001, clon + 0.001));
                    s_ll.add(new LatLong(clat - 0.001, clon + 0.001));
                    s_ll.add(new LatLong(clat - 0.001, clon - 0.001));

                    map.addUIEventHandler(rectangle, UIEventType.click, new UIEventHandler() {
                        @Override
                        public void handle(JSObject obj) {
                            try {
                                String poly = kmlBuilder.polygon(s_ll, new ArrayList<LatLong>(), "Test for Square");
                                kmlBuilder.appendTo(poly);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    map.addUIEventHandler(rectangle, UIEventType.rightclick, new UIEventHandler() {
                        @Override
                        public void handle(JSObject obj) {
                            map.removeMapShape(rectangle);
                        }
                    });

                    all_map_shapes.add(rectangle);
                    map.addMapShape(rectangle);
                    break;

                case POLYGON:
                    if (polygon_coords.size() > 1) {
                        LatLong[] poly_array = new LatLong[polygon_coords.size()];
                        for (int i = 0; i < polygon_coords.size(); i++) {
                            poly_array[i] = new LatLong(polygon_coords.get(i).getLatitude(), polygon_coords.get(i).getLongitude()); //NOTE, switch lat and long here for proper location
                        }
                        final MVCArray poly_mvc = new MVCArray(poly_array);
                        final PolygonOptions poly_opts = new PolygonOptions()
                                .paths(poly_mvc)
                                .strokeColor("blue")
                                .strokeWeight(2)
                                .editable(false)
                                .fillColor("red");
                        Polygon polygon = new Polygon(poly_opts);
                        all_map_shapes.add(polygon);
                        map.addMapShape(polygon);

                        map.addUIEventHandler(polygon, UIEventType.click, new UIEventHandler() {
                            @Override
                            public void handle(JSObject obj) {
                                try {
                                    String poly = kmlBuilder.polygon(Arrays.asList(poly_array), new ArrayList<LatLong>(), "Test for Polygon");
                                    kmlBuilder.appendTo(poly);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });

                        map.addUIEventHandler(polygon, UIEventType.rightclick, new UIEventHandler() {
                            @Override
                            public void handle(JSObject obj) {
                                map.removeMapShape(polygon);
                            }
                        });

                    } else {
                        System.out.println("No More Than 1 point clicked");
                    }
                    polygon_coords = new ArrayList<LatLong>();  // reset polygon points to fresh state
                    break;
                default:
                    break;
            }
        });
    }
}