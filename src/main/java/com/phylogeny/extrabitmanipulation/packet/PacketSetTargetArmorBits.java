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

public class PacketSetTargetArmorBits extends PacketBoolean {

  public static final PacketType<PacketSetTargetArmorBits> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_target_armor_bits"), PacketSetTargetArmorBits::new);

  public PacketSetTargetArmorBits(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetTargetArmorBits(boolean targetBits) {
    super(targetBits);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetTargetArmorBits> {

    @Override
    public void receive(PacketSetTargetArmorBits packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          BitToolSettingsHelper.setArmorBitsTargeted(player, player.getMainHandItem(),
              packet.value, null);
        }
      });
    }

  }

}