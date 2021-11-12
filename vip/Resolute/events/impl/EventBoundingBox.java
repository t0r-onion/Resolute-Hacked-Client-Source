package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class EventBoundingBox extends Event<EventBoundingBox> {
    private BlockPos blockPos;
    private AxisAlignedBB bounds;

    public EventBoundingBox(BlockPos blockPos, AxisAlignedBB bounds) {
        this.blockPos = blockPos;
        this.bounds = bounds;
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public void blockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public AxisAlignedBB bounds() {
        return bounds;
    }

    public void bounds(AxisAlignedBB bounds) {
        this.bounds = bounds;
    }
}
