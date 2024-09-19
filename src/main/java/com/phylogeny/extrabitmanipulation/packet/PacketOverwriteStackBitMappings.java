package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.Map;
import mod.chiselsandbits.api.IBitBrush;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PacketOverwriteStackBitMappings extends PacketBitMapIO {

  public static final PacketType<PacketOverwriteStackBitMappings> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "overwrite_stack_bit_mappings"), PacketOverwriteStackBitMappings::new);

  private Map<BlockState, IBitBrush> bitMap;

  public PacketOverwriteStackBitMappings(FriendlyByteBuf buf) {
    super(buf);
    bitMap = BitIOHelper.stateToBitMapFromBytes(buf);
  }

  public PacketOverwriteStackBitMappings(Map<BlockState, IBitBrush> bitMap, String nbtKey,
                                         boolean saveStatesById) {
    super(nbtKey, saveStatesById);
    this.bitMap = bitMap;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    super.write(buf);
    BitIOHelper.stateToBitMapToBytes(buf, bitMap);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketOverwriteStackBitMappings> {


    @Override
    public void receive(PacketOverwriteStackBitMappings packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            BitIOHelper.writeStateToBitMapToNBT(stack, packet.nbtKey, packet.bitMap,
                packet.saveStatesById);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });

    }
  }

}