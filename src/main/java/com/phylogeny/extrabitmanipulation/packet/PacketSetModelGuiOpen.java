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

public class PacketSetModelGuiOpen extends PacketBoolean {

  public static final PacketType<PacketSetModelGuiOpen> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_model_gui_open"), PacketSetModelGuiOpen::new);

  public PacketSetModelGuiOpen(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetModelGuiOpen(boolean openGui) {
    super(openGui);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetModelGuiOpen> {

    @Override
    public void receive(PacketSetModelGuiOpen packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setModelGuiOpen(player, player.getMainHandItem(), packet.value,
              null);
        }
      });
    }

  }

}