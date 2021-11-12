package disabler.mc.protocol.packet.status.client;

import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class StatusQueryPacket extends MinecraftPacket {
    public StatusQueryPacket() {
    }

    @Override
    public void read(NetInput in) throws IOException {
    }

    @Override
    public void write(NetOutput out) throws IOException {
    }
}
