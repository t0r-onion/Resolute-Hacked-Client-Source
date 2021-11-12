package vip.Resolute.modules.impl.player;

import vip.Resolute.auth.Authentication;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRenderNametag;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;

public class StreamerMode extends Module {
    public BooleanSetting hideNames = new BooleanSetting("Hide Names", true);
    public static BooleanSetting hideScoreboard = new BooleanSetting("Hide Scoreboard", true);

    public static String name = Authentication.username;
    public static boolean enabled = false;

    public StreamerMode() {
        super("StreamerMode", 0, "Protects your name", Category.PLAYER);
        this.addSettings(hideNames);
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }

    public void onEvent(Event e) {
        if(e instanceof EventRenderNametag) {
            if(hideNames.isEnabled()) {
                e.setCancelled(true);
            }
        }
    }
}
