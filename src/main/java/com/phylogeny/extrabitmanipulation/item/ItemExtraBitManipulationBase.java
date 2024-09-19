package com.phylogeny.extrabitmanipulation.item;

import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemExtraBitManipulationBase extends Item {
  private final String name;
  private final ResourceLocation registryName;

  @SuppressWarnings("null")
  public ItemExtraBitManipulationBase(Item.Properties properties, String name) {
    super(properties);
    this.name = name;
    this.registryName = new ResourceLocation(Reference.MOD_ID, name);
//    setUnlocalizedName(getRegistryName().toString());
//    setCreativeTab(CreativeTabExtraBitManipulation.CREATIVE_TAB);
  }

  public ResourceLocation getRegistryName() {
    return registryName;
  }

  public String getName() {
    return name;
  }

}