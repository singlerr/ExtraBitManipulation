package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketCycleBitWrenchMode extends PacketBoolean {

  public static final PacketType<PacketCycleBitWrenchMode> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "cycle_bit_wrench_mode"), PacketCycleBitWrenchMode::new);

  public PacketCycleBitWrenchMode(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketCycleBitWrenchMode(boolean forward) {
    super(forward);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketCycleBitWrenchMode> {

    @Override
    public void receive(PacketCycleBitWrenchMode packet, ServerPlayer player,
                        PacketSender responseSender) {

      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isBitWrenchStack(stack)) {
            ((ItemBitWrench) stack.getItem()).cycleModes(stack, packet.value);
          }
        }
      });

    }

  }

}