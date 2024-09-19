package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.extension.ModelPartExtension;
import com.phylogeny.extrabitmanipulation.extension.ModelPartType;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements ModelPartExtension {

  @Shadow
  @Final
  private Map<String, ModelPart> children;

  @Override
  public void ebm$putChild(ModelPartType type, ModelPart child) {
    children.put(type.getName(), child);
  }

  @Override
  public ModelPart ebm$getChild(ModelPartType type) {
    return children.get(type.getName());
  }
}
