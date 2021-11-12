package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventSafeWalk;
import vip.Resolute.modules.Module;

public class Safewalk extends Module {
    public Safewalk() {
        super("Safewalk", 0, "Doesn't let you fall off edges", Category.PLAYER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventSafeWalk) {
            e.setCancelled(true);
        }
    }
}
