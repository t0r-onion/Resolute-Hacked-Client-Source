package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

public class Eagle extends Module {
    public Eagle() {
        super("Eagle", 0, "Automatically shifts for you", Category.PLAYER);
    }


    public void onDisable() {
        mc.gameSettings.keyBindSneak.pressed = false;
    }

    public void onEvent(Event e) {
        if(e instanceof EventUpdate) {
            if(mc.thePlayer != null && mc.theWorld != null) {
                ItemStack i = mc.thePlayer.getCurrentEquippedItem();
                BlockPos Bp = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1D, mc.thePlayer.posZ);
                if (i != null) {
                    if (i.getItem() instanceof ItemBlock) {
                        mc.gameSettings.keyBindSneak.pressed = false;
                        if (mc.theWorld.getBlockState(Bp).getBlock() == Blocks.air) {
                            mc.gameSettings.keyBindSneak.pressed = true;
                        }
                    }
                }
            }
        }
    }
}
