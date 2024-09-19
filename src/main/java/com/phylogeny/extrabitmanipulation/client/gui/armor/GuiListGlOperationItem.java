package com.phylogeny.extrabitmanipulation.client.gui.armor;

import com.phylogeny.extrabitmanipulation.armor.ArmorItem;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.GlOperation;
import java.util.List;

public class GuiListGlOperationItem extends GuiListGlOperation<ArmorItem> {
  private final int partIndex;
  private int armorItemIndex;

  public GuiListGlOperationItem(GuiChiseledArmor guiChiseledArmor, int height, int top, int bottom,
                                int slotHeight,
                                int offsetX, int listWidth, DataChiseledArmorPiece armorPiece,
                                int partIndex, int armorItemIndex) {
    super(guiChiseledArmor, height, top, bottom, slotHeight, offsetX, listWidth, armorPiece);
    this.partIndex = partIndex;
    this.armorItemIndex = armorItemIndex;
  }

  private ArmorItem getArmorItem() {
    return armorPiece.getArmorItemForPart(partIndex, armorItemIndex);
  }

  public void refreshList(int armorItemIndex) {
    this.armorItemIndex = armorItemIndex;
    super.refreshList();
  }

  @Override
  public List<GlOperation> getGlOperations() {
    return getArmorItem().getGlOperations();
  }

}