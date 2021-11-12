package vip.Resolute.modules.impl.movement;

import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFireworkCharge;
import net.minecraft.item.ItemStack;
import vip.Resolute.Resolute;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventMove;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.settings.impl.NumberSetting;
import vip.Resolute.util.misc.TimerUtil;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import vip.Resolute.util.movement.MovementUtils;

public class HighJump extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Redesky", "Redesky", "Matrix", "Watchdog");
    public NumberSetting multi = new NumberSetting("Multiplier", 1.0, () -> mode.is("Redesky"), 0.8, 1.5, 0.1);

    int slot = 0;
    int ticks;

    private int matrixStatus = 0;
    private boolean matrixWasTimer = false;

    private TimerUtil timer = new TimerUtil();

    private boolean damaged = false;

    public HighJump() {
        super("HighJump", 0, "Makes the player jump high", Category.MOVEMENT);
        this.addSettings(mode, multi);
    }

    public void onEnable() {
        ticks = 0;
        if(mode.is("Redesky")) {
            if(mc.thePlayer.onGround) {
                mc.thePlayer.motionY = multi.getValue();
                mc.thePlayer.motionX *= 1.5;
                mc.thePlayer.motionZ *= 1.5;
            }
        }
        damaged = false;
        matrixStatus = 0;
        matrixWasTimer = false;
        timer.reset();
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1f;
    }

    public void onEvent(Event e) {
        if(e instanceof EventMove) {
            if(mode.is("Watchdog")) {
                if(!damaged) {
                    /*
                    ((EventMove) e).setX(0.0);
                    ((EventMove) e).setZ(0.0);

                     */
                }
            }
        }

        if(e instanceof EventMotion) {
            if(mode.is("Watchdog")) {
                final int oldPitch = (int) mc.thePlayer.rotationPitch;
                if (mc.thePlayer.hurtTime > 0) {
                    damaged = true;
                }

                if (mc.thePlayer.onGround && !damaged) {
                    for(int i = 0; i < 9; i++) {
                        final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
                        if(itemStack != null) {
                            if (mc.thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemFireball) {
                                mc.getNetHandler().addToSendQueueSilent(new C09PacketHeldItemChange(i));
                                mc.getNetHandler().addToSendQueueSilent(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, 90, mc.thePlayer.onGround));
                                mc.getNetHandler().addToSendQueueSilent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                                mc.getNetHandler().addToSendQueueSilent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                mc.getNetHandler().addToSendQueueSilent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, oldPitch, mc.thePlayer.onGround));
                            }
                        }
                    }
                }

                if (damaged && mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 2.5f;
                    MovementUtils.setMotion(MovementUtils.getBaseSpeedHypixelApplied() * 6.65);
                    toggle();
                } else {
                    damaged = false;
                }
            }

            if(mode.is("Matrix")) {
                if (matrixWasTimer) {
                    mc.timer.timerSpeed = 1.00f;
                    matrixWasTimer = false;
                }
                if ((mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, mc.thePlayer.motionY, 0.0).expand(0.0, 0.0, 0.0)).isEmpty() || mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0, -4.0, 0.0).expand(0.0, 0.0, 0.0)).isEmpty()) && mc.thePlayer.fallDistance > 10) {
                    if (!mc.thePlayer.onGround) {
                        mc.timer.timerSpeed = 0.1f;
                        matrixWasTimer = true;
                    }
                }
                if (timer.hasElapsed(1000) && matrixStatus==1) {
                    mc.timer.timerSpeed = 1.0f;
                    mc.thePlayer.motionX = 0.0;
                    mc.thePlayer.motionZ = 0.0;
                    matrixStatus=0;
                    return;
                }
                if (matrixStatus==1 && mc.thePlayer.hurtTime > 0) {
                    mc.timer.timerSpeed = 1.0f;
                    mc.thePlayer.motionY = 3.0;
                    mc.thePlayer.motionX = 0.0;
                    mc.thePlayer.motionZ = 0.0;
                    mc.thePlayer.jumpMovementFactor = 0.00f;
                    matrixStatus=0;
                    return;
                }
                if (matrixStatus==2) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                    for(int i = 0; i <= 8; i++) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.3990, mc.thePlayer.posZ, false));
                        mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));
                    }
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                    mc.timer.timerSpeed = 0.6f;
                    matrixStatus=1;
                    timer.reset();
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), EnumFacing.UP));
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    return;
                }
                if (mc.thePlayer.isCollidedHorizontally && matrixStatus==0 && mc.thePlayer.onGround) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ), EnumFacing.UP));
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    matrixStatus=2;
                    mc.timer.timerSpeed = 0.05f;
                }
                if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
                    mc.thePlayer.motionX = 0.0;
                    mc.thePlayer.motionZ = 0.0;
                    mc.thePlayer.onGround = false;
                }
            }

            if(e.isPre()) {
                if(mode.is("Redesky")) {
                    if(mc.thePlayer.onGround)
                        toggle();
                }
            }
        }
    }
}
