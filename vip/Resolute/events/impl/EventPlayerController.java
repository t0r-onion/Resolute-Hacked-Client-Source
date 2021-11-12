package vip.Resolute.events.impl;

import vip.Resolute.events.Event;

public class EventPlayerController extends Event<EventPlayerController> {
    private float yaw;
    private double motion;
    private boolean sprinting;

    public EventPlayerController(float yaw, double motion, boolean sprinting) {
        this.yaw = yaw;
        this.motion = motion;
        this.sprinting = sprinting;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public double getMotion() {
        return motion;
    }

    public void setMotion(double motion) {
        this.motion = motion;
    }

    public boolean isSprinting() {
        return sprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }
}
