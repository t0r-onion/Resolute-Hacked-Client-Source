package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.entity.EntityLivingBase;

public class EventEntitySwing extends Event<EventEntitySwing> {
    private final int entityId;

    public EventEntitySwing(final EntityLivingBase entity) {
        this.entityId = entity.getEntityId();
    }

    public int getEntityId() {
        return this.entityId;
    }
}
