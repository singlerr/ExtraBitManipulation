package com.phylogeny.extrabitmanipulation.helper;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.config.ConfigBitStack;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingBoolean;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingInt;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.config.ConfigReplacementBits;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorMovingPart;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.BodyPartTemplate;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorMovingPart;
import com.phylogeny.extrabitmanipulation.packet.PacketSetArmorScale;
import com.phylogeny.extrabitmanipulation.packet.PacketSetBitStack;
import com.phylogeny.extrabitmanipulation.packet.PacketSetDirection;
import com.phylogeny.extrabitmanipulation.packet.PacketSetEndsOpen;
import com.phylogeny.extrabitmanipulation.packet.PacketSetHollowShape;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelAreaMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelGuiOpen;
import com.phylogeny.extrabitmanipulation.packet.PacketSetModelSnapMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetSculptMode;
import com.phylogeny.extrabitmanipulation.packet.PacketSetSemiDiameter;
import com.phylogeny.extrabitmanipulation.packet.PacketSetShapeOffset;
import com.phylogeny.extrabitmanipulation.packet.PacketSetShapeType;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTargetArmorBits;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTargetBitGridVertexes;
import com.phylogeny.extrabitmanipulation.packet.PacketSetWallThickness;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import com.phylogeny.extrabitmanipulation.shape.Shape;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import javax.annotation.Nullable;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BitToolSettingsHelper {

  public static String[] getDirectionNames() {
    String[] directionTexts = new String[6];
    for (Direction facing : Direction.values()) {
      directionTexts[facing.ordinal()] =
          facing.getName().substring(0, 1).toUpperCase() + facing.getName().substring(1);
    }

    return directionTexts;
  }

  private static int getInt(ConfigBitToolSettingInt config, CompoundTag nbt, String nbtKey) {
    return config.isPerTool() ? ItemStackHelper.getInt(nbt, config.getDefaultValue(), nbtKey) :
        config.getValue();
  }

  private static boolean getBoolean(ConfigBitToolSettingBoolean config, CompoundTag nbt,
                                    String nbtKey) {
    return config.isPerTool() ? ItemStackHelper.getBoolean(nbt, config.getDefaultValue(), nbtKey) :
        config.getValue();
  }

  private static ItemStack getStack(ConfigBitStack config, CompoundTag nbt, String nbtKey) {
    return config.isPerTool() ? ItemStackHelper.getStack(nbt, nbtKey) : config.getValue();
  }

  private static void setIntProperty(Level world, Configuration configFile,
                                     ConfigBitToolSettingInt config, String catagory, int value) {
    if (!world.isClientSide) {
      return;
    }

    Property prop = configFile.get(catagory, config.getTitle(), config.getDefaultValue());
    if (prop != null) {
      config.setValue(value);
      prop.setValue(value);
      configFile.save();
    }
  }

  private static void setBooleanProperty(Level world, Configuration configFile,
                                         ConfigBitToolSettingBoolean config, String catagory,
                                         boolean value) {
    if (!world.isClientSide) {
      return;
    }

    Property prop = configFile.get(catagory, config.getTitle(), config.getDefaultValue());
    if (prop != null) {
      config.setValue(value);
      prop.setValue(value);
      configFile.save();
    }
  }

  private static void setStackProperty(Level world, Configuration configFile, ConfigBitStack config,
                                       String catagory, IBitBrush value) {
    if (!world.isClientSide) {
      return;
    }

    Property prop = configFile.get(catagory, config.getTitle(), config.getStringDeafult());
    if (prop != null) {
      config.setValue(value == null ? null : value.getItemStack(1));
      prop.setValue(BitIOHelper.getStringFromState(value == null ? null : value.getState()));
      configFile.save();
    }
  }

  public static void setBitMapProperty(boolean isStateMap, String[] stringEntries) {
    Configuration configFile = ConfigHandlerExtraBitManipulation.modelingMapConfigFile;
    Property prop = configFile.get(ConfigHandlerExtraBitManipulation.MODELING_TOOL_MANUAL_MAPPINGS,
        isStateMap ? ConfigHandlerExtraBitManipulation.STATE_TO_BIT_MAP :
            ConfigHandlerExtraBitManipulation.BLOCK_TO_BIT_MAP, new String[] {});
    if (prop != null) {
      prop.setValues(stringEntries);
      configFile.save();
    }
  }

  public static int getModelAreaMode(CompoundTag nbt) {
    return getInt(Configs.modelAreaMode, nbt, NBTKeys.MODEL_AREA_MODE);
  }

  public static void setModelAreaMode(Player player, ItemStack stack, int mode,
                                      @Nullable ConfigBitToolSettingInt modelAreaMode) {
    Level world = player.level();
    if (modelAreaMode == null || modelAreaMode.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetModelAreaMode(mode));
      } else {
        ItemStackHelper.setInt(player, stack, mode, NBTKeys.MODEL_AREA_MODE);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.modelingMapConfigFile,
          modelAreaMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_MODEL, mode);
    }
  }

  public static int getModelSnapMode(CompoundTag nbt) {
    return getInt(Configs.modelSnapMode, nbt, NBTKeys.MODEL_SNAP_MODE);
  }

  public static void setModelSnapMode(Player player, ItemStack stack, int mode,
                                      @Nullable ConfigBitToolSettingInt modelSnapMode) {
    Level world = player.level();
    if (modelSnapMode == null || modelSnapMode.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetModelSnapMode(mode));
      } else {
        ItemStackHelper.setInt(player, stack, mode, NBTKeys.MODEL_SNAP_MODE);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.modelingMapConfigFile,
          modelSnapMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_MODEL, mode);
    }
  }

  public static boolean getModelGuiOpen(CompoundTag nbt) {
    return getBoolean(Configs.modelGuiOpen, nbt, NBTKeys.MODEL_GUI_OPEN);
  }

  public static void setModelGuiOpen(Player player, ItemStack stack, boolean isOpen,
                                     @Nullable ConfigBitToolSettingBoolean modelGuiOpen) {
    Level world = player.level();
    if (modelGuiOpen == null || modelGuiOpen.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetModelGuiOpen(isOpen));
      } else {
        ItemStackHelper.setBoolean(player, stack, isOpen, NBTKeys.MODEL_GUI_OPEN);
      }
    } else if (world.isClientSide) {
      setBooleanProperty(world, ConfigHandlerExtraBitManipulation.modelingMapConfigFile,
          modelGuiOpen, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_MODEL, isOpen);
    }
  }

  public static int getSculptMode(CompoundTag nbt) {
    return getInt(Configs.sculptMode, nbt, NBTKeys.SCULPT_MODE);
  }

  public static void setSculptMode(Player player, ItemStack stack, int mode,
                                   @Nullable ConfigBitToolSettingInt sculptMode) {
    Level world = player.level();
    if (sculptMode == null || sculptMode.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetSculptMode(mode));
      } else {
        ItemStackHelper.setInt(player, stack, mode, NBTKeys.SCULPT_MODE);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, mode);
    }
  }

  public static int getDirection(CompoundTag nbt) {
    return getInt(Configs.sculptDirection, nbt, NBTKeys.DIRECTION);
  }

  public static void setDirection(Player player, ItemStack stack, int direction,
                                  @Nullable ConfigBitToolSettingInt sculptDirection) {
    Level world = player.level();
    if (sculptDirection == null || sculptDirection.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetDirection(direction));
      } else {
        ItemStackHelper.setInt(player, stack, direction, NBTKeys.DIRECTION);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptDirection, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, direction);
    }
  }

  public static int getShapeType(CompoundTag nbt, boolean isCurved) {
    int shapeType =
        getInt(isCurved ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat, nbt,
            NBTKeys.SHAPE_TYPE);
    return isCurved && shapeType > 2 ? Configs.sculptShapeTypeCurved.getDefaultValue()
        : (!isCurved && shapeType < 3 ? Configs.sculptShapeTypeFlat.getDefaultValue() : shapeType);
  }

  public static void setShapeType(Player player, ItemStack stack, boolean isCurved,
                                  int shapeType,
                                  @Nullable ConfigBitToolSettingInt sculptShapeType) {
    Level world = player.level();
    if (sculptShapeType == null || sculptShapeType.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketSetShapeType(isCurved, shapeType));
      } else {
        ItemStackHelper.setInt(player, stack, shapeType, NBTKeys.SHAPE_TYPE);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptShapeType, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, shapeType);
    }
  }

  public static boolean isBitGridTargeted(CompoundTag nbt) {
    return getBoolean(Configs.sculptTargetBitGridVertexes, nbt, NBTKeys.TARGET_BIT_GRID_VERTEXES);
  }

  public static void setBitGridTargeted(Player player, ItemStack stack,
                                        boolean isTargeted, @Nullable
                                        ConfigBitToolSettingBoolean sculptTargetBitGridVertexes) {
    Level world = player.level();
    if (sculptTargetBitGridVertexes == null || sculptTargetBitGridVertexes.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketSetTargetBitGridVertexes(isTargeted));
      } else {
        ItemStackHelper.setBoolean(player, stack, isTargeted, NBTKeys.TARGET_BIT_GRID_VERTEXES);
      }
    } else if (world.isClientSide) {
      setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptTargetBitGridVertexes, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT,
          isTargeted);
    }
  }

  public static int getSemiDiameter(CompoundTag nbt) {
    return getInt(Configs.sculptSemiDiameter, nbt, NBTKeys.SCULPT_SEMI_DIAMETER);
  }

  public static void setSemiDiameter(Player player, ItemStack stack, int semiDiameter,
                                     @Nullable ConfigBitToolSettingInt sculptSemiDiameter) {
    Level world = player.level();
    if (sculptSemiDiameter == null || sculptSemiDiameter.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetSemiDiameter(semiDiameter));
      } else {
        ItemStackHelper.setInt(player, stack, semiDiameter, NBTKeys.SCULPT_SEMI_DIAMETER);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptSemiDiameter, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, semiDiameter);
    }
  }

  public static boolean isHollowShape(CompoundTag nbt, boolean isWire) {
    return getBoolean(isWire ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade, nbt,
        NBTKeys.SCULPT_HOLLOW_SHAPE);
  }

  public static void setHollowShape(Player player, ItemStack stack, boolean isWire,
                                    boolean hollowShape,
                                    @Nullable ConfigBitToolSettingBoolean sculptHollowShape) {
    Level world = player.level();
    if (sculptHollowShape == null || sculptHollowShape.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketSetHollowShape(hollowShape, isWire));
      } else {
        ItemStackHelper.setBoolean(player, stack, hollowShape, NBTKeys.SCULPT_HOLLOW_SHAPE);
      }
    } else if (world.isClientSide) {
      setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptHollowShape, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, hollowShape);
    }
  }

  public static boolean areEndsOpen(CompoundTag nbt) {
    return getBoolean(Configs.sculptOpenEnds, nbt, NBTKeys.OPEN_ENDS);
  }

  public static void setEndsOpen(Player player, ItemStack stack, boolean openEnds,
                                 @Nullable ConfigBitToolSettingBoolean sculptOpenEnds) {
    Level world = player.level();
    if (sculptOpenEnds == null || sculptOpenEnds.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetEndsOpen(openEnds));
      } else {
        ItemStackHelper.setBoolean(player, stack, openEnds, NBTKeys.OPEN_ENDS);
      }
    } else if (world.isClientSide) {
      setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptOpenEnds, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, openEnds);
    }
  }

  public static int getWallThickness(CompoundTag nbt) {
    return getInt(Configs.sculptWallThickness, nbt, NBTKeys.WALL_THICKNESS);
  }

  public static void setWallThickness(Player player, ItemStack stack, int wallThickness,
                                      @Nullable ConfigBitToolSettingInt sculptWallThickness) {
    Level world = player.level();
    if (sculptWallThickness == null || sculptWallThickness.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetWallThickness(wallThickness));
      } else {
        ItemStackHelper.setInt(player, stack, wallThickness, NBTKeys.WALL_THICKNESS);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptWallThickness, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT,
          wallThickness);
    }
  }

  public static ItemStack getBitStack(CompoundTag nbt, boolean isWire) {
    return getStack(isWire ? Configs.sculptSetBitWire : Configs.sculptSetBitSpade, nbt,
        NBTKeys.SET_BIT);
  }

  public static void setBitStack(Player player, ItemStack stack, boolean isWire,
                                 IBitBrush bit, @Nullable ConfigBitStack sculptSetBit) {
    Level world = player.level();
    if (sculptSetBit == null || sculptSetBit.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetBitStack(isWire, bit));
      } else {
        ItemStackHelper.setStack(player, stack, bit == null ? null : bit.getItemStack(1),
            NBTKeys.SET_BIT);
      }
    } else if (world.isClientSide) {
      setStackProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptSetBit, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, bit);
    }
  }

  public static boolean isShapeOffset(CompoundTag nbt) {
    return getBoolean(Configs.sculptOffsetShape, nbt, NBTKeys.OFFSET_SHAPE);
  }

  public static void setShapeOffset(Player player, ItemStack stack, boolean offsetShape,
                                    @Nullable ConfigBitToolSettingBoolean sculptOffsetShape) {
    Level world = player.level();
    if (sculptOffsetShape == null || sculptOffsetShape.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetShapeOffset(offsetShape));
      } else {
        ItemStackHelper.setBoolean(player, stack, offsetShape, NBTKeys.OFFSET_SHAPE);
      }
    } else if (world.isClientSide) {
      setBooleanProperty(world, ConfigHandlerExtraBitManipulation.sculptingConfigFile,
          sculptOffsetShape, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_SCULPT, offsetShape);
    }
  }

  public static int getArmorMode(CompoundTag nbt) {
    return getInt(Configs.armorMode, nbt, NBTKeys.ARMOR_MODE);
  }

  public static void setArmorMode(Player player, ItemStack stack, int mode,
                                  @Nullable ConfigBitToolSettingInt armorMode) {
    Level world = player.level();
    if (armorMode == null || armorMode.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetArmorMode(mode));
      } else {
        ItemStackHelper.setInt(player, stack, mode, NBTKeys.ARMOR_MODE);
      }
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
          armorMode, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR, mode);
    }
  }

  public static int getArmorScale(CompoundTag nbt) {
    return getInt(Configs.armorScale, nbt, NBTKeys.ARMOR_SCALE);
  }

  public static void setArmorScale(Player player, ItemStack stack, int scale,
                                   @Nullable ConfigBitToolSettingInt armorScale) {
    setArmorScale(player, stack, scale, armorScale, null, 0);
  }

  public static void setArmorScale(Player player, ItemStack stack, int scale,
                                   @Nullable ConfigBitToolSettingInt armorScale,
                                   @Nullable ArmorType armorType, int indexArmorSet) {
    Level world = player.level();
    if (armorScale == null || armorScale.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketSetArmorScale(scale, armorType, indexArmorSet));
      }
      ItemStackHelper.setInt(player, stack, scale, NBTKeys.ARMOR_SCALE);
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
          armorScale, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR, scale);
    }
  }

  public static ConfigBitToolSettingInt getArmorMovingPartConfig(ArmorType armorType) {
    return armorType == ArmorType.HELMET ? Configs.armorMovingPartHelmet
        : (armorType == ArmorType.CHESTPLATE ? Configs.armorMovingPartChestplate
        : (armorType == ArmorType.LEGGINGS ? Configs.armorMovingPartLeggings :
        Configs.armorMovingPartBoots));
  }

  public static ArmorMovingPart getArmorMovingPart(CompoundTag nbt,
                                                   ItemChiseledArmor armorPiece) {
    return armorPiece.MOVING_PARTS[getInt(getArmorMovingPartConfig(armorPiece.armorType), nbt,
        NBTKeys.ARMOR_MOVING_PART)];
  }

  public static void setArmorMovingPart(Player player, ItemStack stack,
                                        ItemChiseledArmor armorPiece, int partIndex) {
    setArmorMovingPart(player, stack, partIndex, getArmorMovingPartConfig(armorPiece.armorType),
        null, 0);
  }

  public static void setArmorMovingPart(Player player, ItemStack stack, int partIndex,
                                        @Nullable ConfigBitToolSettingInt armorMovingPart,
                                        @Nullable ArmorType armorType, int indexArmorSet) {
    Level world = player.level();
    if (armorMovingPart == null || armorMovingPart.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketSetArmorMovingPart(partIndex, armorType, indexArmorSet));
      }
      ItemStackHelper.setInt(player, stack, partIndex, NBTKeys.ARMOR_MOVING_PART);
    } else if (world.isClientSide) {
      setIntProperty(world, ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
          armorMovingPart, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR, partIndex);
    }
  }

  public static boolean areArmorBitsTargeted(CompoundTag nbt) {
    return getBoolean(Configs.armorTargetBits, nbt, NBTKeys.ARMOR_TARGET_BITS);
  }

  public static void setArmorBitsTargeted(Player player, ItemStack stack, boolean isTargeted,
                                          @Nullable ConfigBitToolSettingBoolean armorTargetBits) {
    Level world = player.level();
    if (armorTargetBits == null || armorTargetBits.isPerTool()) {
      if (world.isClientSide) {
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetTargetArmorBits(isTargeted));
      } else {
        ItemStackHelper.setBoolean(player, stack, isTargeted, NBTKeys.ARMOR_TARGET_BITS);
      }
    } else if (world.isClientSide) {
      setBooleanProperty(world, ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
          armorTargetBits, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR, isTargeted);
    }
  }

  @Environment(EnvType.CLIENT)
  public static int getArmorTabIndex() {
    return Configs.armorTabIndex.getValue();
  }

  @Environment(EnvType.CLIENT)
  public static void setArmorTabIndex(int armorTabIndex) {
    setIntProperty(ClientHelper.getWorld(),
        ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
        Configs.armorTabIndex, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR,
        armorTabIndex);
  }

  @Environment(EnvType.CLIENT)
  public static int getArmorSetTabIndex() {
    return Configs.armorSetTabIndex.getValue();
  }

  @Environment(EnvType.CLIENT)
  public static void setArmorSetTabIndex(int armorSetTabIndex) {
    setIntProperty(ClientHelper.getWorld(),
        ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
        Configs.armorSetTabIndex, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR,
        armorSetTabIndex);
  }

  @Environment(EnvType.CLIENT)
  public static boolean getArmorPixelTranslation() {
    return Configs.armorPixelTranslation.getValue();
  }

  @Environment(EnvType.CLIENT)
  public static void setArmorPixelTranslation(boolean pixelTranslation) {
    setBooleanProperty(ClientHelper.getWorld(),
        ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
        Configs.armorPixelTranslation, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR,
        pixelTranslation);
  }

  @Environment(EnvType.CLIENT)
  public static boolean getArmorFullIllumination() {
    return Configs.armorFullIllumination.getValue();
  }

  @Environment(EnvType.CLIENT)
  public static void setArmorFullIllumination(boolean armorFullIllumination) {
    setBooleanProperty(ClientHelper.getWorld(),
        ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
        Configs.armorFullIllumination, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR,
        armorFullIllumination);
  }

  @Environment(EnvType.CLIENT)
  public static boolean getArmorLookAtCursor() {
    return Configs.armorLookAtCursor.getValue();
  }

  @Environment(EnvType.CLIENT)
  public static void setArmorLookAtCursor(boolean armorLookAtCursor) {
    setBooleanProperty(ClientHelper.getWorld(),
        ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
        Configs.armorLookAtCursor, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR,
        armorLookAtCursor);
  }

  @Environment(EnvType.CLIENT)
  public static Pair<Integer, Integer> getArmorButtonPosition() {
    return new ImmutablePair<Integer, Integer>(Configs.armorButtonX.getValue(),
        Configs.armorButtonY.getValue());
  }

  @Environment(EnvType.CLIENT)
  public static void setArmorButtonPosition(int armorButtonX, int armorButtonY) {
    setArmorButtonInt(Configs.armorButtonX, armorButtonX);
    setArmorButtonInt(Configs.armorButtonY, armorButtonY);
  }

  private static void setArmorButtonInt(ConfigBitToolSettingInt config, int armorButtonAxis) {
    setIntProperty(ClientHelper.getWorld(),
        ConfigHandlerExtraBitManipulation.chiseledArmorConfigFile,
        config, ConfigHandlerExtraBitManipulation.DATA_CATAGORY_ARMOR, armorButtonAxis);
  }

  public static String getArmorModeText(CompoundTag nbt) {
    return getArmorModeText(getArmorMode(nbt));
  }

  public static String getArmorModeText(int mode) {
    return "Mode: " + ItemChiseledArmor.MODE_TITLES[mode].toLowerCase();
  }

  public static String getArmorScaleText(CompoundTag nbt) {
    return getArmorScaleText(getArmorScale(nbt));
  }

  public static String getArmorScaleText(int scale) {
    return "Scale: " + ItemChiseledArmor.SCALE_TITLES[scale].toLowerCase();
  }

  public static String getArmorMovingPartText(CompoundTag nbt, ItemChiseledArmor armorPiece) {
    return getArmorMovingPartText(getArmorMovingPart(nbt, armorPiece), armorPiece);
  }

  public static String getArmorMovingPartText(ArmorMovingPart part, ItemChiseledArmor armorPiece) {
    return "Moving Part: " + armorPiece.MOVING_PART_TITLES[part.getPartIndex()].toLowerCase();
  }

  public static String getArmorBitsTargetedText(CompoundTag nbt) {
    return getArmorBitsTargetedText(areArmorBitsTargeted(nbt));
  }

  public static String getArmorBitsTargetedText(boolean targetBits) {
    return "Targeting: " + (targetBits ? "bits" : "blocks");
  }

  public static String getModeText(String[] titles, String pefaceText, int mode) {
    return pefaceText + " Mode: " + titles[mode].toLowerCase();
  }

  public static String getModelAreaModeText(CompoundTag nbt) {
    return getModelAreaModeText(getModelAreaMode(nbt));
  }

  public static String getModelAreaModeText(int mode) {
    return getModeText(ItemModelingTool.AREA_MODE_TITLES, "Area", mode);
  }

  public static String getModelSnapModeText(CompoundTag nbt) {
    return getModelSnapModeText(getModelSnapMode(nbt));
  }

  public static String getModelSnapModeText(int mode) {
    return getModeText(ItemModelingTool.SNAP_MODE_TITLES, "Chunk Snap", mode);
  }

  public static String getModelGuiOpenText(CompoundTag nbt) {
    return getModelGuiOpenText(getModelGuiOpen(nbt));
  }

  public static String getModelGuiOpenText(boolean openGui) {
    return "Open GUI Upon Read: " + (openGui ? "true" : "false");
  }

  public static String getSculptModeText(CompoundTag nbt) {
    return getSculptModeText(getSculptMode(nbt));
  }

  public static String getSculptModeText(int mode) {
    return getModeText(ItemSculptingTool.MODE_TITLES, "Sculpting", mode);
  }

  public static String getDirectionText(CompoundTag nbt, boolean showRotation) {
    return getDirectionText(getDirection(nbt), showRotation);
  }

  public static String getDirectionText(int direction, boolean showRotation) {
    String text = "Direction: " + Direction.from3DDataValue(direction % 6).getName().toLowerCase();
    if (showRotation) {
      int rotation = direction / 6;
      if (rotation > 0) {
        text += " (rotation " + (rotation * 90) + "\u00B0)";
      }
    }
    return text;
  }

  public static String getShapeTypeText(CompoundTag nbt, ItemSculptingTool item) {
    return getShapeTypeText(getShapeType(nbt, item.isCurved()));
  }

  public static String getShapeTypeText(int shapeType) {
    return "Shape: " + Shape.SHAPE_NAMES[shapeType].toLowerCase();
  }

  public static String getBitGridTargetedText(CompoundTag nbt) {
    return getBitGridTargetedText(isBitGridTargeted(nbt));
  }

  public static String getBitGridTargetedText(boolean targetBitGrid) {
    return "Targeting: " + (targetBitGrid ? "bit grid vertiecies" : "bits");
  }

  public static String getSemiDiameterText(CompoundTag nbt) {
    return getSemiDiameterText(nbt, getSemiDiameter(nbt));
  }

  public static String getSemiDiameterText(CompoundTag nbt, int semiDiameter) {
    double size = semiDiameter;
    boolean targetBitGrid = isBitGridTargeted(nbt);
    String diameterText = "Semi-Diameter: ";
    if (Configs.displayNameDiameter) {
      size = size * 2;
      if (!targetBitGrid) {
        size += 1;
      }

      diameterText = diameterText.substring(5);
    } else {
      if (!targetBitGrid) {
        size += 0.5;
      }
    }
    if (Configs.displayNameUseMeterUnits) {
      diameterText += Math.round(size * Utility.PIXEL_D * 100) / 100.0 + " meters";
    } else {
      diameterText = addBitLengthString(size, diameterText);
    }
    return diameterText;
  }

  public static String getHollowShapeText(CompoundTag nbt, ItemSculptingTool item) {
    return getHollowShapeText(isHollowShape(nbt, item.removeBits()));
  }

  public static String getHollowShapeText(boolean isHollowShape) {
    return "Interior: " + (isHollowShape ? "hollow" : "solid");
  }

  public static String getOpenEndsText(CompoundTag nbt) {
    return getOpenEndsText(areEndsOpen(nbt));
  }

  public static String getOpenEndsText(boolean areEndsOpen) {
    return "Ends: " + (areEndsOpen ? "open" : "closed");
  }

  public static String getWallThicknessText(CompoundTag nbt) {
    return getWallThicknessText(getWallThickness(nbt));
  }

  public static String getWallThicknessText(int wallThickness) {
    return addBitLengthString(wallThickness, "Wall Thickness: ");
  }

  public static String getOffsetShapeText(CompoundTag nbt) {
    return getOffsetShapeText(isShapeOffset(nbt));
  }

  public static String getOffsetShapeText(boolean offsetShape) {
    return "Shape Placement: " + (offsetShape ? "offset" : "centered");
  }

  public static int cycleData(int intValue, boolean forward, int max) {
    return (intValue + (forward ? 1 : max - 1)) % max;
  }

  private static String addBitLengthString(double size, String diameterText) {
    if (size >= 16) {
      int size2 = ((int) size / 16);
      diameterText += size2 + " meter";
      if (size2 > 1) {
        diameterText += "s";
      }

      size %= 16;
      if (size > 0) {
        diameterText += " & ";
      }
    }
    if (size > 0) {
      if (size == (int) size) {
        diameterText += (int) size;
      } else {
        diameterText += size;
      }
      diameterText += " bits";
    }
    return diameterText;
  }

  public static String getBitName(ItemStack bitStack) {
    return bitStack.getHoverName().getString().replace("Chiseled Bit - ", "");
  }

  public static class ArmorData {
    protected int scale;
    protected ArmorMovingPart part;

    public ArmorData() {
    }

    public ArmorData(CompoundTag nbt, ItemChiseledArmor armorPiece) {
      scale = BitToolSettingsHelper.getArmorScale(nbt);
      part = BitToolSettingsHelper.getArmorMovingPart(nbt, armorPiece);
    }

    public void toBytes(ByteBuf buffer) {
      buffer.writeInt(scale);
      buffer.writeInt(part.ordinal());
    }

    public void fromBytes(ByteBuf buffer) {
      scale = buffer.readInt();
      part = ArmorMovingPart.values()[buffer.readInt()];
    }

    public int getScale() {
      return scale;
    }

    public ArmorMovingPart getMovingPart() {
      return part;
    }

  }

  public static class ArmorBodyPartTemplateData extends ArmorData {
    private int mode;
    private boolean bitsTargeted;

    public ArmorBodyPartTemplateData() {
    }

    public ArmorBodyPartTemplateData(CompoundTag nbt, ItemChiseledArmor armor) {
      super(nbt, armor);
      mode = BitToolSettingsHelper.getArmorMode(nbt);
      bitsTargeted = BitToolSettingsHelper.areArmorBitsTargeted(nbt);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
      super.toBytes(buffer);
      buffer.writeInt(mode);
      buffer.writeBoolean(bitsTargeted);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
      super.fromBytes(buffer);
      mode = buffer.readInt();
      bitsTargeted = buffer.readBoolean();
    }

    public int getMode() {
      return mode;
    }

    public boolean areBitsTargeted() {
      return bitsTargeted;
    }

  }

  public static class ArmorCollectionData extends ArmorData {
    private AABB boxCollection;
    private Direction facing;
    private Vec3 originBodyPart;

    public ArmorCollectionData() {
    }

    public ArmorCollectionData(CompoundTag nbt, ItemChiseledArmor armor, AABB boxCollection) {
      super(nbt, armor);
      ArmorBodyPartTemplateBoxData boxData = new ArmorBodyPartTemplateBoxData(nbt, armor);
      facing = boxData.getFacingBox();
      this.boxCollection = boxCollection;
      originBodyPart =
          new Vec3(boxData.getBox().minX, boxData.getBox().minY, boxData.getBox().minZ);
      float offsetX, offsetY, offsetZ;
      ArmorMovingPart movingPart = part;
      int scale = (int) Math.pow(2, this.scale);
      if (movingPart == ArmorMovingPart.HEAD) {
        offsetX = offsetZ = 8 - scale * 4;
        offsetY = 8 - scale * 8;
      } else if (movingPart.getBodyPartTemplate() == BodyPartTemplate.TORSO) {
        if (facing.getAxis() == Direction.Axis.Z) {
          offsetX = 8 - scale * 4;
          offsetZ = 8 - scale * 2;
        } else {
          offsetX = 8 - scale * 2;
          offsetZ = 8 - scale * 4;
        }
        offsetY = 8 - scale * (movingPart == ArmorMovingPart.PELVIS ? 8 : 4);
      } else {
        offsetX = offsetZ = 8 - scale * 2;
        offsetY = 8 - scale *
            (movingPart == ArmorMovingPart.FOOT_RIGHT || movingPart == ArmorMovingPart.FOOT_LEFT ?
                8 : 4);
      }
      originBodyPart = originBodyPart.subtract(offsetX * Utility.PIXEL_D, offsetY * Utility.PIXEL_D,
          offsetZ * Utility.PIXEL_D);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
      super.toBytes(buffer);
      buffer.writeDouble(boxCollection.minX);
      buffer.writeDouble(boxCollection.minY);
      buffer.writeDouble(boxCollection.minZ);
      buffer.writeDouble(boxCollection.maxX);
      buffer.writeDouble(boxCollection.maxY);
      buffer.writeDouble(boxCollection.maxZ);
      buffer.writeInt(facing.ordinal());
      buffer.writeDouble(originBodyPart.x);
      buffer.writeDouble(originBodyPart.y);
      buffer.writeDouble(originBodyPart.z);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
      super.fromBytes(buffer);
      boxCollection = new AABB(buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
          buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
      facing = Direction.values()[buffer.readInt()];
      originBodyPart = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public Direction getFacing() {
      return facing;
    }

    public AABB getCollectionBox() {
      return boxCollection;
    }

    public Vec3 getOriginBodyPart() {
      return originBodyPart;
    }

  }

  public static class ArmorBodyPartTemplateBoxData {
    private final Direction facingBox;
    AABB boxTemplate;

    public ArmorBodyPartTemplateBoxData(CompoundTag nbt, ItemChiseledArmor armorPiece) {
      facingBox = BitAreaHelper.readFacingFromNBT(nbt, NBTKeys.ARMOR_FACING_BOX);
      boxTemplate = ItemChiseledArmor.getBodyPartTemplateBox(nbt.getFloat(NBTKeys.ARMOR_YAW_PLAYER),
          nbt.getBoolean(NBTKeys.ARMOR_USE_BIT_GRID),
          facingBox, BitAreaHelper.readFacingFromNBT(nbt, NBTKeys.ARMOR_FACING_PLACEMENT),
          BitAreaHelper.readBlockPosFromNBT(nbt, NBTKeys.ARMOR_POS),
          BitAreaHelper.readVecFromNBT(nbt, NBTKeys.ARMOR_HIT),
          BitToolSettingsHelper.getArmorScale(nbt),
          BitToolSettingsHelper.getArmorMovingPart(nbt, armorPiece));
    }

    public Direction getFacingBox() {
      return facingBox;
    }

    public AABB getBox() {
      return boxTemplate;
    }

  }

  public static class ModelReadData {
    private int areaMode, chunkSnapMode;
    private boolean guiOpen;

    public ModelReadData() {
    }

    public ModelReadData(CompoundTag nbt) {
      areaMode = BitToolSettingsHelper.getModelAreaMode(nbt);
      chunkSnapMode = BitToolSettingsHelper.getModelSnapMode(nbt);
      guiOpen = BitToolSettingsHelper.getModelGuiOpen(nbt);
    }

    public void toBytes(ByteBuf buffer) {
      buffer.writeInt(areaMode);
      buffer.writeInt(chunkSnapMode);
      buffer.writeBoolean(guiOpen);
    }

    public void fromBytes(ByteBuf buffer) {
      areaMode = buffer.readInt();
      chunkSnapMode = buffer.readInt();
      guiOpen = buffer.readBoolean();
    }

    public int getAreaMode() {
      return areaMode;
    }

    public int getSnapMode() {
      return chunkSnapMode;
    }

    public boolean getGuiOpen() {
      return guiOpen;
    }

  }

  public static class ModelWriteData {
    private ConfigReplacementBits replacementBitsUnchiselable = new ConfigReplacementBits();
    private ConfigReplacementBits replacementBitsInsufficient = new ConfigReplacementBits();
    private Map<BlockState, IBitBrush> stateToBitMap, blockToBitMap;
    private boolean bitMapPerTool;

    public ModelWriteData() {
    }

    public ModelWriteData(boolean bitMapPerTool) {
      this.bitMapPerTool = bitMapPerTool;
      replacementBitsUnchiselable = Configs.replacementBitsUnchiselable;
      replacementBitsInsufficient = Configs.replacementBitsInsufficient;
      stateToBitMap = Configs.modelStateToBitMap;
      blockToBitMap = Configs.modelBlockToBitMap;
    }

    public void toBytes(ByteBuf buffer) {
      replacementBitsUnchiselable.toBytes(buffer);
      replacementBitsInsufficient.toBytes(buffer);
      buffer.writeBoolean(bitMapPerTool);
      if (!bitMapPerTool) {
        BitIOHelper.stateToBitMapToBytes(buffer, stateToBitMap);
        BitIOHelper.stateToBitMapToBytes(buffer, blockToBitMap);
      }
    }

    public void fromBytes(ByteBuf buffer) {
      replacementBitsUnchiselable.fromBytes(buffer);
      replacementBitsInsufficient.fromBytes(buffer);
      bitMapPerTool = buffer.readBoolean();
      stateToBitMap = bitMapPerTool ? null : BitIOHelper.stateToBitMapFromBytes(buffer);
      blockToBitMap = bitMapPerTool ? null : BitIOHelper.stateToBitMapFromBytes(buffer);
    }

    public ConfigReplacementBits getReplacementBitsUnchiselable() {
      return replacementBitsUnchiselable;
    }

    public ConfigReplacementBits getReplacementBitsInsufficient() {
      return replacementBitsInsufficient;
    }

    public Map<BlockState, IBitBrush> getStateToBitMap(IChiselAndBitsAPI api, ItemStack stack) {
      return bitMapPerTool ?
          BitIOHelper.readStateToBitMapFromNBT(api, stack, NBTKeys.STATE_TO_BIT_MAP_PERMANENT) :
          stateToBitMap;
    }

    public Map<BlockState, IBitBrush> getBlockToBitMap(IChiselAndBitsAPI api, ItemStack stack) {
      return bitMapPerTool ?
          BitIOHelper.readStateToBitMapFromNBT(api, stack, NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT) :
          blockToBitMap;
    }

  }

  public static class SculptingData {
    private int sculptMode, direction, shapeType, semiDiameter, wallThickness;
    private boolean targetBitGridVertexes, hollowShape, openEnds, offsetShape;
    private ItemStack setBitStack;
    private float semiDiameterPadding;

    public SculptingData() {
    }

    public SculptingData(CompoundTag nbt, ItemSculptingTool toolItem) {
      sculptMode = BitToolSettingsHelper.getSculptMode(nbt);
      direction = BitToolSettingsHelper.getDirection(nbt);
      shapeType = BitToolSettingsHelper.getShapeType(nbt, toolItem.isCurved());
      targetBitGridVertexes = BitToolSettingsHelper.isBitGridTargeted(nbt);
      semiDiameter = BitToolSettingsHelper.getSemiDiameter(nbt);
      hollowShape = BitToolSettingsHelper.isHollowShape(nbt, toolItem.removeBits());
      openEnds = BitToolSettingsHelper.areEndsOpen(nbt);
      wallThickness = BitToolSettingsHelper.getWallThickness(nbt);
      setBitStack = BitToolSettingsHelper.getBitStack(nbt, toolItem.removeBits());
      semiDiameterPadding = Configs.semiDiameterPadding;
      offsetShape = BitToolSettingsHelper.isShapeOffset(nbt);
    }

    public void toBytes(ByteBuf buffer) {
      buffer.writeInt(sculptMode);
      buffer.writeInt(direction);
      buffer.writeInt(shapeType);
      buffer.writeBoolean(targetBitGridVertexes);
      buffer.writeInt(semiDiameter);
      buffer.writeBoolean(hollowShape);
      buffer.writeBoolean(openEnds);
      buffer.writeInt(wallThickness);
      FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buffer);
      friendlyByteBuf.writeItem(setBitStack);
      buffer.writeFloat(semiDiameterPadding);
      buffer.writeBoolean(offsetShape);
    }

    public void fromBytes(ByteBuf buffer) {
      sculptMode = buffer.readInt();
      direction = buffer.readInt();
      shapeType = buffer.readInt();
      targetBitGridVertexes = buffer.readBoolean();
      semiDiameter = buffer.readInt();
      hollowShape = buffer.readBoolean();
      openEnds = buffer.readBoolean();
      wallThickness = buffer.readInt();
      FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(buffer);
      setBitStack = friendlyByteBuf.readItem();
      semiDiameterPadding = buffer.readFloat();
      offsetShape = buffer.readBoolean();
    }

    public int getSculptMode() {
      return sculptMode;
    }

    public int getDirection() {
      return direction;
    }

    public int getShapeType() {
      return shapeType;
    }

    public int getSemiDiameter() {
      return semiDiameter;
    }

    public int getWallThickness() {
      return wallThickness;
    }

    public boolean isBitGridTargeted() {
      return targetBitGridVertexes;
    }

    public boolean isHollowShape() {
      return hollowShape;
    }

    public boolean areEndsOpen() {
      return openEnds;
    }

    public ItemStack getBitStack() {
      return setBitStack;
    }

    public float getSemiDiameterPadding() {
      return semiDiameterPadding;
    }

    public boolean isShapeOffset() {
      return offsetShape;
    }

  }

}