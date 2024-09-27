package com.phylogeny.extrabitmanipulation.extension;

import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;

public interface MinecraftExtension {

  BlockEntityWithoutLevelRenderer ebm$getBlockEntityWithoutLevelRenderer();

  ItemColors ebm$getItemColors();

}
