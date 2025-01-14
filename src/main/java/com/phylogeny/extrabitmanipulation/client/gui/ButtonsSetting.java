package com.phylogeny.extrabitmanipulation.client.gui;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.GuiBitToolSettingsMenu.GuiButtonSetting;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.packet.PacketSetWrechMode;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ButtonsSetting {
  protected List<GuiButtonSetting> buttons;

  public ButtonsSetting() {
    buttons = new ArrayList<GuiButtonSetting>();
  }

  public List<GuiButtonSetting> getButtons() {
    return buttons;
  }

  public void addButton(GuiButtonSetting button) {
    button.selected = buttons.size() == getValue();
    buttons.add(button);
  }

  public void initButtons() {
    for (GuiButtonSetting button : buttons) {
      button.setButtonList(buttons);
    }
  }

  protected int getValue() {
    return 0;
  }

  protected abstract void setValue(Player player, int value);

  public void setValueIfDiffrent() {
    int value = buttons.indexOf(getTargetButton());
    if (value != getValue()) {
      setValue(ClientHelper.getPlayer(), value);
    }
  }

  protected GuiButtonSetting getTargetButton() {
    GuiButtonSetting buttonTarget = null;
    for (GuiButtonSetting button : buttons) {
      if (button.isHovered()) {
        buttonTarget = button;
      }
    }
    if (buttonTarget == null) {
      for (GuiButtonSetting button : buttons) {
        if (button.selected) {
          buttonTarget = button;
        }
      }
    }
    return buttonTarget;
  }

  protected CompoundTag getHeldStackNBT() {
    return ItemStackHelper.getNBTOrNew(ClientHelper.getHeldItemMainhand());
  }

  private static ItemSculptingTool getSculptingTool() {
    ItemStack stack = ClientHelper.getHeldItemMainhand();
    return stack.isEmpty() ? null : (ItemSculptingTool) stack.getItem();
  }

  private static ItemChiseledArmor getChiseledArmor() {
    ItemStack stack = ClientHelper.getHeldItemMainhand();
    return stack.isEmpty() ? null : (ItemChiseledArmor) stack.getItem();
  }

  public static class WrenchMode extends ButtonsSetting {

    @Override
    protected int getValue() {
      return getHeldStackNBT().getInt(NBTKeys.WRENCH_MODE);
    }

    @Override
    protected void setValue(Player player, int value) {
      ClientPlayNetworking.send(new PacketSetWrechMode(value));
    }

  }

  public static class ModelAreaMode extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getModelAreaMode(getHeldStackNBT());
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setModelAreaMode(player, player.getMainHandItem(), value,
          Configs.modelAreaMode);
    }

  }

  public static class ModelSnapMode extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getModelSnapMode(getHeldStackNBT());
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setModelSnapMode(player, player.getMainHandItem(), value,
          Configs.modelSnapMode);
    }

  }

  public static class ModelGuiOpen extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getModelGuiOpen(getHeldStackNBT()) ? 0 : 1;
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setModelGuiOpen(player, player.getMainHandItem(), value == 0,
          Configs.modelGuiOpen);
    }

  }

  public static class SculptMode extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getSculptMode(getHeldStackNBT());
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setSculptMode(player, player.getMainHandItem(), value,
          Configs.sculptMode);
    }

  }

  public static class Direction extends
      ButtonsSetting // TODO decompose to direction and rotation when triangular shapes are implemented
  {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getDirection(getHeldStackNBT()) % 6;
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setDirection(player, player.getMainHandItem(), value % 6,
          Configs.sculptDirection);
    }

  }

  public static class ShapeType extends ButtonsSetting {

    @Override
    public void setValueIfDiffrent()//TODO remove method when triangular shapes are implemented
    {
      ItemSculptingTool tool = getSculptingTool();
      if (tool == null) {
        return;
      }

      int value = buttons.indexOf(getTargetButton());
      if (!tool.isCurved()) {
        value = value * 3 + 3;
      }

      if (value != getValue()) {
        setValue(ClientHelper.getPlayer(), value);
      }
    }

    @Override
    protected int getValue() {
      ItemSculptingTool tool = getSculptingTool();
//			return tool == null ? 0 : BitToolSettingsHelper.getShapeType(getHeldStackNBT(), tool.isCurved()); TODO
      if (tool == null) {
        return 0;
      }

      int shapeType = BitToolSettingsHelper.getShapeType(getHeldStackNBT(), tool.isCurved());
      return tool.isCurved() ? shapeType : shapeType / 3 - 1;

    }

    @Override
    protected void setValue(Player player, int value) {
      ItemSculptingTool tool = getSculptingTool();
      if (tool != null) {
        BitToolSettingsHelper.setShapeType(player, player.getMainHandItem(), tool.isCurved(),
            value,
            tool.isCurved() ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat);
      }
    }

  }

  public static class BitGridTargeted extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.isBitGridTargeted(getHeldStackNBT()) ? 1 : 0;
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setBitGridTargeted(player, player.getMainHandItem(), value == 1,
          Configs.sculptTargetBitGridVertexes);
    }

  }

  public static class HollowShape extends ButtonsSetting {

    @Override
    protected int getValue() {
      ItemSculptingTool tool = getSculptingTool();
      return tool == null ? 0 :
          BitToolSettingsHelper.isHollowShape(getHeldStackNBT(), tool.removeBits()) ? 0 : 1;
    }

    @Override
    protected void setValue(Player player, int value) {
      ItemSculptingTool tool = getSculptingTool();
      if (tool != null) {
        BitToolSettingsHelper.setHollowShape(player, player.getMainHandItem(),
            tool.removeBits(),
            value == 0,
            tool.removeBits() ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade);
      }
    }

  }

  public static class OpenEnds extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.areEndsOpen(getHeldStackNBT()) ? 0 : 1;
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setEndsOpen(player, player.getMainHandItem(), value == 0,
          Configs.sculptOpenEnds);
    }

  }

  public static class OffsetShape extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.isShapeOffset(getHeldStackNBT()) ? 0 : 1;
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setShapeOffset(player, player.getMainHandItem(), value == 0,
          Configs.sculptOffsetShape);
    }

  }

  public static class ArmorMode extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getArmorMode(getHeldStackNBT());
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setArmorMode(player, player.getMainHandItem(), value,
          Configs.armorMode);
    }

  }

  public static class ArmorScale extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.getArmorScale(getHeldStackNBT());
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setArmorScale(player, player.getMainHandItem(), value,
          Configs.armorScale);
    }

  }

  public static class ArmorMovingPart extends ButtonsSetting {

    @Override
    protected int getValue() {
      ItemChiseledArmor armorPiece = getChiseledArmor();
      if (armorPiece == null) {
        return 0;
      }

      return BitToolSettingsHelper.getArmorMovingPart(getHeldStackNBT(), armorPiece).getPartIndex();
    }

    @Override
    protected void setValue(Player player, int value) {
      ItemChiseledArmor armorPiece = getChiseledArmor();
      if (armorPiece != null) {
        BitToolSettingsHelper.setArmorMovingPart(player, player.getMainHandItem(), armorPiece,
            value);
      }
    }

  }

  public static class ArmorGridTarget extends ButtonsSetting {

    @Override
    protected int getValue() {
      return BitToolSettingsHelper.areArmorBitsTargeted(getHeldStackNBT()) ? 1 : 0;
    }

    @Override
    protected void setValue(Player player, int value) {
      BitToolSettingsHelper.setArmorBitsTargeted(player, player.getMainHandItem(), value == 1,
          Configs.armorTargetBits);
    }

  }

}