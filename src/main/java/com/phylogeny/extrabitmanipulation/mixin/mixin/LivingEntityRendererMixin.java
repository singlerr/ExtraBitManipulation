package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phylogeny.extrabitmanipulation.mixin.events.LivingEntityRenderEvents;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

  @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
  private void ebm$preRender(LivingEntity livingEntity, float f, float partialTicks,
                             PoseStack poseStack,
                             MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    if (LivingEntityRenderEvents.BEFORE.invoker()
        .render(livingEntity, partialTicks, (LivingEntityRenderer<?, ?>) (Object) this)) {
      ci.cancel();
    }
  }

  @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"), cancellable = true)
  private void ebm$postRender(LivingEntity livingEntity, float f, float partialTicks,
                              PoseStack poseStack,
                              MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
    LivingEntityRenderEvents.AFTER.invoker()
        .render(livingEntity, partialTicks, (LivingEntityRenderer<?, ?>) (Object) this);
  }
}
