package vip.Resolute.modules.impl.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.util.movement.MovementUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;

import java.util.List;
import java.util.Arrays;

public class NoFall extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Ground Spoof", "Ground Spoof", "NCP", "Verus", "Packet", "None", "Edit", "Rounded", "Dev");

    private static final List<Double> BLOCK_HEIGHTS;
    private float fallDist = 0.0f;

    private boolean packetModify = false;
    private boolean needSpoof = false;
    private int packet1Count = 0;

    static {
        BLOCK_HEIGHTS = Arrays.asList(0.015625, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0);
    }

    public NoFall() {
        super("NoFall", 0, "Removes fall damage", Category.PLAYER);
        this.addSettings(mode);
    }

    public void onEnable() {
        packetModify = false;
        needSpoof = false;
        packet1Count = 0;
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
        super.onDisable();
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());
        double minFallDist;

        if(e instanceof EventMotion) {
            EventMotion eventMotion = (EventMotion) e;

            if(mode.is("Dev")) {
                if (MovementUtils.isOverVoid())
                    return;
                if (mc.thePlayer.fallDistance >= 2.5f) {
                    eventMotion.setOnGround(mc.thePlayer.ticksExisted % 2 == 0);
                }
            }

            if(mode.is("Verus")) {
                if (mc.thePlayer.fallDistance - mc.thePlayer.motionY > 3) {
                    mc.thePlayer.motionY = 0.0;
                    mc.thePlayer.fallDistance = 0.0f;
                    mc.thePlayer.motionX *= 0.6;
                    mc.thePlayer.motionZ *= 0.6;
                    needSpoof = true;
                }

                if (mc.thePlayer.fallDistance / 3 > packet1Count) {
                    packet1Count = (int) (mc.thePlayer.fallDistance / 3);
                    packetModify = true;
                }
                if (mc.thePlayer.onGround) {
                    packet1Count = 0;
                }
            }

            if(mode.is("NCP")) {
                BlockPos blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 6.0D, mc.thePlayer.posZ);
                Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
                BlockPos blockPos2 = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 5.0D, mc.thePlayer.posZ);
                Block block2 = Minecraft.getMinecraft().theWorld.getBlockState(blockPos2).getBlock();
                BlockPos blockPos3 = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 4.0D, mc.thePlayer.posZ);
                Block block3 = Minecraft.getMinecraft().theWorld.getBlockState(blockPos3).getBlock();
                if ((block != Blocks.air || block2 != Blocks.air || block3 != Blocks.air) && mc.thePlayer.fallDistance > 2.0F) {
                    mc.getNetHandler().addToSendQueue( new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1D, mc.thePlayer.posZ, false));
                    mc.thePlayer.motionY = -10.0D;
                    mc.thePlayer.fallDistance = 0.0F;
                }
            }

            if(mode.is("Edit")) {
                double motionY;
                double fallingDist;
                double realDist;
                if (this.fallDist > mc.thePlayer.fallDistance) {
                    this.fallDist = 0.0f;
                }
                if (!(mc.thePlayer.motionY < 0.0) || !((double)mc.thePlayer.fallDistance > 2.124) || this.checkVoid(mc.thePlayer) || !this.isBlockUnder() || mc.thePlayer.isSpectator() || mc.thePlayer.capabilities.allowFlying || !((realDist = (fallingDist = (double)(mc.thePlayer.fallDistance - this.fallDist)) + -(((motionY = mc.thePlayer.motionY) - 0.08) * (double)0.98f)) >= 3.0)) return;
                eventMotion.setOnGround(true);
            }

            if(mode.is("Watchdog")) {
                double motionY;
                double fallingDist;
                double realDist;
                if (this.fallDist > mc.thePlayer.fallDistance) {
                    this.fallDist = 0.0f;
                }
                if (!(mc.thePlayer.motionY < 0.0) || !((double)mc.thePlayer.fallDistance > 2.124) || this.checkVoid(mc.thePlayer) || !this.isBlockUnder() || mc.thePlayer.isSpectator() || mc.thePlayer.capabilities.allowFlying || !((realDist = (fallingDist = (double)(mc.thePlayer.fallDistance - this.fallDist)) + -(((motionY = mc.thePlayer.motionY) - 0.08) * (double)0.98f)) >= 3.0)) return;
                mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(true));
                this.fallDist = mc.thePlayer.fallDistance;
            }

            if(mode.is("Rounded")) {
                minFallDist = MovementUtils.getMinFallDist();

                if(mc.thePlayer.fallDistance % minFallDist == 0) {
                    double currentYOffset = MovementUtils.getBlockHeight();
                    BLOCK_HEIGHTS.sort((h, h1) -> (int) ((Math.abs(currentYOffset - h) - Math.abs(currentYOffset - h1)) * 10));
                    double yPos = ((int) eventMotion.getY()) + BLOCK_HEIGHTS.get(0);
                    eventMotion.setY(yPos);
                }
            }

            if(e.isPre()) {
                if(mode.is("Packet")) {
                    if(mc.thePlayer.fallDistance > 3) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(true));
                    }
                }
            }


            if(mode.is("Ground Spoof")) {
                if(mc.thePlayer.fallDistance > 3.0f) {
                    eventMotion.setOnGround(true);
                    mc.thePlayer.fallDistance = 0.0f;
                }
            }
        }

        if(e instanceof EventPacket) {
            if(((EventPacket) e).getPacket() instanceof C03PacketPlayer) {
                C03PacketPlayer packet = ((EventPacket) e).getPacket();

                if (mode.is("Verus") && needSpoof) {
                    packet.onGround = true;
                    needSpoof = false;
                }
            }
        }
    }

    private boolean checkVoid(EntityLivingBase entity) {
        for (int b = -1; b <= 0; b = (int)((byte)(b + 1))) {
            for (int b1 = -1; b1 <= 0; b1 = (int)((byte)(b1 + 1))) {
                if (!this.isVoid(b, b1, entity)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean isVoid(int X, int Z, EntityLivingBase entity) {
        if (mc.thePlayer.posY < 0.0) {
            return true;
        }
        for (int off = 0; off < (int)entity.posY + 2; off += 2) {
            AxisAlignedBB bb = entity.getEntityBoundingBox().offset(X, -off, Z);
            if (mc.theWorld.getCollidingBoundingBoxes(entity, bb).isEmpty()) {
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean isBlockUnder() {
        int offset = 0;
        while ((double)offset < mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight()) {
            AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(0.0, -offset, 0.0);
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty()) {
                return true;
            }
            offset += 2;
        }
        return false;
    }
}

