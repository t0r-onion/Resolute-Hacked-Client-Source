package disabler.mc.protocol.packet;

import disabler.mc.protocol.util.ObjectUtil;
import disabler.packetlib.packet.Packet;

public abstract class MinecraftPacket implements Packet {
    @Override
    public boolean isPriority() {
        return false;
    }

    @Override
    public String toString() {
        return ObjectUtil.toString(this);
    }
}
