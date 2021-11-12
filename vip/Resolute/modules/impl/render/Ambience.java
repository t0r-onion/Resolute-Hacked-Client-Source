package vip.Resolute.modules.impl.render;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

public class Ambience extends Module {

    public NumberSetting time = new NumberSetting("Time", 10, 1 , 20, 1);

    public Ambience() {
        super("Ambience", 0, "Changes world time", Category.RENDER);
        this.addSettings(time);
    }

    public void onEvent(Event e) {
        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof S03PacketTimeUpdate) {
                //e.setCancelled(true);
                ((S03PacketTimeUpdate) ((EventPacket) e).getPacket()).setWorldTime((long) (time.getValue() * 1000));
            }
        }

        if(e instanceof EventUpdate) {
            if(mc.thePlayer == null) return;

            mc.theWorld.setWorldTime((long) time.getValue() * 1000);
        }
    }
}
