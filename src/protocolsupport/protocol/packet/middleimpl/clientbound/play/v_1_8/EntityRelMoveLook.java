package protocolsupport.protocol.packet.middleimpl.clientbound.play.v_1_8;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.legacyremapper.LegacyRelMoveConverter;
import protocolsupport.protocol.legacyremapper.LegacyRelMoveConverter.RelMove;
import protocolsupport.protocol.packet.ClientBoundPacket;
import protocolsupport.protocol.packet.middle.clientbound.play.MiddleEntityRelMoveLook;
import protocolsupport.protocol.packet.middleimpl.PacketData;
import protocolsupport.utils.recyclable.RecyclableArrayList;
import protocolsupport.utils.recyclable.RecyclableCollection;

public class EntityRelMoveLook extends MiddleEntityRelMoveLook<RecyclableCollection<PacketData>> {

	@Override
	public RecyclableCollection<PacketData> toData(ProtocolVersion version) {
		int relMoveX = relX / 128;
		int relMoveY = relY / 128;
		int relMoveZ = relZ / 128;
		RecyclableArrayList<PacketData> packets = RecyclableArrayList.create();
		for (RelMove relMove : LegacyRelMoveConverter.getRelMoves(new RelMove(relMoveX, relMoveY, relMoveZ), 127)) {
			PacketData serializer = PacketData.create(ClientBoundPacket.PLAY_ENTITY_REL_MOVE_LOOK_ID, version);
			serializer.writeVarInt(entityId);
			serializer.writeByte(relMove.getX());
			serializer.writeByte(relMove.getY());
			serializer.writeByte(relMove.getZ());
			serializer.writeByte(yaw);
			serializer.writeByte(pitch);
			serializer.writeBoolean(onGround);
			packets.add(serializer);
		}
		return packets;
	}

}
