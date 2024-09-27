package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.phylogeny.extrabitmanipulation.extension.MinecraftExtension;
import com.phylogeny.extrabitmanipulation.mixin.events.UniversalKeyInputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExtension {

  @Shadow
  @Final
  private ItemColors itemColors;
  @Unique
  private BlockEntityWithoutLevelRenderer renderer;

  @Inject(method = "handleKeybinds", at = @At("TAIL"))
  private void ebm$keyTyped(CallbackInfo ci) {
    UniversalKeyInputEvent.EVENT.invoker().keyTyped();
  }

  @Inject(method = "<init>", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/texture/TextureManager;Lnet/minecraft/client/resources/model/ModelManager;Lnet/minecraft/client/color/item/ItemColors;Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;)V"))
  private void ebm$captureRenderer(GameConfig gameConfig, CallbackInfo ci,
                                   @Local() BlockEntityWithoutLevelRenderer renderer) {
    this.renderer = renderer;
  }

  @Override
  public BlockEntityWithoutLevelRenderer ebm$getBlockEntityWithoutLevelRenderer() {
    return renderer;
  }

  @Override
  public ItemColors ebm$getItemColors() {
    return itemColors;
  }
}
