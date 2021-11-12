package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.ModeSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.C03PacketPlayer;

public class FastEat extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Redesky", "NCP", "Redesky", "Vanilla");
    public BooleanSetting ground = new BooleanSetting("On Ground", true);

    public FastEat() {
        super("FastEat", 0, "Allows for faster eating", Category.PLAYER);
        this.addSettings(mode, ground);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            EntityPlayerSP player = mc.thePlayer;

            if(mode.is("Redesky")) {
                if(mc.thePlayer.getHeldItem() != null && mc.gameSettings.keyBindUseItem.pressed && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && (!ground.isEnabled() || player.onGround) && player.ticksExisted % 3 == 0) {

                    for (int i = 0; i < 5; ++i) {
                        player.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, true));
                    }

                }
            }

            if(mode.is("Vanilla")) {
                if((this.mc.thePlayer.getHeldItem() != null && (this.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood || this.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && this.mc.thePlayer.isEating()) && (!ground.isEnabled() || player.onGround)) {
                    for (int i = 0; i < 20; ++i) {
                        player.sendQueue.addToSendQueue(new C03PacketPlayer());
                    }
                }
            }

            if(mode.is("NCP")) {
                if (mc.thePlayer.getHeldItem() != null && mc.gameSettings.keyBindUseItem.pressed && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && (!ground.isEnabled() || player.onGround) && player.ticksExisted % 4 == 0) {
                    for (int i = 0; i < 2; ++i) {
                        player.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY + 1.0E-9, player.posZ, player.rotationYaw, player.rotationPitch, true));
                    }
                }
            }
        }
    }
}
