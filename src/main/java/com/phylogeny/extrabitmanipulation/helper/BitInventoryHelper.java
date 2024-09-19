package com.phylogeny.extrabitmanipulation.helper;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.shape.Shape;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.joml.Vector3d;

public class BitInventoryHelper {

  @Nullable
  private static IItemHandler getItemHandler(ItemStack stack) {
    return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
  }

  public static Map<Integer, Integer> getInventoryBitCounts(IChiselAndBitsAPI api,
                                                            Player player) {
    Map<Integer, Integer> bitCounts = new HashMap<Integer, Integer>();
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack stack = player.getInventory().getItem(i);
      Set<ItemStack> stacks;
      if (isBitStack(api, stack)) {
        stacks = Collections.singleton(stack);
      } else {
        IItemHandler itemHandler = getItemHandler(stack);
        if (itemHandler == null) {
          continue;
        }

        stacks = new HashSet<>();
        for (int j = 0; j < itemHandler.getSlots(); j++) {
          stacks.add(itemHandler.getStackInSlot(j));
        }
      }
      for (ItemStack stackBitType : stacks) {
        try {
          int bitStateID = api.createBrush(stackBitType).getStateID();
          if (!bitCounts.containsKey(bitStateID)) {
            bitCounts.put(bitStateID, countInventoryBits(api, player, stackBitType));
          }
        } catch (InvalidBitItem e) {
        }
      }
    }
    return getSortedLinkedHashMap(bitCounts, new Comparator<Object>() {
      @Override
      @SuppressWarnings("unchecked")
      public int compare(Object object1, Object object2) {
        return ((Comparable<Integer>) ((Map.Entry<Integer, Integer>) (object2)).getValue())
            .compareTo(((Map.Entry<Integer, Integer>) (object1)).getValue());
      }
    });
  }

  public static LinkedHashMap getSortedLinkedHashMap(Map bitCounts, Comparator<Object> comparator) {
    List<Map.Entry> bitCountsList = new LinkedList(bitCounts.entrySet());
    Collections.sort(bitCountsList, comparator);
    LinkedHashMap bitCountsSorted = new LinkedHashMap();
    for (Map.Entry entry : bitCountsList) {
      bitCountsSorted.put(entry.getKey(), entry.getValue());
    }

    return bitCountsSorted;
  }

  public static int countInventoryBits(IChiselAndBitsAPI api, Player player,
                                       ItemStack stackBitType) {
    int count = 0;
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack stack = player.getInventory().getItem(i);
      if (stack.isEmpty()) {
        continue;
      }

      count += getBitCountFromStack(api, stackBitType, stack);
      IItemHandler itemHandler = getItemHandler(stack);
      if (itemHandler == null) {
        continue;
      }

      for (int j = 0; j < itemHandler.getSlots(); j++) {
        count += getBitCountFromStack(api, stackBitType, itemHandler.getStackInSlot(j));
      }
    }
    return count;
  }

  public static int countInventoryBlocks(Player player, Block block) {
    int count = 0;
    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack stack = player.getInventory().getItem(i);
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem &&
          ((BlockItem) stack.getItem()).getBlock() == block) {
        count += stack.getCount();
      }
    }
    return count;
  }

  private static int getBitCountFromStack(IChiselAndBitsAPI api, ItemStack stackBitType,
                                          ItemStack stack) {
    return areBitStacksEqual(api, stackBitType, stack) ? stack.getCount() : 0;
  }

  private static boolean areBitStacksEqual(IChiselAndBitsAPI api, ItemStack stackBitType,
                                           ItemStack putativeBitStack) {
    return isBitStack(api, putativeBitStack) &&
        ItemStack.matches(putativeBitStack, stackBitType);
  }

  public static boolean isBitStack(IChiselAndBitsAPI api, ItemStack putativeBitStack) {
    return !putativeBitStack.isEmpty() &&
        api.getItemType(putativeBitStack) == ItemType.CHISELED_BIT;
  }

  public static void removeBitsFromBlocks(IChiselAndBitsAPI api, Player player,
                                          ItemStack bitStack, Block block, int quota) {
    if (quota <= 0) {
      return;
    }

    Inventory inventoy = player.getInventory();
    for (int i = 0; i < inventoy.getContainerSize(); i++) {
      ItemStack stack = inventoy.getItem(i);
      if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
        continue;
      }

      Block block2 = ((BlockItem) stack.getItem()).getBlock();
      if (block2 != block) {
        continue;
      }

      int count = stack.getCount();
      for (int j = 0; j < count; j++) {
        if (quota >= 4096) {
          quota -= 4096;
          stack.shrink(1);
        } else {
          stack.shrink(1);
          break;
        }
        if (quota <= 0) {
          break;
        }
      }
      if (quota > 0 && quota < 4096) {
        Vec3 spawnPos = new Vec3(player.getX(), player.getY(), player.getZ());
        quota = 4096 - quota;
        int stakCount = (int) Math.ceil(quota / 64.0);
        for (int j = 0; j < stakCount; j++) {
          ItemStack stack2 = bitStack.copy();
          stack2.setCount(Math.min(64, quota));
          quota -= stack2.getCount();
          api.giveBitToPlayer(player, stack2, spawnPos);
        }
      }
      if (quota <= 0) {
        break;
      }
    }
  }

  public static int removeOrAddInventoryBits(IChiselAndBitsAPI api, Player player,
                                             ItemStack stackBitType, int quota, boolean addBits) {
    if (quota <= 0) {
      return quota;
    }

    Inventory inventoy = player.getInventory();
    for (int i = 0; i < inventoy.getContainerSize(); i++) {
      ItemStack stack = inventoy.getItem(i);
      if (!addBits) {
        quota = removeBitsFromStack(api, stackBitType, quota, inventoy, null, i, stack);
      }

      if (api.getItemType(stack) == ItemType.BIT_BAG) {
        IItemHandler itemHandler = getItemHandler(stack);
        if (itemHandler == null) {
          continue;
        }

        for (int j = 0; j < itemHandler.getSlots(); j++) {
          quota = addBits ? addBitsToInventoryOfStack(quota, itemHandler, j, stackBitType)
              : removeBitsFromStack(api, stackBitType, quota, null, itemHandler, j,
              itemHandler.getStackInSlot(j));
          if (quota <= 0) {
            break;
          }
        }
      }
      if (quota <= 0) {
        break;
      }
    }
    return quota;
  }

  private static int addBitsToInventoryOfStack(int quota, IItemHandler itemHandler, int index,
                                               ItemStack stack) {
    if (!stack.isEmpty()) {
      int size = stack.getCount();
      ItemStack remainingStack = itemHandler.insertItem(index, stack, false);
      int reduction = size - (!remainingStack.isEmpty() ? remainingStack.getCount() : 0);
      quota -= reduction;
      stack.shrink(reduction);
    }
    return quota;
  }

  private static int removeBitsFromStack(IChiselAndBitsAPI api, ItemStack setBitStack,
                                         int quota, @Nullable Inventory inventoy,
                                         @Nullable IItemHandler itemHandler, int index,
                                         ItemStack stack) {
    if (areBitStacksEqual(api, setBitStack, stack)) {
      int size = stack.getCount();
      if (size > quota) {
        if (itemHandler != null) {
          itemHandler.extractItem(index, quota, false);
        } else {
          stack.shrink(quota);
        }
        quota = 0;
      } else {
        if (itemHandler != null) {
          itemHandler.extractItem(index, size, false);
        } else if (inventoy != null) {
          inventoy.setItem(index, ItemStack.EMPTY);
        }
        quota -= size;
      }
    }
    return quota;
  }

  public static void giveOrDropStacks(Player player, Level world, BlockPos pos, Shape shape,
                                      IChiselAndBitsAPI api, Map<BlockState, Integer> bitTypes) {
    if (bitTypes != null) {
      Set<BlockState> keySet = bitTypes.keySet();
      for (BlockState state : keySet) {
        ItemStack stackBitType;
        IBitBrush bitType;
        try {
          stackBitType = api.getBitItem(state);
          if (stackBitType.getItem() == null) {
            continue;
          }

          bitType = api.createBrush(stackBitType);
        } catch (InvalidBitItem e) {
          continue;
        }
        int totalBits = bitTypes.get(state);
        if (Configs.dropBitsAsFullChiseledBlocks && totalBits >= 4096) {
          IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
          setAllBits(bitAccess, bitType);
          int blockCount = totalBits / 4096;
          totalBits -= blockCount * 4096;
          while (blockCount > 0) {
            int stackSize = blockCount > 64 ? 64 : blockCount;
            @SuppressWarnings("null")
            ItemStack stack2 = bitAccess.getBitsAsItem(null, ItemType.CHISELED_BLOCK, false);
            if (!stack2.isEmpty()) {
              stack2.setCount(stackSize);
              givePlayerStackOrDropOnGround(player, world, api, pos, shape, stack2);
            }
            blockCount -= stackSize;
          }
        }
        int quota;
        while (totalBits > 0) {
          quota = totalBits > 64 ? 64 : totalBits;
          ItemStack bitStack2 = bitType.getItemStack(quota);
          givePlayerStackOrDropOnGround(player, world, api, pos, shape, bitStack2);
          totalBits -= quota;
        }
      }
      bitTypes.clear();
      if (Configs.placeBitsInInventory) {
        player.containerMenu.sendAllDataToRemote();
      }
    }
  }

  private static void givePlayerStackOrDropOnGround(Player player, Level world,
                                                    IChiselAndBitsAPI api, BlockPos pos,
                                                    Shape shape, ItemStack stack) {
    if (Configs.placeBitsInInventory) {
      removeOrAddInventoryBits(api, player, stack, stack.getCount(), true);
      if (stack.getCount() > 0) {
        player.getInventory().add(stack);
      }
    }
    if (stack.getCount() > 0) {
      if (Configs.dropBitsInBlockspace) {
        spawnStacksInShape(world, pos, shape, stack);
      } else {
        player.drop(stack, false, false);
      }
    }
  }

  private static void spawnStacksInShape(Level world, BlockPos pos, Shape shape, ItemStack stack) {
    if (!world.isClientSide) {
      Vector3d spawnPoint = shape.getRandomInternalPoint(world, pos);
      ItemEntity entityitem =
          new ItemEntity(world, spawnPoint.x, spawnPoint.y - 0.25, spawnPoint.z, stack);
      entityitem.setDefaultPickUpDelay();
      world.addFreshEntity(entityitem);
    }
  }

  private static void setAllBits(IBitAccess bitAccess, IBitBrush bit) {
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          try {
            bitAccess.setBitAt(i, j, k, bit);
          } catch (SpaceOccupied e) {
          }
        }
      }
    }
  }

  public static void setHeldDesignStack(Player player, ItemStack stackChiseledBlock) {
    ItemStack stack = player.getMainHandItem();
    ItemType itemType = ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack);
    if (itemType == null || !ItemStackHelper.isDesignItemType(itemType)) {
      return;
    }

    IBitAccess bitAccess = ChiselsAndBitsAPIAccess.apiInstance.createBitItem(stackChiseledBlock);
    if (bitAccess == null) {
      return;
    }

    ItemStack stackDesign =
        bitAccess.getBitsAsItem(Direction.from3DDataValue(ItemStackHelper.getNBTOrNew(stack)
            .getInt(ChiselsAndBitsReferences.NBT_KEY_DESIGN_SIDE)), itemType, false);
    if (stackDesign.isEmpty()) {
      stackDesign = new ItemStack(BuiltInRegistries.ITEM.get(
          new ResourceLocation(
              ChiselsAndBitsReferences.MOD_ID + ":" + (itemType == ItemType.POSITIVE_DESIGN
                  ? ChiselsAndBitsReferences.ITEM_PATH_DESIGN_POSITIVE :
                  (itemType == ItemType.NEGATIVE_DESIGN
                      ? ChiselsAndBitsReferences.ITEM_PATH_DESIGN_NEGATIVE :
                      ChiselsAndBitsReferences.ITEM_PATH_DESIGN_MIRROR)))));
    }

    if (!stack.isEmpty() && stack.hasTag()) {
      stackDesign.addTagElement(ChiselsAndBitsReferences.NBT_KEY_DESIGN_MODE,
          StringTag.valueOf(ItemStackHelper.getNBT(stack)
              .getString(ChiselsAndBitsReferences.NBT_KEY_DESIGN_MODE)));
    }
    player.setItemInHand(InteractionHand.MAIN_HAND, stackDesign);
  }

}