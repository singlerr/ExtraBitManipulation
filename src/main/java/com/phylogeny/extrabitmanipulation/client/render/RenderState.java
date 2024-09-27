package com.phylogeny.extrabitmanipulation.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.extension.AbstractTextureExtension;
import com.phylogeny.extrabitmanipulation.extension.MinecraftExtension;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.List;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class RenderState {
  private static final ResourceLocation RES_ITEM_GLINT =
      new ResourceLocation("textures/misc/enchanted_item_glint.png");

  private static final RandomSource RANDOM = RandomSource.create(0L);

  public static void renderStateIntoGUI(final BlockState state, int x, int y) {
    BlockModelShaper blockModelShapes = ClientHelper.getBlockModelShapes();
    BakedModel model = blockModelShapes.getBlockModel(state);
    boolean emptyModel;
    try {
      boolean missingModel = isMissingModel(blockModelShapes, model);
      emptyModel = missingModel || model.getQuads(state, null, RANDOM).isEmpty();
      if (!missingModel && emptyModel) {
        for (Direction enumfacing : Direction.values()) {
          if (!model.getQuads(state, enumfacing, RANDOM).isEmpty()) {
            emptyModel = false;
            break;
          }
        }
      }
    } catch (Exception e) {
      emptyModel = true;
    }
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block, 1/*, block.getMetaFromState(state)*/);
    if (isNullItem(block, stack)) {
      stack = ItemStack.EMPTY;
    }

    boolean isVanillaChest =
        block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST;
    if (!stack.isEmpty() && emptyModel) {
      model = getItemModelWithOverrides(stack);
      if (!isVanillaChest && isMissingModel(blockModelShapes, model)) {
        stack = new ItemStack(block);
        if (isNullItem(block, stack)) {
          stack = ItemStack.EMPTY;
        }

        if (!stack.isEmpty()) {
          model = getItemModelWithOverrides(stack);
        }
      }
    }
    boolean renderAsTileEntity = !stack.isEmpty() && (!model.isCustomRenderer() || isVanillaChest);
    try {
      renderStateModelIntoGUI(state, model, stack, renderAsTileEntity, x, y, 0, 0, -1);
    } catch (Throwable throwable) {
      CrashReport crashreport = CrashReport.forThrowable(throwable,
          "Rendering block state in " + Reference.MOD_ID + " bit mapping GUI");
      CrashReportCategory crashreportcategory =
          crashreport.addCategory("Block state being rendered");
      crashreportcategory.setDetail("Block State", new CrashReportDetail<String>() {
        @Override
        public String call() throws Exception {
          return String.valueOf(state);
        }
      });
      if (!stack.isEmpty()) {
        final ItemStack stack2 = stack.copy();
        crashreportcategory.setDetail("State's Item Type", new CrashReportDetail<String>() {
          @Override
          public String call() throws Exception {
            return String.valueOf(stack2.getItem());
          }
        });
        crashreportcategory.setDetail("State's Item Aux", new CrashReportDetail<String>() {
          @Override
          public String call() throws Exception {
            return String.valueOf(stack2.getDamageValue());
          }
        });
        crashreportcategory.setDetail("State's Item NBT", new CrashReportDetail<String>() {
          @Override
          public String call() throws Exception {
            return String.valueOf(stack2.getTag());
          }
        });
        crashreportcategory.setDetail("State's Item Foil", new CrashReportDetail<String>() {
          @Override
          public String call() throws Exception {
            return String.valueOf(stack2.hasFoil());
          }
        });
      }
      throw new ReportedException(crashreport);
    }
  }

  public static BakedModel getItemModelWithOverrides(ItemStack stack) {
    return ClientHelper.getRenderItem()
        .getModel(stack, null, ClientHelper.getPlayer(), 0);
  }

  private static boolean isNullItem(final Block block, ItemStack stack) {
    return stack.getItem() == null || block instanceof BannerBlock || block == Blocks.BARRIER;
  }

  private static boolean isMissingModel(BlockModelShaper blockModelShapes, BakedModel model) {
    return model.equals(blockModelShapes.getModelManager().getMissingModel());
  }

  public static void renderStateModelIntoGUI(PoseStack poseStack, MultiBufferSource bufferSource,
                                             BlockState state, BakedModel model, ItemStack stack,
                                             boolean renderAsTileEntity, int x, int y, float angleX,
                                             float angleY, float scale) {
    renderStateModelIntoGUI(poseStack, bufferSource, state, model, stack, 1.0F, false,
        renderAsTileEntity, x, y, angleX,
        angleY, scale);
  }

  public static void renderStateModelIntoGUI(PoseStack poseStack, MultiBufferSource bufferSource,
                                             BlockState state, BakedModel model, ItemStack stack,
                                             float alphaMultiplier,
                                             boolean transformFroGui, boolean renderAsTileEntity,
                                             int x, int y, float angleX, float angleY,
                                             float scale) {
    TextureManager textureManager = Minecraft.getInstance().getTextureManager();
    GL11.glPushMatrix();
    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
//    textureManager.bind(TextureMap.LOCATION_BLOCKS_TEXTURE);
    textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
    Lighting.setupForFlatItems();
//    GlStateManager.enableRescaleNormal();
//    GlStateManager.enableAlpha();
    GL11.glAlphaFunc(516, 0.1F);
    GlStateManager._enableBlend();
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    setupGuiTransform(x, y, model);
    if (transformFroGui) {
//      model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GUI,
//          false);
      model.getTransforms().getTransform(ItemDisplayContext.GUI).apply(false, poseStack);
    }

    renderState(poseStack, bufferSource, state, model, stack, alphaMultiplier, renderAsTileEntity,
        angleX, angleY, scale);
//    GlStateManager.disableRescaleNormal();
//    GlStateManager.disableLighting();
//    GlStateManager.popMatrix();
    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
    ((AbstractTextureExtension) textureManager.getTexture(
        TextureAtlas.LOCATION_BLOCKS)).ebm$restoreFilter();
  }

  public static void renderState(PoseStack poseStack, MultiBufferSource bufferSource,
                                 BlockState state, BakedModel model, ItemStack stack,
                                 float alphaMultiplier, boolean renderAsTileEntity, float angleX,
                                 float angleY, float scale) {
    RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, renderAsTileEntity);
    boolean autoScale = scale < 0;
    if (autoScale) {
      scale = 1;
    }
    poseStack.pushPose();
    if (autoScale) {
      try {
        int size = renderType.format().getIntegerSize();
        int[] data;
        float x, y, z;
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (BakedQuad quad : model.getQuads(state, null, RANDOM)) {

          data = quad.getVertices();
          for (int i = 0; i < 4; i++) {
            int index = size * i;
            x = Float.intBitsToFloat(data[index]);
            if (x < minX) {
              minX = x;
            }

            if (x > maxX) {
              maxX = x;
            }

            y = Float.intBitsToFloat(data[index + 1]);
            if (y < minY) {
              minY = y;
            }

            if (y > maxY) {
              maxY = y;
            }

            z = Float.intBitsToFloat(data[index + 2]);
            if (z < minZ) {
              minZ = z;
            }

            if (z > maxZ) {
              maxZ = z;
            }
          }
        }
        for (Direction enumfacing : Direction.values()) {
          for (BakedQuad quad : model.getQuads(state, enumfacing, RANDOM)) {
            data = quad.getVertices();
            for (int i = 0; i < 4; i++) {
              int index = size * i;
              x = Float.intBitsToFloat(data[index]);
              if (x < minX) {
                minX = x;
              }

              if (x > maxX) {
                maxX = x;
              }

              y = Float.intBitsToFloat(data[index + 1]);
              if (y < minY) {
                minY = y;
              }

              if (y > maxY) {
                maxY = y;
              }

              z = Float.intBitsToFloat(data[index + 2]);
              if (z < minZ) {
                minZ = z;
              }

              if (z > maxZ) {
                maxZ = z;
              }
            }
          }
        }
        scale = 1 / Math.max(1.0F, Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)));
      } catch (Exception e) {
      }
      scale *= 0.65F;
    }
    poseStack.scale(scale, scale, scale);
    poseStack.mulPose(Axis.XP.rotationDegrees(angleX));
    poseStack.mulPose(Axis.YP.rotationDegrees(angleY));
