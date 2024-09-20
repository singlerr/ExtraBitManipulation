package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PacketChangeGlOperationList extends PacketChangeChiseledArmorList {

  public static final PacketType<PacketChangeGlOperationList> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "change_gl_operation_list"), PacketChangeGlOperationList::new);

  private final String nbtKey;

  public PacketChangeGlOperationList(FriendlyByteBuf buffer) {
    super(buffer);
    nbtKey = buffer.readUtf();
  }

  public PacketChangeGlOperationList(CompoundTag nbt, String nbtKey, ArmorType armorType,
                                     int indexArmorSet, int partIndex, int armorItemIndex,
                                     int selectedEntry, boolean refreshLists, Player player) {
    super(nbt, armorType, indexArmorSet, partIndex, armorItemIndex, selectedEntry, refreshLists,
        player);
    this.nbtKey = nbtKey;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    buffer.writeUtf(nbtKey);
  }


  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class ClientHandler implements
      ClientPlayNetworking.PlayPacketHandler<PacketChangeGlOperationList> {

    @Override
    public void receive(PacketChangeGlOperationList message, LocalPlayer player,
                        PacketSender responseSender) {
      var mainThread = ClientHelper.getThreadListener();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          Player player = ClientHelper.getPlayer();
          ItemStack stack = ItemStackHelper.getChiseledArmorStack(player, message.armorType,
              message.indexArmorSet);
          if (!ItemStackHelper.isChiseledArmorStack(stack)) {
            return;
          }

          message.initData(message, stack);
          CompoundTag nbt = ItemStackHelper.getNBT(stack);
          CompoundTag data = message.getData(nbt, false);
          ListTag glOperationList = message.nbt.getList(message.nbtKey, ListTag.TAG_COMPOUND);
          if (message.nbtKey.equals(NBTKeys.ARMOR_GL_OPERATIONS)) {
            ListTag movingParts = data.getList(NBTKeys.ARMOR_PART_DATA, ListTag.TAG_LIST);
            Tag nbtBase = movingParts.get(message.value);
            if (nbtBase.getId() != ListTag.TAG_LIST) {
              return;
            }

            ListTag itemList = (ListTag) nbtBase;
            CompoundTag armorItemNbt = itemList.getCompound(message.armorItemIndex);
            armorItemNbt.put(message.nbtKey, glOperationList);
            itemList.set(message.armorItemIndex, armorItemNbt);
            data.put(NBTKeys.ARMOR_PART_DATA, movingParts);
          } else {
            data.put(message.nbtKey, glOperationList);
          }
          message.finalizeDataChange(message, stack, nbt, data, false, false, false, -1);
        }
      });

    }


  }


  public static class ServerHandler
      implements ServerPlayNetworking.PlayPacketHandler<PacketChangeGlOperationList> {

    @Override
    public void receive(PacketChangeGlOperationList message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = ItemStackHelper.getChiseledArmorStack(player, message.armorType,
              message.indexArmorSet);
          if (!ItemStackHelper.isChiseledArmorStack(stack)) {
            return;
          }

          message.initData(message, stack);
          CompoundTag nbt = ItemStackHelper.getNBT(stack);
          CompoundTag data = message.getData(nbt, true);
          ListTag glOperationList = message.nbt.getList(message.nbtKey, ListTag.TAG_COMPOUND);
          if (message.nbtKey.equals(NBTKeys.ARMOR_GL_OPERATIONS)) {
            ListTag movingParts = data.getList(NBTKeys.ARMOR_PART_DATA, ListTag.TAG_LIST);
            Tag nbtBase = movingParts.get(message.value);
            if (nbtBase.getId() != ListTag.TAG_LIST) {
              return;
            }

            ListTag itemList = (ListTag) nbtBase;
            CompoundTag armorItemNbt = itemList.getCompound(message.armorItemIndex);
            armorItemNbt.put(message.nbtKey, glOperationList);
            itemList.set(message.armorItemIndex, armorItemNbt);
            data.put(NBTKeys.ARMOR_PART_DATA, movingParts);
          } else {
            data.put(message.nbtKey, glOperationList);
          }
          message.finalizeDataChange(message, stack, nbt, data, true, false, false, -1);
          ExtraBitManipulation.packetNetwork.sendTo(
              new PacketChangeGlOperationList(message.nbt, message.nbtKey,
                  message.armorType, message.indexArmorSet, message.value, message.armorItemIndex,
                  message.selectedEntry, message.refreshLists, player), player);
        }
      });

    }
  }

}