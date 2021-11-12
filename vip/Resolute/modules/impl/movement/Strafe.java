package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.util.movement.MovementUtils;

public class Strafe extends Module {

    public Strafe() {
        super("Strafe", 0, "Allows in air strafing", Category.MOVEMENT);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            if(e.isPre()) {
                if(MovementUtils.isMoving()) {
                    MovementUtils.strafe();
                }
            }
        }
    }
}

