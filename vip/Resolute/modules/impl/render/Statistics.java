package vip.Resolute.modules.impl.render;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventRender2D;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.font.FontUtil;
import vip.Resolute.util.font.MinecraftFontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S45PacketTitle;

public class Statistics extends Module {
    public NumberSetting yadd = new NumberSetting("Y Pos", -60, -60, 180, 1);

    MinecraftFontRenderer fontRenderer = FontUtil.moon;

    int wins = 0;
    int lost = 0;
    public static int kills = 0;
    int flags = 0;


    public Statistics() {
        super("Statistics", 0, "Displays ingame statistics", Category.RENDER);
    }

    public void onEvent(Event e) {
        if(e instanceof EventPacket) {
            EventPacket eventPacket = (EventPacket) e;
            Packet<?> packet = eventPacket.getPacket();
            if(((EventPacket) e).getPacket() instanceof S08PacketPlayerPosLook) {
                flags++;
            }

            if(this.mc.thePlayer != null && this.mc.thePlayer.ticksExisted >= 0 && packet instanceof S45PacketTitle) {
                if (((S45PacketTitle) packet).getMessage() == null)
                    return;

                String message = ((S45PacketTitle) packet).getMessage().getUnformattedText();

                if(message.equals("VICTORY!")) {
                    wins++;
                }

                if(message.equals("YOU DIED!") || message.equals("GAME END") || message.equals("You are now a spectator!")) {
                    lost++;
                }
            }

            if(this.mc.thePlayer != null && this.mc.thePlayer.ticksExisted >= 0 && packet instanceof S02PacketChat) {
                final String look = "killed by " + mc.thePlayer.getName();
                final String look2 = "slain by " + mc.thePlayer.getName();
                final String look3 = "void while escaping " + mc.thePlayer.getName();
                final String look4 = "was killed with magic while fighting " + mc.thePlayer.getName();
                final String look5 = "couldn't fly while escaping " + mc.thePlayer.getName();
                final String look6 = "fell to their death while escaping " + mc.thePlayer.getName();
                final String look7 = "foi morto por " + mc.thePlayer.getName();
                final String look8 = "fue asesinado por " + mc.thePlayer.getName();
                final String look9 = "fue destrozado a manos de " + mc.thePlayer.getName();
                final S02PacketChat s02PacketChat = (S02PacketChat) packet;
                final String cp21 = s02PacketChat.getChatComponent().getUnformattedText();

                if (cp21.contains(look) || cp21.contains(look2) || cp21.contains(look3) || cp21.contains(look4) || cp21.contains(look5) || cp21.contains(look6) || cp21.contains(look7) || cp21.contains(look8) || cp21.contains(look9)) {
                    kills++;
                }

                if((cp21.contains(mc.thePlayer.getName() + "killed by ") && cp21.contains("elimination")) || (cp21.contains(mc.thePlayer.getName() + " morreu sozinho")) || (cp21.contains(mc.thePlayer.getName() + " foi morto por"))) {
                    lost++;
                }

                if(cp21.contains(mc.thePlayer.getName() + " venceu a partida!")) {
                    wins++;
                }
            }
        }

        if(e instanceof EventRender2D) {
            ScaledResolution sr = new ScaledResolution(mc);

            Gui.drawRect(sr.getScaledWidth() / 20 - 40, sr.getScaledHeight() / 5 + this.yadd.getValue(), sr.getScaledWidth() / 8 + 30, sr.getScaledHeight() / 2.5 + this.yadd.getValue() - 29, 0x90000000);

            Gui.drawRect(sr.getScaledWidth() / 20 - 40, sr.getScaledHeight() / 2 + this.yadd.getValue() - 140, sr.getScaledWidth() / 8 + 30, sr.getScaledHeight() / 2 + this.yadd.getValue() - 138.5f, 0x906C6C6C);

            Gui.drawRect(sr.getScaledWidth() / 20 - 40, sr.getScaledHeight() / 5 + this.yadd.getValue() - 0, sr.getScaledWidth() / 8 + 30, sr.getScaledHeight() / 5 + this.yadd.getValue() - 1, 0xFF00EEFF);

            FontUtil.moon.drawString("Session Information", (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 1 + this.yadd.getValue()), -1);
            FontUtil.moon.drawString("Session Time: " + formatTime(Resolute.sessionTime.elapsed()), (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 17 + this.yadd.getValue()), -1);
            FontUtil.moon.drawString("Bad Config: " + "True", (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 28 + this.yadd.getValue()), -1);
            //FontUtil.moon.drawString("Flags: " + flags, (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 28 + this.yadd.getValue()), -1);
            FontUtil.moon.drawString("Kills: " + kills, (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 39 + this.yadd.getValue()), -1);
            FontUtil.moon.drawString("Won: " + wins, (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 50 + this.yadd.getValue()), -1);
            FontUtil.moon.drawString("Lost: " + lost, (float)sr.getScaledWidth() / 16 - 50, (float) ((float) sr.getScaledHeight() / 5 + 61 + this.yadd.getValue()), -1);
        }
    }

    private String formatTime(long time) {
        time /= 1000;
        return String.format("%d:%02d", time / 60, time % 60);
    }
}
