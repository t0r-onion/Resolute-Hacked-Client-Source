package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Regen extends Module {
    private NumberSetting health = new NumberSetting("Health", 5, 1, 20, 1);
    private NumberSetting packets = new NumberSetting("Packets", 100, 5, 500, 5);

    public Regen() {
        super("Regen", 0, "Regenerates your health", Category.PLAYER);
        this.addSettings(health, packets);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            if (mc.thePlayer.getHealth() < health.getValue()) {
                for (int i = 0; i < packets.getValue(); i++) {
                    mc.thePlayer.sendQueue.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround));
                }
            }
        }
    }
}
