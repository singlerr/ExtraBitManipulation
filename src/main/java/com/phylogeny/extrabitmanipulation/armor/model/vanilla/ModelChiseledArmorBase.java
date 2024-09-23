package com.phylogeny.extrabitmanipulation.armor.model.vanilla;

import com.phylogeny.extrabitmanipulation.extension.PartDefinitionExtension;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public abstract class ModelChiseledArmorBase<T extends LivingEntity> extends HumanoidModel<T> {
  protected float scale;

  protected int textureWidth;
  protected int textureHeight;

  public ModelChiseledArmorBase(ModelPart root) {
    super(root);
    textureWidth = 86;
    textureHeight = 64;
    scale = Configs.armorZFightingBufferScale;
  }

  protected static void setRotationAngles(ModelPart modelRenderer, float angleX, float angleY,
                                          float angleZ) {
    modelRenderer.xRot = angleX;
    modelRenderer.yRot = angleY;
    modelRenderer.zRot = angleZ;
  }

  protected static void setRotationAngles(PartDefinition def, float angleX, float angleY,
                                          float angleZ) {
    if (!(def instanceof PartDefinitionExtension extension)) {
      return;
    }

    extension.ebm$registerBakeListener(
        (modelPart -> setRotationAngles(modelPart, angleX, angleY, angleZ)));
  }

}