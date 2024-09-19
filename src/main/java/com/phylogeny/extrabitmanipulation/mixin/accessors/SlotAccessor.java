package com.phylogeny.extrabitmanipulation.mixin.accessors;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {

  @Accessor("slot")
  void setSlot(int slot);

  @Accessor("slot")
  int getSlot();
}
