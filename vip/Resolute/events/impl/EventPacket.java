package vip.Resolute.events.impl;

import vip.Resolute.events.Event;
import vip.Resolute.events.EventDirection;
import net.minecraft.network.Packet;

public class EventPacket extends Event<EventPacket> {
    public static Packet packet;

    public EventPacket(Packet packet, EventDirection direction) {
        this.packet = packet;
        this.direction = direction;
    }


    public <T extends Packet> T getPacket() {
        return (T) packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
