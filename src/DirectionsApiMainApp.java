
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * For more info see: http://rterp.github.io/GMapsFX/apidocs/
 *
 * In the event of the slf4j distro being incorrect, try...
 *      * slf4j-api-1.7.22.jar
 *      * slf4j-simple-1.7.22.jar
 * These two worked and this is a note of that
 *
 */
public class DirectionsApiMainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        setPrimaryStage(stage);

        Parent root = FXMLLoader.load(getClass().getResource("Scene.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("./Styles.css");

        stage.setTitle("Directions API Example");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sets the primaryStage field to the stage in parameter.
     *
     * @param stage
     */
    private void setPrimaryStage(Stage stage) {
        DirectionsApiMainApp.primaryStage = stage;
    }

    /**
     * Grabs the primaryStage field.
     *
     * @return primaryStage field
     */
    static public Stage getPrimaryStage() {
        return DirectionsApiMainApp.primaryStage;
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {launch(args);}
}
