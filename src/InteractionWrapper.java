import com.google.gson.Gson;

/**
 * Created by Bob S on 8/13/2017.
 */
public class InteractionWrapper {

    private String text;
    public enum Type {CENTER_MOVED, ADDRESS_BAR, BAD_EVENT}
    private Type type = Type.BAD_EVENT;
    private double lat;
    private double lon;

    public InteractionWrapper(String text, javafx.scene.Node subject) {
        this.text = text;
        this.type = Type.ADDRESS_BAR;
    }

    public InteractionWrapper(double lat, double lon, com.lynden.gmapsfx.javascript.object.GoogleMap map) {
        this.lat = lat;
        this.lon = lon;
        this.type = Type.CENTER_MOVED;
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

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
