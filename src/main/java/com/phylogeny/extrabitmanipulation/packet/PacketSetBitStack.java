package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBrush;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketSetBitStack implements FabricPacket {

  public static final PacketType<PacketSetBitStack> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_bit_stack"), PacketSetBitStack::new);

  private final boolean isWire;
  private final ItemStack bitStack;

  public PacketSetBitStack(FriendlyByteBuf buffer) {
    isWire = buffer.readBoolean();
    bitStack = buffer.readItem();
  }

  public PacketSetBitStack(boolean isCurved, IBitBrush bit) {
    this.isWire = isCurved;
    this.bitStack = bit == null ? ItemStack.EMPTY : bit.getItemStack(1);
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeBoolean(isWire);
    buf.writeItem(bitStack);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketSetBitStack> {

    @Override
    public void receive(PacketSetBitStack packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          try {
            BitToolSettingsHelper.setBitStack(player, player.getMainHandItem(), packet.isWire,
                ChiselsAndBitsAPIAccess.apiInstance.createBrush(packet.bitStack), null);
          } catch (InvalidBitItem e) {
          }
        }
      });
    }


  }

}