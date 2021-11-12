package vip.Resolute.util.render;

import java.awt.Color;
import net.minecraft.util.MathHelper;

public class ColorUtils {
    public static final int WHITE = Color.WHITE.getRGB();
    public static final int RED = new Color(0xf44336).getRGB();
    public static final int PINK = new Color(0xff80ab).getRGB();
    public static final int PURPLE = new Color(0xba68c8).getRGB();
    public static final int DEEP_PURPLE = new Color(0x7E5EB5).getRGB();
    public static final int INDIGO = new Color(0x7986cb).getRGB();
    public static final int BLUE = new Color(0x1976d2).getRGB();
    public static final int LIGHT_BLUE = new Color(0x74C3FF).getRGB();
    public static final int CYAN = new Color(0x00ACC1).getRGB();
    public static final int TEAL = new Color(0xA7FFEB).getRGB();
    public static final int GREEN = new Color(0x00FF46).getRGB();

    public static java.awt.Color rainbow(float speed, float off) {

        double time = (double) System.currentTimeMillis() / speed;
        time += off;
        time %= 255.0f;
        return java.awt.Color.getHSBColor((float) (time / 255.0f), 1.0f, 1.0f);

    }



    public static Color getHealthColor(final float health, final float maxHealth) {
        final float[] fractions = { 0.0f, 0.5f, 1.0f };
        final Color[] colors = { new Color(108, 20, 20), new Color(255, 0, 60), Color.GREEN };
        final float progress = health / maxHealth;
        return blendColors(fractions, colors, progress).brighter();
    }

    public static Color blendColors(final float[] fractions, final Color[] colors, final float progress) {
        if (fractions.length == colors.length) {
            final int[] indices = getFractionIndices(fractions, progress);
            final float[] range = { fractions[indices[0]], fractions[indices[1]] };
            final Color[] colorRange = { colors[indices[0]], colors[indices[1]] };
            final float max = range[1] - range[0];
            final float value = progress - range[0];
            final float weight = value / max;
            final Color color = blend(colorRange[0], colorRange[1], 1.0f - weight);
            return color;
        }
        throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
    }

    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float)ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = color1.getColorComponents(new float[3]);
        final float[] rgb2 = color2.getColorComponents(new float[3]);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        }
        else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        }
        else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        }
        else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color3 = null;
        try {
            color3 = new Color(red, green, blue);
        }
        catch (IllegalArgumentException ex) {}
        return color3;
    }

    public static int[] getFractionIndices(final float[] fractions, final float progress) {
        final int[] range = new int[2];
        int startPoint;
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {}
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    public static int astolfoColors(int yOffset, int yTotal) {
        float speed = 2900F;
        float hue = (float) (System.currentTimeMillis() % (int)speed) + ((yTotal - yOffset) * 9);
        while (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 0.5) {
            hue = 0.5F - (hue - 0.5f);
        }
        hue += 0.5F;
        return Color.HSBtoRGB(hue, 0.5f, 1F);
    }

    public static int moonColors(int yOffset, int yTotal) {
        float speed = 2900F;
        float hue = (float) (MathHelper.sin(System.currentTimeMillis()) % (int)speed) + ((yTotal - yOffset) * 9);

        while (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 1) {
            hue = 1F - (hue - 1f);
        }
        hue += 1F;
        return Color.HSBtoRGB(hue, 0.5f, 1F);
    }

    public static int getRainbow(float seconds, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % (int)(seconds * 1000)) / (float)(seconds * 1000);
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return color;
    }

    public static int getRainbow(float seconds, float saturation, float brightness, long index) {
        float hue = ((System.currentTimeMillis() + index) % (int)(seconds * 1000)) / (float)(seconds * 1000);
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        return color;
    }
    public static Color colorLerpv2(Color start, Color end, float ratio) {
        ratio = Math.min(Math.max(ratio, 0.0f), 1.0f);

        int red = (int)Math.abs((ratio * start.getRed()) + ((1 - ratio) * end.getRed()));
        int green = (int)Math.abs((ratio * start.getGreen()) + ((1 - ratio) * end.getGreen()));
        int blue = (int)Math.abs((ratio * start.getBlue()) + ((1 - ratio) * end.getBlue()));

        return new Color(red, green, blue);
    }


    public static int rainbow(int delay) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360.0f), 0.8f, 0.7f).getRGB();
    }

    public static Color pulseBrightness(Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float)(System.currentTimeMillis() % 2000L) / 1000.0F + (float)index / (float)count * 2.0F) % 2.0F - 1.0F);
        brightness = 0.5F + 0.5F * brightness;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness % 2.0F));
    }

    public static int getColor(Color color) {
        return getColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int getColor(int brightness) {
        return getColor(brightness, brightness, brightness, 255);
    }

    public static int getColor(int brightness, int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(int red, int green, int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(int red, int green, int blue, int alpha) {
        int color = MathHelper.clamp_int(alpha, 0, 255) << 24;
        color |= MathHelper.clamp_int(red, 0, 255) << 16;
        color |= MathHelper.clamp_int(green, 0, 255) << 8;
        color |= MathHelper.clamp_int(blue, 0, 255);
        return color;
    }
}
