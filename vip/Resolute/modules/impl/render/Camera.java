package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;

public class Camera extends Module {
    public static boolean noHurtEnabled;
    public static boolean viewclipEnabled;

    public BooleanSetting nohurt = new BooleanSetting("NoHurtCamera", true);
    public BooleanSetting viewclip = new BooleanSetting("View Clip", false);

    public Camera() {
        super("Camera", 0, "Adjusts player camera", Category.RENDER);
        this.addSettings(nohurt, viewclip);
    }

    public void onDisable() {
        noHurtEnabled = false;
        viewclipEnabled = false;
    }

    public void onEvent(Event e) {
        if(e instanceof EventUpdate) {
            if(nohurt.isEnabled())
                noHurtEnabled = true;
            else
                noHurtEnabled = false;

            if(viewclip.isEnabled())
                viewclipEnabled = true;
            else
                viewclipEnabled = false;
        }
    }
}
