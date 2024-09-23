package com.phylogeny.extrabitmanipulation.armor;

import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.lwjgl.opengl.GL11;

public class GlOperation {
  private final GlOperationType type;
  private float x, y, z, angle;

  public enum GlOperationType {
    TRANSLATION("Translation"),
    ROTATION("Rotation"),
    SCALE("Scale");

    private final String name;

    GlOperationType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public GlOperation(GlOperationType type) {
    this.type = type;
  }

  public GlOperation(GlOperationType type, float x, float y, float z) {
    this(type);
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public GlOperation(GlOperationType type, float x, float y, float z, float angle) {
    this(type, x, y, z);
    this.angle = angle;
  }

  public GlOperation(CompoundTag nbt) {
    type = GlOperationType.values()[nbt.getInt(NBTKeys.ARMOR_GL_OPERATION_TYPE)];
    x = nbt.getFloat(NBTKeys.ARMOR_GL_OPERATION_X);
    y = nbt.getFloat(NBTKeys.ARMOR_GL_OPERATION_Y);
    z = nbt.getFloat(NBTKeys.ARMOR_GL_OPERATION_Z);
    angle = nbt.getFloat(NBTKeys.ARMOR_GL_OPERATION_ANGLE);
  }

  public static GlOperation createTranslation(float x, float y, float z) {
    return new GlOperation(GlOperationType.TRANSLATION, x, y, z);
  }

  public static GlOperation createRotation(float angle, float x, float y, float z) {
    return new GlOperation(GlOperationType.ROTATION, x, y, z, angle);
  }

  public static GlOperation createScale(float x, float y, float z) {
    return new GlOperation(GlOperationType.SCALE, x, y, z);
  }

  public boolean hasData() {
    return x != 0 || y != 0 || z != 0 || (type == GlOperationType.ROTATION && angle % 360 != 0);
  }

  public void execute() {
    switch (type) {
      case TRANSLATION:
        GL11.glTranslatef(x, y, z);
        break;
      case ROTATION:
        GL11.glRotatef(angle, x, y, z);
        break;
      case SCALE:
        GL11.glScalef(x, y, z);
    }
  }

  public static void executeList(List<GlOperation> glOperations) {
    for (GlOperation glOperation : glOperations) {
      glOperation.execute();
    }
  }

  public void saveToNBT(CompoundTag nbt) {
    nbt.putInt(NBTKeys.ARMOR_GL_OPERATION_TYPE, type.ordinal());
    nbt.putFloat(NBTKeys.ARMOR_GL_OPERATION_X, x);
    nbt.putFloat(NBTKeys.ARMOR_GL_OPERATION_Y, y);
    nbt.putFloat(NBTKeys.ARMOR_GL_OPERATION_Z, z);
    nbt.putFloat(NBTKeys.ARMOR_GL_OPERATION_ANGLE, angle);
  }

  public static void saveListToNBT(CompoundTag nbt, String key, List<GlOperation> glOperations) {
    ListTag glOperationsNbt = new ListTag();
    for (GlOperation glOperation : glOperations) {
      CompoundTag glOperationNbt = new CompoundTag();
      glOperation.saveToNBT(glOperationNbt);
      glOperationsNbt.add(glOperationNbt);
    }
    nbt.put(key, glOperationsNbt);
  }

  public static void loadListFromNBT(CompoundTag nbt, String key,
                                     List<GlOperation> glOperations) {
    glOperations.clear();
    ListTag glOperationsNbt = nbt.getList(key, ListTag.TAG_COMPOUND);
    for (int i = 0; i < glOperationsNbt.size(); i++) {
      glOperations.add(new GlOperation(glOperationsNbt.getCompound(i)));
    }
  }

  public GlOperationType getType() {
    return type;
  }

  public float getX() {
    return x;
  }

  public void setX(float x) {
    this.x = x;
  }

  public float getY() {
    return y;
  }

  public void setY(float y) {
    this.y = y;
  }

  public float getZ() {
    return z;
  }

  public void setZ(float z) {
    this.z = z;
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
  }

}