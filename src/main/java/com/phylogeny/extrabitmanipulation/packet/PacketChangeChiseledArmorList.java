package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiChiseledArmor;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class PacketChangeChiseledArmorList extends PacketArmorSlotInt {
  protected int armorItemIndex, selectedEntry;
  protected boolean refreshLists;
  protected CompoundTag nbt = new CompoundTag();

  public PacketChangeChiseledArmorList(FriendlyByteBuf buffer) {
    super(buffer);
    nbt = buffer.readNbt();
    armorItemIndex = buffer.readInt();
    selectedEntry = buffer.readInt();
    refreshLists = buffer.readBoolean();
  }

  public PacketChangeChiseledArmorList(CompoundTag nbt, ArmorType armorType, int indexArmorSet,
                                       int partIndex, int armorItemIndex, int selectedEntry,
                                       boolean refreshLists, @Nullable Player player) {
    super(armorType, indexArmorSet, partIndex);
    this.nbt = nbt;
    this.armorItemIndex = armorItemIndex;
    this.selectedEntry = selectedEntry;
    this.refreshLists = refreshLists;
    if (indexArmorSet > 0 && player instanceof ServerPlayer) {
      IChiseledArmorSlotsHandler cap =
          ChiseledArmorSlotsHandler.getCapability(player).orElseThrow(IllegalStateException::new);
      if (cap != null) {
        cap.markSlotDirty(armorType.getSlotIndex(indexArmorSet));
      }
    }
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    buffer.writeNbt(nbt);
    buffer.writeInt(armorItemIndex);
    buffer.writeInt(selectedEntry);
    buffer.writeBoolean(refreshLists);
  }


  protected CompoundTag getData(CompoundTag nbt, boolean serverSide) {
    CompoundTag data = ItemStackHelper.getArmorData(nbt);
    if (!serverSide) {
      RenderLayersExtraBitManipulation.removeFromRenderMaps(data);
    }

    return data;
  }

  protected void initData(final PacketChangeChiseledArmorList message, ItemStack stack) {
    CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
    if (nbt.contains(NBTKeys.ARMOR_DATA)) {
      return;
    }

    new DataChiseledArmorPiece(message.armorType).saveToNBT(nbt);
    stack.setTag(nbt);
  }

  protected void finalizeDataChange(PacketChangeChiseledArmorList message, ItemStack stack,
                                    CompoundTag nbt,
                                    CompoundTag data, boolean serverSide, boolean isArmorItem,
                                    boolean scrollToEnd, int glListRemovalIndex) {
    nbt.put(NBTKeys.ARMOR_DATA, data);
    stack.setTag(nbt);
    if (serverSide) {
      return;
    }

    if (message.refreshLists && GuiHelper.getOpenGui() instanceof GuiChiseledArmor) {
      ((GuiChiseledArmor) GuiHelper.getOpenGui()).refreshListsAndSelectEntry(message.selectedEntry,
          isArmorItem, scrollToEnd, glListRemovalIndex);
    }
  }

}