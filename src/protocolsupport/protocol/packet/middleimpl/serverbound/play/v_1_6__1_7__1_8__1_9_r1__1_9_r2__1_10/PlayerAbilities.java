package protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_6__1_7__1_8__1_9_r1__1_9_r2__1_10;

import java.io.IOException;

import protocolsupport.protocol.packet.middle.serverbound.play.MiddlePlayerAbilities;
import protocolsupport.protocol.serializer.ProtocolSupportPacketDataSerializer;

public class PlayerAbilities extends MiddlePlayerAbilities {

	@Override
	public void readFromClientData(ProtocolSupportPacketDataSerializer serializer) throws IOException {
		flags = serializer.readUnsignedByte();
		flySpeed = serializer.readFloat();
		walkSpeed = serializer.readFloat();
	}

}
