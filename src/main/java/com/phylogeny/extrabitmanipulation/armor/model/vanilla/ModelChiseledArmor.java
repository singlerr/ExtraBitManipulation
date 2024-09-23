package com.phylogeny.extrabitmanipulation.armor.model.vanilla;

import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ModelChiseledArmor<T extends LivingEntity> extends ModelChiseledArmorBase<T> {
  private static final Set<Direction> ALL_VISIBLE = EnumSet.allOf(Direction.class);

  //Create layer definition
  private static LayerDefinition bakeLayer() {
    MeshDefinition meshDefinition = new MeshDefinition();
    PartDefinition partDefinition = meshDefinition.getRoot();

    float angle90 = (float) Math.toRadians(90);
    float angle180 = (float) Math.toRadians(180);
    float angle270 = (float) Math.toRadians(270);
    //Head
    partDefinition.addOrReplaceChild("hat", CubeListBuilder.create()
            .texOffs(0, 0),
        PartPose.ZERO);

    PartDefinition head = partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-5.0F, -8.0F, -4.0F, 1, 3, 9),
        PartPose.ZERO);

    PartDefinition headFront1 = head.addOrReplaceChild("headFront1", CubeListBuilder.create()
            .texOffs(44, 0)
            .addBox(-5.0F, -8.0F, -4.0F, 1, 3, 9),
        PartPose.ZERO);
    setRotationAngles(headFront1, 0.0F, angle270, 0.0F);

    head.addOrReplaceChild("headFront2", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(0.0F, 0.0F, 0.0F, 2, 2, 1),
        PartPose.rotation(-1.0F, -5.0F, -5.0F));

    PartDefinition headBack1 = head.addOrReplaceChild("headBack1", CubeListBuilder.create()
            .texOffs(44, 0)
            .addBox(-5.0F, -8.0F, -4.0F, 1, 3, 9),
        PartPose.rotation(-2.0F, -2.0F, 4.0F));
    setRotationAngles(headBack1, 0.0F, angle90, 0.0F);

    head.addOrReplaceChild("headBack2", CubeListBuilder.create()
            .texOffs(31, 0)
            .addBox(0.0F, 0.0F, 0.0F, 10, 3, 1),
        PartPose.rotation(-5.0F, -5.0F, 4.0F));

    head.addOrReplaceChild("headBack3", CubeListBuilder.create()
            .texOffs(0, 4)
            .addBox(0.0F, 0.0F, 0.0F, 4, 1, 1),
        PartPose.rotation(-2.0F, -2.0F, 4.0F));

    head.addOrReplaceChild("headRight1", CubeListBuilder.create()
            .texOffs(44, 0)
            .addBox(-5.0F, -8.0F, -4.0F, 1, 3, 9),
        PartPose.ZERO);

    head.addOrReplaceChild("headRight2", CubeListBuilder.create()
            .texOffs(0, 12)
            .addBox(0.0F, 0.0F, 0.0F, 1, 1, 5),
        PartPose.rotation(4.0F, -5.0F, -5.0F));

    head.addOrReplaceChild("headRight3", CubeListBuilder.create()
            .texOffs(26, 12)
            .addBox(0.0F, 0.0F, 0.0F, 1, 2, 4),
        PartPose.rotation(4.0F, -5.0F, 0.0F));

    PartDefinition headLeft1 = head.addOrReplaceChild("headLeft1", CubeListBuilder.create()
            .texOffs(44, 0)
            .addBox(-5.0F, -8.0F, -4.0F, 1, 3, 9),
        PartPose.ZERO);
    setRotationAngles(headLeft1, 0.0F, angle180, 0.0F);

    head.addOrReplaceChild("headLeft2", CubeListBuilder.create()
            .texOffs(13, 12)
            .addBox(0.0F, 0.0F, 0.0F, 1, 1, 5),
        PartPose.rotation(-5.0F, -5.0F, -5.0F));

    head.addOrReplaceChild("headLeft3", CubeListBuilder.create()
            .texOffs(37, 12)
            .addBox(0.0F, 0.0F, 0.0F, 1, 2, 4),
        PartPose.rotation(-5.0F, -5.0F, 0.0F));
    //Body

    PartDefinition body = partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(64, 52)
            .addBox(-5.0F, -1.0F, 2.0F, 10, 11, 1),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyRight", CubeListBuilder.create()
            .texOffs(64, 30)
            .addBox(-5.0F, 4.0F, -2.0F, 1, 6, 4),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyLeft", CubeListBuilder.create()
            .texOffs(75, 30)
            .addBox(4.0F, 4.0F, -2.0F, 1, 6, 4),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyBack", CubeListBuilder.create()
            .texOffs(68, 12)
            .addBox(-4.0F, 10.0F, 2.0F, 8, 1, 1),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyTop1", CubeListBuilder.create()
            .texOffs(68, 23)
            .addBox(-5.0F, -1.0F, -3.0F, 3, 1, 5),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyTop2", CubeListBuilder.create()
            .texOffs(68, 16)
            .addBox(2.0F, -1.0F, -3.0F, 3, 1, 5),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyFront1", CubeListBuilder.create()
            .texOffs(64, 41)
            .addBox(-5.0F, 1.0F, -3.0F, 10, 9, 1),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyFront2", CubeListBuilder.create()
            .texOffs(68, 9)
            .addBox(-4.0F, 10.0F, -3.0F, 8, 1, 1),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyFront3", CubeListBuilder.create()
            .texOffs(68, 6)
            .addBox(-3.0F, 11.0F, -3.0F, 6, 1, 1),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyFront4", CubeListBuilder.create()
            .texOffs(68, 3)
            .addBox(-5.0F, 0.0F, -3.0F, 4, 1, 1),
        PartPose.ZERO);

    body.addOrReplaceChild("bodyFront5", CubeListBuilder.create()
            .texOffs(68, 0)
            .addBox(1.0F, 0.0F, -3.0F, 4, 1, 1),
        PartPose.ZERO);

    //Right Arm
    PartDefinition rightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
            .texOffs(0, 0),
        PartPose.rotation(-5.0F, 2.0F, 0.0F));

    PartDefinition armRightTop = rightArm.addOrReplaceChild("armRightTop", CubeListBuilder.create()
            .texOffs(0, 22)
            .addBox(-7.0F, -11.0F, -3.0F, 6, 1, 6),
        PartPose.rotation(-5.0F, 8.0F, 0.0F));
    setRotationAngles(armRightTop, 0.0F, angle180, 0.0F);
    PartDefinition armRightFront1 =
        rightArm.addOrReplaceChild("armRightFront1", CubeListBuilder.create()
                .texOffs(25, 23)
                .addBox(6.0F, -2.0F, -3.0F, 3, 5, 1),
            PartPose.rotation(5.0F, 0.0F, 0.0F));
    setRotationAngles(armRightFront1, 0.0F, angle180, 0.0F);
    PartDefinition armRightFront2 =
        rightArm.addOrReplaceChild("armRightFront2", CubeListBuilder.create()
                .texOffs(43, 25)
                .addBox(3.0F, -2.0F, -3.0F, 3, 3, 1),
            PartPose.rotation(5.0F, 0.0F, 0.0F));
    setRotationAngles(armRightFront2, 0.0F, angle180, 0.0F);
    PartDefinition armRightBack1 =
        rightArm.addOrReplaceChild("armRightBack1", CubeListBuilder.create()
                .texOffs(34, 31)
                .addBox(6.0F, -2.0F, 2.0F, 3, 5, 1),
            PartPose.rotation(5.0F, 0.0F, 0.0F));
    setRotationAngles(armRightBack1, 0.0F, angle180, 0.0F);
    PartDefinition armRightBack2 =
        rightArm.addOrReplaceChild("armRightBack2", CubeListBuilder.create()
                .texOffs(52, 25)
                .addBox(3.0F, -2.0F, 2.0F, 3, 3, 1),
            PartPose.rotation(5.0F, 0.0F, 0.0F));
    setRotationAngles(armRightBack2, 0.0F, angle180, 0.0F);
    PartDefinition armRightSide =
        rightArm.addOrReplaceChild("armRightSide", CubeListBuilder.create()
                .texOffs(49, 40)
                .addBox(10.0F, -3.0F, -3.0F, 1, 6, 6),
            PartPose.rotation(7.0F, 0.0F, 0.0F));
    setRotationAngles(armRightSide, 0.0F, angle180, 0.0F);

    //Left Arm
    PartDefinition leftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
            .texOffs(0, 30)
            .addBox(-2.0F, -3.0F, -3.0F, 6, 1, 6),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftArm.addOrReplaceChild("armLeftFront1", CubeListBuilder.create()
            .texOffs(25, 31)
            .addBox(-4.0F, -4.0F, -3.0F, 3, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftArm.addOrReplaceChild("armLeftFront1", CubeListBuilder.create()
            .texOffs(43, 33)
            .addBox(-7.0F, -4.0F, -3.0F, 3, 3, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftArm.addOrReplaceChild("armLeftBack1", CubeListBuilder.create()
            .texOffs(34, 23)
            .addBox(-4.0F, -4.0F, 2.0F, 3, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftArm.addOrReplaceChild("armLeftBack2", CubeListBuilder.create()
            .texOffs(52, 33)
            .addBox(-7.0F, -4.0F, 2.0F, 3, 3, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftArm.addOrReplaceChild("armLeftSide", CubeListBuilder.create()
            .texOffs(49, 40)
            .addBox(-4.0F, -5.0F, -3.0F, 1, 6, 6),
        PartPose.rotation(7.0F, 2.0F, 0.0F));

    //Right Foot
    float scale2 = 3.0f + Configs.armorZFightingBufferScaleRightLegOrFoot;
    PartDefinition rightFoot =
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(16, 57)
                .addBox(-3.0F, 11.0F, -3.0F, 6, 1, 6),
            PartPose.rotation(-1.9F, 12.0F, 0.0F));
    rightFoot.addOrReplaceChild("footRightFront", CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(0.0F, -6.0F, -3.0F, 5, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    PartDefinition footRightBack =
        rightFoot.addOrReplaceChild("footRightBack", CubeListBuilder.create()
                .texOffs(0, 57)
                .addBox(-4.0F, -6.0F, -3.0F, 5, 5, 1),
            PartPose.rotation(5.0F, 2.0F, 0.0F));
    setRotationAngles(footRightBack, 0.0F, angle180, 0.0F);
    rightFoot.addOrReplaceChild("footRightSide1", CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(-2.0F, -6.0F, -1.0F, 5, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    PartDefinition footRightSide2 =
        rightFoot.addOrReplaceChild("footRightSide2", CubeListBuilder.create()
                .texOffs(0, 57)
                .addBox(-2.0F, -6.0F, -5.0F, 5, 5, 1),
            PartPose.rotation(5.0F, 2.0F, 0.0F));
    setRotationAngles(footRightSide2, 0.0F, angle270, 0.0F);
    //Left Foot
    PartDefinition leftLeg = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
            .texOffs(16, 57)
            .addBox(-3.0F, 11.0F, -3.0F, 6, 1, 6),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftLeg.addOrReplaceChild("footLeftFront", CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(0.0F, -6.0F, -3.0F, 5, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftLeg.addOrReplaceChild("footLeftBack", CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(-4.0F, -6.0F, -3.0F, 5, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftLeg.addOrReplaceChild("footLeftSide1", CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(-2.0F, -6.0F, -5.0F, 5, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));
    leftLeg.addOrReplaceChild("footLeftSide2", CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(-2.0F, -6.0F, -5.0F, 5, 5, 1),
        PartPose.rotation(5.0F, 2.0F, 0.0F));

    return LayerDefinition.create(meshDefinition, 86, 64);
  }

  public ModelChiseledArmor() {
    super(bakeLayer().bakeRoot());
  }

  @Override
  public void setupAnim(T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks,
                        float netHeadYaw, float headPitch) {
    if (livingEntity instanceof ArmorStand entityArmorStand) {

      head.xRot = 0.017453292F * entityArmorStand.getHeadPose().getX();
      head.yRot = 0.017453292F * entityArmorStand.getHeadPose().getY();
      head.zRot = 0.017453292F * entityArmorStand.getHeadPose().getZ();
      head.setRotation(0.0F, 1.0F, 0.0F);
      body.xRot = 0.017453292F * entityArmorStand.getBodyPose().getX();
      body.yRot = 0.017453292F * entityArmorStand.getBodyPose().getY();
      body.zRot = 0.017453292F * entityArmorStand.getBodyPose().getZ();
      leftArm.xRot = 0.017453292F * entityArmorStand.getLeftArmPose().getX();
      leftArm.yRot = 0.017453292F * entityArmorStand.getLeftArmPose().getY();
      leftArm.zRot = 0.017453292F * entityArmorStand.getLeftArmPose().getZ();
      rightArm.xRot = 0.017453292F * entityArmorStand.getRightArmPose().getX();
      rightArm.yRot = 0.017453292F * entityArmorStand.getRightArmPose().getY();
      rightArm.zRot = 0.017453292F * entityArmorStand.getRightArmPose().getZ();
      leftLeg.xRot = 0.017453292F * entityArmorStand.getLeftLegPose().getX();
      leftLeg.yRot = 0.017453292F * entityArmorStand.getLeftLegPose().getY();
      leftLeg.zRot = 0.017453292F * entityArmorStand.getLeftLegPose().getZ();
      leftLeg.setRotation(1.9F, 11.0F, 0.0F);
      rightLeg.xRot = 0.017453292F * entityArmorStand.getRightLegPose().getX();
      rightLeg.yRot = 0.017453292F * entityArmorStand.getRightLegPose().getY();
      rightLeg.zRot = 0.017453292F * entityArmorStand.getRightLegPose().getZ();
      rightLeg.setRotation(-1.9F, 11.0F, 0.0F);

      hat.copyFrom(head);
      return;
    }

    head.x = livingEntity instanceof ZombieVillager ? -Utility.PIXEL_F * 2 : 0.0F;
    super.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    if (livingEntity instanceof Zombie) {
      boolean flag = livingEntity instanceof Zombie && ((Zombie) livingEntity).isAggressive();
      float f = Mth.sin(this.attackTime * (float) Math.PI);
      float f1 =
          Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - attackTime)) * (float) Math.PI);
      rightArm.zRot = 0.0F;
      leftArm.zRot = 0.0F;
      rightArm.yRot = -(0.1F - f * 0.6F);
      leftArm.yRot = 0.1F - f * 0.6F;
      float f2 = -(float) Math.PI / (flag ? 1.5F : 2.25F);
      rightArm.xRot = f2;
      leftArm.xRot = f2;
      rightArm.xRot += f * 1.2F - f1 * 0.4F;
      leftArm.xRot += f * 1.2F - f1 * 0.4F;
      rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
      leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
      rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
      leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
    }

  }

}