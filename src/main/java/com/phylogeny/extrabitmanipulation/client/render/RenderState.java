package com.phylogeny.extrabitmanipulation.client.render;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.List;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.opengl.GL11;

public class RenderState {
  private static final ResourceLocation RES_ITEM_GLINT =
      new ResourceLocation("textures/misc/enchanted_item_glint.png");

  private static final RandomSource RANDOM = RandomSource.create();

  public static void renderStateIntoGUI(final BlockState state, int x, int y) {
    BlockModelShaper blockModelShapes = ClientHelper.getBlockModelShapes();
    BakedModel model = blockModelShapes.getBlockModel(state);
    boolean emptyModel;
    try {
      boolean missingModel = isMissingModel(blockModelShapes, model);
      emptyModel = missingModel || model.getQuads(state, null, RANDOM).isEmpty();
      if (!missingModel && emptyModel) {
        for (Direction enumfacing : Direction.values()) {
          if (!model.getQuads(state, enumfacing, 0L).isEmpty()) {
            emptyModel = false;
            break;
          }
        }
      }
    } catch (Exception e) {
      emptyModel = true;
    }
    Block block = state.getBlock();
    ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
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
            return String.valueOf(stack2.getMetadata());
          }
        });
        crashreportcategory.setDetail("State's Item NBT", new CrashReportDetail<String>() {
          @Override
          public String call() throws Exception {
            return String.valueOf(stack2.getTagCompound());
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
    return stack.getItem() == null || block == Blocks.STANDING_BANNER || block == Blocks.BARRIER;
  }

  private static boolean isMissingModel(BlockModelShaper blockModelShapes, BakedModel model) {
    return model.equals(blockModelShapes.getModelManager().getMissingModel());
  }

  public static void renderStateModelIntoGUI(BlockState state, BakedModel model, ItemStack stack,
                                             boolean renderAsTileEntity, int x, int y, float angleX,
                                             float angleY, float scale) {
    renderStateModelIntoGUI(state, model, stack, 1.0F, false, renderAsTileEntity, x, y, angleX,
        angleY, scale);
  }

  public static void renderStateModelIntoGUI(BlockState state, BakedModel model, ItemStack stack,
                                             float alphaMultiplier,
                                             boolean transformFroGui, boolean renderAsTileEntity,
                                             int x, int y, float angleX, float angleY,
                                             float scale) {
    TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
    GlStateManager.pushMatrix();
    textureManager.bind(TextureMap.LOCATION_BLOCKS_TEXTURE);
    textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
    GlStateManager.enableRescaleNormal();
    GlStateManager.enableAlpha();
    GlStateManager.alphaFunc(516, 0.1F);
    GlStateManager.enableBlend();
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    setupGuiTransform(x, y, model);
    if (transformFroGui) {
      model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.GUI,
          false);
    }

    renderState(state, model, stack, alphaMultiplier, renderAsTileEntity, angleX, angleY, scale);
    GlStateManager.disableRescaleNormal();
    GlStateManager.disableLighting();
    GlStateManager.popMatrix();
    textureManager.bind(TextureMap.LOCATION_BLOCKS_TEXTURE);
    textureManager.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
  }

  public static void renderState(BlockState state, BakedModel model, ItemStack stack,
                                 float alphaMultiplier, boolean renderAsTileEntity, float angleX,
                                 float angleY, float scale) {
    boolean autoScale = scale < 0;
    if (autoScale) {
      scale = 1;
    }

    GlStateManager.pushMatrix();
    if (autoScale) {
      try {
        int size;
        int[] data;
        float x, y, z;
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (BakedQuad quad : model.getQuads(state, null, 0L)) {
          size = quad.getFormat().getIntegerSize();
          data = quad.getVertexData();
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
          for (BakedQuad quad : model.getQuads(state, enumfacing, 0L)) {
            size = quad.getFormat().getIntegerSize();
            data = quad.getVertexData();
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
    GlStateManager.scale(scale, scale, scale);
    GlStateManager.rotate(angleX, 1, 0, 0);
    GlStateManager.rotate(angleY, 0, 1, 0);

    if (renderAsTileEntity) {
      if (autoScale) {
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.rotate(30, 1, 0, 1);
      }
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.enableRescaleNormal();
      TileEntityItemStackRenderer.instance.renderByItem(stack);
    } else {
      if (autoScale) {
        GlStateManager.rotate(225, 0, 1, 0);
        GlStateManager.rotate(30, -1, 0, -1);
      }
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      renderModel(state, model, -1, alphaMultiplier, stack);
      if (stack.hasFoil()) {
        renderEffect(state, model);
      }
    }
    GlStateManager.popMatrix();
  }

  private static void setupGuiTransform(int x, int y, BakedModel model) {
    GlStateManager.translate(x + 6, y + 2, 100.0F + ClientHelper.getRenderItem().zLevel + 400);
    GlStateManager.translate(8.0F, 8.0F, 0.0F);
    GlStateManager.scale(1.0F, -1.0F, 1.0F);
    GlStateManager.scale(16.0F, 16.0F, 16.0F);
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
    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
    try {
      for (Direction enumfacing : Direction.values()) {
        renderQuads(buffer, model.getQuads(state, enumfacing, 0L), color, alphaMultiplier, stack);
      }

      renderQuads(buffer, model.getQuads(state, null, 0L), color, alphaMultiplier, stack);
    } catch (Exception e) {
    } finally {
      tessellator.end();
    }
  }

  private static void renderEffect(BlockState state, BakedModel model) {
    GlStateManager.depthMask(false);
    GlStateManager.depthFunc(514);
    GlStateManager.disableLighting();
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
    ClientHelper.bindTexture(RES_ITEM_GLINT);
    GlStateManager.matrixMode(5890);
    GlStateManager.pushMatrix();
    GlStateManager.scale(8.0F, 8.0F, 8.0F);
    float f = Minecraft.getSystemTime() % 3000L / 3000.0F / 8.0F;
    GlStateManager.translate(f, 0.0F, 0.0F);
    GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
    renderModel(state, model, -8372020, 1.0F, null);
    GlStateManager.popMatrix();
    GlStateManager.pushMatrix();
    GlStateManager.scale(8.0F, 8.0F, 8.0F);
    float f1 = Minecraft.getSystemTime() % 4873L / 4873.0F / 8.0F;
    GlStateManager.translate(-f1, 0.0F, 0.0F);
    GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
    renderModel(state, model, -8372020, 1.0F, null);
    GlStateManager.popMatrix();
    GlStateManager.matrixMode(5888);
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    GlStateManager.enableLighting();
    GlStateManager.depthFunc(515);
    GlStateManager.depthMask(true);
    ClientHelper.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
  }

  private static void renderQuads(BufferBuilder buffer, List<BakedQuad> quads, int color,
                                  float alphaMultiplier, ItemStack stack) {
    boolean flag = color == -1 && !stack.isEmpty();
    int i = 0;
    for (int j = quads.size(); i < j; ++i) {
      BakedQuad quad = quads.get(i);
      int colorQuad = color;
      if (flag && quad.hasTintIndex()) {
        colorQuad = Minecraft.getMinecraft().getItemColors()
            .getColorFromItemstack(stack, quad.getTintIndex());
        if (EntityRenderer.anaglyphEnable) {
          colorQuad = TextureUtil.anaglyphColor(colorQuad);
        }

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