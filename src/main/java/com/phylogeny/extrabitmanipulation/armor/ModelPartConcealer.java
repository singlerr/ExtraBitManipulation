package com.phylogeny.extrabitmanipulation.armor;

import com.google.common.primitives.Bytes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ModelMovingPart;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public class ModelPartConcealer {
  private Set<ModelMovingPart> concealedParts = new HashSet<>();
  private Set<ModelMovingPart> concealedPartOverlays = new HashSet<>();
  private final Set<ModelMovingPart> concealedPartsCombined = new HashSet<>();
  private final Map<ModelMovingPart, ModelPart> concealedPartRenderers = new HashMap<>();

  private final ModelPart EMPTY_MODEL =
      new ModelPart(Collections.emptyList(), Collections.emptyMap());

  public ModelPartConcealer() {
  }

  private ModelPartConcealer(byte[] concealedParts, byte[] concealedPartOverlays) {
    concealedPartsCombined.addAll(this.concealedParts = indexArrayToPartSet(concealedParts));
    concealedPartsCombined.addAll(
        this.concealedPartOverlays = indexArrayToPartSet(concealedPartOverlays));
  }

  private Set<ModelMovingPart> indexArrayToPartSet(byte[] parts) {
    return IntStream.range(0, parts.length).boxed()
        .map(index -> ModelMovingPart.values()[parts[index]]).collect(Collectors.toSet());
  }

  public boolean isEmpty() {
    return concealedPartsCombined.isEmpty();
  }

  public boolean isFull() {
    return concealedParts.size() == ModelMovingPart.values().length &&
        concealedPartOverlays.size() == ModelMovingPart.values().length;
  }

  private byte[] partsToByteArray(Set<ModelMovingPart> parts) {
    return Bytes.toArray(
        parts.stream().map(part -> (byte) part.ordinal()).collect(Collectors.toSet()));
  }

  public void saveToNBT(CompoundTag nbt) {
    savePartsToNBT(nbt, this.concealedParts, NBTKeys.ARMOR_CONCEALED_MODEL_PARTS);
    savePartsToNBT(nbt, this.concealedPartOverlays, NBTKeys.ARMOR_CONCEALED_MODEL_PART_OVERLAYS);
  }

  private void savePartsToNBT(CompoundTag nbt, Set<ModelMovingPart> parts, String key) {
    byte[] partsArray = partsToByteArray(parts);
    if (partsArray.length > 0) {
      nbt.putByteArray(key, partsArray);
    } else {
      nbt.remove(key);
    }
  }

  @Nullable
  public static ModelPartConcealer loadFromNBT(CompoundTag nbt) {
    if (!nbt.contains(NBTKeys.ARMOR_CONCEALED_MODEL_PARTS) &&
        !nbt.contains(NBTKeys.ARMOR_CONCEALED_MODEL_PART_OVERLAYS)) {
      return null;
    }

    byte[] concealedParts = nbt.getByteArray(NBTKeys.ARMOR_CONCEALED_MODEL_PARTS);
    byte[] concealedPartOverlays = nbt.getByteArray(NBTKeys.ARMOR_CONCEALED_MODEL_PART_OVERLAYS);
    return concealedParts.length > 0 || concealedPartOverlays.length > 0 ?
        new ModelPartConcealer(concealedParts, concealedPartOverlays).copy() : null;
  }

  public void merge(@Nullable ModelPartConcealer modelPartConcealer) {
    if (modelPartConcealer != null) {
      concealedParts.addAll(modelPartConcealer.concealedParts);
      concealedPartOverlays.addAll(modelPartConcealer.concealedPartOverlays);
      concealedPartsCombined.addAll(modelPartConcealer.concealedPartsCombined);
    }
  }

  public ModelPartConcealer copy() {
    return new ModelPartConcealer(partsToByteArray(concealedParts),
        partsToByteArray(concealedPartOverlays));
  }

  private Set<ModelMovingPart> getParts(boolean isOverlay) {
    return isOverlay ? concealedPartOverlays : concealedParts;
  }

  public boolean contains(ModelMovingPart part, boolean isOverlay) {
    return getParts(isOverlay).contains(part);
  }

  public void addOrRemove(int partIndex, boolean isOverlay, boolean remove) {
    ModelMovingPart part = ModelMovingPart.values()[partIndex];
    Set<ModelMovingPart> parts = getParts(isOverlay);
    if (remove) {
      parts.remove(part);
    } else {
      parts.add(part);
    }
  }

  public <T extends LivingEntity> void restoreModelPartVisibility(HumanoidModel<T> model) {


    concealedPartRenderers.keySet().forEach(part ->
    {
      ModelPart renderer = concealedPartRenderers.get(part);
      switch (part) {
        case HEAD:
          model.head = renderer;
          break;
        case BODY:
          model.body = renderer;
          break;
        case ARM_RIGHT:
          model.rightArm = renderer;
          break;
        case ARM_LEFT:
          model.leftArm = renderer;
          break;
        case LEG_RIGHT:
          model.rightLeg = renderer;
          break;
        case LEG_LEFT:
          model.leftLeg = renderer;
      }
    });
  }

  public <T extends LivingEntity> ModelPartConcealer applyToModel(HumanoidModel<T> model) {
    concealedParts.forEach(part ->
    {

      switch (part) {
        case HEAD:
          concealedPartRenderers.put(part, model.head);
          model.head = EMPTY_MODEL;
          break;
        case BODY:
          concealedPartRenderers.put(part, model.body);
          model.body = EMPTY_MODEL;
          break;
        case ARM_RIGHT:
          concealedPartRenderers.put(part, model.rightArm);
          model.rightArm = EMPTY_MODEL;
          break;
        case ARM_LEFT:
          concealedPartRenderers.put(part, model.leftArm);
          model.leftArm = EMPTY_MODEL;
          break;
        case LEG_RIGHT:
          concealedPartRenderers.put(part, model.rightLeg);
          model.rightLeg = EMPTY_MODEL;
          break;
        case LEG_LEFT:
          concealedPartRenderers.put(part, model.leftLeg);
          model.leftLeg = EMPTY_MODEL;
      }
    });
    if (!(model instanceof PlayerModel<?> modelPlayer)) {
      return this;
    }

    concealedPartOverlays.forEach(part ->
    {
      switch (part) {
        case HEAD:
          modelPlayer.hat.skipDraw = true;
          break;
        case BODY:
          modelPlayer.jacket.skipDraw = true;
          break;
        case ARM_RIGHT:
          modelPlayer.rightSleeve.skipDraw = true;
          break;
        case ARM_LEFT:
          modelPlayer.leftSleeve.skipDraw = true;
          break;
        case LEG_RIGHT:
          modelPlayer.rightPants.skipDraw = true;
          break;
        case LEG_LEFT:
          modelPlayer.leftPants.skipDraw = true;
      }
    });
    return this;
  }

  private EntityModel<?> getEmptyModelRenderer() {
    return new ModelRendererEmpty<>();
  }

  public static class ModelRendererEmpty<T extends LivingEntity> extends EntityModel<T> {

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j,
                               float f, float g, float h, float k) {

    }
  }
}