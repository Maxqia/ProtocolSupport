package protocolsupport.protocol.pipeline.version.v_1_8;

import java.util.List;

import org.spigotmc.SneakyThrow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.server.v1_10_R1.EnumProtocol;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.legacyremapper.LegacyAnimatePacketReorderer;
import protocolsupport.protocol.packet.middle.ServerBoundMiddlePacket;
import protocolsupport.protocol.packet.middleimpl.PacketCreator;
import protocolsupport.protocol.packet.middleimpl.serverbound.handshake.v_1_7__1_8__1_9_r1__1_9_r2__1_10.SetProtocol;
import protocolsupport.protocol.packet.middleimpl.serverbound.login.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.EncryptionResponse;
import protocolsupport.protocol.packet.middleimpl.serverbound.login.v_1_7__1_8__1_9_r1__1_9_r2__1_10.LoginStart;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.Chat;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.CreativeSetSlot;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.Flying;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.HeldSlot;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.InventoryClick;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.InventoryClose;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.InventoryEnchant;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.InventoryTransaction;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_4__1_5__1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.Look;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_6__1_7__1_8__1_9_r1__1_9_r2__1_10.PlayerAbilities;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_7__1_8__1_9_r1__1_9_r2__1_10.ClientCommand;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8.Animation;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8.BlockPlace;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8.ClientSettings;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8.TabComplete;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8.UseEntity;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2.ResourcePackStatus;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.BlockDig;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.CustomPayload;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.EntityAction;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.KeepAlive;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.Position;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.PositionLook;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.Spectate;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.SteerVehicle;
import protocolsupport.protocol.packet.middleimpl.serverbound.play.v_1_8__1_9_r1__1_9_r2__1_10.UpdateSign;
import protocolsupport.protocol.packet.middleimpl.serverbound.status.v_1_7__1_8__1_9_r1__1_9_r2__1_10.Ping;
import protocolsupport.protocol.packet.middleimpl.serverbound.status.v_1_7__1_8__1_9_r1__1_9_r2__1_10.ServerInfoRequest;
import protocolsupport.protocol.pipeline.IPacketDecoder;
import protocolsupport.protocol.serializer.ProtocolSupportPacketDataSerializer;
import protocolsupport.protocol.storage.SharedStorage;
import protocolsupport.protocol.utils.registry.MiddleTransformerRegistry;
import protocolsupport.protocol.utils.registry.MiddleTransformerRegistry.InitCallBack;
import protocolsupport.utils.netty.ChannelUtils;

public class PacketDecoder implements IPacketDecoder {

	private final MiddleTransformerRegistry<ServerBoundMiddlePacket> registry = new MiddleTransformerRegistry<>();
	{
		try {
			registry.register(EnumProtocol.HANDSHAKING, 0x00, SetProtocol.class);
			registry.register(EnumProtocol.LOGIN, 0x00, LoginStart.class);
			registry.register(EnumProtocol.LOGIN, 0x01, EncryptionResponse.class);
			registry.register(EnumProtocol.STATUS, 0x00, ServerInfoRequest.class);
			registry.register(EnumProtocol.STATUS, 0x01, Ping.class);
			registry.register(EnumProtocol.PLAY, 0x00, KeepAlive.class);
			registry.register(EnumProtocol.PLAY, 0x01, Chat.class);
			registry.register(EnumProtocol.PLAY, 0x02, UseEntity.class);
			registry.register(EnumProtocol.PLAY, 0x03, Flying.class);
			registry.register(EnumProtocol.PLAY, 0x04, Position.class);
			registry.register(EnumProtocol.PLAY, 0x05, Look.class);
			registry.register(EnumProtocol.PLAY, 0x06, PositionLook.class);
			registry.register(EnumProtocol.PLAY, 0x07, BlockDig.class);
			registry.register(EnumProtocol.PLAY, 0x08, BlockPlace.class);
			registry.register(EnumProtocol.PLAY, 0x09, HeldSlot.class);
			registry.register(EnumProtocol.PLAY, 0x0A, Animation.class);
			registry.register(EnumProtocol.PLAY, 0x0B, EntityAction.class);
			registry.register(EnumProtocol.PLAY, 0x0C, SteerVehicle.class);
			registry.register(EnumProtocol.PLAY, 0x0D, InventoryClose.class);
			registry.register(EnumProtocol.PLAY, 0x0E, InventoryClick.class);
			registry.register(EnumProtocol.PLAY, 0x0F, InventoryTransaction.class);
			registry.register(EnumProtocol.PLAY, 0x10, CreativeSetSlot.class);
			registry.register(EnumProtocol.PLAY, 0x11, InventoryEnchant.class);
			registry.register(EnumProtocol.PLAY, 0x12, UpdateSign.class);
			registry.register(EnumProtocol.PLAY, 0x13, PlayerAbilities.class);
			registry.register(EnumProtocol.PLAY, 0x14, TabComplete.class);
			registry.register(EnumProtocol.PLAY, 0x15, ClientSettings.class);
			registry.register(EnumProtocol.PLAY, 0x16, ClientCommand.class);
			registry.register(EnumProtocol.PLAY, 0x17, CustomPayload.class);
			registry.register(EnumProtocol.PLAY, 0x18, Spectate.class);
			registry.register(EnumProtocol.PLAY, 0x19, ResourcePackStatus.class);
			registry.setCallBack(new InitCallBack<ServerBoundMiddlePacket>() {
				@Override
				public void onInit(ServerBoundMiddlePacket object) {
					object.setSharedStorage(sharedstorage);
				}
			});
		} catch (Throwable t) {
			SneakyThrow.sneaky(t);
		}
	}

	protected final SharedStorage sharedstorage;
	private final ProtocolSupportPacketDataSerializer serializer = new ProtocolSupportPacketDataSerializer(null, ProtocolVersion.MINECRAFT_1_8);
	public PacketDecoder(SharedStorage sharedstorage) {
		this.sharedstorage = sharedstorage;
	}

	private final LegacyAnimatePacketReorderer reorderer = new LegacyAnimatePacketReorderer();

	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> list) throws Exception {
		if (!input.isReadable()) {
			return;
		}
		serializer.setBuf(input);
		int packetId = serializer.readVarInt();
		Channel channel = ctx.channel();
		ServerBoundMiddlePacket packetTransformer = registry.getTransformer(channel.attr(ChannelUtils.CURRENT_PROTOCOL_KEY).get(), packetId);
		if (packetTransformer.needsPlayer()) {
			packetTransformer.setPlayer(ChannelUtils.getBukkitPlayer(channel));
		}
		packetTransformer.readFromClientData(serializer);
		PacketCreator.addAllTo(reorderer.orderPackets(packetTransformer.toNative()), list);
		if (serializer.isReadable()) {
			throw new DecoderException("Did not read all data from packet " + packetId+ ", bytes left: " + serializer.readableBytes());
		}
	}

}
