package disabler.packetlib.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import disabler.packetlib.Session;
import disabler.packetlib.event.session.PacketReceivedEvent;
import disabler.packetlib.io.NetInput;
import disabler.packetlib.io.NetOutput;
import disabler.packetlib.packet.Packet;
import disabler.packetlib.tcp.io.ByteBufNetInput;
import disabler.packetlib.tcp.io.ByteBufNetOutput;

import java.util.List;

public class TcpPacketCodec extends ByteToMessageCodec<Packet> {
    private Session session;

    public TcpPacketCodec(Session session) {
        this.session = session;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) throws Exception {
        try {
            NetOutput out = new ByteBufNetOutput(buf);
            this.session.getPacketProtocol().getPacketHeader().writePacketId(out, this.session.getPacketProtocol().getOutgoingId(packet.getClass()));
            packet.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int initial = buf.readerIndex();
        NetInput in = new ByteBufNetInput(buf);
        int id = this.session.getPacketProtocol().getPacketHeader().readPacketId(in);
        if (id == -1) {
            buf.readerIndex(initial);
            return;
        }

        Packet packet = this.session.getPacketProtocol().createIncomingPacket(id);
        packet.read(in);

        if (buf.readableBytes() > 0) {
            throw new IllegalStateException("Packet \"" + packet.getClass().getSimpleName() + "\" not fully read.");
        }

        if (packet.isPriority()) {
            this.session.callEvent(new PacketReceivedEvent(this.session, packet));
        }

        out.add(packet);
    }
}
