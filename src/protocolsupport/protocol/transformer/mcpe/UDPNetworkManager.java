package protocolsupport.protocol.transformer.mcpe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

import org.apache.commons.io.IOUtils;
import org.spigotmc.SneakyThrow;

import com.google.common.base.Function;

import protocolsupport.protocol.transformer.mcpe.handler.ClientboundPacketHandler;
import protocolsupport.protocol.transformer.mcpe.handler.PELoginListener;
import protocolsupport.protocol.transformer.mcpe.handler.ServerboundPacketHandler;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ClientboundPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.both.BatchPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound.KickPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound.LoginStatusPacket;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.ACK;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.Connection1Reply;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.Connection2Reply;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.EncapsulatedPacket;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.NACK;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.RakNetConstants;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.RakNetDataSerializer;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.RakNetPacket;
import protocolsupport.protocol.transformer.mcpe.packet.raknet.ServerInfoPacket;
import protocolsupport.protocol.transformer.mcpe.pipeline.UDPRouter;
import protocolsupport.protocol.transformer.utils.LegacyUtils;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;

public class UDPNetworkManager extends NetworkManager {

	public static final byte[] defaultSkin = new Function<Object, byte[]>() {
		@Override
		public byte[] apply(Object n) {
			try {
				return IOUtils.toByteArray(UDPNetworkManager.class.getResourceAsStream("SKIN.bin"));
			} catch (Throwable t) {
				SneakyThrow.sneaky(t);
			}
			return null;
		}
	}.apply(null);

	public final static long serverID = 0x0000000012345678L;

	private final ClientboundPacketHandler clientboundTransforner = new ClientboundPacketHandler(this);
	private final ServerboundPacketHandler serverboundTransformer = new ServerboundPacketHandler(this);

	private long lastSentPacket = System.currentTimeMillis();

	private int mtu;

	public UDPNetworkManager(EnumProtocolDirection enumprotocoldirection) {
		super(enumprotocoldirection);
		a(new PELoginListener(this));
		preparing = false;
	}

	@Override
	public void a() {
		if (System.currentTimeMillis() - lastSentPacket > 30000) {
			close(new ChatComponentText("Timed out"));
			return;
		}
		try {
			if (clientboundTransforner.canSpawnPlayer()) {
				clientboundTransforner.setSpawned();
				sendPEPacket(new LoginStatusPacket(LoginStatusPacket.Status.PLAYER_SPAWN));
			}
		} catch (Exception e) {
			if (MinecraftServer.getServer().isDebugging()) {
				e.printStackTrace();
			}
			close(new ChatComponentText(e.getMessage()));
		}
	}

	@Override
	public void close(IChatBaseComponent comp) {
		if (channel.isOpen()) {
			try {
				sendPEPacket(new KickPacket(LegacyUtils.fromComponent(comp)));
				super.close(comp);
			} catch (Throwable e) {
			} finally {
				channel.close();
				UDPRouter.getInstance().remove(getClientAddress());
			}
		}
	}

