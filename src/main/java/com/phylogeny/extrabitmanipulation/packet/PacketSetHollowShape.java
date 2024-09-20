package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketSetHollowShape implements FabricPacket {

  public static final PacketType<PacketSetHollowShape> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_hollow_shape"), PacketSetHollowShape::new);

  private final boolean isWire;
  private final boolean hollowShape;

  public PacketSetHollowShape(FriendlyByteBuf buf) {
    hollowShape = buf.readBoolean();
    isWire = buf.readBoolean();
  }

  public PacketSetHollowShape(boolean hollowShape, boolean isWire) {
    this.hollowShape = hollowShape;
    this.isWire = isWire;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeBoolean(hollowShape);
    buf.writeBoolean(isWire);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketSetHollowShape> {

    @Override
    public void receive(PacketSetHollowShape packet, ServerPlayer player,
                        PacketSender responseSender) {

      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setHollowShape(player, player.getMainHandItem(), packet.isWire,
              packet.hollowShape, null);
        }
      });

    }

  }

}