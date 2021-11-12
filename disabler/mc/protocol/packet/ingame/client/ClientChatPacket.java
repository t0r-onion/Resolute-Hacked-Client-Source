package disabler.mc.protocol.packet.ingame.client;

import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ClientChatPacket extends MinecraftPacket {
    private String message;

    @SuppressWarnings("unused")
    private ClientChatPacket() {
    }

    public ClientChatPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.message = in.readString();
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.message);
    }
}
