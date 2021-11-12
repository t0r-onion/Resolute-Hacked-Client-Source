package vip.Resolute.settings.impl;

import vip.Resolute.settings.Setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {

    public boolean enabled;

    public BooleanSetting(String name, boolean enabled, final Supplier<Boolean> dependancy) {
        super(name, enabled, dependancy);
        this.name = name;
        this.enabled = enabled;
    }

    public BooleanSetting(String name, boolean enabled) {
        this(name, enabled, () -> true);
    }

    /*
    public BooleanSetting(String name, ModeSetting parentMode, String parentValue, boolean enabled) {
        this.name = name;
        this.parentMode = parentMode;
        this.parentValue = parentValue;
        this.enabled = enabled;
    }

     */

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }
}

