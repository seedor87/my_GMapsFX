import javafx.event.*;
import javafx.scene.control.TextField;
import java.awt.*;

public class MyEventTarget implements EventTarget {

    private TextField targetTextField;
    private String input;

    public MyEventTarget(String input, TextField target) {
        this.input = input;
        this.targetTextField = target;
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.append(new EventDispatcher() {

            @Override
            public javafx.event.Event dispatchEvent(javafx.event.Event event, EventDispatchChain tail) {
                targetTextField.setText(input);
                tail.dispatchEvent(event);
                return event;
            }
        })
        .append(new EventDispatcher() {

            @Override
            public javafx.event.Event dispatchEvent(javafx.event.Event event, EventDispatchChain tail) {
                targetTextField.requestFocus();
                tail.dispatchEvent(event);
                return event;
            }
        })
        .append(new EventDispatcher() {
            @Override
            public javafx.event.Event dispatchEvent(javafx.event.Event event, EventDispatchChain tail) {
//              targetTextField.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER, true, true, true, true));
                pressEnter();
                return event;
            }
        });
    }

    /***
     * Still kind of a STUB
     */
    public void pressEnter() {
        try {
            Robot robot = new Robot();
            robot.keyPress(java.awt.event.KeyEvent.VK_ENTER);
        } catch (java.awt.AWTException ex) {
            ex.printStackTrace();
        }
    }
}
