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

public class PacketSetEndsOpen extends PacketBoolean {

  public static final PacketType<PacketSetEndsOpen> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_ends_open"), PacketSetEndsOpen::new);

  public PacketSetEndsOpen(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetEndsOpen(boolean openEnds) {
    super(openEnds);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketSetEndsOpen> {
    @Override
    public void receive(PacketSetEndsOpen packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setEndsOpen(player, player.getMainHandItem(), packet.value,
              null);
        }
      });
    }


  }

}