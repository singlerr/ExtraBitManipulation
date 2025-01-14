package com.phylogeny.extrabitmanipulation.api.jei;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;

/**
 * This class is taken by permission from JEI.
 *
 * @author mezz
 */
public class CycleTimer {
  /* the amount of time in ms to display one thing before cycling to the next one */
  private static final int cycleTime = 1000;
  private long startTime = 0;
  private long drawTime = 0;
  private long pausedDuration = 0;

  public CycleTimer(int offset) {
    long time = System.currentTimeMillis();
    this.startTime = time - ((long) offset * cycleTime);
    this.drawTime = time;
  }

  @Nullable
  public <T> T getCycledItem(List<T> list) {
    if (list.isEmpty()) {
      return null;
    }

    Long index = ((drawTime - startTime) / cycleTime) % list.size();
    return list.get(index.intValue());
  }

  public void onDraw() {
    if (!Screen.hasShiftDown()) {
      if (pausedDuration > 0) {
        startTime += pausedDuration;
        pausedDuration = 0;
      }
      drawTime = System.currentTimeMillis();
    } else {
      pausedDuration = System.currentTimeMillis() - drawTime;
    }
  }

}