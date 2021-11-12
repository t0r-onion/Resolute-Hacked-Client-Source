package disabler.mc.protocol.packet.ingame.client;

import disabler.mc.protocol.data.game.entity.metadata.Position;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.mc.protocol.util.NetUtil;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ClientTabCompletePacket extends MinecraftPacket {
    private String text;
    private boolean assumeCommand;
    private Position lookingAt;

    @SuppressWarnings("unused")
    private ClientTabCompletePacket() {
    }

    public ClientTabCompletePacket(String text, boolean assumeCommand) {
        this(text, assumeCommand, null);
    }

    public ClientTabCompletePacket(String text, boolean assumeCommand, Position lookingAt) {
        this.text = text;
        this.assumeCommand = assumeCommand;
        this.lookingAt = lookingAt;
    }

    public String getText() {
        return this.text;
    }

    public boolean getAssumeCommand() {
        return this.assumeCommand;
    }

    public Position getLookingAt() {
        return this.lookingAt;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.text = in.readString();
        this.assumeCommand = in.readBoolean();
        this.lookingAt = in.readBoolean() ? NetUtil.readPosition(in) : null;
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.text);
        out.writeBoolean(this.assumeCommand);
        out.writeBoolean(this.lookingAt != null);
        if (this.lookingAt != null) {
            NetUtil.writePosition(out, this.lookingAt);
        }
    }
}
