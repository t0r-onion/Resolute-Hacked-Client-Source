package vip.Resolute.modules.impl.player;

import vip.Resolute.events.Event;
import vip.Resolute.events.impl.EventBlockDamaged;
import vip.Resolute.events.impl.EventMotion;
import vip.Resolute.events.impl.EventPacket;
import vip.Resolute.events.impl.EventUpdate;
import vip.Resolute.modules.Module;
import vip.Resolute.settings.impl.ModeSetting;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class FastBreak extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Normal", "Normal", "Watchdog");

    private boolean bzs = false;
    private float bzx = 0.0f;
    public BlockPos blockPos;
    public EnumFacing facing;
    public static float speed;
    public static int delay;

    public FastBreak() {
        super("FastBreak", 0, "Allows you to break blocks faster", Category.PLAYER);
        this.addSettings(mode);
    }

    public void onEvent(Event e) {
        if(e instanceof EventMotion) {
            if(mode.is("Watchdog")) {
                if (mc.playerController.extendedReach()) {
                    mc.playerController.blockHitDelay = 0;
                } else if (this.bzs) {
                    Block block = mc.theWorld.getBlockState(this.blockPos).getBlock();
                    this.bzx = (float)((double)this.bzx + (double)block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, this.blockPos) * 1.4);
                    if (this.bzx >= 1.0f) {
                        mc.theWorld.setBlockState(this.blockPos, Blocks.air.getDefaultState(), 11);
                        mc.thePlayer.sendQueue.getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.blockPos, this.facing));
                        this.bzx = 0.0f;
                        this.bzs = false;
                    }
                }
            }
        }

        if(e instanceof EventPacket) {
            if(mode.is("Watchdog")) {
                try {
                    if(((EventPacket) e).getPacket() instanceof C07PacketPlayerDigging) {
                        if(mc.playerController != null) {
                            C07PacketPlayerDigging c07PacketPlayerDigging = ((EventPacket) e).getPacket();
                            if (c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                                this.bzs = true;
                                this.blockPos = c07PacketPlayerDigging.getPosition();
                                this.facing = c07PacketPlayerDigging.getFacing();
                                this.bzx = 0.0f;
                            } else if (c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK || c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                                this.bzs = false;
                                this.blockPos = null;
                                this.facing = null;
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        if(e instanceof EventUpdate) {
            if(e.isPre()) {
                if(mode.is("Normal")) {
                    mc.playerController.blockHitDelay = 0;
                }
            }
        }

        if(e instanceof EventBlockDamaged) {
            EventBlockDamaged event = (EventBlockDamaged) e;

            if(mode.is("Normal")) {
                PlayerControllerMP playerController = mc.playerController;
                BlockPos pos = event.getBlockPos();
                mc.thePlayer.swingItem();
                playerController.curBlockDamageMP += this.getBlock((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()).getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) * 0.186F;
            }
        }
    }

    public Block getBlock(double posX, double posY, double posZ) {
        BlockPos pos = new BlockPos((int)posX, (int)posY, (int)posZ);
        return mc.theWorld.getChunkFromBlockCoords(pos).getBlock(pos);
    }
}

