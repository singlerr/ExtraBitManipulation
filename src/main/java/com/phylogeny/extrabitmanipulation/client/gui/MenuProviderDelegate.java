package com.phylogeny.extrabitmanipulation.client.gui;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public final class MenuProviderDelegate implements MenuProvider {

  private final Component displayName;
  private final int id;

  public MenuProviderDelegate(Component displayName, int id) {
    this.displayName = displayName;
    this.id = id;
  }

  @Override
  public Component getDisplayName() {
    return displayName;
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
    return ExtraBitManipulation.instance.createMenu(id, inventory, player);
  }
}
