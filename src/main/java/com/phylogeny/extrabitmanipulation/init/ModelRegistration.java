package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.armor.ChiseledArmorStackHandeler.ChiseledArmorBakedModel;
import com.phylogeny.extrabitmanipulation.armor.model.vanilla.ModelChiseledArmor;
import com.phylogeny.extrabitmanipulation.armor.model.vanilla.ModelChiseledArmorLeggings;
import com.phylogeny.extrabitmanipulation.block.BlockExtraBitManipulationBase;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.item.ItemExtraBitManipulationBase;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.ModelLoader;
import org.jetbrains.annotations.Nullable;

public class ModelRegistration implements ModelLoadingPlugin {
  private static final String ARMOR_TEXTURE_PATH_DIANOND =
      Reference.MOD_ID + ":textures/armor/chiseled_armor_diamond.png";
  private static final String ARMOR_TEXTURE_PATH_IRON =
      Reference.MOD_ID + ":textures/armor/chiseled_armor_iron.png";
  private static ModelPart armorModelEmpty, armorModel, armorModelLeggings, armorModelMPM,
      armorModelLeggingsMPM, armorModelCNPC, armorModelLeggingsCNPC;

  public static void registerItemModels() {
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledHelmetDiamond);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledChestplateDiamond);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledLeggingsDiamond);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledBootsDiamond);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledHelmetIron);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledChestplateIron);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledLeggingsIron);
    registerChiseledArmorItemModel(ItemsExtraBitManipulation.chiseledBootsIron);
    registerItemModel(ItemsExtraBitManipulation.diamondNugget);
    registerItemModel(ItemsExtraBitManipulation.bitWrench);
    registerItemModel(ItemsExtraBitManipulation.sculptingLoop);
    registerItemModel(ItemsExtraBitManipulation.sculptingSquare);
    registerItemModel(ItemsExtraBitManipulation.sculptingSpadeCurved);
    registerItemModel(ItemsExtraBitManipulation.sculptingSpadeSquared);
    registerItemModel(ItemsExtraBitManipulation.modelingTool);
    registerItemModel(ItemsExtraBitManipulation.modelingToolHead);
    registerItemModel(ItemsExtraBitManipulation.bitWrenchHead);
    registerItemModel(ItemsExtraBitManipulation.sculptingLoopHead);
    registerItemModel(ItemsExtraBitManipulation.sculptingSquareHead);
    registerItemModel(ItemsExtraBitManipulation.sculptingSpadeCurvedHead);
    registerItemModel(ItemsExtraBitManipulation.sculptingSpadeSquaredHead);
    registerItemBlockModel(BlocksExtraBitManipulation.bodyPartTemplate);
    armorModel = new ModelChiseledArmor();
    armorModelLeggings = new ModelChiseledArmorLeggings();
    armorModelEmpty = new ModelBiped();
