package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.extension.IllagerModelExtension;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IllagerModel.class)
public abstract class IllagerModelMixin implements IllagerModelExtension {

  @Shadow
  @Final
  private ModelPart rightLeg;
  @Shadow
  @Final
  private ModelPart leftLeg;
  @Shadow
  @Final
  private ModelPart arms;
  @Shadow
  @Final
  private ModelPart rightArm;
  @Shadow
  @Final
  private ModelPart leftArm;
  @Unique
  private ModelPart body;

  @Inject(method = "<init>", at = @At("TAIL"))
  private void ebm$captureModelPart(ModelPart modelPart, CallbackInfo ci) {
    this.body = modelPart.getChild("body");
  }

  @Override
  public ModelPart ebm$getBody() {
    return body;
  }

  @Override
  public ModelPart ebm$getRightLeg() {
    return rightLeg;
  }

  @Override
  public ModelPart ebm$getLeftLeg() {
    return leftLeg;
  }

  @Override
  public ModelPart ebm$getArms() {
    return arms;
  }

  @Override
  public ModelPart ebm$getRightArm() {
    return rightArm;
  }

  @Override
  public ModelPart ebm$getLeftArm() {
    return leftArm;
  }
}
