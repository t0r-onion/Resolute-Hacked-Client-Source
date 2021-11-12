package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.entity.Entity;

public class EventEntityDamage extends Event<EventEntityDamage> {

    Entity entity;
    private final double damage;

    public EventEntityDamage(Entity entity, final double damage) {
        this.entity = entity;
        this.damage = damage;
    }

    public Entity getEntity() {
        return entity;
    }

    public double getDamage() {
        return this.damage;
    }
}
