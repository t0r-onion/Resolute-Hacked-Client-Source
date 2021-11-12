package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.util.BlockPos;

public class EventBlockDamaged extends Event<EventBlockDamaged> {
    private final BlockPos blockPos;

    public EventBlockDamaged(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }
}
