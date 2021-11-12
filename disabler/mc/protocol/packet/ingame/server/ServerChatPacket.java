package disabler.mc.protocol.packet.ingame.server;

import disabler.mc.protocol.data.MagicValues;
import disabler.mc.protocol.data.game.MessageType;
import disabler.mc.protocol.data.message.Message;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ServerChatPacket extends MinecraftPacket {
    private Message message;
    private MessageType type;

    @SuppressWarnings("unused")
    private ServerChatPacket() {
    }

    public ServerChatPacket(String text) {
        this(Message.fromString(text));
    }

    public ServerChatPacket(Message message) {
        this(message, MessageType.SYSTEM);
    }

    public ServerChatPacket(String text, MessageType type) {
        this(Message.fromString(text), type);
    }

    public ServerChatPacket(Message message, MessageType type) {
        this.message = message;
        this.type = type;
    }

    public Message getMessage() {
        return this.message;
    }

    public MessageType getType() {
        return this.type;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.message = Message.fromString(in.readString());
        this.type = MagicValues.key(MessageType.class, in.readByte());
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeString(this.message.toJsonString());
        out.writeByte(MagicValues.value(Integer.class, this.type));
    }
}
