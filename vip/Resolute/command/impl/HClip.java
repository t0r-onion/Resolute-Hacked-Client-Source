package vip.Resolute.command.impl;

import vip.Resolute.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class HClip extends Command {

    public Minecraft mc = Minecraft.getMinecraft();

    public HClip() {
        super("HClip", "HClips through blocks", ".hclip <value>", "hclip");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            float distance = Float.parseFloat(args[0]);
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            double x = -Math.sin(yaw) * distance;
            double z = Math.cos(yaw) * distance;
            Minecraft.getMinecraft().getNetHandler().addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + x, mc.thePlayer.posY , mc.thePlayer.posZ + z, true));
            mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z);
        }
    }
}
