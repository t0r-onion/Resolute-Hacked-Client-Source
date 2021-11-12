package vip.Resolute.events.impl;

import vip.Resolute.events.Event;

public class EventMoveFlying extends Event<EventMoveFlying> {
    private float yaw;

    public EventMoveFlying(float yaw) {
        this.yaw = yaw;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
