package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketCursorStack implements FabricPacket {

  public static final PacketType<PacketCursorStack> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "cursor_stack"), PacketCursorStack::new);

  private final ItemStack stack;

  public PacketCursorStack(FriendlyByteBuf buf) {
    stack = buf.readItem();
  }

  public PacketCursorStack(ItemStack stack) {
    this.stack = stack;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeItem(stack);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketCursorStack> {

    @Override
    public void receive(PacketCursorStack packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          if (player.isCreative()) {
            player.inventoryMenu.setCarried(packet.stack);
          }
        }
      });
    }

  }

}