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

public class PacketSetModelAreaMode extends PacketInt {

  public static final PacketType<PacketSetModelAreaMode> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_model_area_mode"), PacketSetModelAreaMode::new);

  public PacketSetModelAreaMode(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetModelAreaMode(int mode) {
    super(mode);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketSetModelAreaMode> {

    @Override
    public void receive(PacketSetModelAreaMode packet, ServerPlayer player,
                        PacketSender responseSender) {

      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setModelAreaMode(player, player.getMainHandItem(),
              packet.value, null);
        }
      });
    }

  }

}