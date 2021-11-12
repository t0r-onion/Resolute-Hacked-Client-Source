package vip.Resolute.util.world;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    private static final Random RANDOM = new Random();
    private static final ThreadLocalRandom r = ThreadLocalRandom.current();

    public static double getRandomInRange(double min, double max) {
        return min + (RANDOM.nextDouble() * (max - min));
    }

    public static final double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static double getDouble(double min, double max) {
        return r.nextDouble(min, max);
    }

    public static double getRandom(double min, double max) {
        double shifted;
        Random random = new Random();
        double range = max - min;
        double scaled = random.nextDouble() * range;
        if (scaled > max) {
            scaled = max;
        }
        if ((shifted = scaled + min) > max) {
            shifted = max;
        }
        return shifted;
    }
}
