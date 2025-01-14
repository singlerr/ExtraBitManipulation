package com.phylogeny.extrabitmanipulation.item;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.config.ConfigProperty;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.SculptingData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import com.phylogeny.extrabitmanipulation.shape.AsymmetricalShape;
import com.phylogeny.extrabitmanipulation.shape.Cone;
import com.phylogeny.extrabitmanipulation.shape.ConeElliptic;
import com.phylogeny.extrabitmanipulation.shape.Cube;
import com.phylogeny.extrabitmanipulation.shape.Cuboid;
import com.phylogeny.extrabitmanipulation.shape.Cylinder;
import com.phylogeny.extrabitmanipulation.shape.CylinderElliptic;
import com.phylogeny.extrabitmanipulation.shape.Ellipsoid;
import com.phylogeny.extrabitmanipulation.shape.PrismIsoscelesTriangular;
import com.phylogeny.extrabitmanipulation.shape.PyramidIsoscelesTriangular;
import com.phylogeny.extrabitmanipulation.shape.PyramidRectangular;
import com.phylogeny.extrabitmanipulation.shape.PyramidSquare;
import com.phylogeny.extrabitmanipulation.shape.Shape;
import com.phylogeny.extrabitmanipulation.shape.Sphere;
import com.phylogeny.extrabitmanipulation.shape.SymmetricalShape;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ItemSculptingTool extends ItemBitToolBase {
  public static final String[] MODE_TITLES = new String[] {"Local", "Global", "Drawn"};
  private final boolean curved;
  private final boolean removeBits;

  public ItemSculptingTool(Properties properties, boolean curved, boolean removeBits, String name) {
    super(properties, name);
    this.curved = curved;
    this.removeBits = removeBits;
  }

  public boolean isCurved() {
    return curved;
  }

  public boolean removeBits() {
    return removeBits;
  }


  @Override
  public boolean isFoil(ItemStack stack) {
    return ItemStackHelper.hasKey(stack, NBTKeys.REMAINING_USES) &&
        ItemStackHelper.getNBT(stack).getInt(NBTKeys.REMAINING_USES)
            < ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage;
  }

  @Override
  public int getBarWidth(ItemStack itemStack) {
    return (int) (1 - getDurability(itemStack));
  }

  @Override
  public int getBarColor(ItemStack itemStack) {
    return Mth.hsvToRgb((float) (Math.max(0.0F, getDurability(itemStack)) / 3.0F), 1.0F, 1.0F);
  }


  private double getDurability(ItemStack stack) {
    return ItemStackHelper.getNBTOrNew(stack).getInt(NBTKeys.REMAINING_USES)
        / ((double) ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage);
  }

  @Override
  public boolean initialize(ItemStack stack) {
    super.initialize(stack);
    CompoundTag nbt = stack.getTag();
    initInt(nbt, NBTKeys.REMAINING_USES,
        ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage);
    return true;
  }

  public CompoundTag initialize(ItemStack stack, SculptingData sculptingData) {
    CompoundTag nbt = ItemStackHelper.initNBT(stack);
    initInt(nbt, NBTKeys.REMAINING_USES,
        ((ConfigProperty) Configs.itemPropertyMap.get(this)).maxDamage);
    initInt(nbt, NBTKeys.SCULPT_MODE, sculptingData.getSculptMode());
    initInt(nbt, NBTKeys.SCULPT_SEMI_DIAMETER, sculptingData.getSemiDiameter());
    initInt(nbt, NBTKeys.DIRECTION, sculptingData.getDirection());
    initBoolean(nbt, NBTKeys.TARGET_BIT_GRID_VERTEXES, sculptingData.isBitGridTargeted());
    initInt(nbt, NBTKeys.SHAPE_TYPE, sculptingData.getShapeType());
    initBoolean(nbt, NBTKeys.SCULPT_HOLLOW_SHAPE, sculptingData.isHollowShape());
    initBoolean(nbt, NBTKeys.OPEN_ENDS, sculptingData.areEndsOpen());
    initInt(nbt, NBTKeys.WALL_THICKNESS, sculptingData.getWallThickness());
    if (!nbt.contains(NBTKeys.SET_BIT) && !sculptingData.getBitStack().isEmpty()) {
      CompoundTag nbt2 = new CompoundTag();
      sculptingData.getBitStack().save(nbt2);
      nbt.put(NBTKeys.SET_BIT, nbt2);
    }
    initBoolean(nbt, NBTKeys.OFFSET_SHAPE, sculptingData.isShapeOffset());
    return nbt;
  }

  public boolean sculptBlocks(ItemStack stack, Player player, Level world, BlockPos pos,
                              Direction side, Vec3 hit, Vec3 drawnStartPoint,
                              SculptingData sculptingData) {
    ItemStack setBitStack = sculptingData.getBitStack();
    if (setBitStack.isEmpty() && !removeBits) {
      return false;
    }

    if (!world.isClientSide) {
      initialize(stack);
      player.containerMenu.sendAllDataToRemote();
    }
    CompoundTag nbt = initialize(stack, sculptingData);
    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    boolean inside = wasInsideClicked(side, hit, pos);
    if (!removeBits && !inside) {
      pos = pos.relative(side);
    }

    boolean globalMode = sculptingData.getSculptMode() == 1;
    if (drawnStartPoint != null || globalMode || isValidBlock(api, world, pos)) {
      float hitX = (float) hit.x - pos.getX();
      float hitY = (float) hit.y - pos.getY();
      float hitZ = (float) hit.z - pos.getZ();
      IBitLocation bitLoc = api.getBitPos(hitX, hitY, hitZ, side, pos, false);
      if (bitLoc != null) {
        int direction = sculptingData.getDirection();
        int shapeType = sculptingData.getShapeType();
        boolean hollowShape = sculptingData.isHollowShape();
        boolean openEnds = sculptingData.areEndsOpen();
        float wallThickness = sculptingData.getWallThickness() * Utility.PIXEL_F;
        float padding = sculptingData.getSemiDiameterPadding();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        float x2 = x + bitLoc.getBitX() * Utility.PIXEL_F;
        float y2 = y + bitLoc.getBitY() * Utility.PIXEL_F;
        float z2 = z + bitLoc.getBitZ() * Utility.PIXEL_F;
        Shape shape;
        AABB box;
        if (shapeType != 4 && shapeType != 5) {
          direction %= 6;
        }

        if (drawnStartPoint != null) {
          switch (shapeType) {
            case 1:
              shape = new CylinderElliptic();
              break;
            case 2:
              shape = new ConeElliptic();
              break;
            case 3:
              shape = new Cuboid();
              break;
            case 4:
              shape = new PrismIsoscelesTriangular();
              break;
            case 5:
              shape = new PyramidIsoscelesTriangular();
              break;
            case 6:
              shape = new PyramidRectangular();
              break;
            default:
              shape = new Ellipsoid();
              break;
          }
          float x3 = (float) drawnStartPoint.x;
          float y3 = (float) drawnStartPoint.y;
          float z3 = (float) drawnStartPoint.z;
          float minX = addPaddingToMin(x2, x3, padding);
          float minY = addPaddingToMin(y2, y3, padding);
          float minZ = addPaddingToMin(z2, z3, padding);
          float maxX = addPaddingToMax(x2, x3, padding);
          float maxY = addPaddingToMax(y2, y3, padding);
          float maxZ = addPaddingToMax(z2, z3, padding);
          box = new AABB(Math.floor(minX), Math.floor(minY), Math.floor(minZ),
              Math.ceil(maxX), Math.ceil(maxY), Math.ceil(maxZ));
          float f = 0.5F;
          minX *= f;
          minY *= f;
          minZ *= f;
          maxX *= f;
          maxY *= f;
          maxZ *= f;
          ((AsymmetricalShape) shape).init(maxX + minX, maxY + minY, maxZ + minZ, maxX - minX,
              maxY - minY, maxZ - minZ,
              direction, hollowShape, wallThickness, openEnds);
        } else {
          switch (shapeType) {
            case 1:
              shape = new Cylinder();
              break;
            case 2:
              shape = new Cone();
              break;
            case 3:
              shape = new Cube();
              break;
            case 4:
              shape = new PrismIsoscelesTriangular();
              break;
            case 5:
              shape = new PyramidIsoscelesTriangular();
              break;
            case 6:
              shape = new PyramidSquare();
              break;
            default:
              shape = new Sphere();
              break;
          }
          int semiDiameter = sculptingData.getSemiDiameter();
          int blockSemiDiameter = globalMode ? (int) Math.ceil(semiDiameter / 16.0) : 0;
          if (sculptingData.isShapeOffset() && !removeBits) {
            int offsetX = side.getStepX();
            int offsetY = side.getStepY();
            int offsetZ = side.getStepZ();
            x2 += offsetX * Utility.PIXEL_F * semiDiameter;
            y2 += offsetY * Utility.PIXEL_F * semiDiameter;
            z2 += offsetZ * Utility.PIXEL_F * semiDiameter;
            x += offsetX * blockSemiDiameter;
            y += offsetY * blockSemiDiameter;
            z += offsetZ * blockSemiDiameter;
          }
          box = new AABB(x - blockSemiDiameter, y - blockSemiDiameter, z - blockSemiDiameter,
              x + blockSemiDiameter, y + blockSemiDiameter, z + blockSemiDiameter);
          float f = 0;
          Vec3 vecOffset = new Vec3(0, 0, 0);
          if (sculptingData.isBitGridTargeted()) {
            f = Utility.PIXEL_F * 0.5F;
            vecOffset = BitAreaHelper.getBitGridOffset(side, inside, hitX, hitY, hitZ, removeBits);
          }
          if (shapeType == 4 || shapeType == 5) {
            AsymmetricalShape asymmetricalShape = (AsymmetricalShape) shape;
            asymmetricalShape.setEquilateral(true);
            float radius = addPadding(semiDiameter, padding) - f;
            asymmetricalShape.init(x2 + f * (float) vecOffset.x, y2 + f * (float) vecOffset.y,
                z2 + f * (float) vecOffset.z, radius,
                radius, radius, direction, hollowShape, wallThickness, openEnds);
          } else {
            ((SymmetricalShape) shape).init(x2 + f * (float) vecOffset.x,
                y2 + f * (float) vecOffset.y, z2 + f * (float) vecOffset.z,
                addPadding(semiDiameter, padding) - f, direction, hollowShape, wallThickness,
                openEnds);
          }
        }
        boolean creativeMode = player.isCreative();
        Map<BlockState, Integer> bitTypes = null;
        if (removeBits && !world.isClientSide && !creativeMode) {
          bitTypes = new HashMap<BlockState, Integer>();
        }

        int initialpossibleUses = Integer.MAX_VALUE;
        IBitBrush setBit = null;
        try {
          setBit = api.createBrush(setBitStack);
          if (!removeBits && !creativeMode) {
            initialpossibleUses = BitInventoryHelper.countInventoryBits(api, player, setBitStack);
          }
        } catch (InvalidBitItem e) {
        }
        int remainingUses = nbt.getInt(NBTKeys.REMAINING_USES);
        if (!creativeMode && initialpossibleUses > remainingUses) {
          initialpossibleUses = remainingUses;
        }

        int possibleUses = initialpossibleUses;
        boolean changed = false;
        try {
          api.beginUndoGroup(player);
          for (int i = (int) box.minX; i <= box.maxX; i++) {
            for (int j = (int) box.minY; j <= box.maxY; j++) {
              for (int k = (int) box.minZ; k <= box.maxZ; k++) {
                if (possibleUses > 0) {
                  possibleUses =
                      sculptBlock(api, player, world, new BlockPos(i, j, k), shape, bitTypes,
                          possibleUses, setBit);
                }
              }
            }
          }
        } finally {
          api.endUndoGroup(player);
          if (!world.isClientSide && !Configs.dropBitsPerBlock) {
            BitInventoryHelper.giveOrDropStacks(player, world, pos, shape, api, bitTypes);
          }

          int change = initialpossibleUses - possibleUses;
          int newRemainingUses = remainingUses -
              (((ConfigProperty) Configs.itemPropertyMap.get(this)).takesDamage ? change : 0);
          if (!world.isClientSide && !creativeMode) {
            nbt.putInt(NBTKeys.REMAINING_USES, newRemainingUses);
            if (!removeBits) {
              BitInventoryHelper.removeOrAddInventoryBits(api, player, setBitStack, change, false);
            }

            if (newRemainingUses <= 0) {
              player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
//              ForgeEventFactory.onPlayerDestroyItem(player, stack, player.getActiveHand());
            }
            player.containerMenu.sendAllDataToRemote();
          }
          if (!creativeMode && newRemainingUses <= 0) {
            player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
//            player.renderBrokenItemStack(stack);
          }

          changed = possibleUses < initialpossibleUses;
          if (changed) {
            playPlacementSound(player, world, pos, 8.0F);
          }
        }
        return changed;
      }
    }
    return false;
  }

  public static void playPlacementSound(Player player, Level world, BlockPos pos,
                                        float volumeReduction) {
    @SuppressWarnings("deprecation")
    SoundType sound = Blocks.STONE.getSoundType(Blocks.STONE.defaultBlockState());
    world.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
        (sound.getVolume()) / volumeReduction, sound.getPitch() * 0.8F);
  }

  private float addPadding(float value, float padding) {
    return (value + padding) * Utility.PIXEL_F;
  }

  private float addPaddingToMin(float value1, float value2, float padding) {
    return Math.min(value1, value2) - padding * Utility.PIXEL_F;
  }

  private float addPaddingToMax(float value1, float value2, float padding) {
    return Math.max(value1, value2) + padding * Utility.PIXEL_F;
  }

  public static boolean wasInsideClicked(Direction dir, Vec3 hit, BlockPos pos) {
    if (hit != null) {
      switch (dir.ordinal()) {
        case 0:
          return hit.y > pos.getY();
        case 1:
          return hit.y < pos.getY() + 1;
        case 2:
          return hit.z > pos.getZ();
        case 3:
          return hit.z < pos.getZ() + 1;
        case 4:
          return hit.x > pos.getX();
        case 5:
          return hit.x < pos.getX() + 1;
      }
    }
    return false;
  }

  @SuppressWarnings("null")
  private int sculptBlock(IChiselAndBitsAPI api, Player player, Level world, BlockPos pos,
                          Shape shape,
                          Map<BlockState, Integer> bitTypes, int remainingUses, IBitBrush setBit) {
    if (isValidBlock(api, world, pos)) {
      IBitAccess bitAccess;
      try {
        bitAccess = api.getBitAccess(world, pos);
      } catch (CannotBeChiseled e) {
        return remainingUses;
      }
      boolean byPassBitChecks = shape.isBlockInsideShape(pos);
      int initialRemainingUses = remainingUses;
      for (int i = 0; i < 16; i++) {
        for (int j = 0; j < 16; j++) {
          for (int k = 0; k < 16; k++) {
            IBitBrush bit = bitAccess.getBitAt(i, j, k);
            if ((removeBits ? (!bit.isAir() &&
                !(setBit != null && !setBit.isAir() && !setBit.getState().equals(bit.getState()))) :
                bit.isAir())
                && (byPassBitChecks || shape.isPointInsideShape(pos, i, j, k))) {
              if (bitTypes != null) {
                BlockState state = bit.getState();
                if (!bitTypes.containsKey(state)) {
                  bitTypes.put(state, 1);
                } else {
                  bitTypes.put(state, bitTypes.get(state) + 1);
                }
              }
              try {
                bitAccess.setBitAt(i, j, k, removeBits ? null : setBit);
                remainingUses--;
              } catch (SpaceOccupied e) {
              }
              if (remainingUses == 0) {
                bitAccess.commitChanges(true);
                return remainingUses;
              }
            }
          }
        }
      }
      if (!world.isClientSide && Configs.dropBitsPerBlock) {
        BitInventoryHelper.giveOrDropStacks(player, world, pos, shape, api, bitTypes);
      }

      if (remainingUses < initialRemainingUses) {
        bitAccess.commitChanges(true);
      }
    }
    return remainingUses;
  }

  private boolean isValidBlock(IChiselAndBitsAPI api, Level world, BlockPos pos) {
    return api.canBeChiseled(world, pos) && (!removeBits || !world.isEmptyBlock(pos));
  }

  @Override
  public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                              List<Component> tooltip, TooltipFlag tooltipFlag) {
    boolean shiftDown = Screen.hasShiftDown();
    boolean ctrlDown = Screen.hasControlDown();
    addColorInformation(tooltip, shiftDown);
    CompoundTag nbt = stack.getTag();
    int mode = BitToolSettingsHelper.getSculptMode(nbt);
    if (shiftDown) {
      tooltip.add(
          colorSettingText(BitToolSettingsHelper.getSculptModeText(mode), Configs.sculptMode));
    }

    ItemStack setBitStack = BitToolSettingsHelper.getBitStack(nbt, removeBits);
    if (!ctrlDown || shiftDown) {
      String bitType = "Bit Type To " + (removeBits ? "Remove" : "Add") + ": ";
      if (!setBitStack.isEmpty()) {
        Component bitStateName = Component.literal("N/A");
        BlockState state = ModUtil.getStateById(ItemChiseledBit.getStackState(setBitStack));
        if (state != null) {
          Component name = ItemChiseledBit.getBitStateName(state);
          if (name != null) {
            bitStateName = name;
          }
        }
        bitType += bitStateName;
      } else {
        bitType += removeBits ? "any" : "none";
      }
      tooltip.add(colorSettingText(bitType,
          removeBits ? Configs.sculptSetBitWire : Configs.sculptSetBitSpade));
    }
    if (shiftDown) {
      int shapeType = BitToolSettingsHelper.getShapeType(nbt, curved);
      tooltip.add(colorSettingText(
          BitToolSettingsHelper.getDirectionText(nbt, shapeType == 4 || shapeType == 5),
          Configs.sculptDirection));
      tooltip.add(colorSettingText(BitToolSettingsHelper.getShapeTypeText(shapeType),
          removeBits ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat));
      boolean targetBits = BitToolSettingsHelper.isBitGridTargeted(nbt);
      tooltip.add(colorSettingText(BitToolSettingsHelper.getBitGridTargetedText(targetBits),
          Configs.sculptTargetBitGridVertexes).append(targetBits ? " (corners)" : " (centers)"));
      tooltip.add(colorSettingText(BitToolSettingsHelper.getSemiDiameterText(nbt),
          Configs.sculptSemiDiameter));
      if (!removeBits) {
        tooltip.add(colorSettingText(BitToolSettingsHelper.getOffsetShapeText(nbt),
            Configs.sculptOffsetShape));
      }

      tooltip.add(colorSettingText(BitToolSettingsHelper.getHollowShapeText(nbt, this),
          removeBits ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade));
      tooltip.add(colorSettingText("  - " + BitToolSettingsHelper.getOpenEndsText(nbt),
          Configs.sculptOpenEnds));
      tooltip.add(colorSettingText("  - " + BitToolSettingsHelper.getWallThicknessText(nbt),
          Configs.sculptWallThickness));
    } else {
      if (ctrlDown) {
        String shiftText = getColoredKeyBindText(KeyBindingsExtraBitManipulation.SHIFT);
        String removeAddText = removeBits ? "remove" : "add";
        String toFromText = removeBits ? "from" : "to";
        if (mode == 2) {
          tooltip.add(Component.literal("Left click point on block, drag"));
          tooltip.add(Component.literal("    to another point, then"));
          tooltip.add(Component.literal("    release to " + removeAddText + " bits " + toFromText));
          tooltip.add(Component.literal("    all intersecting blocks."));
        } else {
          String shapeControlText = "Left click block to " + removeAddText + " bits";
          if (mode == 0) {
            shapeControlText += ".";
          }

          tooltip.add(Component.literal(shapeControlText));
          if (mode != 0) {
            String areaText = toFromText;
            tooltip.add(Component.literal("    " + areaText + " all intersecting blocks."));
          }
        }
        tooltip.add(Component.literal("Right click to cycle modes."));
        if (!removeBits) {
          tooltip.add(Component.literal(shiftText + " left click bit to set bit type."));
        }

        tooltip.add(Component.literal(shiftText + " mouse wheel to change"));
        tooltip.add(Component.literal("    " + (removeBits ? "removal" : "addition") +
            (Configs.displayNameDiameter ? " " : " semi-") + "diameter."));
        tooltip.add(Component.empty());
        String controlText = getColoredKeyBindText(KeyBindingsExtraBitManipulation.CONTROL);
        tooltip.add(Component.literal(controlText + " right click to"));
        tooltip.add(Component.literal("    change shape."));
        tooltip.add(Component.literal(controlText + " left click to toggle"));
        tooltip.add(Component.literal("    target between"));
        tooltip.add(Component.literal("    bits & vertecies."));
        tooltip.add(Component.literal(controlText + " mouse wheel to"));
        tooltip.add(Component.literal("    change direction."));
        tooltip.add(Component.empty());
        String altText = getColoredKeyBindText(KeyBindingsExtraBitManipulation.ALT);
        tooltip.add(Component.literal(altText + " right click to toggle"));
        tooltip.add(Component.literal("    shapes solid or hollow."));
        tooltip.add(Component.literal(altText + " left click to toggle hollow"));
        tooltip.add(Component.literal("    shapes open or closed."));
        tooltip.add(Component.literal(altText + " mouse wheel to change hollow"));
        tooltip.add(Component.literal("    shape wall thickness."));
        addKeybindReminders(tooltip, KeyBindingsExtraBitManipulation.SHIFT,
            KeyBindingsExtraBitManipulation.CONTROL, KeyBindingsExtraBitManipulation.ALT);
      } else {
        addKeyInformation(tooltip, true);
      }
    }

  }


}