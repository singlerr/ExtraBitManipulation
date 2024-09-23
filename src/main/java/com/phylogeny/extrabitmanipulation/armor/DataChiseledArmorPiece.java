package com.phylogeny.extrabitmanipulation.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.opengl.GL11;

public class DataChiseledArmorPiece {
  private final List<GlOperation> globalGlOperationsPre = new ArrayList<GlOperation>();
  private final List<GlOperation> globalGlOperationsPost = new ArrayList<GlOperation>();
  private final List<ArmorItem>[] partItemLists;
  private final ArmorType armorType;

  public DataChiseledArmorPiece(ArmorType armorType) {
    this.armorType = armorType;
    partItemLists = new ArrayList[armorType.getMovingpartCount()];
    for (int i = 0; i < partItemLists.length; i++) {
      partItemLists[i] = new ArrayList<ArmorItem>();
    }
  }

  public DataChiseledArmorPiece(CompoundTag nbt, ArmorType armorType) {
    this(armorType);
    loadFromNBT(nbt);
  }

  private List<GlOperation> getGlobalGlOperationsInternal(boolean isPre) {
    return isPre ? globalGlOperationsPre : globalGlOperationsPost;
  }

  public List<GlOperation> getGlobalGlOperations(boolean isPre) {
    List<GlOperation> glOperations = new ArrayList<GlOperation>();
    glOperations.addAll(getGlobalGlOperationsInternal(isPre));
    return glOperations;
  }

  public void addGlobalGlOperation(GlOperation glOperation, boolean isPre) {
    getGlobalGlOperationsInternal(isPre).add(glOperation);
  }

  public void addGlobalGlOperation(int index, GlOperation glOperation, boolean isPre) {
    getGlobalGlOperationsInternal(isPre).add(index, glOperation);
  }

  public void removeGlobalGlOperation(int index, boolean isPre) {
    getGlobalGlOperationsInternal(isPre).remove(index);
  }

  public void addItemToPart(int partIndex, ArmorItem armorItem) {
    if (outOfPartRange(partIndex)) {
      return;
    }

    partItemLists[partIndex].add(armorItem);
  }

  public void addItemToPart(int partIndex, int armorItemIndex, ArmorItem armorItem) {
    if (outOfPartRange(partIndex)) {
      return;
    }

    partItemLists[partIndex].add(armorItemIndex, armorItem);
  }

  public void removeItemFromPart(int partIndex, int armorItemIndex) {
    if (outOfPartRange(partIndex)) {
      return;
    }

    partItemLists[partIndex].remove(armorItemIndex);
  }

  public List<ArmorItem> getArmorItemsForPart(int partIndex) {
    List<ArmorItem> armorItems = new ArrayList<ArmorItem>();
    if (outOfPartRange(partIndex)) {
      return armorItems;
    }

    armorItems.addAll(partItemLists[partIndex]);
    return armorItems;
  }

  public ArmorItem getArmorItemForPart(int partIndex, int armorItemIndex) {
    return outOfPartRange(partIndex) ? new ArmorItem() :
        partItemLists[partIndex].get(armorItemIndex);
  }

  public int generateDisplayList(int partIndex, LivingEntity entity, float scale,
                                 PoseStack poseStack, MultiBufferSource bufferSource, int i, int j,
                                 int k) {
    GL11.glPushMatrix();

    int displayList = GL11.glGenLists(1);
    GL11.glNewList(displayList, GL11.GL_COMPILE);
    GlOperation.executeList(globalGlOperationsPre);
    for (ArmorItem armorItem : partItemLists[partIndex]) {
      if (armorItem.isEmpty()) {
        continue;
      }

      GL11.glPushMatrix();
      armorItem.executeGlOperations();
      GlOperation.executeList(globalGlOperationsPost);
      armorItem.render(entity, poseStack, bufferSource, scale,
          (armorType == ArmorType.BOOTS && partIndex == 0) ||
              (armorType == ArmorType.LEGGINGS && partIndex == 1), i, j, k);
      GL11.glPopMatrix();
    }
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    GL11.glEndList();
    GL11.glPopMatrix();
    return displayList;
  }

  private boolean outOfPartRange(int partIndex) {
    return partIndex < 0 || partIndex >= partItemLists.length;
  }

  public void saveToNBT(CompoundTag nbt) {
    CompoundTag data = new CompoundTag();
    data.putInt(NBTKeys.ARMOR_TYPE, armorType.ordinal());
    ListTag movingParts = new ListTag();
    boolean empty = true;
    for (List<ArmorItem> partItemList : partItemLists) {
      ListTag itemList = new ListTag();
      for (ArmorItem armorItem : partItemList) {
        CompoundTag armorItemNbt = new CompoundTag();
        armorItem.saveToNBT(armorItemNbt);
        itemList.add(armorItemNbt);
        if (!armorItem.getStack().isEmpty()) {
          empty = false;
        }
      }
      movingParts.add(itemList);
    }
    data.put(NBTKeys.ARMOR_PART_DATA, movingParts);
    data.putBoolean(NBTKeys.ARMOR_NOT_EMPTY, !empty);
    GlOperation.saveListToNBT(data, NBTKeys.ARMOR_GL_OPERATIONS_PRE, globalGlOperationsPre);
    GlOperation.saveListToNBT(data, NBTKeys.ARMOR_GL_OPERATIONS_POST, globalGlOperationsPost);
    nbt.put(NBTKeys.ARMOR_DATA, data);
  }

  public void loadFromNBT(CompoundTag nbt) {
    CompoundTag data = ItemStackHelper.getArmorData(nbt);
    ListTag movingParts = data.getList(NBTKeys.ARMOR_PART_DATA, ListTag.TAG_LIST);
    for (int i = 0; i < movingParts.size(); i++) {
      Tag nbtBase = movingParts.get(i);
      if (nbtBase.getId() != ListTag.TAG_LIST) {
        continue;
      }

      partItemLists[i].clear();
      ListTag itemList = (ListTag) nbtBase;
      for (int j = 0; j < itemList.size(); j++) {
        partItemLists[i].add(new ArmorItem(itemList.getCompound(j)));
      }
    }
    GlOperation.loadListFromNBT(data, NBTKeys.ARMOR_GL_OPERATIONS_PRE, globalGlOperationsPre);
    GlOperation.loadListFromNBT(data, NBTKeys.ARMOR_GL_OPERATIONS_POST, globalGlOperationsPost);
  }

  public static void setPartData(CompoundTag data, ListTag movingParts) {
    data.put(NBTKeys.ARMOR_PART_DATA, movingParts);
    boolean empty = true;
    for (int i = 0; i < movingParts.size(); i++) {
      Tag nbtBase = movingParts.get(i);
      if (nbtBase.getId() != ListTag.TAG_LIST) {
        continue;
      }

      ListTag itemList = (ListTag) nbtBase;
      for (int j = 0; j < itemList.size(); j++) {
        if (!ItemStackHelper.loadStackFromNBT(itemList.getCompound(j), NBTKeys.ARMOR_ITEM)
            .isEmpty()) {
          empty = false;
          break;
        }
      }
      if (!empty) {
        break;
      }
    }
    data.putBoolean(NBTKeys.ARMOR_NOT_EMPTY, !empty);
  }

}