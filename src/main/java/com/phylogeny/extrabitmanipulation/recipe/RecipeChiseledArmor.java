package com.phylogeny.extrabitmanipulation.recipe;

import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.Random;
import mod.chiselsandbits.items.ItemChisel;
import net.minecraft.core.NonNullList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

public class RecipeChiseledArmor extends ShapelessRecipe {
  private final Random rand = new Random();
  private final int bitCost;

  public RecipeChiseledArmor(NonNullList<Ingredient> ingredients, Item output, Item input,
                             int bitCost) {
    super(null, ingredients, new ItemStack(output));
    this.bitCost = bitCost;
    setRegistryName(Reference.MOD_ID,
        );
  }

  private String getItemName(Item item) {
    ResourceLocation name = item.getRegistryName();
    return name != null ?
        name.toString().substring(name.toString().indexOf(":") + 1).replace("_", "") : "";
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
    NonNullList<ItemStack> remainingItems =
        NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);
    for (int i = 0; i < remainingItems.size(); i++) {
      ItemStack stack = container.getItem(i);
      if (stack.getItem() instanceof ItemChisel) {
        ItemStack chiselRemaining = stack.copy();
        EntityPlayer player = ForgeHooks.getCraftingPlayer();
        if (chiselRemaining.hurt(bitCost, rand,
            player instanceof EntityPlayerMP ? (EntityPlayerMP) player : null)) {
          ForgeEventFactory.onPlayerDestroyItem(player, stack, null);
          chiselRemaining = ItemStack.EMPTY;
        }
        remainingItems.set(i, chiselRemaining);
        break;
      }
    }
    return remainingItems;
  }

}