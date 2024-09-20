package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorCollectionData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
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

public class PacketCollectArmorBlocks implements FabricPacket {

  public static final PacketType<PacketCollectArmorBlocks> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "collect_armor_blocks"), PacketCollectArmorBlocks::new);

  private ArmorCollectionData collectionData = new ArmorCollectionData();

  public PacketCollectArmorBlocks(FriendlyByteBuf buf) {
    collectionData.fromBytes(buf);
  }

  public PacketCollectArmorBlocks(ArmorCollectionData collectionData) {
    this.collectionData = collectionData;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    collectionData.toBytes(buf);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketCollectArmorBlocks> {

    @Override
    public void receive(PacketCollectArmorBlocks packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isChiseledArmorStack(stack)) {
            ItemChiseledArmor.collectArmorBlocks(player, packet.collectionData);
          }
        }
      });

    }

  }

}