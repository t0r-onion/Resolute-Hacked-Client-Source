package vip.Resolute.util.render;

public class TranslationUtils {
    private float x;
    private float y;
    private long lastMS;

    public TranslationUtils(float x, float y) {
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
        x = RenderUtils.calculateCompensation(targetX, x, delta, deltaX);
        y = RenderUtils.calculateCompensation(targetY, y, delta, deltaY);
    }

    public static float moveUD(float current, float end, float smoothSpeed, float minSpeed) {
        float movement = (end - current) * smoothSpeed;
        if (movement > 0.0f) {
            movement = Math.max((float) minSpeed, (float) movement);
            movement = Math.min((float) (end - current), (float) movement);
        } else if (movement < 0.0f) {
            movement = Math.min((float) (-minSpeed), (float) movement);
            movement = Math.max((float) (end - current), (float) movement);
        }
        return current + movement;
    }

    public void interpolate(float targetX, float targetY, double speed) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - lastMS;//16.66666
        lastMS = currentMS;
        double deltaX = 0;
        double deltaY = 0;
        if(speed != 0){
            deltaX = (Math.abs(targetX - x) * 0.35f)/(10/speed);
            deltaY = (Math.abs(targetY - y) * 0.35f)/(10/speed);
        }
        x = RenderUtils.calculateCompensation(targetX, x, delta, deltaX);
        y = RenderUtils.calculateCompensation(targetY, y, delta, deltaY);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

}
