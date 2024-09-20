package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketSetTargetBitGridVertexes extends PacketBoolean {

  public static final PacketType<PacketSetTargetBitGridVertexes> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_target_bit_grid_vertexes"), PacketSetTargetBitGridVertexes::new);

  public PacketSetTargetBitGridVertexes(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetTargetBitGridVertexes(boolean targetBitGridVertexes) {
    super(targetBitGridVertexes);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetTargetBitGridVertexes> {

    @Override
    public void receive(PacketSetTargetBitGridVertexes packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setBitGridTargeted(player, player.getMainHandItem(),
              packet.value, null);
        }
      });
    }
  }

}