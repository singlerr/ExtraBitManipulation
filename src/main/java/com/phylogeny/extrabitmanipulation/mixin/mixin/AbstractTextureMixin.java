package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.extension.AbstractTextureExtension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractTexture.class)
public abstract class AbstractTextureMixin implements AbstractTextureExtension {

  @Shadow
  public abstract void setFilter(boolean bl, boolean bl2);

  @Unique
  private boolean lastBlur;

  @Unique
  private boolean lastMipmap;

  @Inject(method = "setFilter", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThreadOrInit()V"))
  private void ebm$cacheLastFilter(boolean blur, boolean mipmap, CallbackInfo ci) {
    this.lastBlur = blur;
    this.lastMipmap = mipmap;
  }


  @Override
  public void ebm$restoreFilter() {
    setFilter(lastBlur, lastMipmap);
  }
}
