package com.phylogeny.extrabitmanipulation.mixin.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ClientMouseEvents {

  Event<Mouse> EVENT =
      EventFactory.createArrayBacked(Mouse.class, listeners -> ((deltaX, deltaY, button) -> {
        boolean result = false;
        for (Mouse listener : listeners) {
          result |= listener.mouseInput(deltaX, deltaY, button);
        }

        return result;
      }));

  interface Mouse {
    boolean mouseInput(double deltaX, double deltaY, int button);
  }
}
