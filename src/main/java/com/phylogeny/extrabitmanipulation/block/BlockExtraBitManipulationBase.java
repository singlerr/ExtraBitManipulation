package com.phylogeny.extrabitmanipulation.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockExtraBitManipulationBase extends Block {
  private final String name;

  @SuppressWarnings("null")
  public BlockExtraBitManipulationBase(BlockBehaviour.Properties properties, String name) {
    super(properties.destroyTime(0.5f));
    this.name = name;
//    setRegistryName(name);
//    setUnlocalizedName(getRegistryName().toString());
//    setCreativeTab(CreativeTabExtraBitManipulation.CREATIVE_TAB);
//    setHardness(0.5F);
  }

  public String getCustomName() {
    return name;
  }

}