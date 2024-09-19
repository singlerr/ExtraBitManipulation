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
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketChangeArmorItemList extends PacketChangeChiseledArmorList {

  public static final PacketType<PacketChangeArmorItemList> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "change_armor_item_list"), PacketChangeArmorItemList::new);

  private ItemStack stack;
  private ListOperation listOperation;

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

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketChangeArmorItemList> {


    @Override
    public IMessage onMessage(final PacketChangeArmorItemList message, final MessageContext ctx) {
      final boolean serverSide = ctx.side == Side.SERVER;
      IThreadListener mainThread = serverSide ? (WorldServer) ctx.getServerHandler().player.world :
          ClientHelper.getThreadListener();
      mainThread.addScheduledTask(new Runnable() {
        @Override
        public void run() {
          EntityPlayer player =
              serverSide ? ctx.getServerHandler().player : ClientHelper.getPlayer();
          ItemStack stack = ItemStackHelper.getChiseledArmorStack(player, message.armorType,
              message.indexArmorSet);
          if (!ItemStackHelper.isChiseledArmorStack(stack)) {
            return;
          }

          message.initData(message, stack);
          NBTTagCompound nbt = ItemStackHelper.getNBT(stack);
          NBTTagCompound data = message.getData(nbt, serverSide);
          NBTTagList movingParts = data.getTagList(NBTKeys.ARMOR_PART_DATA, NBT.TAG_LIST);
          NBTBase nbtBase = movingParts.get(message.value);
          if (nbtBase.getId() != NBT.TAG_LIST) {
            return;
          }

          Container container = player.openContainer;
          if (container == null || !(container instanceof ContainerPlayerInventory)) {
            return;
          }

          NBTTagList itemList = (NBTTagList) nbtBase;
          int glListRemovalIndex = -1;
          boolean add = message.listOperation == ListOperation.ADD;
          if (message.listOperation == ListOperation.MODIFY) {
            NBTTagCompound armorItemNbt = itemList.getCompoundTagAt(message.armorItemIndex);
            ItemStackHelper.saveStackToNBT(armorItemNbt, message.stack, NBTKeys.ARMOR_ITEM);
            itemList.set(message.armorItemIndex, armorItemNbt);
          } else if (add) {
            NBTTagCompound armorItemNbt = new NBTTagCompound();
            ArmorItem armorItem = new ArmorItem(message.stack);
            armorItem.saveToNBT(armorItemNbt);
            if (message.nbt.hasKey(NBTKeys.ARMOR_GL_OPERATIONS)) {
              armorItemNbt.setTag(NBTKeys.ARMOR_GL_OPERATIONS,
                  message.nbt.getTagList(NBTKeys.ARMOR_GL_OPERATIONS, NBT.TAG_COMPOUND));
            }

            itemList.appendTag(armorItemNbt);
          } else {
            itemList.removeTag(message.armorItemIndex);
            glListRemovalIndex = message.armorItemIndex;
          }
          movingParts.set(message.value, itemList);
          DataChiseledArmorPiece.setPartData(data, movingParts);
          message.finalizeDataChange(message, stack, nbt, data, serverSide, true, add,
              glListRemovalIndex);
          if (serverSide) {
            ExtraBitManipulation.packetNetwork.sendTo(
                new PacketChangeArmorItemList(message.armorType, message.indexArmorSet,
                    message.value, message.armorItemIndex, message.selectedEntry,
                    message.listOperation,
                    message.stack, message.nbt, message.refreshLists, player),
                (EntityPlayerMP) player);
          }
        }
      });
      return null;
    }

    @Override
    public void receive(PacketChangeArmorItemList packet, ServerPlayer player,
                        PacketSender responseSender) {

    }
  }

  public enum ListOperation {
    ADD, REMOVE, MODIFY
  }

}