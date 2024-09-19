package com.phylogeny.extrabitmanipulation.api.jei;

import com.phylogeny.extrabitmanipulation.api.jei.armor.ChiseledArmorInfoRecipe;
import com.phylogeny.extrabitmanipulation.api.jei.armor.ChiseledArmorInfoRecipeCategory;
import com.phylogeny.extrabitmanipulation.api.jei.armor.ChiseledArmorInfoRecipeHandler;
import com.phylogeny.extrabitmanipulation.api.jei.model.ModelInfoRecipe;
import com.phylogeny.extrabitmanipulation.api.jei.model.ModelInfoRecipeCategory;
import com.phylogeny.extrabitmanipulation.api.jei.model.ModelInfoRecipeHandler;
import com.phylogeny.extrabitmanipulation.api.jei.shape.ShapeInfoRecipe;
import com.phylogeny.extrabitmanipulation.api.jei.shape.ShapeInfoRecipeCategory;
import com.phylogeny.extrabitmanipulation.api.jei.shape.ShapeInfoRecipeHandler;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

@JeiPlugin
public class JustEnoughItemsPlugin implements IModPlugin {
  private static IJeiRuntime jeiRuntime;

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    JustEnoughItemsPlugin.jeiRuntime = jeiRuntime;
  }

  public static void openCategory(String categoryUid) {
    jeiRuntime.getRecipesGui().showCategories(Collections.<String>singletonList(categoryUid));
  }

  public static String translate(String langKey) {
    return Translator.translateToLocal("jei." + Reference.MOD_ID + "." + langKey);
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {

  }

  @Override
  public void register(IModRegistry registry) {
    addDescription(registry, ItemsExtraBitManipulation.bitWrench);
    ItemStack modelingStack = addDescription(registry, ItemsExtraBitManipulation.modelingTool);
    List<ItemStack> sculptingStacks =
        addDescription(registry, "sculpting", ItemsExtraBitManipulation.sculptingLoop,
            ItemsExtraBitManipulation.sculptingSquare,
            ItemsExtraBitManipulation.sculptingSpadeCurved,
            ItemsExtraBitManipulation.sculptingSpadeSquared);
    addDescription(registry, ModItems.ITEM_BLOCK_BIT.get());
    addDescription(registry, "designs", ModItems.ITEM_MIRROR_PRINT.get(),
        ModItems.ITEM_NEGATIVE_PRINT.get(),
        ModItems.ITEM_POSITIVE_PRINT.get());
    List<ItemStack> armorStacks =
        addDescription(registry, "chiseled_armor", ItemsExtraBitManipulation.chiseledHelmetDiamond,
            ItemsExtraBitManipulation.chiseledChestplateDiamond,
            ItemsExtraBitManipulation.chiseledLeggingsDiamond,
            ItemsExtraBitManipulation.chiseledBootsDiamond,
            ItemsExtraBitManipulation.chiseledHelmetIron,
            ItemsExtraBitManipulation.chiseledChestplateIron,
            ItemsExtraBitManipulation.chiseledLeggingsIron,
            ItemsExtraBitManipulation.chiseledBootsIron);
    Item templateItem = Item.byBlock(BlocksExtraBitManipulation.bodyPartTemplate);
    addDescription(registry, templateItem);
    IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
    registry.handleRecipes(ShapeInfoRecipe.class, new ShapeInfoRecipeHandler(),
        ShapeInfoRecipeCategory.UID);
    registry.addRecipes(ShapeInfoRecipe.create(guiHelper, sculptingStacks),
        ShapeInfoRecipeCategory.UID);
    registry.handleRecipes(ModelInfoRecipe.class, new ModelInfoRecipeHandler(),
        ModelInfoRecipeCategory.UID);
    registry.addRecipes(ModelInfoRecipe.create(guiHelper, Collections.singletonList(modelingStack)),
        ModelInfoRecipeCategory.UID);
    registry.handleRecipes(ChiseledArmorInfoRecipe.class, new ChiseledArmorInfoRecipeHandler(),
        ChiseledArmorInfoRecipeCategory.UID);
    List<ItemStack> iconStacks = new ArrayList<ItemStack>();
    iconStacks.addAll(armorStacks);
    iconStacks.add(new ItemStack(templateItem));
    registry.addRecipes(ChiseledArmorInfoRecipe.create(guiHelper, iconStacks),
        ChiseledArmorInfoRecipeCategory.UID);
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registry) {
    IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
    registry.addRecipeCategories(new ShapeInfoRecipeCategory(guiHelper));
    registry.addRecipeCategories(new ModelInfoRecipeCategory(guiHelper));
    List<ItemStack> armorStacks = new ArrayList<ItemStack>();
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledHelmetDiamond));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledChestplateDiamond));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledLeggingsDiamond));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledBootsDiamond));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledHelmetIron));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledChestplateIron));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledLeggingsIron));
    armorStacks.add(getStack(ItemsExtraBitManipulation.chiseledBootsIron));
    registry.addRecipeCategories(new ChiseledArmorInfoRecipeCategory(guiHelper, armorStacks));
  }

  private ItemStack getStack(Item item) {
    return new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
  }

  private ItemStack addDescription(IRecipeRegistration registry, Item item) {
    ItemStack stack = getStack(item);
    registry.addIngredientInfo(stack, VanillaTypes.ITEM_STACK,
        Component.literal("jei.description." + BuiltInRegistries.ITEM.getKey(item)));
    return stack;
  }

  private List<ItemStack> addDescription(IRecipeRegistration registry, String langKeySuffix,
                                         Item... items) {
    List<ItemStack> stacks = new ArrayList<ItemStack>();
    for (Item item : items) {
      stacks.add(new ItemStack(item));
    }

    registry.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK,
        Component.literal("jei.description." + Reference.MOD_ID + ":" + langKeySuffix));
    return stacks;
  }

}