package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.ArmorItem;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.container.ContainerPlayerInventory;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PacketChangeArmorItemList extends PacketChangeChiseledArmorList {

  public static final PacketType<PacketChangeArmorItemList> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "change_armor_item_list"), PacketChangeArmorItemList::new);

  private final ItemStack stack;
  private final ListOperation listOperation;

  public PacketChangeArmorItemList(FriendlyByteBuf buffer) {
    super(buffer);
    listOperation = ListOperation.values()[buffer.readInt()];
    stack = buffer.readItem();
  }

  public PacketChangeArmorItemList(ArmorType armorType, int indexArmorSet, int partIndex,
                                   int armorItemIndex,
                                   int selectedEntry, ListOperation listOperation, ItemStack stack,
                                   CompoundTag glOperationsNbt, boolean refreshLists,
                                   Player player) {
    super(glOperationsNbt, armorType, indexArmorSet, partIndex, armorItemIndex, selectedEntry,
        refreshLists, player);
    this.listOperation = listOperation;
    this.stack = stack;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    buffer.writeInt(listOperation.ordinal());
    buffer.writeItem(stack);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class ClientHandler
      implements ClientPlayNetworking.PlayPacketHandler<PacketChangeArmorItemList> {

    @Override
    public void receive(PacketChangeArmorItemList message, LocalPlayer player,
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
          ListTag movingParts = data.getList(NBTKeys.ARMOR_PART_DATA, ListTag.TAG_LIST);
          Tag nbtBase = movingParts.get(message.value);
          if (nbtBase.getId() != ListTag.TAG_LIST) {
            return;
          }

          Container container = player.openContainer;
          if (container == null || !(container instanceof ContainerPlayerInventory)) {
            return;
          }

          ListTag itemList = (ListTag) nbtBase;
          int glListRemovalIndex = -1;
          boolean add = message.listOperation == ListOperation.ADD;
          if (message.listOperation == ListOperation.MODIFY) {
            CompoundTag armorItemNbt = itemList.getCompound(message.armorItemIndex);
            ItemStackHelper.saveStackToNBT(armorItemNbt, message.stack, NBTKeys.ARMOR_ITEM);
            itemList.set(message.armorItemIndex, armorItemNbt);
          } else if (add) {
            CompoundTag armorItemNbt = new CompoundTag();
            ArmorItem armorItem = new ArmorItem(message.stack);
            armorItem.saveToNBT(armorItemNbt);
            if (message.nbt.contains(NBTKeys.ARMOR_GL_OPERATIONS)) {
              armorItemNbt.put(NBTKeys.ARMOR_GL_OPERATIONS,
                  message.nbt.getList(NBTKeys.ARMOR_GL_OPERATIONS, ListTag.TAG_COMPOUND));
            }

            itemList.add(armorItemNbt);
          } else {
            itemList.remove(message.armorItemIndex);
            glListRemovalIndex = message.armorItemIndex;
          }
          movingParts.set(message.value, itemList);
          DataChiseledArmorPiece.setPartData(data, movingParts);
          message.finalizeDataChange(message, stack, nbt, data, false, true, add,
              glListRemovalIndex);
        }
      });

    }
  }

  public static class ServerHandler implements
      ServerPlayNetworking.PlayPacketHandler<PacketChangeArmorItemList> {

    @Override
    public void receive(PacketChangeArmorItemList message, ServerPlayer player,
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
          ListTag movingParts = data.getList(NBTKeys.ARMOR_PART_DATA, ListTag.TAG_LIST);
          Tag nbtBase = movingParts.get(message.value);
          if (nbtBase.getId() != ListTag.TAG_LIST) {
            return;
          }

          var container = player.containerMenu;
          if (container == null || !(container instanceof ContainerPlayerInventory)) {
            return;
          }

          ListTag itemList = (ListTag) nbtBase;
          int glListRemovalIndex = -1;
          boolean add = message.listOperation == ListOperation.ADD;
          if (message.listOperation == ListOperation.MODIFY) {
            CompoundTag armorItemNbt = itemList.getCompound(message.armorItemIndex);
            ItemStackHelper.saveStackToNBT(armorItemNbt, message.stack, NBTKeys.ARMOR_ITEM);
            itemList.set(message.armorItemIndex, armorItemNbt);
          } else if (add) {
            CompoundTag armorItemNbt = new CompoundTag();
            ArmorItem armorItem = new ArmorItem(message.stack);
            armorItem.saveToNBT(armorItemNbt);
            if (message.nbt.contains(NBTKeys.ARMOR_GL_OPERATIONS)) {
              armorItemNbt.put(NBTKeys.ARMOR_GL_OPERATIONS,
                  message.nbt.getList(NBTKeys.ARMOR_GL_OPERATIONS, ListTag.TAG_COMPOUND));
            }

            itemList.add(armorItemNbt);
          } else {
            itemList.remove(message.armorItemIndex);
            glListRemovalIndex = message.armorItemIndex;
          }
          movingParts.set(message.value, itemList);
          DataChiseledArmorPiece.setPartData(data, movingParts);
          message.finalizeDataChange(message, stack, nbt, data, true, true, add,
              glListRemovalIndex);
          ExtraBitManipulation.packetNetwork.sendTo(
              new PacketChangeArmorItemList(message.armorType, message.indexArmorSet,
                  message.value, message.armorItemIndex, message.selectedEntry,
                  message.listOperation,
                  message.stack, message.nbt, message.refreshLists, player),
              (ServerPlayer) player);
        }
      });

    }
  }

  public enum ListOperation {
    ADD, REMOVE, MODIFY
  }

}