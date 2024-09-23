package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.recipe.RecipeChiseledArmor;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

public class RecipesExtraBitManipulation {
  public static void registerOres() {
    if (!Configs.disableDiamondNuggetOreDict) {

      OreDictionary.registerOre("nuggetDiamond", ItemsExtraBitManipulation.diamondNugget);
    }
  }

  void registerRecipes() {
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledHelmetDiamond,
        Items.DIAMOND_HELMET, 272);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledChestplateDiamond,
        Items.DIAMOND_CHESTPLATE, 444);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledLeggingsDiamond,
        Items.DIAMOND_LEGGINGS, 572);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledBootsDiamond,
        Items.DIAMOND_BOOTS, 272);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledHelmetIron,
        Items.IRON_HELMET, 272);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledChestplateIron,
        Items.IRON_CHESTPLATE, 444);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledLeggingsIron,
        Items.IRON_LEGGINGS, 572);
    registerChiseledArmorRecipes(ItemsExtraBitManipulation.chiseledBootsIron,
        Items.IRON_BOOTS, 272);
  }

  private static void registerChiseledArmorRecipes(ResourceLocation resourceLocation,
                                                   Item itemChiseled, Item itemVanilla,
                                                   int bitCost) {
    registerChiseledArmorRecipe(resourceLocation, itemChiseled, itemVanilla, bitCost);

    registerChiseledArmorRecipe(resourceLocation, itemVanilla, itemChiseled, bitCost);
  }

  private static void registerChiseledArmorRecipe(ResourceLocation resourceLocation, Item output,
                                                  Item input, int bitCost) {
    NonNullList<Ingredient> ingredients = NonNullList.create();
    ingredients.add(Ingredient.of(new ItemStack(input)));
    ingredients.add(
        Ingredient.of(new ItemStack(ModItems.ITEM_CHISEL_STONE.get()),
            new ItemStack(ModItems.ITEM_CHISEL_IRON.get()),
            new ItemStack(ModItems.ITEM_CHISEL_GOLD.get()),
            new ItemStack(ModItems.ITEM_CHISEL_DIAMOND.get())));
    Registry.register(BuiltInRegistries.RECIPE_TYPE, resourceLocation, new RecipeType<>() {
      @Override
      public String toString() {
        return resourceLocation.getPath();
      }
    });

    Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, resourceLocation,
        new SimpleCookingSerializer<>(
            (AbstractCookingRecipe.Factory<AbstractCookingRecipe>) (string, cookingBookCategory, ingredient, itemStack, f, i) -> new RecipeChiseledArmor(
                ingredients, output, input, bitCost)));

  }

}