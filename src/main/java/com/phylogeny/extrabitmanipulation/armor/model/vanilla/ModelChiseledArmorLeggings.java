package com.phylogeny.extrabitmanipulation.armor.model.vanilla;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class ModelChiseledArmorLeggings<T extends LivingEntity> extends ModelChiseledArmorBase<T> {

  private static LayerDefinition bakeLayer() {
    MeshDefinition meshDefinition = new MeshDefinition();
    PartDefinition partDefinition = meshDefinition.getRoot();
    //Pelvis
    partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 45)
            .addBox(-4.0F, 7.0F, -2.0F, 8, 5, 4),
        PartPose.ZERO);
    partDefinition.addOrReplaceChild("right_left", CubeListBuilder.create()
            .texOffs(25, 40)
            .addBox(-2.0F, 0.0F, -2.0F, 4, 10, 4),
        PartPose.rotation(-1.9F, 12.0F, 0.0F));
    partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(25, 40)
            .addBox(-2.0F, 0.0F, -2.0F, 4, 10, 4),
        PartPose.rotation(1.9F, 12.0F, 0.0F));
    return LayerDefinition.create(meshDefinition, 86, 64);
  }

  public ModelChiseledArmorLeggings() {
    super(bakeLayer().bakeRoot());

  }

}