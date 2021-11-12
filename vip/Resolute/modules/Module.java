package vip.Resolute.modules;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.settings.Setting;
import vip.Resolute.settings.impl.*;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import vip.Resolute.util.render.Translate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int categoryCount;
    public Category category;
    public String name;
    public String suffix;
    public boolean toggled;
    public boolean hidden;
    public KeybindSetting keyBind = new KeybindSetting("Keybind", 0);
    public static int enabledTicks;
    public String description = "";

    public Translate translate = new Translate(0f, 0f);

    public static boolean expanded;
    public int index;

    private float xanim, yanim;

    public List<Setting> settings = new ArrayList<Setting>();
    public static Module instance;

    public Module(String name, int key, String description, Category c) {
        this.name = name;
        this.description = description;
        this.getKeyBind().setCode(key);
        this.category = c;
        this.addSetting(getKeyBind());
        this.xanim = 0;
        this.yanim = 0;
        setup();
    }


    public void setKey(int key) {
        this.keyBind.setCode(key);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Translate getTranslate() {
        return translate;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDisplayName() {
        return suffix == null || suffix.isEmpty() ? name : name + " \u00A77" + suffix;
    }

    public void onEvent(Event e) {}

    public static Module getInstance() {
        return instance;
    }

    public boolean isEnabled() {
        return isToggled();
    }

    public boolean isToggled() {
        return toggled;
    }

    public void toggle() {
        toggled = !toggled;
        if (toggled) {
            this.enabledTicks = 0;
            onEnable();
            Resolute.getNotificationManager().add(new Notification("Enabled", getName(), 1500L, NotificationType.INFO));
        } else {
            onDisable();
            Resolute.getNotificationManager().add(new Notification("Disabled", getName(), 1500L, NotificationType.INFO));
        }
    }

    public void setState(boolean state) {
        if (this.toggled != state) {
            this.toggled = state;
            if (state) {
                this.onEnable();
                Resolute.getNotificationManager().add(new Notification("Enabled", getName(), 1500L, NotificationType.INFO));
            } else {
                this.onDisable();
                Resolute.getNotificationManager().add(new Notification("Disabled", getName(), 1500L, NotificationType.INFO));
            }
        }
    }

    public JsonObject save() {
        final JsonObject object = new JsonObject();
        object.addProperty("toggled", Boolean.valueOf(this.isEnabled()));
        object.addProperty("key", (Number)this.getKey());
        object.addProperty("hidden", Boolean.valueOf(this.isHidden()));
        final List<Setting> properties = this.getSettings();
        if (!properties.isEmpty()) {
            final JsonObject propertiesObject = new JsonObject();
            for (final Setting<?> property : properties) {
                if (property instanceof NumberSetting) {
                    propertiesObject.addProperty(property.name,((NumberSetting)property).getValue());
                }
                else if (property instanceof ModeSetting) {
                    final ModeSetting enumProperty = (ModeSetting)property;
                    propertiesObject.add(property.name, (JsonElement)new JsonPrimitive(enumProperty.getMode()));
                }
                else if (property instanceof BooleanSetting) {
                    propertiesObject.addProperty(property.name, ((BooleanSetting) property).isEnabled());
                }
                else if (property instanceof ColorSetting) {
                    final ColorSetting colorSetting = (ColorSetting)property;
                    propertiesObject.addProperty(property.name, colorSetting.getColor());
                }
            }
            object.add("Properties", (JsonElement)propertiesObject);
        }
        return object;
    }

    public void load(final JsonObject object) {
        if (object.has("toggled")) {
            this.setState(object.get("toggled").getAsBoolean());
        }
        if (object.has("key")) {
            this.setKey(object.get("key").getAsInt());
        }
        if (object.has("hidden")) {
            this.setHidden(object.get("hidden").getAsBoolean());
        }
        if (object.has("Properties") && !this.getSettings().isEmpty()) {
            final JsonObject propertiesObject = object.getAsJsonObject("Properties");
            for (final Setting<?> property : this.getSettings()) {
                if (propertiesObject.has(property.name)) {
                    if (property instanceof NumberSetting) {
                        ((NumberSetting)property).setValue(propertiesObject.get(property.name).getAsDouble());
                    }
                    else if (property instanceof ModeSetting) {
                        this.findEnumValue(property, propertiesObject);
                    }
                    else if (property instanceof BooleanSetting) {
                        ((BooleanSetting) property).setEnabled(propertiesObject.get(property.name).getAsBoolean());
                    }
                    else if (property instanceof ColorSetting) {
                        ((ColorSetting) property).setValue(propertiesObject.get(property.name).getAsInt());
                    }
                }
            }
        }
    }

    private <T extends Enum<T>> void findEnumValue(final Setting<?> property, final JsonObject propertiesObject) {
        final ModeSetting enumProperty = (ModeSetting) property;
        final String value = propertiesObject.getAsJsonPrimitive(property.name).getAsString();
        List<String> values;
        for (int length = (values = enumProperty.getModes()).size(), i = 0; i < length; ++i) {
            final String possibleValue = values.get(i);
            if (possibleValue.equalsIgnoreCase(value)) {
                enumProperty.setSelected(possibleValue);
                break;
            }
        }
    }

    public void setEnabled() {
        toggled = true;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setToggled(boolean toggled) {
        this.enabledTicks = 0;
        this.toggled = toggled;
    }

    public void addSettings(Setting... settings) {
        for (Setting setting : settings) {
            addSetting(setting);
        }
    }

    public void addSetting(Setting setting) {
        getSettings().add(setting);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public void setSettings(List<Setting> settings) {
        this.settings = settings;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getName() {
        return name;
    }

    public void setup() {
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public float getXAnim() {
        return xanim;
    }

    public void setXAnim(float anim) {
        this.xanim = anim;
    }

    public float getYAnim() {
        return yanim;
    }

    public void setYAnim(float anim) {
        this.yanim = anim;
    }

    public KeybindSetting getKeyBind() {
        return keyBind;
    }

    public int getKey() {
        return getKeyBind().getCode();
    }

    public Category getCategory() {
        return category;
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        RENDER("Render"),
        EXPLOIT("Exploit");

        public float offset;
        public static Category instance;

        public String name;
        public int moduleIndex;
        public int posX, posY;
        public boolean expanded;

        Category(String name) {
            this.name = name;
            posX = 150 + (categoryCount * 95);
            posY = 85;
            offset = 0;
            expanded = true;
            categoryCount++;
        }
    }
}
