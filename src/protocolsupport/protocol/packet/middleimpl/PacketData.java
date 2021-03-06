package protocolsupport.protocol.packet.middleimpl;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.util.Recycler;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.serializer.ProtocolSupportPacketDataSerializer;
import protocolsupport.utils.netty.Allocator;
import protocolsupport.utils.recyclable.Recyclable;

public class PacketData extends ProtocolSupportPacketDataSerializer implements Recyclable {

	private static final Recycler<PacketData> RECYCLER = new Recycler<PacketData>() {
		@Override
		protected PacketData newObject(Recycler.Handle handle) {
			return new PacketData(handle);
		}
	};

	public static PacketData create(int packetId, ProtocolVersion version) {
		PacketData packetdata = RECYCLER.get();
		packetdata.packetId = packetId;
		packetdata.version = version;
		return packetdata;
	}

	private final Recycler.Handle handle;
	private PacketData(Recycler.Handle handle) {
		super(Allocator.allocateUnpooledBuffer(), null);
		this.handle = handle;
	}

	@Override
	public void recycle() {
		packetId = 0;
		clear();
		RECYCLER.recycle(this, handle);
	}

	private int packetId;

	public int getPacketId() {
		return packetId;
	}

	@Override
	public void setBuf(ByteBuf buf) {
	}

	@Override
	protected void finalize() {
		release();
	}

	@Override
	public void close() throws IOException {
		recycle();
	}

}
