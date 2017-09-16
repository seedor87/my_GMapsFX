import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.event.EventTarget;

/**
 * Created by Bob S on 9/16/2017.
 */
public class ZoomLevelEventTarget implements EventTarget{

    private GoogleMap targetMap;
    private int zoom;

    public ZoomLevelEventTarget(int zoom, GoogleMap target) {
        this.zoom = zoom;
        this.targetMap = target;
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.append(new EventDispatcher() {

            @Override
            public javafx.event.Event dispatchEvent(javafx.event.Event event, EventDispatchChain tail) {
                targetMap.setZoom(zoom);
                tail.dispatchEvent(event);
                return event;
            }
        });
    }
}
