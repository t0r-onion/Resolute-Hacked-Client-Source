package vip.Resolute.modules.impl.combat;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventMove;
import vip.Resolute.events.impl.EventWindowClick;
import vip.Resolute.modules.Module;
import vip.Resolute.modules.impl.movement.Scaffold;
import vip.Resolute.settings.impl.BooleanSetting;
import vip.Resolute.settings.impl.NumberSetting;

import vip.Resolute.util.misc.TimerUtil;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.player.InventoryUtils;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;

import java.util.Iterator;

public class AutoPot extends Module {

    public NumberSetting healthProp = new NumberSetting("Health", 6.0, 1.0, 10.0, 0.5);
    public NumberSetting slotProp = new NumberSetting("Slot", 7.0, 1.0, 9.0, 1.0);
    public NumberSetting delayProp = new NumberSetting("Delay", 400.0, 0.0, 1000.0, 50.0);

    private static final byte HEALTH_BELOW = 1;
    private static final byte BETTER_THAN_CURRENT = 2;
    private String potionCounter;

    private static final PotionType[] VALID_POTIONS = {PotionType.HEALTH, PotionType.REGEN, PotionType.SPEED};

    private final C08PacketPlayerBlockPlacement THROW_POTION_PACKET = new C08PacketPlayerBlockPlacement(
            new BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f);

    private final TimerUtil interactionTimer = new TimerUtil();
    private int prevSlot;
    private boolean potting;
    private int jumpTicks;
    private boolean jump;

    private boolean jumpNextTick;

    public AutoPot() {
        super("AutoPot", 0, "", Category.COMBAT);
        this.addSettings(healthProp, slotProp, delayProp);
    }

    @Override
    public void onEnable() {
        this.potionCounter = "0";
        prevSlot = -1;
        potting = false;
    }

    public void onEvent(Event e) {
        this.setSuffix(this.potionCounter);

        if(e instanceof EventMove) {
            EventMove event = (EventMove) e;

            if (this.jump && this.jumpTicks >= 0) {
                event.setX(0.0);
                event.setZ(0.0);
            }
        }

        if(e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;
            if (mc.currentScreen != null)
                return;
            if (Scaffold.enabled)
                return;
            if (event.isPre()) {
                this.potionCounter = Integer.toString(this.getValidPotionsInInv());

                if (jumpNextTick) {
                    mc.thePlayer.setPosition(
                            mc.thePlayer.posX,
                            mc.thePlayer.posY + 1.2492F,
                            mc.thePlayer.posZ);
                    event.setY(event.getY() + 1.2492F);
                    jumpNextTick = false;
                }

                if (this.jump) {
                    --this.jumpTicks;
                    if (MovementUtils.isOnGround()) {
                        this.jump = false;
                        this.jumpTicks = -1;
                    }
                }


                if (interactionTimer.hasElapsed((long) delayProp.getValue())) {
                    for (int slot = 9; slot < 45; slot++) {
                        ItemStack stack = Resolute.getStackInSlot(slot);
                        if (stack != null && stack.getItem() instanceof ItemPotion &&
                                ItemPotion.isSplash(stack.getMetadata()) && InventoryUtils.isBuffPotion(stack)) {
                            ItemPotion itemPotion = (ItemPotion) stack.getItem();
                            boolean validEffects = false;

                            for (PotionEffect effect : itemPotion.getEffects(stack.getMetadata())) {
                                for (PotionType potionType : VALID_POTIONS) {
                                    if (potionType.potionId == effect.getPotionID()) {
                                        validEffects = true;
                                        if (hasFlag(potionType.requirementFlags, HEALTH_BELOW))
                                            validEffects = mc.thePlayer.getHealth() < healthProp.getValue() * 2.0F;
                                        boolean orIsLesserPresent = hasFlag(potionType.requirementFlags, BETTER_THAN_CURRENT);
                                        PotionEffect activePotionEffect = mc.thePlayer.getActivePotionEffect(potionType.potionId);
                                        if (orIsLesserPresent)
                                            if (activePotionEffect != null)
                                                validEffects &= activePotionEffect.getAmplifier() < effect.getAmplifier();
                                    }
                                }

                            }

                            if (validEffects) {
                                if (MovementUtils.isOverVoid())
                                    return;

                                prevSlot = mc.thePlayer.inventory.currentItem;

                                double xDist = mc.thePlayer.posX - mc.thePlayer.lastTickPosX;
                                double zDist = mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ;

                                double speed = Math.sqrt(xDist * xDist + zDist * zDist);

                                boolean shouldPredict = speed > 0.38D;
                                boolean shouldJump = speed < MovementUtils.WALK_SPEED;
                                boolean onGround = MovementUtils.isOnGround();

                                if (shouldJump && onGround && !MovementUtils.isBlockAbove() && MovementUtils.getJumpBoostModifier() == 0) {
                                    this.jump = true;
                                    this.jumpTicks = 9;
                                    mc.thePlayer.motionX = 0.0D;
                                    mc.thePlayer.motionZ = 0.0D;
                                    event.setPitch(-90.0F);
                                    mc.thePlayer.jump();
                                } else if (shouldPredict || onGround) {
                                    event.setYaw(MovementUtils.getMovementDirection());
                                    event.setPitch(shouldPredict ? 0.0F : 45.0F);
                                } else return;

                                final int potSlot;
                                KillAura.waitTicks = 2;
                                if (slot >= 36) {
                                    potSlot = slot - 36;
                                } else {
                                    int potionSlot = (int) (slotProp.getValue() - 1);
                                    InventoryUtils.windowClick(slot, potionSlot,
                                            InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                                    potSlot = potionSlot;
                                }
                                mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(potSlot));
                                potting = true;
                                break;
                            }
                        }
                    }
                }
            } else if (potting && prevSlot != -1) {
                mc.getNetHandler().getNetworkManager().sendPacket(THROW_POTION_PACKET);
                mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(prevSlot));
                interactionTimer.reset();
                prevSlot = -1;
                potting = false;
            }
        }
    }

    private int getValidPotionsInInv() {
        int count = 0;
        for (int i = 9; i < 45; i++) {
            ItemStack stack = Resolute.getStackInSlot(i);

            if (stack != null && stack.getItem() instanceof ItemPotion &&
                    ItemPotion.isSplash(stack.getMetadata()) && InventoryUtils.isBuffPotion(stack)) {
                ItemPotion itemPotion = (ItemPotion) stack.getItem();
                for (PotionEffect effect : itemPotion.getEffects(stack.getMetadata())) {
                    boolean breakOuter = false;
                    for (PotionType type : VALID_POTIONS) {
                        if (type.potionId == effect.getPotionID()) {
                            count++;
                            breakOuter = true;
                            break;
                        }
                    }
                    if (breakOuter) break;
                }
            }
        }

        return count;
    }

    private boolean hasFlag(int flags, int flagToCheck) {
        return (flags & flagToCheck) == flagToCheck;
    }

    private enum Items {
        HEADS, POTIONS, SOUPS
    }

    private enum PotionType {
        SPEED(Potion.moveSpeed.id, BETTER_THAN_CURRENT),
        REGEN(Potion.regeneration.id, BETTER_THAN_CURRENT | HEALTH_BELOW),
        HEALTH(Potion.heal.id, HEALTH_BELOW);

        private final int potionId;
        private final int requirementFlags;

        PotionType(int potionId, int requirementFlags) {
            this.potionId = potionId;
            this.requirementFlags = requirementFlags;
        }
    }
}



