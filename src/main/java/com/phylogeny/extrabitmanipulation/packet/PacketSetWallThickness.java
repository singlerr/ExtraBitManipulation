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

public class PacketSetWallThickness extends PacketInt {

  public static final PacketType<PacketSetWallThickness> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_wall_thickness"), PacketSetWallThickness::new);

  public PacketSetWallThickness(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketSetWallThickness(int wallThickness) {
    super(wallThickness);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetWallThickness> {

    @Override
    public void receive(PacketSetWallThickness packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setWallThickness(player, player.getMainHandItem(),
              packet.value, null);
        }
      });
    }

  }

}