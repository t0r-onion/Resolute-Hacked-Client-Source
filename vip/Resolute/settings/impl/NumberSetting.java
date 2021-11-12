package vip.Resolute.settings.impl;

import vip.Resolute.settings.Setting;

import java.util.function.Supplier;

public class NumberSetting extends Setting<Double> {

    public double value, minimum, maximum, increment;

    public NumberSetting(String name, double value, final Supplier<Boolean> dependency, double minimum, double maximum, double increment) {
        super(name, value, dependency);
        this.name = name;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    public NumberSetting(String name, double value, double minimum, double maximum, double increment) {
        this(name, value, () -> true, minimum, maximum, increment);
        this.name = name;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

    /*
    public NumberSetting(String name, ModeSetting parentMode, String parentValue, double value, double minimum, double maximum, double increment) {
        this.name = name;
        this.parentMode = parentMode;
        this.parentValue = parentValue;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.increment = increment;
    }

     */

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        double precision = 1 / increment;
        this.value = Math.round(Math.max(minimum, Math.min(maximum, value)) * precision) / precision;
    }


    public void increment(boolean positive) {
        setValue(getValue() + (positive ? 1 : -1) * increment);
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }
}

