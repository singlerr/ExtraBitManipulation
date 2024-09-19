package com.phylogeny.extrabitmanipulation.helper;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.item.ItemBitToolBase;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import javax.annotation.Nullable;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStackHelper {

  public static CompoundTag initNBT(ItemStack stack) {
    if (!stack.hasTag()) {
      stack.setTag(new CompoundTag());
    }

    return stack.getTag();
  }

  public static int getInt(CompoundTag nbt, int intValue, String key) {
    if (nbt != null && nbt.contains(key)) {
      intValue = nbt.getInt(key);
    }

    return intValue;
  }

  public static void setInt(Player player, ItemStack stack, int intValue, String key) {
    CompoundTag nbt = initNBT(stack);
    nbt.putInt(key, intValue);
    player.inventoryMenu.sendAllDataToRemote();
//    player.inventoryContainer.detectAndSendChanges();
  }

  public static boolean getBoolean(CompoundTag nbt, boolean booleanValue, String key) {
    if (nbt != null && nbt.contains(key)) {
      booleanValue = nbt.getBoolean(key);
    }

    return booleanValue;
  }

  public static void setBoolean(Player player, ItemStack stack, boolean booleanValue,
                                String key) {
    CompoundTag nbt = initNBT(stack);
    nbt.putBoolean(key, booleanValue);
    player.inventoryMenu.sendAllDataToRemote();
//    player.inventoryContainer.detectAndSendChanges();
  }

  public static ItemStack getStack(@Nullable CompoundTag nbt, String key) {
    return nbt != null ? loadStackFromNBT(nbt, key) : ItemStack.EMPTY;
  }

  public static void setStack(Player player, ItemStack stack, ItemStack stackToSet,
                              String key) {
    CompoundTag nbt = initNBT(stack);
    saveStackToNBT(nbt, stackToSet, key);
    player.inventoryMenu.sendAllDataToRemote();
//    player.inventoryContainer.detectAndSendChanges();
  }

  public static void saveStackToNBT(CompoundTag nbt, ItemStack stack, String key) {
    CompoundTag nbt2 = new CompoundTag();
    stack.save(nbt2);
    nbt.put(key, nbt2);
  }

  public static ItemStack loadStackFromNBT(CompoundTag nbt, String key) {
    ItemStack stack = ItemStack.EMPTY;
    if (nbt.contains(key)) {
      stack = ItemStack.of((CompoundTag) nbt.get(key));
    }

    return stack;
  }

  @SuppressWarnings("null")
  public static boolean hasKey(ItemStack stack, String key) {
    return stack.hasTag() && stack.getTag().contains(key);
  }

  public static CompoundTag getNBT(ItemStack stack) {
    return stack.getTag();
  }

  public static CompoundTag getNBTOrNew(ItemStack stack) {
    return stack.hasTag() ? stack.getTag() : new CompoundTag();
  }

  public static boolean isModelingToolStack(ItemStack stack) {
    return isModelingToolItem(stack.getItem());
  }

  public static boolean isModelingToolItem(Item item) {
    return item != null && item instanceof ItemModelingTool;
  }

  public static boolean isSculptingToolStack(ItemStack stack) {
    return isSculptingToolItem(stack.getItem());
  }

  public static boolean isSculptingToolItem(Item item) {
    return item != null && item instanceof ItemSculptingTool;
  }

  public static boolean isBitToolStack(ItemStack stack) {
    return isBitToolItem(stack.getItem());
  }

  public static boolean isBitToolItem(Item item) {
    return item != null && item instanceof ItemBitToolBase;
  }

  public static boolean isBitWrenchStack(ItemStack stack) {
    return isBitWrenchItem(stack.getItem());
  }

  public static boolean isBitWrenchItem(Item item) {
    return item != null && item instanceof ItemBitWrench;
  }

  public static boolean isChiseledArmorStack(ItemStack stack) {
    return isChiseledArmorItem(stack.getItem());
  }

  public static boolean isChiseledArmorItem(Item item) {
    return item != null && item instanceof ItemChiseledArmor;
  }

  public static boolean isDesignStack(ItemStack stack) {
    return isDesignItemType(ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack));
  }

  public static boolean isDesignItemType(ItemType itemType) {
    return itemType == ItemType.MIRROR_DESIGN || itemType == ItemType.NEGATIVE_DESIGN ||
        itemType == ItemType.POSITIVE_DESIGN;
  }

  public static CompoundTag getArmorData(CompoundTag armorNbt) {
    return armorNbt.getCompound(NBTKeys.ARMOR_DATA);
  }

  public static ItemStack getChiseledArmorStack(Player player, @Nullable ArmorType armorType,
                                                int indexArmorSet) {
    if (armorType == null) {
      return player.getMainHandItem();
    } else if (indexArmorSet == 0) {
      return player.getItemBySlot(armorType.getEquipmentSlot());
    }

    IChiseledArmorSlotsHandler cap =
        ChiseledArmorSlotsHandler.getCapability(player).orElseThrow(IllegalStateException::new);
    return cap == null ? ItemStack.EMPTY :
        cap.getStackInSlot(armorType.getSlotIndex(indexArmorSet));
  }

  public static boolean isChiseledArmorNotEmpty(ItemStack stack) {
    return getArmorData(getNBTOrNew(stack)).getBoolean(NBTKeys.ARMOR_NOT_EMPTY);
  }

}