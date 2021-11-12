package vip.Resolute.modules.impl.movement;


import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventMove;
import vip.Resolute.events.impl.EventSprint;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.util.movement.MovementUtils;
import org.lwjgl.input.Keyboard;

public class Sprint extends Module {

    public BooleanSetting omni = new BooleanSetting("Omni", false);

    private int groundTicks;

    public Sprint() {
        super("AutoSprint", Keyboard.KEY_NONE, "Automatically sprints", Category.MOVEMENT);
        this.addSettings(omni);
        toggled = true;
    }

    public void onDisable() {
        mc.thePlayer.setSprinting(mc.gameSettings.keyBindSprint.isPressed());
    }

    public void onEvent(Event e) {
        if(e instanceof EventMove) {
            EventMove event = (EventMove) e;
            if (this.groundTicks > 3 && MovementUtils.isMoving() && !Speed.enabled && !Fly.enabled && this.omni.isEnabled()) {
                MovementUtils.setSpeed(event, MovementUtils.getBaseMoveSpeed());
            }
        }

        if(e instanceof EventMotion) {
            if(e.isPre()) {
                if (MovementUtils.isOnGround()) {
                    ++this.groundTicks;
                } else {
                    this.groundTicks = 0;
                }
            }
        }

        boolean canSprint;

        if(e instanceof EventSprint) {
            EventSprint event = (EventSprint) e;
            if(!event.isSprinting()) {
                if(Scaffold.enabled && !Scaffold.sprint.isEnabled()) {
                    return;
                }

                canSprint = MovementUtils.canSprint(this.omni.isEnabled());
                mc.thePlayer.setSprinting(canSprint);
                event.setSprinting(canSprint);
            }
        }
    }
}
/*
if (e instanceof EventUpdate) {
            if (e.isPre()) {
                if (mode.is("Legit")) {
                    if (!mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isSneaking() && mc.thePlayer.moveForward > 0) {
                        mc.thePlayer.setSprinting(true);
                    }
                }
            }
        }
 */
