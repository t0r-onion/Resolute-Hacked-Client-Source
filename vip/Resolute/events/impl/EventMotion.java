package vip.Resolute.events.impl;

import vip.Resolute.events.Event;

public class EventMotion extends Event<EventMotion> {
    public boolean onGround;
    public double x, y, z;
    public float yaw, pitch;
    private boolean rotating;
    private float prevYaw;
    private float prevPitch;
    private final double prevPosX;
    private final double prevPosY;
    private final double prevPosZ;

    public EventMotion(float prevYaw, float prevPitch, double x, double y, double z, final double prevPosX, final double prevPosY, final double prevPosZ, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevPosX = prevPosX;
        this.prevPosY = prevPosY;
        this.prevPosZ = prevPosZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.prevPitch = prevPitch;
        this.prevYaw = prevYaw;
    }

    public boolean isRotating() {
        return rotating;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        this.rotating = true;
    }

    public double getPrevPosX() {
        return this.prevPosX;
    }

    public double getPrevPosY() {
        return this.prevPosY;
    }

    public double getPrevPosZ() {
        return this.prevPosZ;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.rotating = true;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
}
