package vip.Resolute.util.misc;

public class TimerUtils {
    public long lastMS = System.currentTimeMillis();

    private long time = -1L;

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset)
                reset();


            return true;
        }

        return false;
    }

    public boolean hasReached(final double milliseconds) {
        return this.getCurrentMS() - this.lastMS >= milliseconds;
    }

    private long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

    public final long getElapsedTime() {
        return this.getCurrentMS() - this.time;
    }
}
