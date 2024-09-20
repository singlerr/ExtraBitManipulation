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

public class PacketSetShapeType implements FabricPacket {

  public static final PacketType<PacketSetShapeType> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_shape_type"), PacketSetShapeType::new);

  private final boolean isCurved;
  private final int shapeType;

  public PacketSetShapeType(FriendlyByteBuf buf) {
    isCurved = buf.readBoolean();
    shapeType = buf.readInt();
  }

  public PacketSetShapeType(boolean isCurved, int shapeType) {
    this.isCurved = isCurved;
    this.shapeType = shapeType;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeBoolean(isCurved);
    buf.writeInt(shapeType);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetShapeType> {

    @Override
    public void receive(PacketSetShapeType packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setShapeType(player, player.getMainHandItem(), packet.isCurved,
              packet.shapeType, null);
        }
      });
    }


  }

}