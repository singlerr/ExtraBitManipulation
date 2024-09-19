package com.phylogeny.extrabitmanipulation.helper;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.reference.GuiIDs;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BitAreaHelper {

  public static Vec3 readVecFromNBT(CompoundTag nbt, String key) {
    ListTag bounds = nbt.getList(key, ListTag.TAG_DOUBLE);

    return bounds.isEmpty() ? null :
        new Vec3(bounds.getDouble(0), bounds.getDouble(1), bounds.getDouble(2));
  }

  public static void writeVecToNBT(Vec3 vec, CompoundTag nbt, String key) {
    ListTag bounds = new ListTag();
    appendBound(bounds, vec.x);
    appendBound(bounds, vec.y);
    appendBound(bounds, vec.z);
    nbt.put(key, bounds);
  }

  private static void appendBound(ListTag bounds, double bound) {
    bounds.add(DoubleTag.valueOf(bound));

  }

  public static Direction readFacingFromNBT(CompoundTag nbt, String key) {
    return Direction.values()[nbt.getInt(key)];
  }

  public static void writeFacingToNBT(Direction face, CompoundTag nbt, String key) {
    nbt.putInt(key, face.ordinal());
  }

  public static BlockPos readBlockPosFromNBT(CompoundTag nbt, String key) {
    return BlockPos.of(nbt.getLong(key));
  }

  public static void writeBlockPosToNBT(BlockPos pos, CompoundTag nbt, String key) {
    nbt.putLong(key, pos.asLong());
  }

  public static Vec3 getBitGridOffset(Direction side, boolean inside, float hitX, float hitY,
                                      float hitZ, boolean removeBits) {
    float x = 0, y = 0, z = 0;
    x = hitX < (Math.round(hitX / Utility.PIXEL_F) * Utility.PIXEL_F) ? 1 : -1;
    y = hitY < (Math.round(hitY / Utility.PIXEL_F) * Utility.PIXEL_F) ? 1 : -1;
    z = hitZ < (Math.round(hitZ / Utility.PIXEL_F) * Utility.PIXEL_F) ? 1 : -1;
    double offsetX = Math.abs(side.getStepX());
    double offsetY = Math.abs(side.getStepY());
    double offsetZ = Math.abs(side.getStepZ());
    if (side.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
      if (offsetX > 0) {
        x *= -1;
      }

      if (offsetY > 0) {
        y *= -1;
      }

      if (offsetZ > 0) {
        z *= -1;
      }
    }
    boolean su = side == Direction.UP || side == Direction.SOUTH;
    if (removeBits ? (!inside || !su) : (inside && su)) {
      if (offsetX > 0) {
        x *= -1;
      }

      if (offsetY > 0) {
        y *= -1;
      }

      if (offsetZ > 0) {
        z *= -1;
      }
    }
    return new Vec3(x, y, z);
  }

  public static boolean readBlockStates(ItemStack stack, Player player, Level world,
                                        BlockPos pos,
                                        Vec3 hit, Vec3i drawnStartPoint,
                                        ModelReadData modelingData) {
    ItemModelingTool modelingTool =
        (ItemModelingTool) (ItemStackHelper.isModelingToolStack(stack) ? stack.getItem() : null);
    if (modelingTool == null) {
      return false;
    }

    CompoundTag nbt = modelingTool.initialize(stack, modelingData);
    ModelingBoxSet boxSet = getModelingToolBoxSet(player, pos.getX(), pos.getY(), pos.getZ(),
        hit, drawnStartPoint, false, modelingData.getAreaMode(), modelingData.getSnapMode());
    if (boxSet.isEmpty()) {
      return false;
    }

    BitIOHelper.saveBlockStates(ChiselsAndBitsAPIAccess.apiInstance, player, world,
        boxSet.getBoundingBox(), nbt);
    if (modelingData.getGuiOpen()) {
      player.openMenu(ExtraBitManipulation.instance, GuiIDs.BIT_MAPPING.getID(), player.world, 0, 0,
          0);
    }

    return true;
  }

  public static ModelingBoxSet getModelingToolBoxSet(Player player, int x, int y, int z,
                                                     Vec3 hit,
                                                     Vec3i drawnStartPointModelingTool,
                                                     boolean addToBoxForRender, int modelAreaMode,
                                                     int modeSnapToChunk) {
    AABB boxBounding = null;
    AABB boxPoint = null;
    if (modelAreaMode == 2) {
      if (drawnStartPointModelingTool != null) {
        int x2 = drawnStartPointModelingTool.getX();
        int y2 = drawnStartPointModelingTool.getY();
        int z2 = drawnStartPointModelingTool.getZ();
        if (addToBoxForRender) {
          if (Math.max(x, x2) == x) {
            x++;
          } else {
            x2++;
          }
          if (Math.max(y, y2) == y) {
            y++;
          } else {
            y2++;
          }
          if (Math.max(z, z2) == z) {
            z++;
          } else {
            z2++;
          }
        }
        boxBounding = new AABB(x2, y2, z2,
            Math.abs(x2 - x) <= 16 ? x : (x2 - x > 0 ? x2 - 16 : x2 + 16),
            Math.abs(y2 - y) <= 16 ? y : (y2 - y > 0 ? y2 - 16 : y2 + 16),
            Math.abs(z2 - z) <= 16 ? z : (z2 - z > 0 ? z2 - 16 : z2 + 16));
      }
    } else {
      int hitX = (int) Math.round(hit.x);
      int hitY = (int) Math.round(hit.y);
      int hitZ = (int) Math.round(hit.z);
      boxBounding = new AABB(hitX, hitY, hitZ, hitX, hitY, hitZ);
      boxPoint = boxBounding.inflate(0.005);
      boxBounding = boxBounding.inflate(8);
      if (modelAreaMode == 1) {
        float yaw = Math.abs(player.yHeadRot) % 360;
        int greaterX = 8;
        int lesserX = -8;
        if (player.yHeadRot < 0) {
          greaterX *= -1;
          lesserX *= -1;
        }
        int greaterZ = -8;
        int lesserZ = 8;
        int angleX = 180;
        int angleZ = 90;
        Direction side = player.getDirection();
        if (player.yHeadRot > 0 ? side.getAxisDirection() == Direction.AxisDirection.POSITIVE
            : side == Direction.SOUTH || side == Direction.WEST) {
          greaterZ *= -1;
          lesserZ *= -1;
          if (side == (player.yHeadRot > 0 ? Direction.EAST : Direction.WEST)) {
            lesserX *= -1;
            angleZ = 270;
          } else {
            angleZ = 0;
          }
        }
        boxBounding = boxBounding.move(yaw > angleX ? greaterX : lesserX,
            player.yBodyRot > 0 ? -8 : 8, yaw > angleZ ? greaterZ : lesserZ);
      }
      if (modeSnapToChunk > 0) {
        if (x < 0) {
          x -= 15;
        }

        if (z < 0) {
          z -= 15;
        }

        x -= x % 16;
        z -= z % 16;
        if (modeSnapToChunk == 2) {
          y -= y % 16;
        } else {
          y = (int) boxBounding.minY;
        }
        double offsetX = x - boxBounding.minX;
        double offsetY = y - boxBounding.minY;
        double offsetZ = z - boxBounding.minZ;
        boxBounding = boxBounding.move(offsetX, offsetY, offsetZ);
        boxPoint = boxPoint.move(offsetX, offsetY, offsetZ);
      }
    }
    return new ModelingBoxSet(boxBounding, boxPoint);
  }

  public static class ModelingBoxSet {
    private final AABB boxBounding;
    private final AABB boxPoint;

    public ModelingBoxSet(@Nullable AABB boxBounding, @Nullable AABB boxPoint) {
      this.boxBounding = boxBounding;
      this.boxPoint = boxPoint;
    }

    public AABB getBoundingBox() {
      return boxBounding;
    }

    public AABB getPoint() {
      return boxPoint;
    }

    public boolean hasPoint() {
      return boxPoint != null;
    }

    public boolean isEmpty() {
      return boxBounding == null && boxPoint == null;
    }

  }

}