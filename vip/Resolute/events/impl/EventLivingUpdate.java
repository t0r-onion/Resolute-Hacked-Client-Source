package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.entity.Entity;

public class EventLivingUpdate extends Event<EventLivingUpdate> {
    private Entity entity;
    public EventLivingUpdate(Entity entity) {
        super();
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
