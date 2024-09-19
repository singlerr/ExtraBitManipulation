package com.phylogeny.extrabitmanipulation.mixin.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface UniversalKeyInputEvent {

  Event<KeyInput> EVENT = EventFactory.createArrayBacked(KeyInput.class, listeners -> () -> {
    for (KeyInput listener : listeners) {
      listener.keyTyped();
    }
  });

  interface KeyInput {
    void keyTyped();
  }
}
