package com.phylogeny.extrabitmanipulation.mixin.mixin;

import mod.chiselsandbits.compat.client.TextureStitchCallback;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {
  @Inject(method = "upload", at = @At("RETURN"))
  private void ebm$postStitch(SpriteLoader.Preparations preparations, CallbackInfo ci) {
    TextureStitchCallback.POST.invoker().stitch((TextureAtlas) (Object) this);
  }
}
