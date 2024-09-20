package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketSetWrechMode extends PacketInt {

  public static final PacketType<PacketSetWrechMode> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_wrench_mode"), PacketSetWrechMode::new);

  public PacketSetWrechMode(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketSetWrechMode(int mode) {
    super(mode);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetWrechMode> {

    @Override
    public void receive(PacketSetWrechMode packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isBitWrenchStack(stack)) {
            ((ItemBitWrench) stack.getItem()).initialize(stack);
            ItemStackHelper.getNBT(stack).putInt(NBTKeys.WRENCH_MODE, packet.value);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });
    }

  }

}