	@Override
	public boolean g() {
		return channel.isOpen();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void handle(Packet packet) {
		a(packet, null);
	}

	@SuppressWarnings({ "rawtypes" })
	public void a(Packet packet, GenericFutureListener genericfuturelistener, GenericFutureListener... agenericfuturelistener) {
		try {
			for (ClientboundPEPacket pepacket : clientboundTransforner.transform(packet)) {
				sendPEPacket(pepacket);
			}
		} catch (Throwable t) {
			if (MinecraftServer.getServer().isDebugging()) {
				t.printStackTrace();
			}
			close(new ChatComponentText(t.getMessage()));
		}
	}

	private State state = State.NONE;

	@SuppressWarnings("rawtypes")
	public void handleUDP(RakNetPacket raknetpacket) {
		if (!channel.isOpen()) {
			return;
		}
		lastSentPacket = System.currentTimeMillis();
		ByteBuf buf = raknetpacket.getData();
		try {
			switch (raknetpacket.getId()) {
				case RakNetConstants.ID_PING_OPEN_CONNECTIONS: {
					if (state != State.NONE) {
						break;
					}
					state = State.INFO;
					sendRakNetPacket0(new ServerInfoPacket(getClientAddress(), buf.readLong()));
					close(new ChatComponentText(""));
					break;
				}
				case RakNetConstants.ID_OPEN_CONNECTION_REQUEST_1: {
					if (state != State.NONE) {
						break;
					}
					state = State.CONNECTING;
					sendRakNetPacket0(new Connection1Reply(getClientAddress(), ((raknetpacket.getData().readableBytes() + raknetpacket.getData().readerIndex() - 18) & 0xFFFF)));
					break;
				}
				case RakNetConstants.ID_OPEN_CONNECTION_REQUEST_2: {
					if (state != State.CONNECTING) {
						break;
					}
					state = State.CONNECTED;
					buf.skipBytes(16);
					RakNetDataSerializer.readAddress(buf);
					short clientMTU = buf.readShort();
					this.mtu = clientMTU;
					sendRakNetPacket0(new Connection2Reply(getClientAddress(), clientMTU));
					break;
				}
				case RakNetConstants.ACK: {
					break;
				}
				case RakNetConstants.NACK: {
					break;
				}
				default: {
					if (state == State.CONNECTED) {
						raknetpacket.decodeEncapsulated();
						for (Packet nativepacket : serverboundTransformer.transform(raknetpacket)) {
							channelRead0(null, nativepacket);
						}
					}
					break;
				}
			}
		} catch (Throwable e) {
			if (MinecraftServer.getServer().isDebugging()) {
				e.printStackTrace();
			}
			close(new ChatComponentText(e.getMessage()));
		}
	}

	private static enum State {
		NONE,
		CONNECTING,
		CONNECTED,
		INFO
	}

	public void sendPEPacket(ClientboundPEPacket pepacket) throws Exception {
		ByteBuf buf = Unpooled.buffer();
		buf.writeByte(pepacket.getId());
		pepacket.encode(buf);
		if (buf.readableBytes() > 1024 && !(pepacket instanceof BatchPacket)) {
			sendPEPacket(new BatchPacket(pepacket));
			return;
		}
		int maxsize = mtu - 40;
		if (buf.readableBytes() > mtu + 40) {
			EncapsulatedPacket[] epackets = new EncapsulatedPacket[(buf.readableBytes() / maxsize) + 1];
			int splitID = getNextSplitID();
			for (int splitIndex = 0; splitIndex < epackets.length; splitIndex++) {
				epackets[splitIndex] = new EncapsulatedPacket(buf.readBytes(buf.readableBytes() < maxsize ? buf.readableBytes() : maxsize), splitID, epackets.length, splitIndex);
			}
			sendEncapsulatedPackets(epackets);
		} else {
			sendEncapsulatedPackets(new EncapsulatedPacket(buf));
		}
	}

	private void sendEncapsulatedPackets(EncapsulatedPacket... epackets) {
		for (EncapsulatedPacket epacket : epackets) {
			sendRakNetPacket(new RakNetPacket(epacket, getClientAddress()));
		}
	}

	public void sendACK(int seqNumber) {
		ACK ack = new ACK((InetSocketAddress) l);
		ack.id = seqNumber;
		sendRakNetPacket0(ack);
	}

	public void sendNACK(int seqNumberStart, int seqNumberEnd) {
		NACK nack = new NACK((InetSocketAddress) l);
		nack.idstart = seqNumberStart;
		nack.idfinish = seqNumberEnd;
		sendRakNetPacket0(nack);
	}

	public void sendRakNetPacket(RakNetPacket packet) {
		packet.setSeqNumber(getNextRakSeqID());
		sendRakNetPacket0(packet);
	}

	private void sendRakNetPacket0(RakNetPacket packet) {
		channel.writeAndFlush(packet);
	}

	public InetSocketAddress getClientAddress() {
		return (InetSocketAddress) l;
	}

	private int currentSplitID = 0;
	private int getNextSplitID() {
		return currentSplitID++ % Short.MAX_VALUE;
	}

	private int currentRakSeqID = 0;
	private int getNextRakSeqID() {
		return currentRakSeqID++ % Short.MAX_VALUE;
	}

}
