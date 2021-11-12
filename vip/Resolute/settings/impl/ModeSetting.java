package vip.Resolute.settings.impl;

import vip.Resolute.settings.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {

    private String selected;

    public int index;
    public List<String> modes;

    public ModeSetting(String name, String defaultMode, final Supplier<Boolean> dependency, String... modes) {
        super(name, defaultMode, dependency);
        this.name = name;
        this.modes = Arrays.asList(modes);
        index = this.modes.indexOf(defaultMode);
        this.selected = this.modes.get(index);
    }

    public ModeSetting(String name, String defaultMode, String... modes) {
        this(name, defaultMode, () -> true, modes);
        this.name = name;
        this.modes = Arrays.asList(modes);
        index = this.modes.indexOf(defaultMode);
        this.selected = this.modes.get(index);

    }

    /*
    public ModeSetting(String name, ModeSetting parentMode, String parentValue, String defaultMode, String... modes) {
        this.name = name;
        this.parentMode = parentMode;
        this.parentValue = parentValue;
        this.modes = Arrays.asList(modes);
        index = this.modes.indexOf(defaultMode);
        this.selected = this.modes.get(index);
    }

     */

    public List<String> getModes() {
        return this.modes;
    }

    public String getMode() {
        return modes.get(index);
    }

    public boolean is(String mode) {
        return index == modes.indexOf(mode);
    }

    public void cycle() {
        if(index < modes.size() - 1) {
            index++;
            selected = modes.get(index);
        }
        else
            index = 0;
        selected = modes.get(0);
    }

    public String getSelected() {
        return this.selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
        index = modes.indexOf(selected);
    }
}