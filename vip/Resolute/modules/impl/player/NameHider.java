package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.util.ChatComponentText;

public class NameHider extends Module {
    public NameHider() {
        super("NameHider", 0, "Hides you ingame name", Category.PLAYER);
    }

    public void onEvent(Event e) {
        if (e instanceof EventPacket && e.isPre()) {

            EventPacket packetEvent = (EventPacket) e;
            if (packetEvent.packet instanceof S02PacketChat) {

                S02PacketChat packet = (S02PacketChat) packetEvent.packet;

                if (packet.getChatComponent().getUnformattedText().replaceAll("", "").contains(mc.getSession().getUsername())) {
                    packet.chatComponent = new ChatComponentText(packet.getChatComponent().getFormattedText().replaceAll("", "").replaceAll(mc.getSession().getUsername(), "You"));
                }

            }
            else if (packetEvent.packet instanceof S3CPacketUpdateScore) {
                S3CPacketUpdateScore packet = (S3CPacketUpdateScore) packetEvent.packet;

                if (packet.getObjectiveName().replaceAll("", "").contains(mc.getSession().getUsername())){
                    packet.setObjective(packet.getObjectiveName().replaceAll("", "").replaceAll(mc.getSession().getUsername(), "You"));
                }

                if (packet.getPlayerName().replaceAll("", "").contains(mc.getSession().getUsername())){
                    packet.setName(packet.getPlayerName().replaceAll("", "").replaceAll(mc.getSession().getUsername(), "You"));
                }
            }
        }
    }
}
