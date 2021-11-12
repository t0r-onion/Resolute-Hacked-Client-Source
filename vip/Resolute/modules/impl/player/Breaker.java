package vip.Resolute.modules.impl.player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import vip.Resolute.util.world.BlockUtils;
import vip.Resolute.util.player.RaytraceUtils;
import vip.Resolute.util.player.RotationUtils;
import vip.Resolute.util.misc.TimerUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class Breaker extends Module {
    private int h;
    private int i;
    private int j;
    private static int k = 5;

    protected TimerUtils time = new TimerUtils();
    protected long lastFail;
    protected boolean failed;
    protected BlockPos failedBlock, lastArround;
    protected Block lastArroundBlock;
    protected int ftrys, blockID = 92;
    protected float yaw, pitch;

    public ModeSetting mode = new ModeSetting("Mode", "Bed", "Bed", "Cake");

    public Breaker() {
        super("Breaker", 0, "Breaks certain blocks", Category.PLAYER);
        this.addSettings(mode);
    }

    public void onDisable() {
        super.onDisable();
        this.time.reset();
        this.lastFail = 0;
        this.ftrys = 0;
        this.failed = false;
        this.lastArround = null;
        this.lastArroundBlock = null;
        this.failedBlock = null;
    }

    public void onEvent(Event e) {
        this.setSuffix(mode.getMode());
        if(e instanceof EventMotion && e.isPre()) {
            if(mode.is("Cake")) {
                EventMotion eventMotion = (EventMotion) e;
                if (this.mc.theWorld != null && this.mc.thePlayer != null) {

                    if (this.failedBlock != null && this.getBlocksArround(this.failedBlock).isEmpty()) {
                        this.lastArround = null;
                        this.lastArroundBlock = null;
                        this.failed = false;
                    }

                    try {

                        if(this.failed && !getBlocksArround(this.failedBlock).isEmpty()) {
                            this.checkFailed();
                        }

                        for(int y = -5; y < 5; y++) {
                            for(int x = -5; x < 5; x++) {
                                for(int z = -5; z < 5; z++) {

                                    BlockPos blockPos = this.mc.thePlayer.getPosition().add((double) x, (double) y, (double) z);
                                    Block block = this.mc.theWorld.getBlockState(blockPos).getBlock();

                                    if(block == Block.getBlockById(this.blockID)) {

                                        this.setRotation(blockPos);

                                        if(this.time.hasTimeElapsed(4, true)) {

                                            this.destroyBlock(blockPos);
                                            this.ftrys++;
                                            this.time.reset();

                                            if(this.failedDestroy(blockPos, block) && !this.getBlocksArround(blockPos).isEmpty() && (double) this.ftrys > 5) {

                                                this.lastFail = System.currentTimeMillis();
                                                this.failed = true;
                                                this.failedBlock = blockPos;
                                                this.ftrys = 0;

                                            }
                                        }
                                    }

                                }
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

            if(mode.is("Bed")) {
                int x = -k;
                while (x < k) {
                    int y = k;
                    while (y > -k) {
                        int z = -k;
                        while (z < k) {
                            this.h = (int)mc.thePlayer.posX + x;
                            this.i = (int)mc.thePlayer.posY + y;
                            this.j = (int)mc.thePlayer.posZ + z;
                            BlockPos blockPos = new BlockPos(this.h, this.i, this.j);
                            Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                            float[] rotations = RotationUtils.getRotationFromPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                            if (block.getBlockState().getBlock() == Block.getBlockById(26)) {
                                ((EventMotion) e).setYaw(rotations[0]);
                                ((EventMotion) e).setPitch(rotations[1]);
                                mc.getNetHandler().sendPacketNoEvent(new C0APacketAnimation());
                                mc.playerController.curBlockDamageMP = 1.0f;
                                mc.playerController.onPlayerDamageBlock(blockPos, EnumFacing.NORTH);
                                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.NORTH));
                                mc.thePlayer.sendQueue.addToSendQueue((Packet)new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.NORTH));
                            }
                            z++;
                        }
                        y--;
                    }
                    x++;
                }
            }
        }
    }

    protected void checkFailed() {
        long sinceLastFail = System.currentTimeMillis() - this.lastFail;
        if(!this.getBlocksArround(this.failedBlock).isEmpty()) {
            BlockPos blockPos = this.getClosetBlock(this.getBlocksArround(this.failedBlock));
            this.setRotation(blockPos);
            if(sinceLastFail > 0) {
                this.lastArround = blockPos;
                this.lastArroundBlock = this.mc.theWorld.getBlockState(blockPos).getBlock();
                this.destroyBlock(blockPos);
            }
        }

        if(this.lastArround != null) {
            this.lastArround = null;
            this.lastArroundBlock = null;
            this.failed = false;
        }
    }
    protected BlockPos getClosetBlock(List list) {
        BlockPos currentPos = null;
        double currentRange = Double.MAX_VALUE;
        Iterator i = list.iterator();

        while (i.hasNext()) {
            BlockPos blockPos = (BlockPos) i.next();
            if (this.mc.thePlayer.getDistance((double) blockPos.getX(), (double) blockPos.getY(),
                    (double) blockPos.getZ()) < currentRange) {
                currentRange = this.mc.thePlayer.getDistance(blockPos.getX(), (double) blockPos.getY(),
                        (double) blockPos.getZ());
                currentPos = blockPos;
            }
        }
        return currentPos;
    }



    protected List getBlocksArround(BlockPos mainPos) {
        ArrayList arroundBlocks = new ArrayList();
        Block mainBlock = this.mc.theWorld.getBlockState(mainPos).getBlock();

        for (int y = 0; y < 2; y++) {
            for (int x = -5; x < 5; x++) {
                for (int z = -5; z < 5; z++) {
                    BlockPos blockPos = mainPos.add(x, y, z);
                    Block block = this.mc.theWorld.getBlockState(blockPos).getBlock();
                    if (!(block == Blocks.air)) {
                        arroundBlocks.add(blockPos);
                    }
                }
            }
        }
        return arroundBlocks;


    }
    protected boolean failedDestroy(BlockPos blockPos, Block block) {
        return this.mc.theWorld.getBlockState(blockPos).getBlock() == block;
    }
    protected void setRotation(BlockPos blockPos) {
        BlockUtils sLoc = new BlockUtils(this.mc.thePlayer.posX, this.mc.thePlayer.posY + 1.6D, this.mc.thePlayer.posZ);
        BlockUtils eLoc = new BlockUtils(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        RaytraceUtils rayTrace = new RaytraceUtils(sLoc, eLoc);
        this.pitch = (float) rayTrace.getPitch();
        this.yaw = (float) rayTrace.getYaw();

        //eventMotion.setPitch(pitch);
        //eventMotion.setYaw(yaw);
    }
    protected void destroyBlock(BlockPos blockPos) {
        this.mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.DOWN));
        this.mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.DOWN));
        mc.getNetHandler().addToSendQueueSilent(new C0APacketAnimation());
    }
}
