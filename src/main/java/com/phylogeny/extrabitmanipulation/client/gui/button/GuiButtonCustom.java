package com.phylogeny.extrabitmanipulation.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonCustom extends GuiButtonBase {
  protected float textOffsetX, textOffsetY;

  public GuiButtonCustom(int buttonId, int x, int y, int widthIn, int heightIn, String text,
                         String hoverText) {
    super(buttonId, x, y, widthIn, heightIn, text, hoverText);
  }

  public void setTextOffsetX(float textOffsetX) {
    this.textOffsetX = textOffsetX;
  }

  public void setTextOffsetY(float textOffsetY) {
    this.textOffsetY = textOffsetY;
  }

  protected void drawCustomRect() {
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    if (!visible) {
      return;
    }

    super.drawButton(mc, mouseX, mouseY, partialTicks);
    GlStateManager.enableBlend();
    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
        GlStateManager.DestFactor.ZERO);
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    drawCustomRect();
    int colorText = -1;
    if (packedFGColour != 0) {
      colorText = packedFGColour;
    } else if (!enabled) {
      colorText = 10526880;
    } else if (hovered) {
      colorText = 16777120;
    }
    GlStateManager.pushMatrix();
    GlStateManager.translate(textOffsetX, textOffsetY, 0);
    mc.font.draw(displayString, x + width / 2 - mc.font.width(displayString) / 2,
        y + (height - 8) / 2, colorText);
    GlStateManager.popMatrix();
  }

}