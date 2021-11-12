package vip.Resolute.util.render;

import vip.Resolute.util.animation.impl.Opacity;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;
import java.text.NumberFormat;
import java.util.function.Supplier;

public enum ColorManager {
    BLUE(() -> new Color(116, 202, 255)),
    NICE_BLUE(() -> new Color(116, 202, 255)),
    DARK_PURPLE(() -> new Color(133, 46, 215)),
    GREEN(() -> new Color(0, 255, 138)),
    PURPLE(() -> new Color(198, 139, 255)),
    WHITE(() -> Color.WHITE);

    private final Supplier<Color> colorSupplier;

    ColorManager(Supplier<Color> colorSupplier) {
        this.colorSupplier = colorSupplier;
    }

    public static Color fade(final Color color) {
        return fade(color, 2, 100);
    }

    public static Color flash(final Color color) {
        return flash(color, 2, 10);
    }

    public static Color fade(final Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((((System.currentTimeMillis() % 2000) / 1000f + (index / (float) count) * 2F) % 2F) - 1);
        brightness = 0.5f + (0.5f * brightness);
        hsb[2] = brightness % 2F;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color flash(final Color color, int index, int count) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs((((System.currentTimeMillis() % 200) / 500F + (index / (float) count) * 2F) % 2F) - 1);
        brightness = 0.5f + (0.5f * brightness);
        hsb[2] = brightness % 2F;
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static Color rainbow(int index, double speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = Color.HSBtoRGB(hue, 1, 1);
        return new Color(color);
    }

    public static Color rainbow(int index, int speed, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        int color = Color.HSBtoRGB(hue, saturation, brightness);
        Color obj = new Color(color);
        return new Color(obj.getRed(), obj.getGreen(), obj.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static Color astolfo(int index, int speed, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        float hue = angle / 360f;

        int color = Color.HSBtoRGB(brightness, saturation, hue);
        Color obj = new Color(color);
        return new Color(obj.getRed(), obj.getGreen(), obj.getBlue(), Math.max(0, Math.min(255, (int) (opacity * 255))));
    }

    public static int getAstoGay(int delay, float offset) {
        int yStart = 20;
        float speed = 3000f;
        float index = 0.3f;
        float hue = (float) (System.currentTimeMillis() % delay) + (offset);
        while (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 0.5) {
            hue = 0.5F - (hue - 0.5f);
        }
        hue += 0.5F;
        return Color.HSBtoRGB(hue, 0.5F, 1F);
    }

    public static Color getAstoGayColor(int delay, float offset) {
        int yStart = 20;
        float speed = 3000f;
        float index = 0.3f;
        float hue = (float) (System.currentTimeMillis() % delay) + (offset);
        while (hue > speed) {
            hue -= speed;
        }
        hue /= speed;
        if (hue > 0.5) {
            hue = 0.5F - (hue - 0.5f);
        }
        hue += 0.5F;
        return Color.getHSBColor(hue, 0.5F, 1F);
    }

    public static Color getHealthColor(EntityLivingBase entityLivingBase) {
        float health = entityLivingBase.getHealth();
        float[] fractions = new float[]{0.0F, 0.15f, .55F, 0.7f, .9f};
        Color[] colors = new Color[]{new Color(133, 0, 0), Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN};
        float progress = health / entityLivingBase.getMaxHealth();
        return health >= 0.0f ? blendColors(fractions, colors, progress).brighter() : colors[0];
    }

    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        Opacity color = null;
        if (fractions == null) throw new IllegalArgumentException("Fractions can't be null");
        if (colors == null) throw new IllegalArgumentException("Colours can't be null");
        if (fractions.length != colors.length)
            throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
        int[] indicies = getFractionIndicies(fractions, progress);
        float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
        Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};
        float max = range[1] - range[0];
        float value = progress - range[0];
        float weight = value / max;
        return blend(colorRange[0], colorRange[1], 1.0f - weight);
    }

    public static int[] getFractionIndicies(float[] fractions, float progress) {
        int startPoint;
        int[] range = new int[2];
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {
        }
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0f - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        } else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        } else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        } else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color = null;
        try {
            color = new Color(red, green, blue);
        } catch (IllegalArgumentException exp) {
            NumberFormat nf = NumberFormat.getNumberInstance();
            System.out.println(nf.format(red) + "; " + nf.format(green) + "; " + nf.format(blue));
            exp.printStackTrace();
        }
        return color;
    }

    public Color getColor() {
        return colorSupplier.get();
    }
}
