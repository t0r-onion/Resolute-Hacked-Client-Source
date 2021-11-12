package disabler.mc.protocol.packet.ingame.client.player;

import disabler.mc.protocol.data.MagicValues;
import disabler.mc.protocol.data.game.entity.player.Hand;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ClientPlayerUseItemPacket extends MinecraftPacket {
    private Hand hand;

    @SuppressWarnings("unused")
    private ClientPlayerUseItemPacket() {
    }

    public ClientPlayerUseItemPacket(Hand hand) {
        this.hand = hand;
    }

    public Hand getHand() {
        return this.hand;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.hand = MagicValues.key(Hand.class, in.readVarInt());
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeVarInt(MagicValues.value(Integer.class, this.hand));
    }
}
