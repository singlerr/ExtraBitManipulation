package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.phylogeny.extrabitmanipulation.mixin.events.ClientMouseEvents;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

  @Shadow
  private int activeButton;

  @Inject(
      method = "onScroll",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
  private void ebm$scrollEvent(long l, double d, double e, CallbackInfo ci,
                               @Local(ordinal = 1) double deltaX,
                               @Local(ordinal = 2) double deltaY) {
    if (ClientMouseEvents.EVENT.invoker().mouseInput(deltaX, deltaY, activeButton)) {
      ci.cancel();
    }
  }

  @Inject(
      method = "onPress",
      at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/platform/Window;getWindow()J"), cancellable = true)
  private void ebm$pressEvent(long l, int i, int j, int k, CallbackInfo ci) {
    if (ClientMouseEvents.EVENT.invoker().mouseInput(0, 0, activeButton)) {
      ci.cancel();
    }
  }
}
