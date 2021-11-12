package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class NoRotate extends Module {

    public NoRotate() {
        super("NoRotate", 0, "Cancels server side rotate packets", Category.PLAYER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventPacket) {
            if(mc.thePlayer == null || mc.theWorld == null)
                return;

            if(((EventPacket) e).getPacket() instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook packetPlayerPosLook = (S08PacketPlayerPosLook) ((EventPacket) e).getPacket();
                packetPlayerPosLook.setYaw(mc.thePlayer.rotationYaw);
                packetPlayerPosLook.setPitch(mc.thePlayer.rotationPitch);
            }
        }
    }
}

