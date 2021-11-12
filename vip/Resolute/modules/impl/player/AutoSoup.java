package vip.Resolute.modules.impl.player;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.NumberSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemSoup;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class AutoSoup extends Module {

    public NumberSetting health = new NumberSetting("Health", 7.0D, 2.0D, 9.5D, 0.5D);

    public AutoSoup() {
        super("AutoSoup", 0, "", Category.PLAYER);
        this.addSettings(health);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            final EntityPlayerSP player = mc.thePlayer;
            player.rotationPitch += (float)1.0E-4;
            if (player.getHealth() != player.getMaxHealth() && player.getHealth() < health.getValue() * 2 && doesNextSlotHaveSoup() && player.hurtTime >= 9) {
                player.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYawHead, 90.0f, player.onGround));
                player.sendQueue.addToSendQueue(new C09PacketHeldItemChange(getSlotWithSoup()));
                mc.playerController.sendUseItem(player, mc.theWorld, player.inventory.getStackInSlot(getSlotWithSoup()));new BlockPos(0, 0, 0);
                player.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ALL_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
                player.sendQueue.addToSendQueue(new C09PacketHeldItemChange(player.inventory.currentItem));
                mc.playerController.onStoppedUsingItem(player);
                Resolute.addChatMessage("Consumed Soup");
            }
        }
    }

    public boolean doesNextSlotHaveSoup() {
        final EntityPlayerSP player = mc.thePlayer;
        for (int i = 0; i < 9; ++i) {
            if (player.inventory.getStackInSlot(i) != null && player.inventory.getStackInSlot(i).getItem() instanceof ItemSoup) {
                return true;
            }
        }
        return false;
    }

    public int getSlotWithSoup() {
        final EntityPlayerSP player = mc.thePlayer;
        for (int i = 0; i < 9; ++i) {
            if (player.inventory.getStackInSlot(i) != null && player.inventory.getStackInSlot(i).getItem() instanceof ItemSoup) {
                return i;
            }
        }
        return 0;
    }
}
