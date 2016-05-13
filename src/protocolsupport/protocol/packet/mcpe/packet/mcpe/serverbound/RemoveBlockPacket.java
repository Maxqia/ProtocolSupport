package protocolsupport.protocol.packet.mcpe.packet.mcpe.serverbound;

import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.List;

import protocolsupport.protocol.packet.ServerBoundPacket;
import protocolsupport.protocol.packet.mcpe.packet.mcpe.PEPacketIDs;
import protocolsupport.protocol.packet.mcpe.packet.mcpe.ServerboundPEPacket;
import protocolsupport.protocol.packet.middleimpl.PacketCreator;
import protocolsupport.protocol.storage.SharedStorage;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Packet;

public class RemoveBlockPacket implements ServerboundPEPacket {

	protected int x;
	protected int z;
	protected int y;

	@Override
	public int getId() {
		return PEPacketIDs.REMOVE_BLOCK_PACKET;
	}

	@Override
	public ServerboundPEPacket decode(ByteBuf buf) throws Exception {
		buf.readLong();
		this.x = buf.readInt();
		this.z = buf.readInt();
		this.y = buf.readByte();
		return this;
	}

	@Override
	public List<? extends Packet<?>> transfrom(SharedStorage storage) throws Exception {
		PacketCreator creator = PacketCreator.create(ServerBoundPacket.PLAY_BLOCK_DIG.get());
		creator.writeByte(2);
		creator.a(new BlockPosition(x, y, z));
		creator.writeByte(1);
		return Collections.singletonList(creator.create());
	}

}