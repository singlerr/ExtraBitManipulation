package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketSetArmorMovingPart extends PacketArmorSlotInt {

  public static final PacketType<PacketSetArmorMovingPart> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_armor_moving_part"), PacketSetArmorMovingPart::new);

  public PacketSetArmorMovingPart(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketSetArmorMovingPart(int partIndex, @Nullable ArmorType armorType, int indexArmorSet) {
    super(armorType, indexArmorSet, partIndex);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetArmorMovingPart> {

    @Override
    public void receive(PacketSetArmorMovingPart message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = getArmorStack(player, message);
          if (!stack.isEmpty()) {
            BitToolSettingsHelper.setArmorMovingPart(player, stack, message.value, null,
                message.armorType, 0);
          }
        }
      });
    }
  }

}