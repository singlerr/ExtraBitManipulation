package com.phylogeny.extrabitmanipulation.container;

import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.mixin.accessors.SlotAccessor;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerPlayerArmorSlots extends InventoryMenu {

  public ContainerPlayerArmorSlots(Inventory playerInventory, boolean localWorld,
                                   Player player) {
    super(playerInventory, localWorld, player);
    slots.get(slots.size() - 1).x += 18;
    IChiseledArmorSlotsHandler cap =
        ChiseledArmorSlotsHandler.getCapability(player).orElseThrow(IllegalStateException::new);
    for (int i = 0; i < ChiseledArmorSlotsHandler.COUNT_SETS; i++) {
      for (int j = 0; j < ChiseledArmorSlotsHandler.COUNT_TYPES; j++) {
        SlotChiseledArmor slot =
            new SlotChiseledArmor(cap, i * ChiseledArmorSlotsHandler.COUNT_TYPES + j,
                77 + 18 * i + (i == 0 ? 0 : 21), 8 + j * 18);
        addSlot(slot);
      }
    }

    for (int i = 0; i < slots.size(); i++) {
      Slot slot = slots.get(i);
      if (slot.container instanceof CraftingContainer ||
          slot.container instanceof ResultContainer) {
        SlotNull slotNull = new SlotNull();
        ((SlotAccessor) slotNull).setSlot(((SlotAccessor) slot).getSlot());
        slots.set(i, slotNull);
      }
    }
  }

  public class SlotNull extends Slot {

    public SlotNull() {
      super(new SimpleContainer(1), 0, -10000, -10000);
    }

    @Override
    public boolean mayPickup(Player player) {
      return false;
    }

    @Override
    public boolean isActive() {
      return false;
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
      return false;
    }
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    Slot slot = slots.get(index);
    if (slot != null && slot.hasItem()) {
      ItemStack stack = slot.getItem();
      if (index > 8 && index < 45) {

        int i = 3 - LivingEntity.getEquipmentSlotForItem(stack).getIndex();
        if (ChiseledArmorSlotsHandler.isItemValidStatic(i, stack)) {
          for (int j = 0; j < ChiseledArmorSlotsHandler.COUNT_SETS; j++) {
            int start = i + 46 + j * ChiseledArmorSlotsHandler.COUNT_TYPES;
            if (moveItemStackTo(stack, start, start + 1, false)) {
              slot.onTake(player, stack);
            }
          }
        }
      } else if (index > 45) {
        if (moveItemStackTo(stack, 9, 45, false)) {
          slot.onTake(player, stack);
        }
      }
    }
    return super.quickMoveStack(player, index);

  }

}