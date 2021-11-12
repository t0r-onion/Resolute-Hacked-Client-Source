package vip.Resolute.settings.impl;

import vip.Resolute.settings.Setting;
import org.lwjgl.input.Keyboard;

import java.util.function.Supplier;

public class KeybindSetting extends Setting<Integer> {

    private int code;

    public KeybindSetting(String name, int code, final Supplier<Boolean> dependency) {
        super(name, code, dependency);
        this.name = name;
        this.code = code;
    }

    public KeybindSetting(String name, int code) {
        this(name, code, () -> true);
        this.name = name;
        this.code = code;
    }

    public int getCode() {
        return code == -1 ? Keyboard.KEY_NONE : code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
