package com.phylogeny.extrabitmanipulation.armor;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.extension.IllagerModelExtension;
import com.phylogeny.extrabitmanipulation.extension.VillagerModelExtension;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class LayerChiseledArmor<M extends EntityModel<LivingEntity>>
    extends RenderLayer<LivingEntity, M> {
  private final Map<CompoundTag, List<Integer>> movingPartsDisplayListsMap =
      new HashMap<CompoundTag, List<Integer>>();
  private ModelPart head, body, villagerArms, rightLeg, leftLeg, rightArm, leftArm;
  private Model model;
  private boolean smallArms, isIllager, isVex;
  private RenderLayerParent<LivingEntity, M> livingEntityRenderer;

  public LayerChiseledArmor(RenderLayerParent<LivingEntity, M> renderLayerParent) {
    super(renderLayerParent);
    this.livingEntityRenderer = livingEntityRenderer;
    updateModelAndRenderers(false);
  }

  public void updateModelAndRenderers(boolean force) {
    EntityModel<LivingEntity> modelNew = livingEntityRenderer.getModel();
    if (!force && modelNew == model) {
      return;
    }

    model = modelNew;
    if (model instanceof VillagerModel<?> modelVillager) {
      VillagerModelExtension ext = (VillagerModelExtension) modelVillager;
      head = modelVillager.getHead();
      body = ext.ebm$getBody();
      rightLeg = ext.ebm$getRightLeg();
      leftLeg = ext.ebm$getLeftLeg();
      villagerArms = ext.ebm$getArms();
    } else if (model instanceof IllagerModel<?> modelIllager) {
      IllagerModelExtension ext = (IllagerModelExtension) modelIllager;
      head = modelIllager.getHead();
      body = ext.ebm$getBody();
      rightLeg = ext.ebm$getRightLeg();
      leftLeg = ext.ebm$getLeftLeg();
      villagerArms = ext.ebm$getArms();
      rightArm = ext.ebm$getRightArm();
      leftArm = ext.ebm$getLeftArm();
      isIllager = true;
    } else {
      HumanoidModel<?> modelBiped = ((HumanoidModel<?>) model);
      head = modelBiped.head;
      body = modelBiped.body;
      rightLeg = modelBiped.rightLeg;
      leftLeg = modelBiped.leftLeg;
      rightArm = modelBiped.rightArm;
      leftArm = modelBiped.leftArm;
      villagerArms = null;
      if (model instanceof PlayerModel<?>) {
        smallArms = ReflectionExtraBitManipulation.areArmsSmall((PlayerModel<?>) model);
      }
    }

    isVex = model instanceof VexModel;
  }

  public void clearDisplayListsMap() {
    for (List<Integer> displayLists : movingPartsDisplayListsMap.values()) {
      deleteDisplayLists(displayLists);
    }

    movingPartsDisplayListsMap.clear();
  }

  public void removeFromDisplayListsMap(CompoundTag nbt) {
    deleteDisplayLists(movingPartsDisplayListsMap.remove(nbt));
  }

  private void deleteDisplayLists(List<Integer> displayLists) {
    if (displayLists != null) {
      for (Integer displayList : displayLists) {
        GL11.glDeleteLists(displayList, 1);
//        GLAllocation.deleteDisplayLists(displayList);
      }
    }
  }

  public static boolean isPlayerModelAlt(LivingEntity entity, float partialTicks) {
    if (entity instanceof Player ||
        (!MorePlayerModelsReference.isLoaded && !CustomNPCsReferences.isLoaded)) {
      return false;
    }

    Player player = Minecraft.getInstance().player;
    return entity.xOld + (entity.getX() - entity.xOld) * partialTicks ==
        player.xOld + (player.getX() - player.xOld) * partialTicks
        && entity.yOld + (entity.getY() - entity.yOld) * partialTicks ==
        player.yOld + (player.getY() - player.yOld) * partialTicks
        && entity.zOld + (entity.getZ() - entity.zOld) * partialTicks ==
        player.zOld + (player.getZ() - player.zOld) * partialTicks;
  }

  @Override
  public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int k,
                     LivingEntity entity, float limbSwing, float limbSwingAmount,
                     float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
    float scale = 1.0f;
    updateModelAndRenderers(false);
    GlStateManager._enableBlend();
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    ClientHelper.bindTexture(TextureAtlas.LOCATION_BLOCKS);
    LivingEntity entityCap =
        isPlayerModelAlt(entity, partialTicks) ? Minecraft.getInstance().player : entity;
    IChiseledArmorSlotsHandler cap = entityCap instanceof Player ?
        ChiseledArmorSlotsHandler.getCapability((Player) entityCap).orElse(null) : null;
    List<Integer> displayListsHelmet =
        getStackDisplayLists(entity, scale, ArmorType.HELMET, poseStack, multiBufferSource, k,
            OverlayTexture.NO_OVERLAY, 0);
    List<Integer> displayListsSlotHelmet =
        getSlotStackDisplayLists(entity, scale, cap, ArmorType.HELMET, poseStack, multiBufferSource,
            k,
            OverlayTexture.NO_OVERLAY, 0);
    if (displayListsHelmet != null || displayListsSlotHelmet != null) {
      GL11.glPushMatrix();
      adjustForSneaking(entity);
      if (entity.isBaby() && !(entity instanceof Villager)) {
        GL11.glScalef(0.75F, 0.75F, 0.75F);
        GL11.glTranslatef(0.0F, 1.0F, 0.0F);
      }
      head.translateAndRotate(poseStack);
//      head.postRender(scale);
      GL11.glTranslatef(0.0F, -scale * (8 + Configs.armorZFightingBufferScale), 0.0F);
      GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
      GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);

      if (entity instanceof Villager || entity instanceof ZombieVillager ||
          entity instanceof net.minecraft.world.entity.monster.AbstractIllager) {
        GL11.glTranslatef(0.0F, scale * 2, 0.0F);
      }

      GL11.glPushMatrix();
      if (displayListsHelmet != null && (cap == null || !cap.hasArmorType(0))) {
        GL11.glCallList(displayListsHelmet.get(0));
      }

      GL11.glPopMatrix();
      if (displayListsSlotHelmet != null) {
        for (Integer i : displayListsSlotHelmet) {
          GL11.glCallList(i);
        }
      }
      GL11.glPopMatrix();
    }
    List<Integer> displayListsChestplate =
        getStackDisplayLists(entity, scale, ArmorType.CHESTPLATE, poseStack, multiBufferSource, k,
            OverlayTexture.NO_OVERLAY, 0);
    List<Integer> displayListsSlotChestplate =
        getSlotStackDisplayLists(entity, scale, cap, ArmorType.CHESTPLATE, poseStack,
            multiBufferSource, k,
            OverlayTexture.NO_OVERLAY, 0);
    if (displayListsChestplate != null || displayListsSlotChestplate != null) {
      GL11.glPushMatrix();
      adjustForSneaking(entity);
      adjustForChildModel();
      boolean isPassive = !isIllager ||
          ((AbstractIllager) entity).getArmPose() == AbstractIllager.IllagerArmPose.CROSSED;
      GL11.glPushMatrix();
      if (displayListsChestplate != null && (cap == null || !cap.hasArmorType(1))) {
        renderArmorPiece(body, poseStack, displayListsChestplate.get(0), scale, 8);
        renderSleeve(poseStack, displayListsChestplate.get(1), HumanoidArm.RIGHT, scale, isPassive);
        renderSleeve(poseStack, displayListsChestplate.get(2), HumanoidArm.LEFT, scale, isPassive);
      }
      GL11.glPopMatrix();
      if (displayListsSlotChestplate != null) {
        for (int i = 0; i < displayListsSlotChestplate.size(); i += 3) {
          renderArmorPiece(body, poseStack, displayListsSlotChestplate.get(i), scale, 8);
          renderSleeve(poseStack, displayListsSlotChestplate.get(i + 1), HumanoidArm.RIGHT, scale,
              isPassive);
          renderSleeve(poseStack, displayListsSlotChestplate.get(i + 2), HumanoidArm.LEFT, scale,
              isPassive);
        }
      }
      GL11.glPopMatrix();
    }
    List<Integer> displayListsLeggings =
        getStackDisplayLists(entity, scale, ArmorType.LEGGINGS, poseStack, multiBufferSource, k,
            OverlayTexture.NO_OVERLAY, 0);
    List<Integer> displayListsSlotLeggings =
        getSlotStackDisplayLists(entity, scale, cap, ArmorType.LEGGINGS, poseStack,
            multiBufferSource, k,
            OverlayTexture.NO_OVERLAY, 0);
    if (displayListsLeggings != null || displayListsSlotLeggings != null) {
      GL11.glPushMatrix();
      adjustForSneaking(entity);
      adjustForChildModel();
      GL11.glPushMatrix();
      if (displayListsLeggings != null && (cap == null || !cap.hasArmorType(2))) {
        renderArmorPiece(body, poseStack, displayListsLeggings.get(0), scale, 4);
        renderLegPieces(poseStack, displayListsLeggings.get(1), displayListsLeggings.get(2), scale,
            8);
      }
      GL11.glPopMatrix();
      if (displayListsSlotLeggings != null) {
        for (int i = 0; i < displayListsSlotLeggings.size(); i += 3) {
          renderArmorPiece(body, poseStack, displayListsSlotLeggings.get(i), scale, 4);
          renderLegPieces(poseStack, displayListsSlotLeggings.get(i + 1),
              displayListsSlotLeggings.get(i + 2), scale, 8);
        }
      }
      GL11.glPopMatrix();
    }
    List<Integer> displayListsBoots =
        getStackDisplayLists(entity, scale, ArmorType.BOOTS, poseStack, multiBufferSource, k,
            OverlayTexture.NO_OVERLAY, 0);
    List<Integer> displayListsSlotBoots =
        getSlotStackDisplayLists(entity, scale, cap, ArmorType.BOOTS, poseStack, multiBufferSource,
            k,
            OverlayTexture.NO_OVERLAY, 0);
    if (displayListsBoots != null || displayListsSlotBoots != null) {
      GL11.glPushMatrix();
      adjustForSneaking(entity);
      adjustForChildModel();
      GL11.glTranslatef(0.0F, scale * (Configs.armorZFightingBufferTranslationFeet), 0.0F);
      GL11.glPushMatrix();
      if (displayListsBoots != null && (cap == null || !cap.hasArmorType(3))) {
        renderLegPieces(poseStack, displayListsBoots.get(0), displayListsBoots.get(1), scale, 4);
      }

      GL11.glPopMatrix();
      if (displayListsSlotBoots != null) {
        for (int i = 0; i < displayListsSlotBoots.size(); i += 2) {
          renderLegPieces(poseStack, displayListsSlotBoots.get(i), displayListsSlotBoots.get(i + 1),
              scale, 4);
        }
      }

      GL11.glPopMatrix();
    }
    GlStateManager._disableBlend();

  }

  private List<Integer> getStackDisplayLists(LivingEntity entity, float scale,
                                             ArmorType armorType, PoseStack poseStack,
                                             MultiBufferSource bufferSource, int l, int j,
                                             int k) {
    return getDisplayLists(poseStack, bufferSource, l, j, k, entity, scale, armorType,
        entity.getItemBySlot(armorType.getEquipmentSlot()), null);
  }

  private List<Integer> getSlotStackDisplayLists(LivingEntity entity, float scale,
                                                 IChiseledArmorSlotsHandler cap,
                                                 ArmorType armorType, PoseStack poseStack,
                                                 MultiBufferSource bufferSource, int l, int j,
                                                 int k) {
    if (cap == null || !cap.hasArmor()) {
      return null;
    }

    return getDisplayLists(poseStack, bufferSource, l, j, k, entity, scale, armorType,
        ItemStack.EMPTY, cap);
  }

  private List<Integer> getDisplayLists(PoseStack poseStack, MultiBufferSource bufferSource, int l,
                                        int j,
                                        int k, LivingEntity entity, float scale,
                                        ArmorType armorType,
                                        ItemStack stack, @Nullable IChiseledArmorSlotsHandler cap) {
    List<Integer> displayLists = null;
    int countSet = cap == null ? 1 : ChiseledArmorSlotsHandler.COUNT_SETS;
    for (int i = 0; i < countSet; i++) {
      if (cap != null) {
        if (!cap.hasArmorSet(i)) {
          continue;
        }

        stack = cap.getStackInSlot(i * ChiseledArmorSlotsHandler.COUNT_TYPES + armorType.ordinal());
      }
      if (stack.hasTag() && stack.getItem() instanceof ItemChiseledArmor) {
        CompoundTag nbt = stack.getTag();
        CompoundTag armoreData = ItemStackHelper.getArmorData(nbt);
        if (!armoreData.getBoolean(NBTKeys.ARMOR_NOT_EMPTY)) {
          continue;
        }

        List<Integer> displayListsItem = movingPartsDisplayListsMap.get(armoreData);
        if (displayListsItem == null) {
          displayListsItem =
              addMovingPartsDisplayListsToMap(entity, scale, nbt, armorType, poseStack,
                  bufferSource, l, j, k);
        }

        if (displayLists == null) {
          displayLists = new ArrayList<>();
        }

        displayLists.addAll(displayListsItem);
      }
    }
    return displayLists;
  }

  private void adjustForSneaking(LivingEntity entity) {
    if (entity.isShiftKeyDown()) {
      GL11.glTranslatef(0.0F, 0.2F, 0.0F);
    }
  }

  private void adjustForChildModel() {

    if ((model instanceof EntityModel<?> entityModel) && entityModel.young) {
      GL11.glTranslatef(0.0F, 0.75F, 0.0F);
      GL11.glScalef(0.5F, 0.5F, 0.5F);
    }
  }

  private List<Integer> addMovingPartsDisplayListsToMap(LivingEntity entity, float scale,
                                                        CompoundTag armorNbt, ArmorType armorType,
                                                        PoseStack poseStack,
                                                        MultiBufferSource bufferSource, int l,
                                                        int j,
                                                        int k) {
    List<Integer> movingPartsDisplayLists = new ArrayList<>();
    for (int i = 0; i < armorType.getMovingpartCount(); i++) {
      movingPartsDisplayLists.add(
          new DataChiseledArmorPiece(armorNbt, armorType).generateDisplayList(i, entity, scale,
              poseStack, bufferSource, l, j, k));
    }

    movingPartsDisplayListsMap.put(ItemStackHelper.getArmorData(armorNbt), movingPartsDisplayLists);
    return movingPartsDisplayLists;
  }

  private void renderLegPieces(PoseStack poseStack, int displayListRight, int displayListLeft,
                               float scale,
                               float offsetY) {
    renderArmorPiece(rightLeg, poseStack, displayListRight, scale, isVex ? -scale * 2 : 0.0F,
        offsetY);
    if (!isVex) {
      renderArmorPiece(leftLeg, poseStack, displayListLeft, scale, offsetY);
    }
  }

  private void renderArmorPiece(ModelPart modelArmorPiece, PoseStack poseStack, int displayList,
                                float scale,
                                float offsetX, float offsetY) {
    GL11.glPushMatrix();
    modelArmorPiece.translateAndRotate(poseStack);
    renderArmorPiece(displayList, scale, offsetX, offsetY);
  }

  private void renderArmorPiece(ModelPart modelArmorPiece, PoseStack poseStack, int displayList,
                                float scale,
                                float offsetY) {
    renderArmorPiece(modelArmorPiece, poseStack, displayList, scale, 0.0F, offsetY);
  }

  private void renderArmorPiece(int displayList, float scale, float offsetX, float offsetY) {
    GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
    GL11.glTranslatef(offsetX, -scale * offsetY, 0.0F);
    GL11.glCallList(displayList);
    GL11.glPopMatrix();
  }

  private void renderSleeve(PoseStack poseStack, int displayList, HumanoidArm handSide, float scale,
                            boolean isPassive) {
    GL11.glPushMatrix();
    int armOffset;
    if (villagerArms != null && isPassive) {
      villagerArms.translateAndRotate(poseStack);
      armOffset = 6;
    } else {
      ModelPart modelArm = handSide == HumanoidArm.RIGHT ? rightArm : leftArm;
      if (smallArms) {
        float f = handSide == HumanoidArm.RIGHT ? -0.5F : 0.5F;
        modelArm.xRot += f;
        ((HumanoidModel<?>) model).translateToHand(handSide, poseStack);
        modelArm.xRot -= f;
      } else {
        modelArm.translateAndRotate(poseStack);
      }

      armOffset = 1;
    }
    renderArmorPiece(displayList, scale,
        (handSide == HumanoidArm.LEFT ? -armOffset : armOffset) * scale, 6);
  }


}