//    GlStateManager.scale(scale, scale, scale);
//    GlStateManager.rotate(angleX, 1, 0, 0);
//    GlStateManager.rotate(angleY, 0, 1, 0);

    if (renderAsTileEntity) {
      if (autoScale) {
        poseStack.mulPose(Axis.YP.rotationDegrees(45));
        poseStack.mulPose(Axis.of(new Vector3f(Mth.SQRT_OF_TWO / 2.0F, 0, Mth.SQRT_OF_TWO / 2.0F))
            .rotationDegrees(30));
//        GlStateManager.rotate(45, 0, 1, 0);
//        GlStateManager.rotate(30, 1, 0, 1);
      }
      poseStack.translate(-0.5F, -0.5F, -0.5F);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
//      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//      GlStateManager.enableRescaleNormal();
      ((MinecraftExtension) Minecraft.getInstance()).ebm$getBlockEntityWithoutLevelRenderer()
          .renderByItem(stack, ItemDisplayContext.GUI, poseStack, bufferSource, 0,
              OverlayTexture.NO_OVERLAY);
    } else {
      if (autoScale) {

        poseStack.mulPose(Axis.YP.rotationDegrees(225));
//        GlStateManager.rotate(225, 0, 1, 0);
        GlStateManager.rotate(30, -1, 0, -1);
      }
      poseStack.translate(-0.5F, -0.5F, -0.5F);
      renderModel(state, model, -1, alphaMultiplier, stack);
      if (stack.hasFoil()) {
        renderEffect(state, model);
      }
    }
    poseStack.popPose();
  }

  private static void setupGuiTransform(PoseStack poseStack, int x, int y, BakedModel model) {
    poseStack.translate(x + 6, y + 2, 100.0F + ClientHelper.getRenderItem().zLevel + 400);
    poseStack.translate(8.0F, 8.0F, 0.0F);
    poseStack.scale(1.0F, -1.0F, 1.0F);
    poseStack.scale(16.0F, 16.0F, 16.0F);
    if (model.isGui3d()) {
      GlStateManager.enableLighting();
    } else {
      GlStateManager.disableLighting();
    }
  }

  private static void renderModel(BlockState state, BakedModel model, int color,
                                  float alphaMultiplier, ItemStack stack) {
    Tesselator tessellator = Tesselator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.ITEM);
    try {
      for (Direction enumfacing : Direction.values()) {
        renderQuads(buffer, model.getQuads(state, enumfacing, RANDOM), color, alphaMultiplier,
            stack);
      }

      renderQuads(buffer, model.getQuads(state, null, RANDOM), color, alphaMultiplier, stack);
    } catch (Exception e) {
    } finally {
      tessellator.end();
    }
  }

  private static void renderEffect(PoseStack poseStack, BlockState state, BakedModel model) {
    RenderSystem.depthMask(false);
    RenderSystem.depthFunc(514);
    GlStateManager.disableLighting();
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
    ClientHelper.bindTexture(RES_ITEM_GLINT);
    GlStateManager.matrixMode(5890);
    poseStack.pushPose();
    poseStack.scale(8.0F, 8.0F, 8.0F);
    float f = Minecraft.getSystemTime() % 3000L / 3000.0F / 8.0F;
    poseStack.translate(f, 0.0F, 0.0F);
    poseStack.mulPose(Axis.ZP.rotationDegrees(-50));
    renderModel(state, model, -8372020, 1.0F, null);
    poseStack.popPose();
    poseStack.pushPose();
    poseStack.scale(8.0F, 8.0F, 8.0F);
    float f1 = Minecraft.getSystemTime() % 4873L / 4873.0F / 8.0F;
    poseStack.translate(-f1, 0.0F, 0.0F);
    poseStack.mulPose(Axis.ZP.rotationDegrees(10));
    renderModel(state, model, -8372020, 1.0F, null);
    poseStack.popPose();
    poseStack.matrixMode(5888);
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.enableLighting();
    RenderSystem.depthFunc(515);
    RenderSystem.depthMask(true);
    ClientHelper.bindTexture(TextureAtlas.LOCATION_BLOCKS);
  }

  private static void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, int color,
                                  float alphaMultiplier, ItemStack stack) {
    boolean flag = color == -1 && !stack.isEmpty();
    int i = 0;
    for (int j = quads.size(); i < j; ++i) {
      BakedQuad quad = quads.get(i);
      int colorQuad = color;
      if (flag && quad.isTinted()) {
        colorQuad = ((MinecraftExtension) Minecraft.getInstance()).ebm$getItemColors()
            .getColor(stack, quad.getTintIndex());
//        if (EntityRenderer.anaglyphEnable) {
//          colorQuad = TextureUtil.anaglyphColor(colorQuad);
//        }

        colorQuad = colorQuad | -16777216;
      }
      if (alphaMultiplier < 1) {
        colorQuad = (((int) ((color == -1 ? 255 : colorQuad >> 24) * alphaMultiplier)) << 24) |
            (colorQuad & 0x00ffffff);
      }

      LightUtil.renderQuadColor(buffer, quad, colorQuad);
    }
  }

}