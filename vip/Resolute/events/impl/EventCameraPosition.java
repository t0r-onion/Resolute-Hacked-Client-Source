package vip.Resolute.events.impl;

import vip.Resolute.events.Event;

public class EventCameraPosition extends Event<EventCameraPosition> {
    public double y;

    public EventCameraPosition(double y) {
        this.y = y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return this.y;
    }
}