//    if (MorePlayerModelsReference.isLoaded) {
//      MorePlayerModelsModels.initModels();
//      armorModelMPM = MorePlayerModelsModels.ARMOR_MODEL_MPM;
//      armorModelLeggingsMPM = MorePlayerModelsModels.ARMOR_MODEL_LEGGINGS_MPM;
//    }
//    if (CustomNPCsReferences.isLoaded) {
//      CustomNPCsModels.initModels();
//      armorModelCNPC = CustomNPCsModels.ARMOR_MODEL_CNPC;
//      armorModelLeggingsCNPC = CustomNPCsModels.ARMOR_MODEL_LEGGINGS_CNPC;
//    }
    armorModelEmpty.bipedHead.cubeList.clear();
    armorModelEmpty.bipedBody.cubeList.clear();
    armorModelEmpty.bipedRightArm.cubeList.clear();
    armorModelEmpty.bipedLeftArm.cubeList.clear();
    armorModelEmpty.bipedRightLeg.cubeList.clear();
    armorModelEmpty.bipedLeftLeg.cubeList.clear();
    registerIsolatedModels(ItemsExtraBitManipulation.chiseledHelmetDiamond,
        ArmorMovingPart.initAndGetIconModelLocations());
  }

  private void registerModels(Context event) {
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledHelmetDiamond);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledChestplateDiamond);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledLeggingsDiamond);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledBootsDiamond);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledHelmetIron);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledChestplateIron);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledLeggingsIron);
    registerBakedItemModel(event, ItemsExtraBitManipulation.chiseledBootsIron);
  }

  private static void registerBakedItemModel(Context event, Item item) {
    event.modifyModelAfterBake().register(new ModelModifier.AfterBake() {
      @Override
      public @Nullable BakedModel modifyModelAfterBake(@Nullable BakedModel model,
                                                       Context context) {
        ResourceLocation registryId = BuiltInRegistries.ITEM.getKey(item);
        if (!registryId.equals(context.id())) {
          return model;
        }

        return new ChiseledArmorBakedModel();
      }
    });
  }

  private static void registerItemBlockModel(Block block) {
    Item item = Item.byBlock(block);
    if (item != null) {
      registerItemModel(item, ((BlockExtraBitManipulationBase) block).getCustomName());
    }
  }

  private static void registerItemModel(Item item) {
    registerItemModel(item, ((ItemExtraBitManipulationBase) item).getName());
  }

  private static void registerChiseledArmorItemModel(Item item) {
    ItemChiseledArmor armorPiece = ((ItemChiseledArmor) item);
    ResourceLocation name = armorPiece.getRegistryName();
    if (name == null) {
      return;
    }

    registerItemModel(armorPiece, name.getPath());
    registerIsolatedModels(armorPiece, armorPiece.initItemModelLocation());

    armorPiece.armorType.initIconStack(armorPiece);
  }

  private static void registerItemModel(Context context, Item item, String name) {
    context.addModels(new ResourceLocation(Reference.MOD_ID, name));
//    ModelLoader.setCustomModelResourceLocation(item, 0,
//        new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, name), "inventory"));
  }

  private static void registerIsolatedModels(Item item, ResourceLocation... resourceLocations) {
    ModelLoader.registerItemVariants(item, resourceLocations);
  }

  public static HumanoidModel<LivingEntity> getArmorModel(ItemStack stack, EquipmentSlot slot,
                                                          LivingEntity entity) {
    if (shouldRenderEmptymodel(stack)) {
      return armorModelEmpty;
    }

    if (CustomNPCsReferences.isLoaded && CustomNPCsModels.isCustomNPC(entity)) {
      return slot == EntityEquipmentSlot.LEGS ? armorModelLeggingsCNPC : armorModelCNPC;
    }

    return (!MorePlayerModelsReference.isLoaded || !(entity instanceof EntityPlayer) ?
        (slot == EntityEquipmentSlot.LEGS ? armorModelLeggings : armorModel) :
        (slot == EntityEquipmentSlot.LEGS ? armorModelLeggingsMPM : armorModelMPM));
  }

  public static String getArmorTexture(ItemStack stack, ArmorMaterial material) {
    return shouldRenderEmptymodel(stack) ? null :
        (material == ArmorMaterial.DIAMOND ? ARMOR_TEXTURE_PATH_DIANOND : ARMOR_TEXTURE_PATH_IRON);
  }

  private static boolean shouldRenderEmptymodel(ItemStack stack) {
    return Configs.armorModelRenderMode == ArmorModelRenderMode.NEVER ||
        (Configs.armorModelRenderMode == ArmorModelRenderMode.IF_EMPTY
            && ItemStackHelper.isChiseledArmorNotEmpty(stack));
  }

  @Override
  public void onInitializeModelLoader(Context pluginContext) {
    registerModels(pluginContext);
  }

  public enum ArmorModelRenderMode {
    IF_EMPTY, NEVER, ALWAYS
  }

}