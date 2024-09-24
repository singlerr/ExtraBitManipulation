package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.armor.ChiseledArmorStackHandler.ChiseledArmorBakedModel;
import com.phylogeny.extrabitmanipulation.armor.model.vanilla.ModelChiseledArmor;
import com.phylogeny.extrabitmanipulation.armor.model.vanilla.ModelChiseledArmorLeggings;
import com.phylogeny.extrabitmanipulation.block.BlockExtraBitManipulationBase;
import com.phylogeny.extrabitmanipulation.extension.ModelPartExtension;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.item.ItemExtraBitManipulationBase;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.Collections;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class ModelRegistration implements ModelLoadingPlugin {
  private static final String ARMOR_TEXTURE_PATH_DIANOND =
      Reference.MOD_ID + ":textures/armor/chiseled_armor_diamond.png";
  private static final String ARMOR_TEXTURE_PATH_IRON =
      Reference.MOD_ID + ":textures/armor/chiseled_armor_iron.png";
  private static HumanoidModel<? extends LivingEntity> armorModelEmpty, armorModel,
      armorModelLeggings, armorModelMPM,
      armorModelLeggingsMPM, armorModelCNPC, armorModelLeggingsCNPC;

  public static void registerItemModels(Context context) {
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledHelmetDiamond);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledChestplateDiamond);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledLeggingsDiamond);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledBootsDiamond);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledHelmetIron);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledChestplateIron);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledLeggingsIron);
    registerChiseledArmorItemModel(context, ItemsExtraBitManipulation.chiseledBootsIron);
    registerItemModel(context, ItemsExtraBitManipulation.diamondNugget);
    registerItemModel(context, ItemsExtraBitManipulation.bitWrench);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingLoop);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingSquare);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingSpadeCurved);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingSpadeSquared);
    registerItemModel(context, ItemsExtraBitManipulation.modelingTool);
    registerItemModel(context, ItemsExtraBitManipulation.modelingToolHead);
    registerItemModel(context, ItemsExtraBitManipulation.bitWrenchHead);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingLoopHead);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingSquareHead);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingSpadeCurvedHead);
    registerItemModel(context, ItemsExtraBitManipulation.sculptingSpadeSquaredHead);
    registerItemBlockModel(context, BlocksExtraBitManipulation.bodyPartTemplate);
    armorModel = new ModelChiseledArmor();
    armorModelLeggings = new ModelChiseledArmorLeggings();
    armorModelEmpty =
        new HumanoidModel<>(new ModelPart(Collections.emptyList(), Collections.emptyMap()));
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
    ((ModelPartExtension) armorModelEmpty.head).ebm$clearCubeList();
    ((ModelPartExtension) armorModelEmpty.body).ebm$clearCubeList();
    ((ModelPartExtension) armorModelEmpty.rightArm).ebm$clearCubeList();
    ((ModelPartExtension) armorModelEmpty.leftArm).ebm$clearCubeList();
    ((ModelPartExtension) armorModelEmpty.rightLeg).ebm$clearCubeList();
    ((ModelPartExtension) armorModelEmpty.leftLeg).ebm$clearCubeList();
    registerIsolatedModels(context, ItemsExtraBitManipulation.chiseledHelmetDiamond,
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

  private static void registerItemBlockModel(Context context, Block block) {
    Item item = Item.byBlock(block);
    if (item != null) {
      registerItemModel(context, item, ((BlockExtraBitManipulationBase) block).getCustomName());
    }
  }

  private static void registerItemModel(Context context, Item item) {
    registerItemModel(context, item, ((ItemExtraBitManipulationBase) item).getName());
  }

  private static void registerChiseledArmorItemModel(Context context, Item item) {
    ItemChiseledArmor armorPiece = ((ItemChiseledArmor) item);
    ResourceLocation name = armorPiece.getRegistryName();
    if (name == null) {
      return;
    }

    registerItemModel(context, armorPiece, name.getPath());
    registerIsolatedModels(context, armorPiece, armorPiece.initItemModelLocation());
    armorPiece.armorType.initIconStack(armorPiece);
  }

  private static void registerItemModel(Context context, Item item, String name) {
    context.addModels(new ResourceLocation(Reference.MOD_ID, name));
//    ModelLoader.setCustomModelResourceLocation(item, 0,
//        new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, name), "inventory"));
  }

  private static void registerIsolatedModels(Context context, Item item,
                                             ResourceLocation... resourceLocations) {
    context.addModels(resourceLocations);
//    ModelLoader.registerItemVariants(item, resourceLocations);
  }

  public static <T extends LivingEntity> HumanoidModel<T> getArmorModel(ItemStack stack,
                                                                        EquipmentSlot slot,
                                                                        T entity) {
    if (shouldRenderEmptymodel(stack)) {
      return (HumanoidModel<T>) armorModelEmpty;
    }

//    if (CustomNPCsReferences.isLoaded && CustomNPCsModels.isCustomNPC(entity)) {
//      return slot == EntityEquipmentSlot.LEGS ? armorModelLeggingsCNPC : armorModelCNPC;
//    }

    return !MorePlayerModelsReference.isLoaded || !(entity instanceof Player) ?
        (slot == EquipmentSlot.LEGS ? armorModelLeggings : armorModel) :
        (slot == EquipmentSlot.LEGS ? armorModelLeggingsMPM : armorModelMPM);
  }

  public static String getArmorTexture(ItemStack stack, ArmorMaterials material) {
    return shouldRenderEmptymodel(stack) ? null :
        (material == ArmorMaterials.DIAMOND ? ARMOR_TEXTURE_PATH_DIANOND : ARMOR_TEXTURE_PATH_IRON);
  }

  private static boolean shouldRenderEmptymodel(ItemStack stack) {
    return Configs.armorModelRenderMode == ArmorModelRenderMode.NEVER ||
        (Configs.armorModelRenderMode == ArmorModelRenderMode.IF_EMPTY
            && ItemStackHelper.isChiseledArmorNotEmpty(stack));
  }

  @Override
  public void onInitializeModelLoader(Context pluginContext) {
    registerModels(pluginContext);
    ModelRegistration.registerItemModels(pluginContext);
  }

  public enum ArmorModelRenderMode {
    IF_EMPTY, NEVER, ALWAYS
  }

}