package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.core.BlockPos;

public class Ellipsoid extends AsymmetricalShape {

  @Override
  public boolean isPointInsideShape(BlockPos pos, int i, int j, int k) {
    float dx = getBitPosDiffX(pos, i, j, centerX);
    float dy = getBitPosDiffY(pos, i, j, k, centerY);
    float dz = getBitPosDiffZ(pos, j, k, centerZ);
    boolean inShape = isPointInsideisEllipsoid(dx, dy, dz, a, b, c);
    return sculptHollowShape ?
        inShape && !isPointInsideisEllipsoid(dx, dy, dz, aInset, bInset, cInset) : inShape;
  }

  private boolean isPointInsideisEllipsoid(float dx, float dy, float dz, float a, float b,
                                           float c) {
    float vx = dx / a;
    float vy = dy / b;
    float vz = dz / c;
    return vx * vx + vy * vy + vz * vz <= 1;
  }

}