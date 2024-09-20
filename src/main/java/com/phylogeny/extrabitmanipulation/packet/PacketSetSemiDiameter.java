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

public class PacketSetSemiDiameter extends PacketInt {

  public static final PacketType<PacketSetSemiDiameter> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_semi_diameter"), PacketSetSemiDiameter::new);

  public PacketSetSemiDiameter(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketSetSemiDiameter(int semiDiameter) {
    super(semiDiameter);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetSemiDiameter> {

    @Override
    public void receive(PacketSetSemiDiameter packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setSemiDiameter(player, player.getMainHandItem(), packet.value,
              null);
        }
      });
    }


  }

}