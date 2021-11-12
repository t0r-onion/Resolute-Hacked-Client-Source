package net.minecraft.util;

import vip.Resolute.Resolute;
import vip.Resolute.events.impl.EventPlayerStrafe;

public class MovementInput
{
    /**
     * The speed at which the player is strafing. Postive numbers to the left and negative to the right.
     */
    public static float moveStrafe;

    /**
     * The speed at which the player is moving forward. Negative numbers will move backwards.
     */
    public static float moveForward;
    public boolean jump;
    public boolean sneak;

    public void updatePlayerMoveState()
    {
        EventPlayerStrafe strafePlayerEvent = new EventPlayerStrafe(moveStrafe, moveForward, jump, sneak);
        Resolute.onEvent(strafePlayerEvent);
        moveStrafe = strafePlayerEvent.moveStrafe();
        moveForward = strafePlayerEvent.moveForward();
        jump = strafePlayerEvent.jump();
        sneak = strafePlayerEvent.sneak();
    }

    public void setForward(float moveForward) {
        this.moveForward = moveForward;
    }

    public double getForward() {
        return moveForward;
    }
    public double getStrafe() {
        return moveStrafe;
    }
}
