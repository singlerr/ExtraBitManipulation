package com.phylogeny.extrabitmanipulation.mixin.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

public interface PlayerTickEvents {

  Event<PlayerTick> START = EventFactory.createArrayBacked(PlayerTick.class, listeners -> {
    return player -> {
      for (PlayerTick listener : listeners) {
        listener.tick(player);
      }
    };
  });

  Event<PlayerTick> END = EventFactory.createArrayBacked(PlayerTick.class, listeners -> {
    return player -> {
      for (PlayerTick listener : listeners) {
        listener.tick(player);
      }
    };
  });

  interface PlayerTick {
    void tick(Player player);
  }
}
