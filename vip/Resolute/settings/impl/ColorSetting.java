package vip.Resolute.settings.impl;

import vip.Resolute.settings.Setting;

import java.awt.*;
import java.util.function.Supplier;

public class ColorSetting extends Setting<Color> {

    private float hue;
    private float saturation;
    private float brightness;

    public ColorSetting(String name, Color defaultColor, final Supplier<Boolean> dependency) {
        super(name, defaultColor, dependency);
        this.name = name;
        this.setColor(defaultColor);
    }

    public ColorSetting(String name, Color defaultColor) {
        super(name, defaultColor, () -> true);
        this.name = name;
        this.setColor(defaultColor);
    }

    /*
    public ColorSetting(String name, ModeSetting parentMode, String parentValue, Color defaultColor) {
        this.name = name;
        this.parentMode = parentMode;
        this.parentValue = parentValue;
        this.setColor(defaultColor);
    }

     */

    public Color getValue() {
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public int getColor() {
        return this.getValue().getRGB();
    }

    public float getSaturation() {
        return this.saturation;
    }

    public float getBrightness() {
        return this.brightness;
    }

    public void setHue(float hue) {
        this.hue = hue;
    }

    public float getHue() {
        return this.hue;
    }

    public void setColor(Color color) {
        float[] colors = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = colors[0];
        this.saturation = colors[1];
        this.brightness = colors[2];
    }

    public void setValue(int hex) {
        final float[] hsb = this.getHSBFromColor(hex);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    private float[] getHSBFromColor(final int hex) {
        final int r = hex >> 16 & 0xFF;
        final int g = hex >> 8 & 0xFF;
        final int b = hex & 0xFF;
        return Color.RGBtoHSB(r, g, b, null);
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }
}
