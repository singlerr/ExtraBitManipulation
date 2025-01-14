package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.core.BlockPos;

public class CylinderElliptic extends AsymmetricalShape {

  @Override
  public boolean isPointInsideShape(BlockPos pos, int i, int j, int k) {
    float y = getBitPosY(pos, i, j, k);
    if (isPointOffLine(y, centerY, b)) {
      return false;
    }

    float dx = getBitPosDiffX(pos, i, j, centerX);
    float dz = getBitPosDiffZ(pos, j, k, centerZ);
    boolean inShape = isPointInEllipse(dx, dz, a, c);
    return sculptHollowShape ? inShape && !(isPointInEllipse(dx, dz, aInset, cInset) &&
        (openEnds || !isPointOffLine(y, centerY, bInset))) : inShape;
  }

}