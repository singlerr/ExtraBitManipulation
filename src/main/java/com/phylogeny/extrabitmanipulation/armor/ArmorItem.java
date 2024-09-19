package com.phylogeny.extrabitmanipulation.armor;

import com.mojang.blaze3d.platform.GlStateManager;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.ArrayList;
import java.util.List;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ArmorItem {
  private final List<GlOperation> glOperations = new ArrayList<GlOperation>();
  private ItemStack stack;

  public ArmorItem() {
    stack = ItemStack.EMPTY;
  }

  public ArmorItem(ItemStack stack) {
    this.stack = stack;
  }

  public ArmorItem(CompoundTag nbt) {
    stack = ItemStackHelper.loadStackFromNBT(nbt, NBTKeys.ARMOR_ITEM);
    GlOperation.loadListFromNBT(nbt, NBTKeys.ARMOR_GL_OPERATIONS, glOperations);
  }

  public void addGlOperation(GlOperation glOperation) {
    glOperations.add(glOperation);
  }

  public void addGlOperation(int index, GlOperation glOperation) {
    glOperations.add(index, glOperation);
  }

  public void removeGlOperation(int index) {
    glOperations.remove(index);
  }

  public List<GlOperation> getGlOperations() {
    List<GlOperation> glOperations = new ArrayList<GlOperation>();
    glOperations.addAll(this.glOperations);
    return glOperations;
  }

  public void render(LivingEntity entity, float scale, boolean isRightLegOrFoot) {
    float scale2 = 32 * scale + Configs.armorZFightingBufferScale;
    if (isRightLegOrFoot) {
      scale2 += Configs.armorZFightingBufferScaleRightLegOrFoot;
    }

    GlStateManager.scale(scale2, scale2, scale2);
    if (ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) != ItemType.CHISELED_BLOCK) {
      GlStateManager.scale(0.5F, 0.5F, 0.5F);
    }

    Minecraft.getInstance().getItemRenderer().renderItem(entity, stack, TransformType.NONE);
  }

  public void executeGlOperations() {
    GlOperation.executeList(glOperations);
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  public void saveToNBT(CompoundTag nbt) {
    ItemStackHelper.saveStackToNBT(nbt, stack, NBTKeys.ARMOR_ITEM);
    GlOperation.saveListToNBT(nbt, NBTKeys.ARMOR_GL_OPERATIONS, glOperations);
  }

  public ItemStack getStack() {
    return stack;
  }

  public void setStack(ItemStack stack) {
    this.stack = stack;
  }

}