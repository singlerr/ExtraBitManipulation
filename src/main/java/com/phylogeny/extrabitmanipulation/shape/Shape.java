package com.phylogeny.extrabitmanipulation.shape;

import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import org.joml.Vector3d;

public abstract class Shape {
  public static final String[] SHAPE_NAMES =
      new String[] {"Sphere", "Cylinder", "Cone", "Cube", "Triangular Prism", "Triangular Pyramid",
          "Square Pyramid"};
  protected int direction;
  protected float centerX, centerY, centerZ, wallThickness;
  protected boolean sculptHollowShape, openEnds, inverted;

  public void init(float centerX, float centerY, float centerZ, int direction,
                   boolean sculptHollowShape, float wallThickness, boolean openEnds) {
    this.direction = direction;
    this.sculptHollowShape = sculptHollowShape;
    this.wallThickness = wallThickness;
    this.openEnds = openEnds;
    float v;
    if (direction > 1) {
      if (direction > 3) {
        v = centerX;
        centerX = centerY;
        centerY = v;
      } else {
        v = centerZ;
        centerZ = centerY;
        centerY = v;
      }
    }
    this.centerX = centerX;
    this.centerY = centerY;
    this.centerZ = centerZ;
    inverted = direction % 2 == 0;
  }

  public boolean isBlockInsideShape(BlockPos pos) {
    if (sculptHollowShape) {
      return false;
    }

    for (int i = 0; i < 16; i += 15) {
      for (int j = 0; j < 16; j += 15) {
        for (int k = 0; k < 16; k += 15) {
          if (!isPointInsideShape(pos, i, j, k)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public abstract boolean isPointInsideShape(BlockPos pos, int i, int j, int k);

  public Vector3d getRandomInternalPoint(Level world, BlockPos pos) {
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
    AABB bb = world.getBlockState(pos).getShape(world, pos).bounds();
    AABB blockBounds = bb == null ? new AABB(pos) :
        new AABB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ).move(pos);
    if (blockBounds.getSize() == 0) {
      blockBounds = new AABB(x, y, z, x + 1, y + 1, z + 1);
    }

    AABB box = getIntersectingBox(blockBounds, getBoundingBox());
    float s = Configs.bitSpawnBoxContraction;
    if (s > 0) {
      box = box.inflate(-(box.maxX - box.minX) * s, -(box.maxY - box.minY) * s,
          -(box.maxZ - box.minZ) * s);
    }

    double d0 = world.random.nextFloat() * (box.maxX - box.minX) + box.minX;
    double d1 = world.random.nextFloat() * (box.maxY - box.minY) + box.minY;
    double d2 = world.random.nextFloat() * (box.maxZ - box.minZ) + box.minZ;
    return new Vector3d(d0, d1, d2);
  }

  @Nonnull
  protected AABB getBoundingBox() {
    return Shapes.block().bounds();
  }

  @Nonnull
  private AABB getIntersectingBox(AABB box1, AABB box2) {
    if (box1.minX > box2.maxX || box2.minX > box1.maxX || box1.minY > box2.maxY
        || box2.minY > box1.maxY || box1.minZ > box2.maxZ || box2.minZ > box1.maxZ) {
      return box1;
    }

    double minX = Math.max(box1.minX, box2.minX);
    double minY = Math.max(box1.minY, box2.minY);
    double minZ = Math.max(box1.minZ, box2.minZ);
    double maxX = Math.min(box1.maxX, box2.maxX);
    double maxY = Math.min(box1.maxY, box2.maxY);
    double maxZ = Math.min(box1.maxZ, box2.maxZ);
    return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
  }

  protected float reduceLength(float value) {
    return Utility.PIXEL_F < value ? value - wallThickness : 0.0000000001F;
  }

  protected float getBitPosDiffX(BlockPos pos, int x, int y, float center) {
    return getBitPosX(pos, x, y) - center;
  }

  protected float getBitPosDiffY(BlockPos pos, int x, int y, int z, float center) {
    return getBitPosY(pos, x, y, z) - center;
  }

  protected float getBitPosDiffZ(BlockPos pos, int y, int z, float center) {
    return getBitPosZ(pos, y, z) - center;
  }

  protected float getBitPosX(BlockPos pos, int x, int y) {
    return direction > 3 ? pos.getY() + y * Utility.PIXEL_F : pos.getX() + x * Utility.PIXEL_F;
  }

  protected float getBitPosY(BlockPos pos, int x, int y, int z) {
    return direction < 2 ? pos.getY() + y * Utility.PIXEL_F :
        (direction > 3 ? pos.getX() + x * Utility.PIXEL_F : pos.getZ() + z * Utility.PIXEL_F);
  }

  protected float getBitPosZ(BlockPos pos, int y, int z) {
    return direction == 2 || direction == 3 ? pos.getY() + y * Utility.PIXEL_F :
        pos.getZ() + z * Utility.PIXEL_F;
  }

  protected boolean isPointOffLine(float val, float centerVal, float semiDiameter) {
    return val < centerVal - semiDiameter || val > centerVal + semiDiameter;
  }

  protected boolean isPointInRectangle(float dv1, float dv2, float s1, float s2) {
    return dv1 <= s1 && dv1 >= -s1 && dv2 <= s2 && dv2 >= -s2;
  }

  protected boolean isPointInTriangle(float v1, float v2, float center1, float center2, float s1,
                                      float s2) {
    float az = center2 + s2;
    float bx = center1 + s1;
    float bcz = center2 - s2;
    float cx = center1 - s1;
    float dx = v1 - center1;
    float dz = v2 - az;
    az = bcz - az;
    boolean dxz = (bx - center1) * dz - az * dx > 0;
    if (((cx - center1) * dz - az * dx > 0) == dxz) {
      return false;
    }

    return ((cx - bx) * (v2 - bcz) > 0) == dxz;
  }

}