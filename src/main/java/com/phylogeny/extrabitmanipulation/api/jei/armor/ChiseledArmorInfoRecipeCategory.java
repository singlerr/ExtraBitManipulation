package com.phylogeny.extrabitmanipulation.api.jei.armor;

import com.phylogeny.extrabitmanipulation.api.jei.InfoRecipeCategoryBase;
import com.phylogeny.extrabitmanipulation.api.jei.icon.CategoryIconStackList;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.List;
import mezz.jei.api.IGuiHelper;
import net.minecraft.world.item.ItemStack;

public class ChiseledArmorInfoRecipeCategory
    extends InfoRecipeCategoryBase<ChiseledArmorInfoRecipe> {
  public static final String NAME = "chiseled_armor";
  public static final String UID = Reference.MOD_ID + NAME;

  public ChiseledArmorInfoRecipeCategory(IGuiHelper guiHelper, List<ItemStack> stacks) {
    super(guiHelper, new CategoryIconStackList(16, 16, stacks), NAME, 186, 125);
  }

  @Override
  protected int getSlotPosX() {
    return 60;
  }

  @Override
  public String getUid() {
    return UID;
  }

}