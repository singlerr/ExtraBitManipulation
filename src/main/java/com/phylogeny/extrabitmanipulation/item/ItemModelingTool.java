package com.phylogeny.extrabitmanipulation.item;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.config.ConfigReplacementBits;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelWriteData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.packet.PacketCreateModel;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class ItemModelingTool extends ItemBitToolBase {
  public static final String[] AREA_MODE_TITLES = new String[] {"Centered", "Corner", "Drawn"};
  public static final String[] SNAP_MODE_TITLES =
      new String[] {"Off", "Snap-to-Chunk XZ", "Snap-to-Chunk XYZ"};

  public ItemModelingTool(Properties properties, String name) {
    super(properties, name);
  }

  public CompoundTag initialize(ItemStack stack, ModelReadData modelingData) {
    CompoundTag nbt = ItemStackHelper.initNBT(stack);
    initInt(nbt, NBTKeys.MODEL_AREA_MODE, modelingData.getAreaMode());
    initInt(nbt, NBTKeys.MODEL_SNAP_MODE, modelingData.getSnapMode());
    initBoolean(nbt, NBTKeys.MODEL_GUI_OPEN, modelingData.getGuiOpen());
    return nbt;
  }

  @Override
  public InteractionResult useOn(UseOnContext useOnContext) {
    ItemStack stack = useOnContext.getPlayer().getItemInHand(useOnContext.getHand());
    if (useOnContext.getLevel().isClientSide && stack.hasTag()) {
      @SuppressWarnings("null")
      ModelWriteData modelingData =
          new ModelWriteData(stack.getTag().getBoolean(NBTKeys.BIT_MAPS_PER_TOOL));
      if (createModel(stack, useOnContext.getPlayer(), useOnContext.getLevel(),
          useOnContext.getClickedPos(), useOnContext.getClickedFace(), modelingData) ==
          InteractionResult.SUCCESS) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketCreateModel(useOnContext.getClickedPos(), useOnContext.getClickedFace(),
                modelingData));
      }
    }
    return InteractionResult.SUCCESS;

  }

  public InteractionResult createModel(ItemStack stack, Player player, Level world,
                                       BlockPos pos, Direction facing,
                                       ModelWriteData modelingData) {
    if (!stack.hasTag()) {
      return InteractionResult.FAIL;
    }

    if (!world.getBlockState(pos).getBlock().canBeReplaced(world, pos)) {
      pos = pos.relative(facing);
      if (!world.isEmptyBlock(pos)) {
        return InteractionResult.FAIL;
      }
    }
    world.removeBlock(pos, true);
    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    CompoundTag nbt = ItemStackHelper.getNBT(stack);
    if (!nbt.contains(NBTKeys.SAVED_STATES)) {
      return InteractionResult.FAIL;
    }

    Map<BlockState, Integer> stateMap = new HashMap<BlockState, Integer>();
    BlockState[][][] stateArray = new BlockState[16][16][16];
    BitIOHelper.readStatesFromNBT(nbt, stateMap, stateArray);
    Map<BlockState, ArrayList<BitCount>> stateToBitCountArray =
        new HashMap<BlockState, ArrayList<BitCount>>();
    Map<IBitBrush, Integer> bitMap = new HashMap<IBitBrush, Integer>();
    Map<BlockState, Integer> missingBitMap =
        mapBitsToStates(api, modelingData.getReplacementBitsUnchiselable(),
            modelingData.getReplacementBitsInsufficient(),
            BitInventoryHelper.getInventoryBitCounts(api, player), stateMap, stateToBitCountArray,
            modelingData.getStateToBitMap(api, stack), modelingData.getBlockToBitMap(api, stack),
            bitMap, player.isCreative());
    if (!missingBitMap.isEmpty()) {
      if (world.isClientSide) {
        int missingBitCount = 0;
        for (BlockState state : missingBitMap.keySet()) {
          missingBitCount += missingBitMap.get(state);
        }

        sendMessage(player,
            "Missing " + missingBitCount + " bits to represent the following blocks:");
        for (BlockState state : missingBitMap.keySet()) {
          String name = getBlockName(state,
              new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state)));
          sendMessage(player, "  " + missingBitMap.get(state) + " - " + name);
        }
      }
      return InteractionResult.FAIL;
    }
    return createModel(player, world, pos, stack, api, stateArray, stateToBitCountArray, bitMap);
  }

  private InteractionResult createModel(Player player, Level world, BlockPos pos,
                                        ItemStack stack, IChiselAndBitsAPI api,
                                        BlockState[][][] stateArray,
                                        Map<BlockState, ArrayList<BitCount>> stateToBitCountArray,
                                        Map<IBitBrush, Integer> bitMap) {
    IBitAccess bitAccess;
    try {
      bitAccess = api.getBitAccess(world, pos);
    } catch (CannotBeChiseled e) {
      e.printStackTrace();
      return InteractionResult.FAIL;
    }
    try {
      api.beginUndoGroup(player);
      if (!createModel(player, world, stack, stateArray, stateToBitCountArray, bitAccess)) {
        return InteractionResult.FAIL;
      }

      bitAccess.commitChanges(true);
    } finally {
      api.endUndoGroup(player);
    }
    if (!world.isClientSide && !player.isCreative()) {
      for (IBitBrush bit : bitMap.keySet()) {
        BitInventoryHelper.removeOrAddInventoryBits(api, player, bit.getItemStack(1),
            bitMap.get(bit).intValue(), false);
        player.inventoryMenu.sendAllDataToRemote();
      }
    }
    damageTool(stack, player);
    return InteractionResult.SUCCESS;
  }

  public boolean createModel(Player player, Level world, ItemStack stack,
                             BlockState[][][] stateArray,
                             Map<BlockState, ArrayList<BitCount>> stateToBitCountArray,
                             IBitAccess bitAccess) {
    if (!ItemStackHelper.hasKey(stack, NBTKeys.SAVED_STATES)) {
      return false;
    }

    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          try {
            IBitBrush bit = null;
            BlockState state = stateArray[i][j][k];
            if (!state.equals(Blocks.AIR.defaultBlockState())) {
              for (BitCount bitCount : stateToBitCountArray.get(state)) {
                if (bitCount.count > 0) {
                  bitCount.count--;
                  bit = bitCount.bit;
                  break;
                }
              }
            }
            bitAccess.setBitAt(i, j, k, bit);
          } catch (SpaceOccupied e) {
            if (world != null && world.isClientSide) {
              sendMessage(player, "Multipart(s) are in the way.");
            }

            return false;
          }
        }
      }
    }
    return true;
  }

  public Map<BlockState, Integer> mapBitsToStates(IChiselAndBitsAPI api,
                                                  ConfigReplacementBits replacementBitsUnchiselable,
                                                  ConfigReplacementBits replacementBitsInsufficient,
                                                  Map<Integer, Integer> inventoryBitCounts,
                                                  Map<BlockState, Integer> stateMap,
                                                  Map<BlockState, ArrayList<BitCount>> stateToBitCountArray,
                                                  Map<BlockState, IBitBrush> manualStateToBitMap,
                                                  Map<BlockState, IBitBrush> manualBlockToBitMap,
                                                  Map<IBitBrush, Integer> bitMap,
                                                  boolean isCreative) {
    Map<BlockState, Integer> missingBitMap = new HashMap<BlockState, Integer>();
    Map<BlockState, Integer> skippedStatesMap = new HashMap<BlockState, Integer>();
    Map<BlockState, ArrayList<BitCount>> skippedBitCountArrayMap =
        new HashMap<BlockState, ArrayList<BitCount>>();
    for (int pass = 0; pass < 2; pass++) {
      for (BlockState state : stateMap.keySet()) {
        if (pass == 1 && !skippedStatesMap.containsKey(state)) {
          continue;
        }

        int bitCount = stateMap.get(state);
        ArrayList<BitCount> bitCountArray =
            pass == 1 ? skippedBitCountArrayMap.get(state) : new ArrayList<BitCount>();
        int remainingBitCount = pass == 1 ? skippedStatesMap.get(state) : 0;
        try {
          if (pass == 0) {
            remainingBitCount = addBitCountObject(bitCountArray, bitMap, inventoryBitCounts,
                manualStateToBitMap.containsKey(state)
                    ? manualStateToBitMap.get(state) :
                    (manualBlockToBitMap.containsKey(state.getBlock().defaultBlockState())
                        ? manualBlockToBitMap.get(state.getBlock().defaultBlockState()) :
                        api.createBrushFromState(state)),
                bitCount, isCreative);
          }
          if (remainingBitCount > 0) {
            remainingBitCount = getReplacementBit(api, replacementBitsInsufficient, bitMap,
                inventoryBitCounts, bitCountArray, remainingBitCount, isCreative, pass);
            if (remainingBitCount < 0) {
              skippedStatesMap.put(state, remainingBitCount * -1);
              skippedBitCountArrayMap.put(state, bitCountArray);
            }
          }
        } catch (InvalidBitItem e) {
          remainingBitCount = getReplacementBit(api, replacementBitsUnchiselable, bitMap,
              inventoryBitCounts, bitCountArray, bitCount, isCreative, pass);
          if (remainingBitCount < 0) {
            skippedStatesMap.put(state, remainingBitCount * -1);
            skippedBitCountArrayMap.put(state, bitCountArray);
          }
        }
        stateToBitCountArray.put(state, bitCountArray);
        if (remainingBitCount > 0 && (pass == 1 || !skippedStatesMap.containsKey(state))) {
          missingBitMap.put(state, remainingBitCount);
        }
      }
      if (skippedStatesMap.isEmpty()) {
        break;
      }
    }
    return missingBitMap;
  }

  private int getReplacementBit(IChiselAndBitsAPI api, ConfigReplacementBits replacementBitsConfig,
                                Map<IBitBrush, Integer> bitMap,
                                Map<Integer, Integer> inventoryBitCounts,
                                ArrayList<BitCount> bitCountArray, int remainingBitCount,
                                boolean isCreative, int pass) {
    if (pass == 0 && replacementBitsConfig.useDefaultReplacementBit()) {
      try {
        remainingBitCount = addBitCountObject(bitCountArray, bitMap, inventoryBitCounts,
            api.createBrush(replacementBitsConfig.getDefaultReplacementBit().getDefaultValue()),
            remainingBitCount, isCreative);
      } catch (InvalidBitItem e) {
      }
    }
    if (remainingBitCount > 0 && replacementBitsConfig.useAnyBitsAsReplacements()) {
      if (pass == 0) {
        return -remainingBitCount;
      }

      try {
        for (Integer stateID : inventoryBitCounts.keySet()) {
          remainingBitCount = addBitCountObject(bitCountArray, bitMap, inventoryBitCounts,
              api.createBrushFromState(Block.stateById(stateID)), remainingBitCount, isCreative);
          if (remainingBitCount == 0) {
            break;
          }
        }
      } catch (InvalidBitItem e) {
      }
    }
    if (remainingBitCount > 0 && (replacementBitsConfig.useAirAsReplacement())) {
      try {
        remainingBitCount = addBitCountObject(bitCountArray, bitMap, inventoryBitCounts,
            api.createBrush(null), remainingBitCount, isCreative);
      } catch (InvalidBitItem e) {
      }
    }
    return remainingBitCount;
  }

  private int addBitCountObject(ArrayList<BitCount> bitCountArray, Map<IBitBrush, Integer> bitMap,
                                Map<Integer, Integer> inventoryBitCounts, IBitBrush bit,
                                int bitCount, boolean isCreative) {
    if (bit.isAir()) {
      bitCountArray.add(new BitCount(bit, bitCount));
      return 0;
    }
    boolean hasBitSurvival = inventoryBitCounts.containsKey(bit.getStateID()) && !isCreative;
    int inventoryBitCount = isCreative ? Integer.MAX_VALUE :
        (hasBitSurvival ? inventoryBitCounts.get(bit.getStateID()) : 0);
    if (inventoryBitCount > 0) {
      int bitCount2 = Math.min(inventoryBitCount, bitCount);
      bitCountArray.add(new BitCount(bit, bitCount2));
      bitCount -= bitCount2;
      bitMap.put(bit, bitCount2 + (bitMap.containsKey(bit) ? bitMap.get(bit) : 0));
      if (hasBitSurvival) {
        inventoryBitCounts.put(bit.getStateID(), inventoryBitCount - bitCount2);
      }
    }
    return bitCount;
  }

  private void sendMessage(Player player, String message) {
    player.sendSystemMessage(Component.literal(message));
  }

  private Component getBlockName(BlockState state, ItemStack blockStack) {
    Component name = state.getBlock().getName();
    if (blockStack.getItem() != null) {
      name = blockStack.getHoverName();
    } else if (!state.getFluidState().isEmpty()) {
      Fluid fluid = state.getFluidState().getType();
      if (fluid != null) {
        name = Component.literal(state.getBlock().getName().getString());
      }
    } else {
      Item item = Item.byBlock(state.getBlock());
      if (item != Items.AIR) {
        name = Component.literal(item.toString());
      }
    }
    return name;
  }

  @Override
  public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                              List<Component> tooltip, TooltipFlag tooltipFlag) {
    boolean shiftDown = Screen.hasShiftDown();
    boolean ctrlDown = Screen.hasControlDown();
    addColorInformation(tooltip, shiftDown);
    CompoundTag nbt = stack.getTag();
    int areaMode = BitToolSettingsHelper.getModelAreaMode(nbt);
    int snapMode = BitToolSettingsHelper.getModelSnapMode(nbt);
    if (!ctrlDown || shiftDown) {
      tooltip.add(colorSettingText(BitToolSettingsHelper.getModelAreaModeText(areaMode),
          Configs.modelAreaMode));
    }
    if (shiftDown) {
      tooltip.add(colorSettingText(BitToolSettingsHelper.getModelSnapModeText(snapMode),
          Configs.modelSnapMode));
      tooltip.add(
          colorSettingText(BitToolSettingsHelper.getModelGuiOpenText(nbt), Configs.modelGuiOpen));
    } else {
      if (ctrlDown) {
        if (areaMode == 2) {
          tooltip.add(Component.literal("Left click a block, drag to"));
          tooltip.add(Component.literal("    another block, then release"));
          tooltip.add(Component.literal("    to read all intersecting"));
          tooltip.add(Component.literal("    block states."));
        } else {
          tooltip.add(Component.literal("Left click a block to read all"));
          String readText = "    block states in an area";
          if (areaMode == 0) {
            tooltip.add(Component.literal(readText));
            tooltip.add(Component.literal("    centered on the nearest"));
          } else {
            tooltip.add(Component.literal(readText + " that"));
            tooltip.add(Component.literal("    faces away from the player"));
            tooltip.add(Component.literal("    and has one of its corners"));
            tooltip.add(Component.literal("    positioned on the nearest"));
          }
          tooltip.add(Component.literal("    block grid vertex."));
          if (snapMode > 0) {
            tooltip.add(Component.literal("    (the area will snap in the"));
            if (snapMode == 1) {
              tooltip.add(Component.literal("    XZ axes to the 2D chuck"));
            } else {
              tooltip.add(Component.literal("    XYZ axes to the 3D chuck"));
            }
            tooltip.add(Component.literal("    containing the block looked at)"));
          }
        }

        tooltip.add(Component.literal("Right click to create model block."));
        tooltip.add(Component.empty());
        String shiftText = getColoredKeyBindText(KeyBindingsExtraBitManipulation.SHIFT);
        if (KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.getKeyBinding()
            .isDefault()) {
          tooltip.add(Component.literal(shiftText + " right click to open"));
          tooltip.add(Component.literal("    mapping/preview GUI."));
        } else {
          tooltip.add(Component.literal(shiftText + " right click or press " +
              KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.getText()));
          tooltip.add(Component.literal("    to open mapping/preview GUI."));
        }
        tooltip.add(Component.literal(shiftText + " mouse wheel to cycle"));
        tooltip.add(Component.literal("    area modes."));
        tooltip.add(Component.empty());
        String controlText = getColoredKeyBindText(KeyBindingsExtraBitManipulation.CONTROL);
        tooltip.add(Component.literal(controlText + " right click to toggle GUI"));
        tooltip.add(Component.literal("    opening upon model read."));
        tooltip.add(Component.literal(controlText + " mouse wheel to"));
        tooltip.add(Component.literal("    cycle chunk snap mode."));
        addKeybindReminders(tooltip, KeyBindingsExtraBitManipulation.SHIFT,
            KeyBindingsExtraBitManipulation.CONTROL);
      } else {
        addKeyInformation(tooltip, true);
      }
    }

  }


  public static class BitCount {
    private IBitBrush bit;
    private int count;

    public BitCount(IBitBrush bit, int count) {
      this.bit = bit;
      this.count = count;
    }

    public IBitBrush getBit() {
      return bit;
    }

    public void setBit(IBitBrush bit) {
      this.bit = bit;
    }

    public int getCount() {
      return count;
    }

  }

}