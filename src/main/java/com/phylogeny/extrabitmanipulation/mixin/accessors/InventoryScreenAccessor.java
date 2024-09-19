package com.phylogeny.extrabitmanipulation.mixin.accessors;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InventoryScreen.class)
public interface InventoryScreenAccessor {

  @Accessor("xMouse")
  float getMouseX();

  @Accessor("yMouse")
  float getMouseY();

  @Accessor("xMouse")
  void setMouseX(float x);

  @Accessor("yMouse")
  void setMouseY(float y);
}
