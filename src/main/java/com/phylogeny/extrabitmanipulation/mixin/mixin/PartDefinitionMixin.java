package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.extension.PartDefinitionExtension;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PartDefinition.class)
public abstract class PartDefinitionMixin implements PartDefinitionExtension {

  @Unique
  @Final
  private final List<Consumer<ModelPart>> onBakeListeners = new ArrayList<>();

  @Inject(method = "bake", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/model/geom/ModelPart;loadPose(Lnet/minecraft/client/model/geom/PartPose;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
  private void ebm$onBake(int i, int j, CallbackInfoReturnable<ModelPart> cir,
                          Object2ObjectArrayMap object2ObjectArrayMap, List list,
                          ModelPart modelPart) {
    onBakeListeners.forEach(c -> c.accept(modelPart));
  }

  @Override
  public void ebm$registerBakeListener(Consumer<ModelPart> onBake) {
    this.onBakeListeners.add(onBake);
  }
}
