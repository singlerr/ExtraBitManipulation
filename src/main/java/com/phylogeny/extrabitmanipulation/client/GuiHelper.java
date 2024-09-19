package com.phylogeny.extrabitmanipulation.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;
import com.phylogeny.extrabitmanipulation.mixin.accessors.MouseHandlerAccessor;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

public class GuiHelper {

  private static Minecraft getMinecraft() {
    return Minecraft.getInstance();
  }

  public static void glScissor(int x, int y, int width, int height) {
    GL11.glEnable(GL11.GL_SCISSOR_TEST);
    int scaleFactor = getScaleFactor();
    GL11.glScissor(x * scaleFactor,
        getMinecraft().getWindow().getHeight() - (y + height) * scaleFactor,
        width * scaleFactor, height * scaleFactor);
  }

  public static void glScissorDisable() {
    GL11.glDisable(GL11.GL_SCISSOR_TEST);
  }

  public static int getScaleFactor() {

    return getMinecraft().getWindow()
        .calculateScale(Minecraft.getInstance().options.guiScale().get()
            , Minecraft.getInstance().isEnforceUnicode());
  }

  public static Screen getOpenGui() {
    return getMinecraft().screen;
  }

  public static boolean isCursorInsideBox(AABB box, int mouseX, int mouseY) {
    return box.inflate(1).contains(new Vec3(mouseX, mouseY, 0));
  }

  public static void drawRect(double left, double top, double right, double bottom, int color) {
    Tesselator tessellator = Tesselator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    GlStateManager._enableBlend();
    GlStateManager.disableTexture2D();
    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
        (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
    buffer.vertex(left, bottom, 0.0D).endVertex();
    buffer.vertex(right, bottom, 0.0D).endVertex();
    buffer.vertex(right, top, 0.0D).endVertex();
    buffer.vertex(left, top, 0.0D).endVertex();
    tessellator.end();
    GlStateManager.enableTexture2D();
    GlStateManager._enableBlend();
  }

  public static void drawTexturedRect(double left, double top, double right, double bottom) {
    GlStateManager._enableBlend();
    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    Tesselator t = Tesselator.getInstance();
    BufferBuilder buffer = t.getBuilder();
    buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
    buffer.vertex(left, top, 0).uv(0, 0).endVertex();
    buffer.vertex(left, bottom, 0).uv(0, 1).endVertex();
    buffer.vertex(right, bottom, 0).uv(1, 1).endVertex();
    buffer.vertex(right, top, 0).uv(1, 0).endVertex();
    t.end();
    GlStateManager._disableBlend();
  }

  public static void drawHoveringTextForButtons(GuiGraphics guiGraphics, List<Button> buttonList,
                                                int mouseX, int mouseY) {
    for (Button button : buttonList) {
      if (!(button instanceof GuiButtonBase)) {
        continue;
      }

      if (button.isMouseOver(mouseX, mouseY) && button.visible) {
        List<Component> text = ((GuiButtonBase) button).getHoverText();
        if (!text.isEmpty()) {
          guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, text, mouseX, mouseY);
        }

        break;
      }
    }
  }

  public static Pair<Float, Boolean> changeScale(float scale, float amount, float max) {
    amount *= scale;
    float previewStackInitialScale = scale;
    scale += amount;
    if (scale < 0.1) {
      scale = 0.1F;
      return new ImmutablePair<Float, Boolean>(previewStackInitialScale, true);
    }
    if (scale > max) {
      scale = max;
      return new ImmutablePair<Float, Boolean>(previewStackInitialScale, true);
    }
    return new ImmutablePair<Float, Boolean>(scale, false);
  }

  public static Triple<Vec3, Vec3, Float> dragObject(int clickedMouseButton, float deltaX,
                                                     float deltaY, Vec3 translationInitialVec,
                                                     Vec3 rotationVec, float scale,
                                                     float scaleMax, float rotationMultiplierX,
                                                     float rotationMultiplierY,
                                                     boolean affectRotation) {
    MutableTriple<Vec3, Vec3, Float> triple =
        new MutableTriple<Vec3, Vec3, Float>(translationInitialVec, rotationVec, scale);
    if (clickedMouseButton == 0) {
      if (Screen.hasShiftDown() || Screen.hasControlDown()) {
        triple.setRight(changeScale(scale, deltaY * 0.05F, scaleMax).getLeft());
      } else if (affectRotation) {
        double angleX = rotationVec.x - (deltaY / scale) * rotationMultiplierX;
        double angleY = rotationVec.y - (deltaX / scale) * rotationMultiplierY;
        if (angleX < -90 || angleX > 90) {
          angleX = 90 * (angleX > 0 ? 1 : -1);
        }

        triple.setMiddle(new Vec3(angleX, angleY, 0));
      }
    } else if (clickedMouseButton == 1) {
      triple.setLeft(
          new Vec3(translationInitialVec.x - deltaX, translationInitialVec.y - deltaY, 0));
    }
    return triple;
  }

  public static Pair<Vec3, Float> scaleObjectWithMouseWheel(Screen screen, AABB box,
                                                            Vec3 translationVec, float scale,
                                                            float scaleMax, float yOffset) {
    MutablePair<Vec3, Float> pair = new MutablePair<Vec3, Float>(translationVec, scale);

    Minecraft mc = Minecraft.getInstance();
    MouseHandlerAccessor mouseHandler = (MouseHandlerAccessor) Minecraft.getInstance().mouseHandler;

    if (mouseHandler.getEventDWheel() == 0) {
      return pair;
    }

    int mouseX = (int) mouseHandler.getEventX() * screen.width / mc.getWindow().getScreenWidth();
    int mouseY = screen.height -
        (int) mouseHandler.getEventY() * screen.height / mc.getWindow().getScreenHeight() - 1;
    if (!GuiHelper.isCursorInsideBox(box, mouseX, mouseY)) {
      return pair;
    }

    float amount = (float) mouseHandler.getEventDWheel();
    Pair<Float, Boolean> scaleNew = changeScale(scale, amount * 0.005F, scaleMax);
    if (scaleNew.getRight()) {
      return pair;
    }

    pair.setRight(scaleNew.getLeft());
    float x = mouseX - (int) (translationVec.x + (box.maxX + box.minX) * 0.5F);
    float y = mouseY - (int) (translationVec.y + yOffset + (box.maxY + box.minY) * 0.5F);
    float offset = (amount / -30) * 0.15F;
    pair.setLeft(translationVec.add(x * offset, y * offset, 0));
    return pair;
  }

}