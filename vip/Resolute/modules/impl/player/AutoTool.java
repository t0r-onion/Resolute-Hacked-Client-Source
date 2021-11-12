package vip.Resolute.modules.impl.player;

import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.combat.KillAura;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.util.player.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;

public class AutoTool extends Module {

    public BooleanSetting autoSword = new BooleanSetting("Sword", true);

    public AutoTool() {
        super("AutoTool", 0, "Automatically switches to the best tool", Category.PLAYER);
        this.addSettings(autoSword);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            if(e.isPre()) {
                MovingObjectPosition objectMouseOver;
                if ((objectMouseOver = mc.objectMouseOver) != null &&
                        mc.gameSettings.keyBindAttack.isKeyDown()) {
                    BlockPos blockPos;
                    if (objectMouseOver.entityHit != null)
                        doSwordSwap();
                    else if ((blockPos = objectMouseOver.getBlockPos()) != null) {
                        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                        float strongestToolStr = 1.0F;
                        int strongestToolSlot = -1;
                        for (int i = 36; i < 45; i++) {
                            ItemStack stack = Resolute.getStackInSlot(i);

                            if (stack != null && stack.getItem() instanceof ItemTool) {
                                float strVsBlock = stack.getStrVsBlock(block);
                                if (strVsBlock > strongestToolStr) {
                                    strongestToolStr = strVsBlock;
                                    strongestToolSlot = i;
                                }
                            }
                        }

                        if (strongestToolSlot != -1)
                            mc.thePlayer.inventory.currentItem = strongestToolSlot - 36;
                    }
                } else if (KillAura.target != null)
                    doSwordSwap();
            }
        }
    }

    private void doSwordSwap() {
        double damage = 1.0;
        int slot = -1;
        for (int i = 36; i < 45; i++) {
            ItemStack stack = Resolute.getStackInSlot(i);

            if (stack != null && stack.getItem() instanceof ItemSword) {
                double damageVs = InventoryUtils.getItemDamage(stack);
                if (damageVs > damage) {
                    damage = damageVs;
                    slot = i;
                }
            }
        }

        if (slot != -1)
            mc.thePlayer.inventory.currentItem = slot - 36;
    }
}
/*
public void onEvent(Event e) {
        if(e.isPre()) {
            if(!mc.gameSettings.keyBindAttack.pressed) {
                return;
            }

            if(mc.objectMouseOver == null) {
                return;
            }

            BlockPos pos = mc.objectMouseOver.getBlockPos();

            if(pos == null) {
                return;
            }

            block(pos);
        }
    }

    public void block(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float strength = 1.0f;
        int bestItemIndex = -1;

        for(int i = 0; i < 9; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];



            if(itemStack != null && itemStack.getStrVsBlock(block) > strength) {
                strength = itemStack.getStrVsBlock(block);
                bestItemIndex = i;
            }
        }

        if(bestItemIndex != -1) {
            mc.thePlayer.inventory.currentItem = bestItemIndex;
        }
    }
 */
