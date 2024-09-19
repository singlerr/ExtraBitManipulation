package com.phylogeny.extrabitmanipulation.armor.capability;

import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IChiseledArmorSlotsHandler extends IItemHandlerModifiable {
  void syncAllSlots(Player player);

  void markAllSlotsDirty();

  void markSlotDirty(int index);

  void onContentsChanged(int slot);

  boolean hasArmor();

  boolean hasArmorSet(int indexSet);

  boolean hasArmorType(int indexType);

  ModelPartConcealer getAndApplyModelPartConcealer(ModelPart model);
}