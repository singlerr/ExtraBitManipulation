package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.GuiIDs;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketOpenBitMappingGui extends PacketEmpty {

  public static final PacketType<PacketOpenBitMappingGui> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "open_bit_mapping_gui"), PacketOpenBitMappingGui::new);

  public PacketOpenBitMappingGui(FriendlyByteBuf buffer) {
    super(buffer);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketOpenBitMappingGui> {

    @Override
    public void receive(PacketOpenBitMappingGui packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack) || ItemStackHelper.isDesignStack(stack)) {
            TODO("Implement menu provider")
            player.openMenu(ExtraBitManipulation.instance, GuiIDs.BIT_MAPPING.getID(),
                player.level(),
                0, 0, 0);

          }
        }
      });
    }
  }

}