package com.phylogeny.extrabitmanipulation.mixin.accessors;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

  @Accessor("accumulatedScrollY")
  double getEventDWheel();

  @Accessor("accumulatedDX")
  double getEventX();

  @Accessor("accumulatedDY")
  double getEventY();
}
