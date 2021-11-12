package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import org.lwjgl.input.Keyboard;

public class FastPlace extends Module {

    public FastPlace() {
        super("FastPlace", Keyboard.KEY_NONE, "Removes click delay", Category.PLAYER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventUpdate) {
            if(e.isPre()) {
                mc.rightClickDelayTimer = 0;
            }
        }
    }

    public void onDisable() {
        mc.rightClickDelayTimer = 6;
        super.onDisable();
    }
}
