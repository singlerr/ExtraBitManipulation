package com.phylogeny.extrabitmanipulation.mixin.accessors;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerModel.class)
public interface VillagerModelAccessor {

  @Accessor("rightLeg")
  ModelPart getRightLeg();

  @Accessor("leftLeg")
  ModelPart getLeftLeg();
}
