package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.Map;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
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

public class PacketAddBitMapping extends PacketBitMapIO {

  public static final PacketType<PacketAddBitMapping> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "add_bit_mapping"), PacketAddBitMapping::new);

  private BlockState state;
  private IBitBrush bit;

  public PacketAddBitMapping(FriendlyByteBuf buf) {
    super(buf);

    state = BitIOHelper.stateFromBytes(buf);
    if (buf.readBoolean()) {
      bit = null;
      return;
    }
    try {
      bit = ChiselsAndBitsAPIAccess.apiInstance.createBrush(buf.readItem());
    } catch (InvalidBitItem e) {
      bit = null;
    }
  }

  public PacketAddBitMapping(String nbtKey, BlockState state, IBitBrush bit,
                             boolean saveStatesById) {
    super(nbtKey, saveStatesById);
    this.state = state;
    this.bit = bit;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    super.write(buf);
    BitIOHelper.stateToBytes(buf, state);
    boolean removeMapping = bit == null;
    buf.writeBoolean(removeMapping);
    if (!removeMapping) {
      buf.writeItem(bit.getItemStack(1));
    }
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketAddBitMapping> {
    @Override
    public void receive(PacketAddBitMapping packet, ServerPlayer player,
                        PacketSender responseSender) {

      MinecraftServer server = player.level().getServer();
      server.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            Map<BlockState, IBitBrush> bitMapPermanent =
                BitIOHelper.readStateToBitMapFromNBT(ChiselsAndBitsAPIAccess.apiInstance,
                    stack, packet.nbtKey);
            if (packet.bit != null) {
              bitMapPermanent.put(packet.state, packet.bit);
            } else {
              bitMapPermanent.remove(packet.state);
            }
            BitIOHelper.writeStateToBitMapToNBT(stack, packet.nbtKey, bitMapPermanent,
                packet.saveStatesById);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });
    }
  }

}