package vip.Resolute.modules.impl.movement;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventLiquidBB;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.util.movement.MovementUtils;
import vip.Resolute.util.misc.TimerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class Jesus extends Module {
    public ModeSetting mode = new ModeSetting("Mode", "Simple", "Simple", "NCP", "Matrix");

    TimerUtils timerUtils = new TimerUtils();
    boolean nextTick;

    public Jesus() {
        super("Jesus", 0, "Allows you to walk on water", Category.MOVEMENT);
        this.addSettings(mode);
    }

    public void onEnable() {
        mc.timer.timerSpeed = 1.0f;
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1.0f;
        mc.thePlayer.speedInAir = 0.02f;
    }

    public void onEvent(Event e) {
        if (e instanceof EventMotion) {
            EventMotion event = (EventMotion) e;

            if(mode.is("NCP")) {
                if(mc.thePlayer.onGround && !collideBlock(new AxisAlignedBB(mc.thePlayer.getEntityBoundingBox().maxX, mc.thePlayer.getEntityBoundingBox().maxY, mc.thePlayer.getEntityBoundingBox().maxZ, mc.thePlayer.getEntityBoundingBox().minX, mc.thePlayer.getEntityBoundingBox().minY - 0.01D, mc.thePlayer.getEntityBoundingBox().minZ), block -> block instanceof BlockLiquid)) {
                    mc.timer.timerSpeed = 1f;
                }
            }

            if(mode.is("Solid")) {
                if (e.isPre()) {
                    boolean sh = shouldJesus();
                    if (mc.thePlayer.isInWater() && !mc.thePlayer.isSneaking() && sh) {
                        mc.thePlayer.motionY = 0.09;
                    }

                    if (isOnLiquid(0.001)) {
                        if (isTotalOnLiquid(0.001) && mc.thePlayer.onGround && !mc.thePlayer.isInWater()) {
                            event.setY(event.getY() + (mc.thePlayer.ticksExisted % 2 == 0 ? 0.0000000001D : -0.000000000001D));
                        }
                    }
                }
            }

            if (mode.is("Simple")) {
                if (e instanceof EventLiquidBB && !mc.thePlayer.isInWater() && mc.thePlayer.fallDistance < 4 && !mc.thePlayer.isSneaking()) {
                    ((EventLiquidBB) e).setAxisAlignedBB((new AxisAlignedBB(((EventLiquidBB) e).getPos().getX(), ((EventLiquidBB) e).getPos().getY(), ((EventLiquidBB) e).getPos().getZ(), ((EventLiquidBB) e).getPos().getX() + 1, ((EventLiquidBB) e).getPos().getY() + 1, ((EventLiquidBB) e).getPos().getZ() + 1)));
                }
                if (mc.thePlayer.isInWater() && !mc.thePlayer.isSneaking()) {
                    mc.thePlayer.motionY = 0.2;
                }
            }

            if (mode.is("Matrix")) {
                if (mc.thePlayer.isInWater() && !mc.gameSettings.keyBindJump.pressed && !mc.gameSettings.keyBindSneak.pressed) {
                    mc.thePlayer.motionY = 0;

                    if (mc.gameSettings.keyBindForward.pressed) {
                        double dir = MovementUtils.getDirection();

                        mc.thePlayer.motionX += -Math.sin(dir) * 0.01;
                        mc.thePlayer.motionZ += Math.cos(dir) * 0.01;

                        if (timerUtils.hasTimeElapsed(200L, true)) {
                            mc.timer.timerSpeed = 1.25f;
                        } else {
                            mc.timer.timerSpeed = 1.0f;
                        }
                    }
                }
            }
        }

        if(e instanceof EventPacket) {
            if(mode.is("NCP")) {
                if(mc.thePlayer == null)
                    return;

                if(((EventPacket) e).getPacket() instanceof C03PacketPlayer) {
                    final C03PacketPlayer packetPlayer = (C03PacketPlayer) ((EventPacket) e).getPacket();

                    if(collideBlock(new AxisAlignedBB(mc.thePlayer.getEntityBoundingBox().maxX, mc.thePlayer.getEntityBoundingBox().maxY, mc.thePlayer.getEntityBoundingBox().maxZ, mc.thePlayer.getEntityBoundingBox().minX, mc.thePlayer.getEntityBoundingBox().minY - 0.01D, mc.thePlayer.getEntityBoundingBox().minZ), block -> block instanceof BlockLiquid)) {
                        mc.thePlayer.motionY = 0.4;

                        nextTick = !nextTick;

                        mc.timer.timerSpeed = 1f;

                        if(nextTick) packetPlayer.y -= 0.001D;
                    }else {
                        mc.timer.timerSpeed = 1f;
                    }
                }
            }
        }

        if(e instanceof EventLiquidBB) {
            EventLiquidBB event = (EventLiquidBB) e;

            if(mode.is("Solid")) {
                int n = -1;
                if (event.getPos().getY() + 0.9 < mc.thePlayer.boundingBox.minY) {
                    if (n <= 4) {
                        event.setAxisAlignedBB(new AxisAlignedBB(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getPos().getX() + 1, event.getPos().getY() + (1), event.getPos().getZ() + 1));
                        event.setCancelled(shouldSetBoundingBox());
                    }
                }
            }
        }
    }

    private boolean shouldSetBoundingBox() {
        return (!mc.thePlayer.isSneaking()) && (mc.thePlayer.fallDistance < 12.0F);
    }

    public static boolean collideBlock(AxisAlignedBB axisAlignedBB, Collidable collide) {
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; ++x) {
            for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; ++z) {
                Block block = getBlockAtPos(new BlockPos(x, axisAlignedBB.minY, z));
                if (collide.collideBlock(block)) continue;
                return false;
            }
        }
        return true;
    }

    public static Block getBlockAtPos(BlockPos inBlockPos) {
        IBlockState s = mc.theWorld.getBlockState(inBlockPos);
        return s.getBlock();
    }

    public static boolean isOnLiquid(double profondeur)
    {
        boolean onLiquid = false;

        if(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - profondeur, mc.thePlayer.posZ)).getBlock().getMaterial().isLiquid()) {
            onLiquid = true;
        }
        return onLiquid;
    }

    public static boolean isTotalOnLiquid(double profondeur)
    {
        for(double x = mc.thePlayer.boundingBox.minX; x < mc.thePlayer.boundingBox.maxX; x +=0.01f){

            for(double z = mc.thePlayer.boundingBox.minZ; z < mc.thePlayer.boundingBox.maxZ; z +=0.01f){
                Block block = mc.theWorld.getBlockState(new BlockPos(x, mc.thePlayer.posY - profondeur,z)).getBlock();
                if(!(block instanceof BlockLiquid) && !(block instanceof BlockAir)){
                    return false;
                }
            }
        }
        return true;
    }

    boolean shouldJesus(){
        double x = mc.thePlayer.posX; double y = mc.thePlayer.posY; double z = mc.thePlayer.posZ;
        ArrayList<BlockPos>pos = new ArrayList<BlockPos>(Arrays.asList(new BlockPos(x + 0.3, y, z+0.3),
                new BlockPos(x - 0.3, y, z+0.3),new BlockPos(x + 0.3, y, z-0.3),new BlockPos(x - 0.3, y, z-0.3)));
        for(BlockPos po : pos){
            if(!(mc.theWorld.getBlockState(po).getBlock() instanceof BlockLiquid))
                continue;
            if(mc.theWorld.getBlockState(po).getProperties().get(BlockLiquid.LEVEL) instanceof Integer){
                if((int)mc.theWorld.getBlockState(po).getProperties().get(BlockLiquid.LEVEL) <= 4){
                    return true;
                }
            }
        }
        return false;
    }

    interface Collidable {
        boolean collideBlock(Block var1);
    }
}
