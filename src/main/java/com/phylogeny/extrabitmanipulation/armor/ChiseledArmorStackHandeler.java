package com.phylogeny.extrabitmanipulation.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import mod.chiselsandbits.client.model.baked.BaseBakedPerspectiveModel;
import mod.chiselsandbits.client.model.baked.TransformTypeDependentItemBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ChiseledArmorStackHandeler implements TransformTypeDependentItemBakedModel {
  private static final Map<CompoundTag, BakedModel> movingPartsModelMap =
      new HashMap<>();

  private final RandomSource RANDOM = RandomSource.create(0L);

  public ChiseledArmorStackHandeler() {

  }

  public static void clearModelMap() {
    movingPartsModelMap.clear();
  }

  public static void removeFromModelMap(CompoundTag nbt) {

    movingPartsModelMap.remove(nbt);
  }

  private BakedModel handleItemState(BakedModel originalModel, ItemStack stack,
                                     @Nullable Level world, @Nullable LivingEntity entity) {
    if (stack.hasTag()) {
      CompoundTag armorNbt = ItemStackHelper.getArmorData(stack.getTag());
      if (armorNbt.getBoolean(NBTKeys.ARMOR_NOT_EMPTY)) {
        ItemChiseledArmor armor = (ItemChiseledArmor) stack.getItem();
        BakedModel model = movingPartsModelMap.get(armorNbt);
        if (model == null && !movingPartsModelMap.containsKey(armorNbt)) {
          DataChiseledArmorPiece armorPiece = new DataChiseledArmorPiece(stack.getTag(),
              ArmorType.values()[armorNbt.getInt(NBTKeys.ARMOR_TYPE)]);
          List<GlOperation> glOperationsPre = armorPiece.getGlobalGlOperations(true);
          List<GlOperation> glOperationsPost = armorPiece.getGlobalGlOperations(false);
          List<BakedQuad>[] quadsFace = new ArrayList[Direction.values().length];
          List<BakedQuad> quadsGeneric = new ArrayList<BakedQuad>();
          for (Direction facing : Direction.values()) {
            quadsFace[facing.ordinal()] = new ArrayList<BakedQuad>();
          }

          float[] bounds = new float[] {Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
              Float.POSITIVE_INFINITY,
              Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
          List<GlOperation> glOperationsItem;
          boolean found = false;
          for (int p = 0; p < 3; p++) {
            float scale = 1 + Configs.armorZFightingBufferScale +
                ((armor.armorType == ArmorType.BOOTS && p == 0)
                    || (armor.armorType == ArmorType.LEGGINGS && p == 1) ?
                    Configs.armorZFightingBufferScaleRightLegOrFoot : 0.0F);
            float offset = armor.armorType == ArmorType.CHESTPLATE ? 6.0F :
                (armor.armorType == ArmorType.BOOTS ? 3.9F : 2.0F);
            float offsetX = p == 0 ? 0.0F : (Utility.PIXEL_F * (p == 1 ? offset : -offset));
            float offsetY = armor.armorType == ArmorType.LEGGINGS && p == 0 ? 1.0F : 0.0F;
            for (ArmorItem armorItem : armorPiece.getArmorItemsForPart(p)) {
              if (armorItem.getStack().isEmpty()) {
                continue;
              }

              glOperationsItem = armorItem.getGlOperations();
              model = ClientHelper.getRenderItem()
                  .getItemModelWithOverrides(armorItem.getStack(), null, ClientHelper.getPlayer());
              Matrix4f matrix = generateMatrix(glOperationsPre, glOperationsItem, glOperationsPost);
              try {
                for (BakedQuad quad : model.getQuads(null, null, 0L)) {
                  quadsGeneric.add(
                      createTransformedQuad(quad, null, armorItem.getStack(), bounds, scale,
                          offsetX, offsetY, matrix));
                  found = true;
                }
                for (Direction facing : Direction.values()) {
                  for (BakedQuad quad : model.getQuads(null, facing, 0L)) {
                    quadsFace[facing.ordinal()].add(createTransformedQuad(quad, facing,
                        armorItem.getStack(), bounds, scale, offsetX, offsetY, matrix));
                    found = true;
                  }
                }
              } catch (Exception e) {
              }
            }
          }
          if (found) {
            scaleAndCenterQuads(quadsFace, quadsGeneric, bounds);
            model = new ChiseledArmorBakedModel(quadsFace, quadsGeneric);
          } else {
            model = null;
          }
          movingPartsModelMap.put(armorNbt, model);
        }
        return model != null &&
            (Configs.armorStackModelRenderMode == ArmorStackModelRenderMode.ALWAYS_CUSTOM_MODEL ||
                (Configs.armorStackModelRenderMode.ordinal() < 2
                    && (Configs.armorStackModelRenderMode ==
                    ArmorStackModelRenderMode.CUSTOM_MODEL_IF_HOLDING_SHIFT) ==
                    Screen.hasShiftDown()))
            ? model : armor.getItemModel();
      }
    }
    return ((ItemChiseledArmor) stack.getItem()).getItemModel();
  }

  private Matrix4f generateMatrix(List<GlOperation> glOperationsPre,
                                  List<GlOperation> glOperationsItem,
                                  List<GlOperation> glOperationsPost) {
    Matrix4f matrix = new Matrix4f();
    matrix.identity();
    List<GlOperation> glOperations = new ArrayList<GlOperation>();
    glOperations.addAll(glOperationsPre);
    glOperations.addAll(glOperationsItem);
    glOperations.addAll(glOperationsPost);
    Matrix4f temp;
    Vector3f scaleVec;
    for (GlOperation glOperation : glOperations) {
      switch (glOperation.getType()) {
        case ROTATION:
          temp = new Matrix4f();
          temp.set(new AxisAngle4f(glOperation.getX(), glOperation.getY(),
              glOperation.getZ(), (float) Math.toRadians(glOperation.getAngle())));
          matrix.mul(temp);
          break;
        case TRANSLATION:
          temp = new Matrix4f();

          temp.set(new float[] {glOperation.getX(), glOperation.getY(), glOperation.getZ()});
          matrix.mul(temp);
          break;
        case SCALE:
          scaleVec = new Vector3f(glOperation.getX(), glOperation.getY(), glOperation.getZ());
          temp = new Matrix4f();
          temp.identity();
          temp.m00(scaleVec.x);
          temp.m11(scaleVec.y);
          temp.m22(scaleVec.z);
          matrix.mul(temp);
      }
    }
    temp = new Matrix4f();
    temp.set(new float[] {-0.5F, -0.5F, -0.5F});
    matrix.mul(temp);
    return matrix;
  }

  private void scaleAndCenterQuads(List<BakedQuad>[] quadsFace, List<BakedQuad> quadsGeneric,
                                   float[] bounds) {
    float dimX = bounds[3] - bounds[0];
    float dimY = bounds[4] - bounds[1];
    float dimZ = bounds[5] - bounds[2];
    float scale = 1 / Math.max(dimX, Math.max(dimY, dimZ));
    float translationX = 0.5F - (bounds[3] + bounds[0]) * 0.5F * scale;
    float translationY = 0.5F - (bounds[4] + bounds[1]) * 0.5F * scale;
    float translationZ = 0.5F - (bounds[5] + bounds[2]) * 0.5F * scale;
    for (BakedQuad quad : quadsGeneric) {
      scaleAndCenterQuad(quad, scale, translationX, translationY, translationZ);
    }

    for (Direction facing : Direction.values()) {
      for (BakedQuad quad : quadsFace[facing.ordinal()]) {
        scaleAndCenterQuad(quad, scale, translationX, translationY, translationZ);
      }
    }
  }

  private void scaleAndCenterQuad(BakedQuad quad, float scale, float translationX,
                                  float translationY, float translationZ) {
    int size = quad.getFormat().getIntegerSize();
    int[] data = quad.getVertexData();
    for (int i = 0; i < 4; i++) {
      int index = size * i;
      data[index] =
          Float.floatToRawIntBits(Float.intBitsToFloat(data[index]) * scale + translationX);
      data[index + 1] =
          Float.floatToRawIntBits(Float.intBitsToFloat(data[index + 1]) * scale + translationY);
      data[index + 2] =
          Float.floatToRawIntBits(Float.intBitsToFloat(data[index + 2]) * scale + translationZ);
    }
  }

  private BakedQuad createTransformedQuad(BakedQuad quad, Direction facing, ItemStack stack,
                                          float[] bounds, float scale, float offsetX, float offsetY,
                                          Matrix4f matrix) {
    int size = quad.getFormat().getIntegerSize();
    int[] data = quad.getVertexData().clone();
    float x, y, z;
    Vector4f vec;
    int index;
    for (int i = 0; i < 4; i++) {
      index = size * i;
      x = Float.intBitsToFloat(data[index]);
      y = Float.intBitsToFloat(data[index + 1]);
      z = Float.intBitsToFloat(data[index + 2]);
      vec = new Vector4f(x, y, z, 1);
      matrix.transform(vec);
      x = vec.x * scale + offsetX;
      y = vec.y * scale + offsetY;
      z = vec.z * scale;
      if (x < bounds[0]) {
        bounds[0] = x;
      }

      if (x > bounds[3]) {
        bounds[3] = x;
      }

      if (y < bounds[1]) {
        bounds[1] = y;
      }

      if (y > bounds[4]) {
        bounds[4] = y;
      }

      if (z < bounds[2]) {
        bounds[2] = z;
      }

      if (z > bounds[5]) {
        bounds[5] = z;
      }

      data[index] = Float.floatToRawIntBits(x);
      data[index + 1] = Float.floatToRawIntBits(y);
      data[index + 2] = Float.floatToRawIntBits(z);
    }
    return new BakedQuad(data, quad.getTintIndex() == -1 ? -1 :
        ClientHelper.getItemColors().getColorFromItemstack(stack, quad.getTintIndex()),
        facing, quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
  }

  @Override
  public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack,
                                   boolean leftHand) {
    return null;
  }

  public static class ChiseledArmorBakedModel extends BaseBakedPerspectiveModel {
    private static Matrix4f ground, fixed;
    private final ItemOverrides overrides;
    private List<BakedQuad>[] face;
    private List<BakedQuad> generic;

    public ChiseledArmorBakedModel(List<BakedQuad>[] face, List<BakedQuad> generic) {
      this();
      this.face = face;
      this.generic = generic;
    }

    public ChiseledArmorBakedModel() {
      overrides = new ChiseledArmorStackHandeler();
      if (ground == null) {
        ground = createMatrix(TransformType.GROUND);
        fixed = createMatrix(TransformType.FIXED);
      }
    }

    private Matrix4f createMatrix(TransformType transformType) {
      Matrix4f matrix = new Matrix4f();
      matrix.set(1.35F);
      matrix.mul(handlePerspective(transformType).getRight());
      return matrix;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
        TransformType cameraTransformType) {
      if (ground != null && fixed != null && (cameraTransformType == TransformType.GROUND ||
          cameraTransformType == TransformType.FIXED)) {
        return new ImmutablePair<IBakedModel, Matrix4f>(this,
            cameraTransformType == TransformType.GROUND ? ground : fixed);
      }

      return super.handlePerspective(cameraTransformType);
    }

    @Override
    public boolean isAmbientOcclusion() {
      return true;
    }

    @Override
    public boolean isGui3d() {
      return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
      return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
      return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
      return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side,
                                    long rand) {
      return generic == null ? Collections.EMPTY_LIST :
          (side != null ? face[side.ordinal()] : generic);
    }

    @Override
    public ItemOverrideList getOverrides() {
      return overrides;
    }

  }

  public enum ArmorStackModelRenderMode {
    DEFAULT_MODEL_IF_HOLDING_SHIFT, CUSTOM_MODEL_IF_HOLDING_SHIFT, ALWAYS_CUSTOM_MODEL,
    ALWAYS_DEFAULT_MODEL
  }

}