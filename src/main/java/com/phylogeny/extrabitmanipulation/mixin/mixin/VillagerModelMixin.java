package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.extension.VillagerModelExtension;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin implements VillagerModelExtension {

  @Shadow
  @Final
  private ModelPart leftLeg;
  @Shadow
  @Final
  private ModelPart rightLeg;
  @Unique
  private ModelPart body;

  @Unique
  private ModelPart arms;

  @Inject(method = "<init>", at = @At("TAIL"))
  private void ebm$captureParts(ModelPart modelPart, CallbackInfo ci) {
    body = modelPart.getChild("body");
    arms = modelPart.getChild("arms");
  }

  @Override
  public ModelPart ebm$getArms() {
    return arms;
  }

  @Override
  public ModelPart ebm$getBody() {
    return body;
  }

  @Override
  public ModelPart ebm$getLeftLeg() {
    return leftLeg;
  }

  @Override
  public ModelPart ebm$getRightLeg() {
    return rightLeg;
  }
}

