package com.phylogeny.extrabitmanipulation.client.gui.armor;

import com.mojang.blaze3d.platform.Lighting;
import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;
import com.phylogeny.extrabitmanipulation.client.render.RenderState;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenInventoryGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class GuiButtonArmorSlots extends GuiButtonBase {
  private final ContainerScreen gui;
  private int mouseInitialX, offsetX, offsetY, posX, posY;

  public GuiButtonArmorSlots(ContainerScreen gui, Component buttonText) {
    super(384736845, 0, 0, 12, 10, buttonText);
    setHoverHelpText(
        "While holding SHIFT + CONTROL + ALT:\n" + GuiChiseledArmor.getPointSub("1) ") +
            "Click & drag to change position.\n" + GuiChiseledArmor.getPointSub("2) ") +
            "Press R to reset position.");
    this.gui = gui;
    setPosition();
  }

  public void setPosition() {
    resetOffsets();
    Pair<Integer, Integer> pos = BitToolSettingsHelper.getArmorButtonPosition();
    posX = pos.getLeft();
    posY = pos.getRight();
    setPosisionAbsolute();
  }

  public static boolean shouldMoveButton() {
    return GuiScreen.isShiftKeyDown() && GuiScreen.isCtrlKeyDown() && GuiScreen.isAltKeyDown();
  }

  private void setPosisionAbsolute() {
    x = gui.getGuiLeft() + posX;
    y = gui.getGuiTop() + posY;
  }

  private void resetOffsets() {
    offsetX = offsetY = mouseInitialX = 0;
  }

  @Override
  public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    if (ClientHelper.getPlayer().capabilities.isCreativeMode) {
      visible = false;
      return;
    }
    setPosisionAbsolute();
    hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    Lighting.enableGUIStandardItemLighting();
    GlStateManager.pushMatrix();
    GlStateManager.translate(0, 0, -200);
    GlStateManager.enableDepth();
    RenderState.renderStateModelIntoGUI(null,
        ArmorMovingPart.HEAD.getIconModels(ArmorMaterial.DIAMOND)[0],
        ItemStack.EMPTY, hovered ? 1.0F : 0.5F, true, false, x - 8, y - 1, 0, 0, 1);
    Lighting.turnOff();
    GlStateManager.translate(0, 0, 500);
    mouseDragged(mc, mouseX, mouseY);
    if (hovered) {
      int y = this.y + 2;
      for (String string : mc.font.listFormattedStringToWidth(displayString, 45)) {
        drawCenteredString(mc.font, string, x + 6, y += mc.font.lineHeight, 14737632);
      }
    }
    GlStateManager.popMatrix();
  }

  @Override
  public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
    boolean pressed = super.mousePressed(mc, mouseX, mouseY);
    if (pressed) {
      offsetX = mouseX - posX;
      offsetY = mouseY - posY;
      if (shouldMoveButton()) {
        mouseInitialX = mouseX;
      } else {
        boolean openVanilla = mc.screen instanceof GuiInventoryArmorSlots;
        if (openVanilla) {
          ((GuiInventoryArmorSlots) gui).openVanillaInventory(mouseX, mouseY);
        }

        ExtraBitManipulation.packetNetwork.sendToServer(new PacketOpenInventoryGui(openVanilla));
      }
    } else {
      resetOffsets();
    }
    return pressed;
  }

  @Override
  public void mouseReleased(int mouseX, int mouseY) {
    resetOffsets();
    BitToolSettingsHelper.setArmorButtonPosition(posX, posY);
  }

  @Override
  protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
    if (mouseInitialX > 0 && shouldMoveButton()) {
      posX = mouseX - offsetX;
      posY = mouseY - offsetY;
    }
  }

  @Override
  public void playPressSound(SoundManager soundHandlerIn) {
    if (!shouldMoveButton()) {
      super.playPressSound(soundHandlerIn);
    }
  }

}