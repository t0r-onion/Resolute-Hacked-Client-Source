package vip.Resolute.events.impl;

import vip.Resolute.events.Event;

public class Event2D extends Event<Event2D> {
    double width, height;
    public Event2D(double width, double height){
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
