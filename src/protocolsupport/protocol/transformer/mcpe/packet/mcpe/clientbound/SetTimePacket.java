package protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound;

import io.netty.buffer.ByteBuf;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ClientboundPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.PEPacketIDs;

public class SetTimePacket implements ClientboundPEPacket {

	protected int time;

	public SetTimePacket(int time) {
		this.time = time;
	}

	@Override
	public int getId() {
		return PEPacketIDs.SET_TIME_PACKET;
	}

	@Override
	public ClientboundPEPacket encode(ByteBuf buf) throws Exception {
		buf.writeInt(time);
		buf.writeBoolean(true);
		return this;
	}

}
