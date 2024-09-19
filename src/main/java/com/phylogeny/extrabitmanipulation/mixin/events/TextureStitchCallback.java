package com.phylogeny.extrabitmanipulation.mixin.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.texture.TextureAtlas;

public interface TextureStitchCallback {
  Event<Post> POST = EventFactory.createArrayBacked(Post.class, callbacks -> atlas -> {
    for (Post e : callbacks) {
      e.stitch(atlas);
    }
  });

  @Environment(EnvType.CLIENT)
  interface Post {
    void stitch(TextureAtlas atlas);
  }
}
