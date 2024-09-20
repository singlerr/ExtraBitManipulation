package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketClearStackBitMappings extends PacketEmpty {

  public static final PacketType<PacketClearStackBitMappings> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "clear_stack_bit_mappings"), PacketClearStackBitMappings::new);

  public PacketClearStackBitMappings(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketClearStackBitMappings() {
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketClearStackBitMappings> {

    @Override
    public void receive(PacketClearStackBitMappings packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            BitIOHelper.clearAllBitMapsFromNbt(stack);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });
    }

  }

}