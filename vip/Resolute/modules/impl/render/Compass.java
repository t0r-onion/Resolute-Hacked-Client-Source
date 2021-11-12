package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.modules.Module;
import vip.Resolute.util.render.CompassUtil;
import net.minecraft.client.gui.ScaledResolution;

public class Compass extends Module {
    public Compass() {
        super("Compass", 0, "Displays a ingame compass", Category.RENDER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventRender2D) {
            CompassUtil cpass = new CompassUtil(325, 325, 1, 2, true);
            ScaledResolution sc = new ScaledResolution(mc);
            cpass.draw(sc);
        }
    }
}
