package vip.Resolute.events.impl;

import vip.Resolute.events.Event;

import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class EventLiquidBB extends Event<EventLiquidBB> {
    BlockLiquid blockLiquid;
    BlockPos pos;
    AxisAlignedBB axisAlignedBB;

    public EventLiquidBB(BlockLiquid blockLiquid, BlockPos pos, AxisAlignedBB axisAlignedBB) {
        this.blockLiquid = blockLiquid;
        this.pos = pos;
        this.axisAlignedBB = axisAlignedBB;
    }

    public AxisAlignedBB getAxisAlignedBB() {
        return axisAlignedBB;
    }

    public void setAxisAlignedBB(AxisAlignedBB axisAlignedBB) {
        this.axisAlignedBB = axisAlignedBB;
    }

    public BlockLiquid getBlock() {
        return blockLiquid;
    }

    public BlockPos getPos() {
        return pos;
    }
}
