package com.phylogeny.extrabitmanipulation.client.gui.button;

import com.phylogeny.extrabitmanipulation.init.SoundsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.mixin.accessors.AbstractWidgetAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public class GuiButtonBase extends Button {
  public boolean selected;
  private boolean silent, helpMode;
  private List<Component> hoverText, hoverTextSelected, hoverHelpText;
  private SoundEvent soundSelect, soundDeselect;

  public GuiButtonBase(int x, int y, int width, int height, Component text,
                       Component hoverText, OnPress onPress, CreateNarration narration) {
    this(x, y, width, height, text, hoverText, null, null, onPress, narration);
  }

  public GuiButtonBase(int x, int y, int widthIn, int heightIn, Component text,
                       Component hoverText, @Nullable SoundEvent soundSelect,
                       @Nullable SoundEvent soundDeselect, OnPress onPress,
                       CreateNarration narration) {
    super(x, y, widthIn, heightIn, text, onPress, narration);
    this.hoverText = hoverTextSelected = removeEmptyLines(Collections.singletonList(hoverText));
    this.soundSelect = soundSelect;
    this.soundDeselect = soundDeselect;
  }

  public void setSilent(boolean silent) {
    this.silent = silent;
  }

  @Override
  public void playDownSound(SoundManager soundManager) {
    if (silent) {
      return;
    }

    if (soundSelect != null) {
      SoundsExtraBitManipulation.playSound(selected ? soundDeselect : soundSelect);
      return;
    }
    super.playDownSound(soundManager);

  }

  @Override
  protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
    isHovered = mouseX >= ((AbstractWidgetAccessor) this).getX() &&
        mouseY >= ((AbstractWidgetAccessor) this).getY() &&
        mouseX < ((AbstractWidgetAccessor) this).getX() + width &&
        mouseY < ((AbstractWidgetAccessor) this).getY() + height;
    mouseMoved(mouseX, mouseY);
  }

  public List<Component> getHoverText() {
    return helpMode ? hoverHelpText : (selected ? hoverTextSelected : hoverText);
  }

  private List<Component> removeEmptyLines(List<Component> lines) {
    List<Component> linesNew = new ArrayList<>();
    for (Component line : lines) {
      if (!line.getString().isEmpty()) {
        linesNew.add(line);
      }
    }
    return linesNew;
  }

  private List<Component> textToLines(Component[] text) {
    return removeEmptyLines(Arrays.asList(text));
  }

  public void setHoverText(Component... text) {
    hoverText = textToLines(text);
  }

  public void setHoverTextSelected(Component... text) {
    hoverTextSelected = textToLines(text);
  }

  public void setHoverHelpText(Component... text) {
    hoverHelpText = textToLines(text);
  }

  public void setHelpMode(boolean helpMode) {
    this.helpMode = helpMode;
  }

  public GuiButtonBase setSoundSelect(SoundEvent sound) {
    soundSelect = sound;
    return this;
  }

  public GuiButtonBase setSoundDeselect(SoundEvent sound) {
    soundDeselect = sound;
    return this;
  }

}