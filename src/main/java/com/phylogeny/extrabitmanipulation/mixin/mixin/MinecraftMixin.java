package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.mixin.events.UniversalKeyInputEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

  @Inject(method = "handleKeybinds", at = @At("TAIL"))
  private void ebm$keyTyped(CallbackInfo ci) {
    UniversalKeyInputEvent.EVENT.invoker().keyTyped();
  }
}
