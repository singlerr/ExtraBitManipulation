package com.phylogeny.extrabitmanipulation.armor;

import com.google.common.primitives.Bytes;
import com.phylogeny.extrabitmanipulation.armor.model.cnpc.CustomNPCsModels;
import com.phylogeny.extrabitmanipulation.armor.model.mpm.MorePlayerModelsModels;
import com.phylogeny.extrabitmanipulation.extension.ModelPartExtension;
import com.phylogeny.extrabitmanipulation.extension.ModelPartType;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ModelMovingPart;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;

public class ModelPartConcealer {
  private Set<ModelMovingPart> concealedParts = new HashSet<>();
  private Set<ModelMovingPart> concealedPartOverlays = new HashSet<>();
  private final Set<ModelMovingPart> concealedPartsCombined = new HashSet<>();
  private final Map<ModelMovingPart, ModelPart> concealedPartRenderers = new HashMap<>();

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

  public void restoreModelPartVisiblity(ModelPart model) {


    concealedPartRenderers.keySet().forEach(part ->
    {
      ModelPart renderer = concealedPartRenderers.get(part);
      ModelPartExtension ext = (ModelPartExtension) model;
      switch (part) {
        case HEAD:
          ext.ebm$putChild(ModelPartType.HEAD, renderer);
          break;
        case BODY:
          ext.ebm$putChild(ModelPartType.BODY, renderer);
          break;
        case ARM_RIGHT:
          ext.ebm$putChild(ModelPartType.RIGHT_ARM, renderer);
          break;
        case ARM_LEFT:
          ext.ebm$putChild(ModelPartType.LEFT_ARM, renderer);
          break;
        case LEG_RIGHT:
          ext.ebm$putChild(ModelPartType.RIGHT_LEG, renderer);
          break;
        case LEG_LEFT:
          ext.ebm$putChild(ModelPartType.LEFT_LEG, renderer);
      }
    });
  }

  public ModelPartConcealer applyToModel(ModelPart model) {
    concealedParts.forEach(part ->
    {
      ModelPartExtension ext = (ModelPartExtension) model;
      switch (part) {
        case HEAD:
          concealedPartRenderers.put(part, ext.ebm$getChild(ModelPartType.HEAD));
          ext.ebm$putChild(ModelPartType.HEAD,
              getEmptyModelRenderer(model, ext.ebm$getChild(ModelPartType.HEAD), part));
          break;
        case BODY:
          concealedPartRenderers.put(part, ext.ebm$getChild(ModelPartType.BODY));
          ext.ebm$putChild(ModelPartType.HEAD,
              getEmptyModelRenderer(model, ext.ebm$getChild(ModelPartType.BODY), part));
          break;
        case ARM_RIGHT:
          concealedPartRenderers.put(part, ext.ebm$getChild(ModelPartType.RIGHT_ARM));
          ext.ebm$putChild(ModelPartType.HEAD,
              getEmptyModelRenderer(model, ext.ebm$getChild(ModelPartType.RIGHT_ARM), part));
          break;
        case ARM_LEFT:
          concealedPartRenderers.put(part, ext.ebm$getChild(ModelPartType.LEFT_ARM));
          ext.ebm$putChild(ModelPartType.HEAD,
              getEmptyModelRenderer(model, ext.ebm$getChild(ModelPartType.LEFT_ARM), part));
          break;
        case LEG_RIGHT:
          concealedPartRenderers.put(part, ext.ebm$getChild(ModelPartType.RIGHT_LEG));
          ext.ebm$putChild(ModelPartType.HEAD,
              getEmptyModelRenderer(model, ext.ebm$getChild(ModelPartType.RIGHT_LEG), part));
          break;
        case LEG_LEFT:
          concealedPartRenderers.put(part, ext.ebm$getChild(ModelPartType.LEFT_LEG));
          ext.ebm$putChild(ModelPartType.HEAD,
              getEmptyModelRenderer(model, ext.ebm$getChild(ModelPartType.LEFT_LEG), part));
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

  private ModelPart getEmptyModelRenderer(ModelPart model, ModelPart renderer,
                                          ModelMovingPart part) {
    if (MorePlayerModelsReference.isLoaded && MorePlayerModelsModels.isModelRendererMPM(renderer)) {
      ModelPart modelRenderer =
          MorePlayerModelsModels.getEmptyModelRenderer(model, renderer, part);
      if (part.ordinal() > 3) {
        modelRenderer.xRot += part == ModelMovingPart.LEG_LEFT ? 1.9 : -1.9;
      }

      return modelRenderer;
    }
    if (CustomNPCsReferences.isLoaded && CustomNPCsModels.isModelRendererCNPC(renderer)) {
      ModelPart modelRenderer = CustomNPCsModels.getEmptyModelRenderer(model, renderer, part);
      if (part.ordinal() > 3) {
        modelRenderer.xRot += part == ModelMovingPart.LEG_LEFT ? 1.9 : -1.9;
      }

      return modelRenderer;
    }
    return new ModelRendererEmpty(renderer);
  }

  public static class ModelRendererEmpty extends ModelPart {
    private static final ModelBase MODEL_EMPTY = new ModelBase() {
    };

    public ModelRendererEmpty(ModelRenderer renderer) {
      super(MODEL_EMPTY);

      ModelBiped.copyModelAngles(renderer, this);
    }

    @Override
    public void render(float scale) {
    }

  }
}