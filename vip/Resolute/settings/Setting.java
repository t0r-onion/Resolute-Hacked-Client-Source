package vip.Resolute.settings;

import vip.Resolute.settings.impl.ModeSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Setting<T> {
    public String name;

    public String parentValue;
    public ModeSetting parentMode;
    public boolean hidden;

    protected final Supplier<Boolean> dependency;
    private final List<ValueChangeListener<T>> valueChangeListeners;
    protected T value;

    public Setting(final String label, final T value, final Supplier<Boolean> dependency) {
        this.valueChangeListeners = new ArrayList();
        this.name = label;
        this.value = value;
        this.dependency = dependency;
    }

    public boolean isAvailable() {
        return this.dependency.get();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public ModeSetting getParentMode() {
        return parentMode;
    }
}
