package com.phylogeny.extrabitmanipulation.block;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockBodyPartTemplate extends BlockExtraBitManipulationBase {

  public BlockBodyPartTemplate(BlockBehaviour.Properties properties, String name) {
    super(properties.destroyTime(0.2F), name);
  }

  @Override
  public void appendHoverText(ItemStack itemStack,
                              @org.jetbrains.annotations.Nullable BlockGetter blockGetter,
                              List<Component> tooltip, TooltipFlag tooltipFlag) {
    tooltip.add(
        Component.literal(
            "The bits of this block are used as bodypart placeholders in the creation of chiseled armor."));

  }

}