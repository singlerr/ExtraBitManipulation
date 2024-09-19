package com.phylogeny.extrabitmanipulation.container;

import net.minecraft.world.entity.player.Player;

public class ContainerHeldItem extends ContainerPlayerInventory {

  public ContainerHeldItem(Player player, int startX, int startY) {
    super(player, startX, startY);
  }


//  @Override
//  @Nullable
//  public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
//    return slotId == player.inventory.currentItem ? ItemStack.EMPTY :
//        super.slotClick(slotId, dragType, clickTypeIn, player);
//  }
}