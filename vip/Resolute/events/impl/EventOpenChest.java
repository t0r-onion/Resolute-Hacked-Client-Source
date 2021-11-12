package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import net.minecraft.inventory.ContainerChest;

public class EventOpenChest extends Event<EventOpenChest> {
    ContainerChest chest;

    public EventOpenChest(ContainerChest chest) {
        this.chest = chest;
    }

    public ContainerChest getChest() {
        return chest;
    }

    public void setChest(ContainerChest chest) {
        this.chest = chest;
    }
}
