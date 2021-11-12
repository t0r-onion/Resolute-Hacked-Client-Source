package disabler.mc.protocol.packet.ingame.server.window;

import disabler.mc.protocol.data.game.entity.metadata.ItemStack;
import disabler.mc.protocol.packet.MinecraftPacket;
import disabler.mc.protocol.util.NetUtil;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;

import java.io.IOException;

public class ServerWindowItemsPacket extends MinecraftPacket {
    private int windowId;
    private ItemStack items[];

    @SuppressWarnings("unused")
    private ServerWindowItemsPacket() {
    }

    public ServerWindowItemsPacket(int windowId, ItemStack items[]) {
        this.windowId = windowId;
        this.items = items;
    }

    public int getWindowId() {
        return this.windowId;
    }

    public ItemStack[] getItems() {
        return this.items;
    }

    @Override
    public void read(NetInput in) throws IOException {
        this.windowId = in.readUnsignedByte();
        this.items = new ItemStack[in.readShort()];
        for (int index = 0; index < this.items.length; index++) {
            this.items[index] = NetUtil.readItem(in);
        }
    }

    @Override
    public void write(NetOutput out) throws IOException {
        out.writeByte(this.windowId);
        out.writeShort(this.items.length);
        for (ItemStack item : this.items) {
            NetUtil.writeItem(out, item);
        }
    }
}
