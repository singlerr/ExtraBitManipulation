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

public class PacketSetArmorMode extends PacketInt {

  public static final PacketType<PacketSetArmorMode> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_armor_mode"), PacketSetArmorMode::new);

  public PacketSetArmorMode(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetArmorMode(int mode) {
    super(mode);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetArmorMode> {

    @Override
    public void receive(PacketSetArmorMode packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setArmorMode(player, player.getMainHandItem(), packet.value,
              null);
        }
      });
    }


  }

}