package vip.Resolute.util.misc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtils {
    public static double randomNumber(double max, double min) {
        return (Math.random() * (max - min)) + min;
    }

    public static double roundToPlace(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static final double toPercentage(final double current, final double max) {
        return (current / max);
    }

    private static Random rng = new Random();

    public static double distance(float x, float y, float x1, float y1) {
        return Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
    }

    public static double clamp(double value, double minimum, double maximum) {
        return value > maximum ? maximum : value < minimum ? minimum : value;
    }

    public static int getRandom(int floor, int cap) {
        return floor + getRNG().nextInt(cap - floor + 1);
    }

    public static Random getRNG() {
        return rng;
    }

    public static double square(double motionX) {
        motionX *= motionX;
        return motionX;
    }

    public static double roundToDecimalPlace(double value, double inc) {
        final double halfOfInc = inc / 2.0D;
        final double floored = Math.floor(value / inc) * inc;
        if (value >= floored + halfOfInc)
            return new BigDecimal(Math.ceil(value / inc) * inc, MathContext.DECIMAL64).
                    stripTrailingZeros()
                    .doubleValue();
        else
            return new BigDecimal(floored, MathContext.DECIMAL64)
                    .stripTrailingZeros()
                    .doubleValue();
    }

    public static double getIncremental(double val, double inc) {
        double one = 1.0D / inc;
        return (double)Math.round(val * one) / one;
    }


    public static double preciseRound(double value, double precision) {
        double scale = Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static double round(double num, double increment) {
        if (increment < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale((int) increment, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float[] constrainAngle(float[] vector) {

        vector[0] = (vector[0] % 360F);
        vector[1] = (vector[1] % 360F);

        while (vector[0] <= -180) {
            vector[0] = (vector[0] + 360);
        }

        while (vector[1] <= -180) {
            vector[1] = (vector[1] + 360);
        }

        while (vector[0] > 180) {
            vector[0] = (vector[0] - 360);
        }

        while (vector[1] > 180) {
            vector[1] = (vector[1] - 360);
        }

        return vector;
    }

    public static double roundWithPrecision(double val, int precision){
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(val * scale) / scale;
    }

    public static float randomFloatValue() {
        return (float) getRandomInRange(0.000000296219, 0.00000913303);
    }

    public static double getRandomInRange(double min, double max) {
        Random random = new Random();
        double range = max - min;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + min;
        return shifted;
    }
}
