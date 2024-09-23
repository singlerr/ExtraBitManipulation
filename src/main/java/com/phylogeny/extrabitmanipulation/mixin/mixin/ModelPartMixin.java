package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.extension.ModelPartExtension;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements ModelPartExtension {


  @Shadow
  @Final
  private List<ModelPart.Cube> cubes;

  @Override
  public void ebm$clearCubeList() {
    synchronized (cubes) {
      cubes.clear();
    }
  }
}
