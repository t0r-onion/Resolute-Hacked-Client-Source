package disabler.mc.protocol.packet.ingame.server.world;

import disabler.mc.protocol.data.game.entity.metadata.Position;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.mc.protocol.util.NetUtil;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ServerSpawnPositionPacket extends MinecraftPacket {
    private Position position;

    @SuppressWarnings("unused")
    private ServerSpawnPositionPacket() {
    }

    public ServerSpawnPositionPacket(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.position = NetUtil.readPosition(in);
    }

    @Override
    public void write(NetOutput out) throws IOException {
        NetUtil.writePosition(out, this.position);
    }
}
