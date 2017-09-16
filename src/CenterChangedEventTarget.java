import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventTarget;

import java.awt.*;

/**
 * Created by Bob S on 8/13/2017.
 */
public class CenterChangedEventTarget implements EventTarget {

    private GoogleMap targetMap;
    private double lat;
    private double lon;

    public CenterChangedEventTarget(Double lat, Double lon, GoogleMap target) {
        this.lat = lat;
        this.lon = lon;
        this.targetMap = target;
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.append(new EventDispatcher() {

            @Override
            public javafx.event.Event dispatchEvent(javafx.event.Event event, EventDispatchChain tail) {
                targetMap.setCenter(new LatLong(lat, lon));
                tail.dispatchEvent(event);
                return event;
            }
        });
    }
}
