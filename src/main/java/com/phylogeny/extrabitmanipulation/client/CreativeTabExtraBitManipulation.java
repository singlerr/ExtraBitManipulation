package com.phylogeny.extrabitmanipulation.client;

import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class CreativeTabExtraBitManipulation {

  public static final CreativeModeTab CREATIVE_TAB = FabricItemGroup.builder()
      .icon(() -> new ItemStack(ItemsExtraBitManipulation.sculptingLoop))
      .build();

}