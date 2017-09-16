import com.google.gson.Gson;

/**
 * Created by Bob S on 8/13/2017.
 */
public class InteractionWrapper {

    private String text;
    public enum Type {ZOOM_LEVEL, CENTER_MOVED, ADDRESS_BAR, BAD_EVENT}
    private Type type = Type.BAD_EVENT;
    private double lat;
    private double lon;
    private int zoom;

    public InteractionWrapper(String text, javafx.scene.Node subject) {
        this.text = text;
        this.type = Type.ADDRESS_BAR;
    }

    public InteractionWrapper(double lat, double lon, com.lynden.gmapsfx.javascript.object.GoogleMap map) {
        this.lat = lat;
        this.lon = lon;
        this.type = Type.CENTER_MOVED;
    }

    public InteractionWrapper(int zoom, com.lynden.gmapsfx.javascript.object.GoogleMap map) {
        this.zoom = zoom;
        this.type = Type.ZOOM_LEVEL;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getLat() {
        return this.lat;
    }

    public void setlat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return this.lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
