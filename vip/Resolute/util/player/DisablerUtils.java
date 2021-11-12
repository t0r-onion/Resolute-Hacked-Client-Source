package vip.Resolute.util.player;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class DisablerUtils extends Thread {
    private Packet packet;
    private long delay;

    public DisablerUtils(Packet packet, long delay) {
        this.packet = packet;
        this.delay = delay;
    }

    public static void damage() {
        Minecraft.getMinecraft().getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + 4.1001, Minecraft.getMinecraft().thePlayer.posZ, false));
        Minecraft.getMinecraft().getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, false));
        Minecraft.getMinecraft().getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, true));
        Minecraft.getMinecraft().thePlayer.jump();
    }

    @Override
    public void run() {
        try {
            sleep(this.delay);
            Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
