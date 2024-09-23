package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketBitMappingsPerTool extends PacketBoolean {

  public static final PacketType<PacketBitMappingsPerTool> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "bit_mappings_per_tool"), PacketBitMappingsPerTool::new);

  public PacketBitMappingsPerTool(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketBitMappingsPerTool(boolean perTool) {
    super(perTool);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketBitMappingsPerTool> {
    @Override
    public void receive(PacketBitMappingsPerTool packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            CompoundTag nbt = ItemStackHelper.getNBT(stack);
            nbt.putBoolean(NBTKeys.BIT_MAPS_PER_TOOL, packet.value);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });
    }

  }

}