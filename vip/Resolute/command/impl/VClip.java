package vip.Resolute.command.impl;

import vip.Resolute.command.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VClip extends Command {

    public Minecraft mc = Minecraft.getMinecraft();

    public VClip() {
        super("VClip", "VClips through blocks", ".vclip <value>", "vclip");
    }

    @Override
    public void onCommand(String[] args, String command) {
        if(args.length > 0) {
            float distance = Float.parseFloat(args[0]);
            Minecraft.getMinecraft();
            Minecraft.getMinecraft();
            Minecraft.getMinecraft();
            Minecraft.getMinecraft().getNetHandler().addToSendQueue((Packet)new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + distance, mc.thePlayer.posZ, false));
            Minecraft.getMinecraft();
            Minecraft.getMinecraft();
            Minecraft.getMinecraft();
            Minecraft.getMinecraft();
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + distance, mc.thePlayer.posZ);
        }
    }
}
