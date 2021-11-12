package disabler.mc.protocol.packet.ingame.client;

import disabler.mc.protocol.data.MagicValues;
import disabler.mc.protocol.data.game.ResourcePackStatus;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ClientResourcePackStatusPacket extends MinecraftPacket {
    private ResourcePackStatus status;

    @SuppressWarnings("unused")
    private ClientResourcePackStatusPacket() {
    }

    public ClientResourcePackStatusPacket(ResourcePackStatus status) {
        this.status = status;
    }

    public ResourcePackStatus getStatus() {
        return this.status;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.status = MagicValues.key(ResourcePackStatus.class, in.readVarInt());
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.status));
    }
}
