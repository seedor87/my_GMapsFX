
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

    private static ArrayList<double[]> result;
    public static void main(String[] args) {
        result = yield_circ(0.0, 0.0, 50.0);
        for (double[] pair : result) {
            System.out.println(pair[0] + ", " + pair[1]);
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

        double offset = 100;
        gc.fillOval(offset, offset, 5, 5);
        for (double[] ll : result) {
            double lat = ll[0] + offset;
            double lon = ll[1] + offset;
            gc.fillOval(lat, lon, 5, 5);
        }
//        gc.strokeLine(40, 10, 10, 40);
//        gc.fillOval(10, 60, 30, 30);
//        gc.strokeOval(60, 60, 30, 30);
//        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
//        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
//        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
//        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
//        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);

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
        for (double i = 0; i < 2; i += 0.05) {
            double x = xc + (radius * Math.cos(i * Math.PI));
            double y = yc + (radius * Math.sin(i * Math.PI));
            ret.add(new double[]{x, y});
        }
        return ret;
    }
}
