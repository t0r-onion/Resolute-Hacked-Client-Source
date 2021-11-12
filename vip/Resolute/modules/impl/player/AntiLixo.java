package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import net.minecraft.network.play.server.S02PacketChat;

public class AntiLixo extends Module {
    public AntiLixo() {
        super("AntiLixo", 0, "Automatically responds to Lixo", Category.PLAYER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof S02PacketChat) {
                S02PacketChat packetChat = (S02PacketChat) ((EventPacket) e).getPacket();

                // || packetChat.getChatComponent().getUnformattedText().replaceAll("", "").contains("hack")
                if(packetChat.getChatComponent().getUnformattedText().replaceAll("", "").contains("lixo")) {
                    mc.thePlayer.sendChatMessage("hoes mad just get good");
                }
            }
        }
    }
}
