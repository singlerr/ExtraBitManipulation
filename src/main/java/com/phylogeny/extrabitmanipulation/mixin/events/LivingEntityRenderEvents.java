package com.phylogeny.extrabitmanipulation.mixin.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public interface LivingEntityRenderEvents {

  Event<PreRenderLivingEntity> BEFORE = EventFactory.createArrayBacked(PreRenderLivingEntity.class,
      listeners -> ((entity, partialTicks, renderer) -> {
        boolean canceled = false;
        for (PreRenderLivingEntity listener : listeners) {
          canceled |= listener.render(entity, partialTicks, renderer);
        }

        return canceled;
      }));

  Event<PostRenderLivingEntity> AFTER = EventFactory.createArrayBacked(PostRenderLivingEntity.class,
      listeners -> ((entity, partialTicks, renderer) -> {
        for (PostRenderLivingEntity listener : listeners) {
          listener.render(entity, partialTicks, renderer);
        }
      }));


  interface PreRenderLivingEntity {
    boolean render(LivingEntity entity, float partialTicks, LivingEntityRenderer<?, ?> renderer);
  }

  interface PostRenderLivingEntity {
    void render(LivingEntity entity, float partialTicks, LivingEntityRenderer<?, ?> renderer);
  }
}
