package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.block.BlockBodyPartTemplate;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlocksExtraBitManipulation {
  public static Block bodyPartTemplate;

  public static void blocksInit() {
    bodyPartTemplate = new BlockBodyPartTemplate(BlockBehaviour.Properties.of().destroyTime(2.0F),
        "bodypart_template");
    Registry.register(BuiltInRegistries.BLOCK,
        new ResourceLocation(Reference.MOD_ID, "bodypart_template"), bodyPartTemplate);
  }

}