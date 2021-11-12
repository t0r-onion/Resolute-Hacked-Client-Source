package disabler.mc.protocol.packet.ingame.server;

import disabler.mc.protocol.data.MagicValues;
import disabler.mc.protocol.data.game.setting.Difficulty;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ServerDifficultyPacket extends MinecraftPacket {
    private Difficulty difficulty;

    @SuppressWarnings("unused")
    private ServerDifficultyPacket() {
    }

    public ServerDifficultyPacket(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.difficulty = MagicValues.key(Difficulty.class, in.readUnsignedByte());
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeByte(MagicValues.value(Integer.class, this.difficulty));
    }
}
