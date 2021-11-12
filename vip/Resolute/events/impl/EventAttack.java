package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.entity.Entity;

public class EventAttack extends Event<EventAttack> {
    public Entity target;
    public EventAttack(Entity t){
        target = t;
    }
    public Entity getTarget(){
        return this.target;
    }
}
