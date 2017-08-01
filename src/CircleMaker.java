
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class CircleMaker extends Application {

    static double xc = 100.0;
    static double yc = 100.0;
    private static ArrayList<double[]> result;

    public static void main(String[] args) {

        result = yield_circ(xc, yc, 50.0);
        for (double[] pair : result) {
//            System.out.println(pair[0] + ", " + pair[1]);
        }

        launch(args);
    }

    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.WHITESMOKE);
        gc.fillRect(gc.getCanvas().getLayoutX(),
                gc.getCanvas().getLayoutY(),
                gc.getCanvas().getWidth(),
                gc.getCanvas().getHeight());
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);

        gc.setLineWidth(5);

        gc.fillOval(xc, yc, 5, 5);
        for (double[] ll : result) {
            double lat = ll[0];
            double lon = ll[1];
            gc.fillOval(lat, lon, 5, 5);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(CircleMaker.class.getSimpleName());
        Group root = new Group();
        final Canvas canvas = new Canvas(300, 250);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawShapes(gc);
        final Text text = new Text("X =    Y =   ");
        text.setTranslateX(100);
        text.setTranslateY(40);
        text.setFont(new Font(20));
        canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent t) {
                text.setText("X = " + t.getX() + "  Y = " + t.getY());
            }
        });

        root.getChildren().addAll(canvas, text);
        primaryStage.setScene(new Scene(root));
        primaryStage.getScene().setFill(Color.AQUA);
        primaryStage.show();

    }

    public static ArrayList<double[]> yield_circ(double xc, double yc , double radius) {
        ArrayList<double[]> ret = new ArrayList<double[]>();
        for (double i = 0.0; i < 2; i += 0.01) {
            double x = xc + (radius * Math.cos(i * Math.PI));
            double y = yc + (radius * Math.sin(i * Math.PI));
            if (i > 0.5 && i < 1.0) {
                x = (x + 10.0 * Math.abs(1 - 2 * i));
                ret.add(new double[]{x, y});
            } else if (i > 1.0 && i < 1.5) {
                x = (x + 10.0 * Math.abs(1 - i));
                ret.add(new double[]{x, y});
            } else {
                x = (x - 10.0 * Math.abs(1 - i));
                ret.add(new double[]{x, y});
            }
        }
        return ret;
    }
}
