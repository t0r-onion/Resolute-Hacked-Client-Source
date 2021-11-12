package vip.Resolute.util.render;

import net.minecraft.util.MathHelper;

public class Translate {
    private double x, y;
    private long lastMS;

    public Translate(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastMS = System.currentTimeMillis();
    }

    public void interpolate(float targetX, float targetY, int xSpeed, int ySpeed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;
        int deltaX = (int) (Math.abs(targetX - x) * 0.51f);
        int deltaY = (int) (Math.abs(targetY - y) * 0.51f);
        x = calculateCompensation(targetX, (float) x, delta, deltaX);
        y = calculateCompensation(targetY, (float) y, delta, deltaY);
    }

    public static final double slide(double current, double min, double max, double speed, boolean sliding) {
        speed *= System.currentTimeMillis() * .2;
        return MathHelper.clamp_double(sliding ? current < max ? current + (max - current) * speed : current : current > min ? current - (current - min) * speed : current, min, max);
    }

    public static double animate(final double target, double current, double speed) {
        final boolean larger = target > current;
        if (speed < 0.0) {
            speed = 0.0;
        } else if (speed > 1.0) {
            speed = 1.0;
        }
        final double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * speed;
        if (factor < 0.1) {
            factor = 0.1;
        }
        if (larger) {
            current += factor;
        } else {
            current -= factor;
        }
        return current;
    }

    public void interpolate(float targetX, float targetY, double speed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;
        double deltaX = 0;
        double deltaY = 0;
        if (speed != 0) {
            deltaX = (Math.abs(targetX - x) * 0.35f) / (10 / speed);
            deltaY = (Math.abs(targetY - y) * 0.35f) / (10 / speed);
        }
        x = calculateCompensation(targetX, (float) x, delta, deltaX);
        y = calculateCompensation(targetY, (float) y, delta, deltaY);
    }

    public void interpolate(float target, float targetY, double doubleYSpeed, double speed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;

        if (x < target)
            x += speed * delta;

        double deltaX = 0;
        double deltaY = 0;
        if (speed != 0) {
            deltaY = (Math.abs(targetY - y) * 0.35f) / (10 / doubleYSpeed);
        }
        y = calculateCompensation(targetY, (float) y, delta, deltaY);
    }

    public static float calculateCompensation(float target, float current, long delta, double speed) {
        float diff = current - target;
        if (delta < 1) {
            delta = 1;
        }
        if (delta > 1000) {
            delta = 16;
        }
        if (diff > speed) {
            double xD = (speed * delta / (1000 / 60) < 0.5 ? 0.5 : speed * delta / (1000 / 60));
            current -= xD;
            if (current < target) {
                current = target;
            }
        } else if (diff < -speed) {
            double xD = (speed * delta / (1000 / 60) < 0.5 ? 0.5 : speed * delta / (1000 / 60));
            current += xD;
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }

    public void animate(double newX, double newY) {
        x = RenderUtils.transition(x, newX, 0.2D);
        y = RenderUtils.transition(y, newY, 0.2D);
    }

    public void animate2(double newX, double newY) {
        x = RenderUtils.transition(x, newX, 0.3D);
        y = RenderUtils.transition(y, newY, 0.8D);
    }

    public double getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
