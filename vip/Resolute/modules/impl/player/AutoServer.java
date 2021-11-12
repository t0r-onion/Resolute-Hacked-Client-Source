package vip.Resolute.modules.impl.player;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.ui.notification.Notification;
import vip.Resolute.ui.notification.NotificationType;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;

public class AutoServer extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Hypixel", "Hypixel");
    public ModeSetting playMode = new ModeSetting("Play Mode", "Solo Insane", "Solo Insane", "Teams Insane");
    public NumberSetting delay = new NumberSetting("Delay", 1300, 100, 5000, 10);

    public AutoServer() {
        super("AutoServer", 0, "Automatically plays", Category.PLAYER);
        this.addSettings(mode, playMode, delay);
    }

    public void onEvent(Event e) {
        this.setSuffix("");
        if(e instanceof EventPacket) {
            if (this.mc.theWorld != null && this.mc.thePlayer != null) {
                if (((EventPacket) e).getPacket() instanceof S02PacketChat) {
                    S02PacketChat packet = ((EventPacket) e).getPacket();

                    if(!packet.getChatComponent().getUnformattedText().isEmpty()) {
                        String message = packet.getChatComponent().getUnformattedText();
                        if (message.contains("You won! Want to play again?") || message.contains("You died! Want to play again?") && onHypixel()) {
                            Thread thread = new Thread() {
                                public void run() {
                                    try {
                                        Resolute.getNotificationManager().add(new Notification("Auto Play", "Sending you to a new game...    " , (long) delay.getValue(), NotificationType.SUCCESS));
                                        Thread.sleep((long) delay.getValue());
                                    } catch (Exception var2) {
                                        var2.printStackTrace();
                                    }

                                    if(playMode.is("Solo Insane")) {
                                        mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("/play solo_insane"));
                                    }
                                    if(playMode.is("Teams Insane")) {
                                        mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("/play teams_insane"));
                                    }
                                }
                            };
                            thread.start();
                        }
                    }
                }
            }
        }
    }

    public static boolean onHypixel() {
        ServerData serverData = mc.getCurrentServerData();
        if (serverData == null) {
            return false;
        } else {
            return serverData.serverIP.endsWith("hypixel.net") || serverData.serverIP.endsWith("hypixel.net:25565") || serverData.serverIP.equals("104.17.71.15") || serverData.serverIP.equals("104.17.71.15:25565");
        }
    }
}
