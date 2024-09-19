package com.phylogeny.extrabitmanipulation.mixin.mixin;

import com.phylogeny.extrabitmanipulation.mixin.events.PlayerTickEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

  @Inject(method = "tick", at = @At("HEAD"))
  private void ebm$tickStart(CallbackInfo ci) {
    PlayerTickEvents.START.invoker().tick((Player) (Object) this);
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void ebm$tickEnd(CallbackInfo ci) {
    PlayerTickEvents.END.invoker().tick((Player) (Object) this);
  }
}
