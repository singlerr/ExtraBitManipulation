package com.phylogeny.extrabitmanipulation.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerPlayerInventory extends AbstractContainerMenu {

  public ContainerPlayerInventory(Player player, int startX, int startY) {
    super(MenuType.GENERIC_9x5, 0);
    for (int i1 = 0; i1 < 9; ++i1) {
      addSlot(new Slot(player.getInventory(), i1, startX + i1 * 18, startY + 58));
    }
    for (int k = 0; k < 3; ++k) {
      for (int l = 0; l < 9; ++l) {
        addSlot(
            new Slot(player.getInventory(), l + k * 9 + 9, startX + l * 18, startY + k * 18));
      }
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    ItemStack stack = ItemStack.EMPTY;
    Slot slot = slots.get(index);
    if (slot != null && slot.hasItem()) {
      ItemStack stack2 = slot.getItem();
      stack = stack2.copy();
      if (index < 9) {
        if (!moveItemStackTo(stack2, 9, 36, false)) {
          return ItemStack.EMPTY;
        }
      } else if (!moveItemStackTo(stack2, 0, 9, true)) {
        return ItemStack.EMPTY;
      }
      if (stack2.isEmpty()) {
        slot.set(ItemStack.EMPTY);
      } else {
        slot.setChanged();
      }
    }
    return stack;

  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

}