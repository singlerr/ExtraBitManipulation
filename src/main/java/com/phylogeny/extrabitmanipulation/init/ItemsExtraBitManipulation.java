package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.config.ConfigProperty;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRender;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRenderPair;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.item.ItemExtraBitManipulationBase;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

public class ItemsExtraBitManipulation {
  private static boolean clientSide;
  public static Item diamondNugget, bitWrench, sculptingLoop, sculptingSquare, sculptingSpadeCurved,
      sculptingSpadeSquared, modelingTool,
      modelingToolHead, bitWrenchHead, sculptingLoopHead, sculptingSquareHead,
      sculptingSpadeCurvedHead,
      sculptingSpadeSquaredHead, chiseledHelmetDiamond, chiseledChestplateDiamond,
      chiseledLeggingsDiamond, chiseledBootsDiamond,
      chiseledHelmetIron, chiseledChestplateIron, chiseledLeggingsIron, chiseledBootsIron;

  public static void itemsInit() {
    chiseledHelmetDiamond =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_helmet", ArmorMaterials.DIAMOND,
            ArmorType.HELMET,
            ArmorMovingPart.HEAD);
    chiseledChestplateDiamond =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_chestplate", ArmorMaterials.DIAMOND,
            ArmorType.CHESTPLATE,
            ArmorMovingPart.TORSO, ArmorMovingPart.ARM_RIGHT, ArmorMovingPart.ARM_LEFT);
    chiseledLeggingsDiamond =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_leggings", ArmorMaterials.DIAMOND,
            ArmorType.LEGGINGS,
            ArmorMovingPart.PELVIS, ArmorMovingPart.LEG_RIGHT, ArmorMovingPart.LEG_LEFT);
    chiseledBootsDiamond =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_boots", ArmorMaterials.DIAMOND,
            ArmorType.BOOTS, ArmorMovingPart.FOOT_RIGHT, ArmorMovingPart.FOOT_LEFT);
    chiseledHelmetIron =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_helmet_iron", ArmorMaterials.IRON,
            ArmorType.HELMET,
            ArmorMovingPart.HEAD);
    chiseledChestplateIron =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_chestplate_iron",
            ArmorMaterials.IRON, ArmorType.CHESTPLATE,
            ArmorMovingPart.TORSO, ArmorMovingPart.ARM_RIGHT, ArmorMovingPart.ARM_LEFT);
    chiseledLeggingsIron =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_leggings_iron", ArmorMaterials.IRON,
            ArmorType.LEGGINGS,
            ArmorMovingPart.PELVIS, ArmorMovingPart.LEG_RIGHT, ArmorMovingPart.LEG_LEFT);
    chiseledBootsIron =
        new ItemChiseledArmor(new Item.Properties(), "chiseled_boots_iron", ArmorMaterials.IRON,
            ArmorType.BOOTS, ArmorMovingPart.FOOT_RIGHT, ArmorMovingPart.FOOT_LEFT);
    diamondNugget = new ItemExtraBitManipulationBase(new Item.Properties(), "diamond_nugget");
    bitWrench = new ItemBitWrench(new Item.Properties(), "bit_wrench");
    sculptingLoop = new ItemSculptingTool(new Item.Properties(), true, true, "sculpting_loop");
    sculptingSquare = new ItemSculptingTool(new Item.Properties(), false, true, "sculpting_square");
    sculptingSpadeCurved =
        new ItemSculptingTool(new Item.Properties(), true, false, "sculpting_spade_curved");
    sculptingSpadeSquared =
        new ItemSculptingTool(new Item.Properties(), false, false, "sculpting_spade_squared");
    modelingTool = new ItemModelingTool(new Item.Properties(), "modeling_tool");
    modelingToolHead =
        new ItemExtraBitManipulationBase(new Item.Properties(), "modeling_tool_head");
    bitWrenchHead = new ItemExtraBitManipulationBase(new Item.Properties(), "bit_wrench_head");
    sculptingLoopHead =
        new ItemExtraBitManipulationBase(new Item.Properties(), "sculpting_loop_head");
    sculptingSquareHead =
        new ItemExtraBitManipulationBase(new Item.Properties(), "sculpting_square_head");
    sculptingSpadeCurvedHead =
        new ItemExtraBitManipulationBase(new Item.Properties(), "sculpting_spade_curved_head");
    sculptingSpadeSquaredHead =
        new ItemExtraBitManipulationBase(new Item.Properties(), "sculpting_spade_squared_head");
    initBitToolProperties(bitWrench, "Bit Wrench");
    initBitToolProperties(sculptingLoop, "Curved Sculpting Wire");
    initBitToolProperties(sculptingSquare, "Straight Sculpting Wire");
    initBitToolProperties(sculptingSpadeCurved, "Curved Sculpting Spade");
    initBitToolProperties(sculptingSpadeSquared, "Flat Sculpting Spade");
    initBitToolProperties(modelingTool, "Modeling Tool");
    clientSide = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "diamond_nugget"), diamondNugget);
    Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Reference.MOD_ID, "bit_wrench"),
        bitWrench);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_loop"), sculptingLoop);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_square"), sculptingSquare);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_spade_curved"), sculptingSpadeCurved);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_spade_squared"), sculptingSpadeSquared);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "modeling_tool"), modelingTool);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "modeling_tool_head"), modelingToolHead);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "bit_wrench_head"), bitWrenchHead);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_loop_head"), sculptingLoopHead);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_square_head"), sculptingSquareHead);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_spade_curved_head"),
        sculptingSpadeCurvedHead);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "sculpting_spade_squared_head"),
        sculptingSpadeSquaredHead);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_helmet"), chiseledHelmetDiamond);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_chestplate"), chiseledChestplateDiamond);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_leggings"), chiseledLeggingsDiamond);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_boots"), chiseledBootsDiamond);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_helmet_iron"), chiseledHelmetIron);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_chestplate_iron"), chiseledChestplateIron);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_leggings_iron"), chiseledLeggingsIron);
    Registry.register(BuiltInRegistries.ITEM,
        new ResourceLocation(Reference.MOD_ID, "chiseled_boots_iron"), chiseledBootsIron);

    if (clientSide) {
      ModelRegistration.registerItemModels();
    }
  }

  private static void initBitToolProperties(Item item, String itemTitle) {
    boolean isSculptingTool = item instanceof ItemSculptingTool;
    Configs.itemPropertyMap.put(item, new ConfigProperty(itemTitle, true,
        isSculptingTool ? 2000000 : (item instanceof ItemBitWrench ? 5000 : 1000)));
    if (isSculptingTool) {
      ItemSculptingTool itemTool = (ItemSculptingTool) item;
      ConfigShapeRender boundingBox =
          itemTool.removeBits() ? Configs.itemShapes[0] : Configs.itemShapes[1];
      Configs.itemShapeMap.put(item, new ConfigShapeRenderPair(boundingBox,
          itemTool.removeBits() ? Configs.itemShapes[2] : Configs.itemShapes[3]));
    }
  }

}