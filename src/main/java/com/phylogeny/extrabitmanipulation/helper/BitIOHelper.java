package com.phylogeny.extrabitmanipulation.helper;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import io.netty.buffer.ByteBuf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IMultiStateBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class BitIOHelper {

  public static Map<BlockState, IBitBrush> getModelBitMapFromEntryStrings(String[] entryStrings) {
    Map<BlockState, IBitBrush> bitMap = new HashMap<BlockState, IBitBrush>();
    for (String entryString : entryStrings) {
      if (entryString.indexOf("-") < 0 || entryString.length() < 3) {
        continue;
      }

      String[] entryStringArray = entryString.split("-");
      BlockState key = getStateFromString(entryStringArray[0]);
      if (key == null || BitIOHelper.isAir(key)) {
        continue;
      }

      BlockState value = getStateFromString(entryStringArray[1]);
      if (value == null) {
        continue;
      }

      try {
        bitMap.put(key, ChiselsAndBitsAPIAccess.apiInstance.createBrushFromState(value));
      } catch (InvalidBitItem e) {
      }
    }
    return bitMap;
  }

  public static String[] getEntryStringsFromModelBitMap(Map<BlockState, IBitBrush> bitMap) {
    String[] entryStrings = new String[bitMap.size()];
    int index = 0;
    for (Entry<BlockState, IBitBrush> entry : bitMap.entrySet()) {
      entryStrings[index++] = getModelBitMapEntryString(entry);
    }

    return entryStrings;
  }

  public static void stateToBitMapToBytes(ByteBuf buffer,
                                          Map<BlockState, IBitBrush> stateToBitMap) {
    if (notNullToBuffer(buffer, stateToBitMap)) {
      objectToBytes(buffer, stateToBitMapToStateIdArray(stateToBitMap));
    }
  }

  public static Map<BlockState, IBitBrush> stateToBitMapFromBytes(ByteBuf buffer) {
    Map<BlockState, IBitBrush> stateToBitMap = new HashMap<BlockState, IBitBrush>();
    if (!buffer.readBoolean()) {
      return stateToBitMap;
    }

    int[] mapArray = (int[]) objectFromBytes(buffer);
    if (mapArray == null) {
      return stateToBitMap;
    }

    stateToBitMapFromStateIdArray(stateToBitMap, mapArray, ChiselsAndBitsAPIAccess.apiInstance);
    return stateToBitMap;
  }

  private static int[] stateToBitMapToStateIdArray(Map<BlockState, IBitBrush> stateToBitMap) {
    int counter = 0;
    int[] mapArray = new int[stateToBitMap.size() * 2];
    for (Entry<BlockState, IBitBrush> entry : stateToBitMap.entrySet()) {
      mapArray[counter++] = Block.getId(entry.getKey());
      mapArray[counter++] = entry.getValue().getStateID();
    }
    return mapArray;
  }

  private static void stateToBitMapFromStateIdArray(Map<BlockState, IBitBrush> stateToBitMap,
                                                    int[] mapArray, IChiselAndBitsAPI api) {
    for (int i = 0; i < mapArray.length; i += 2) {
      BlockState state = Block.stateById(mapArray[i]);
      if (!isAir(state)) {
        try {
          stateToBitMap.put(state, api.createBrushFromState(Block.stateById(mapArray[i + 1])));
        } catch (InvalidBitItem e) {
        }
      }
    }
  }

  public static void writeStateToBitMapToNBT(ItemStack bitStack, String key,
                                             Map<BlockState, IBitBrush> stateToBitMap,
                                             boolean saveStatesById) {
    if (!bitStack.hasTag()) {
      return;
    }

    CompoundTag nbt = ItemStackHelper.getNBT(bitStack);
    if (saveStatesById) {
      writeObjectToNBT(nbt, key + 0, stateToBitMapToStateIdArray(stateToBitMap));
      nbt.remove(key + 1);
      nbt.remove(key + 2);
      nbt.remove(key + 3);
    } else {
      int counter = 0;
      int n = stateToBitMap.size();
      boolean isBlockMap = key.equals(NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT);
      String[] domainArray = new String[n * 2];
      String[] pathArray = new String[n * 2];
      byte[] metaArray = new byte[isBlockMap ? n : n * 2];
      for (Entry<BlockState, IBitBrush> entry : stateToBitMap.entrySet()) {
        saveStateToMapArrays(domainArray, pathArray, isBlockMap ? null : metaArray, counter++,
            isBlockMap, entry.getKey());
        saveStateToMapArrays(domainArray, pathArray, metaArray, counter++, isBlockMap,
            Block.stateById(entry.getValue().getStateID()));
      }
      nbt.remove(key + 0);
      writeObjectToNBT(nbt, key + 1, domainArray);
      writeObjectToNBT(nbt, key + 2, pathArray);
      writeObjectToNBT(nbt, key + 3, metaArray);
    }
  }

  private static void saveStateToMapArrays(String[] domainArray, String[] pathArray,
                                           byte[] metaArray, int index, boolean isBlockMap,
                                           BlockState state) {

    ResourceLocation regName = BuiltInRegistries.BLOCK.getKey(state.getBlock());
    if (regName == null) {
      return;
    }

    domainArray[index] = regName.getNamespace();
    pathArray[index] = regName.getPath();
    if (metaArray != null) {
      metaArray[isBlockMap ? index / 2 : index] = (byte) Block.getId(state);
    }
  }

  public static Map<BlockState, IBitBrush> readStateToBitMapFromNBT(IChiselAndBitsAPI api,
                                                                    ItemStack bitStack,
                                                                    String key) {
    Map<BlockState, IBitBrush> stateToBitMap = new HashMap<BlockState, IBitBrush>();
    if (!bitStack.hasTag()) {
      return stateToBitMap;
    }

    CompoundTag nbt = ItemStackHelper.getNBT(bitStack);
    boolean saveStatesById = !nbt.contains(key + 2);
    if (saveStatesById ? !nbt.contains(key + 0) :
        !nbt.contains(key + 1) || !nbt.contains(key + 3)) {
      return stateToBitMap;
    }

    if (saveStatesById) {
      int[] mapArray = (int[]) readObjectFromNBT(nbt, key + 0);
      if (mapArray == null) {
        return stateToBitMap;
      }

      stateToBitMapFromStateIdArray(stateToBitMap, mapArray, api);
    } else {
      String[] domainArray = (String[]) readObjectFromNBT(nbt, key + 1);
      String[] pathArray = (String[]) readObjectFromNBT(nbt, key + 2);
      byte[] metaArray = (byte[]) readObjectFromNBT(nbt, key + 3);
      if (domainArray == null || pathArray == null || metaArray == null) {
        return stateToBitMap;
      }

      boolean isBlockMap = key.equals(NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT);
      for (int i = 0; i < domainArray.length; i += 2) {
        BlockState state =
            readStateFromMapArrays(domainArray, pathArray, isBlockMap ? null : metaArray, i,
                isBlockMap);
        if (!isAir(state)) {
          try {
            stateToBitMap.put(state, api.createBrushFromState(
                readStateFromMapArrays(domainArray, pathArray, metaArray, i + 1, isBlockMap)));
          } catch (InvalidBitItem e) {
          }
        }
      }
    }
    return stateToBitMap;
  }

  private static BlockState readStateFromMapArrays(String[] domainArray, String[] pathArray,
                                                   byte[] metaArray, int index,
                                                   boolean isBlockMap) {
    Block block =
        BuiltInRegistries.BLOCK.get(new ResourceLocation(domainArray[index], pathArray[index]));
    return block == null ? Blocks.AIR.defaultBlockState() : (metaArray != null
        ? getStateFromMeta(block, metaArray[isBlockMap ? index / 2 : index]) :
        block.defaultBlockState());
  }

  private static byte[] compressObject(Object object) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream objectStream = new ObjectOutputStream(new DeflaterOutputStream(byteStream));
    objectStream.writeObject(object);
    objectStream.close();
    return byteStream.toByteArray();
  }

  private static Object decompressObject(byte[] bytes) throws IOException, ClassNotFoundException {
    return (new ObjectInputStream(
        new InflaterInputStream(new ByteArrayInputStream(bytes)))).readObject();
  }

  private static void writeObjectToNBT(CompoundTag nbt, String key, Object object) {
    try {
      nbt.putByteArray(key, compressObject(object));
    } catch (IOException e) {
    }
  }

  private static Object readObjectFromNBT(CompoundTag nbt, String key) {
    try {
      return decompressObject(nbt.getByteArray(key));
    } catch (ClassNotFoundException e) {
    } catch (IOException e) {
    }
    return null;
  }

  private static Object objectFromBytes(ByteBuf buffer) {
    int length = buffer.readInt();
    if (length == 0) {
      return null;
    }

    try {
      byte[] bytes = new byte[length];
      buffer.readBytes(bytes);
      return decompressObject(bytes);
    } catch (ClassNotFoundException e) {
    } catch (IOException e) {
    }
    return null;
  }

  private static void objectToBytes(ByteBuf buffer, Object object) {
    try {
      byte[] bytes = compressObject(object);
      buffer.writeInt(bytes.length);
      buffer.writeBytes(bytes);
    } catch (IOException e) {
      buffer.writeInt(0);
    }
  }

  public static void readStatesFromNBT(CompoundTag nbt, Map<BlockState, Integer> stateMap,
                                       BlockState[][][] stateArray) {
    String key = NBTKeys.SAVED_STATES;
    int[] stateIDs = (int[]) readObjectFromNBT(nbt, key);
    if (stateIDs == null) {
      stateIDs = new int[4096];
    }

    for (int n = 0; n < stateIDs.length; n++) {
      int i = n / 256;
      int n2 = n % 256;
      int j = n2 / 16;
      int k = n2 % 16;
      BlockState state = Block.stateById(stateIDs[n]);
      stateArray[i][j][k] = state;
      if (!isAir(state)) {
        stateMap.put(state, 1 + (stateMap.containsKey(state) ? stateMap.get(state) : 0));
      }
    }
    if (stateIDs.length == 0) {
      BlockState air = Blocks.AIR.defaultBlockState();
      for (int i = 0; i < 16; i++) {
        for (int j = 0; j < 16; j++) {
          for (int k = 0; k < 16; k++) {
            stateArray[i][j][k] = air;
          }
        }
      }
    }
  }

  public static void saveBlockStates(IChiselAndBitsAPI api, Player player, Level world,
                                     AABB box, CompoundTag nbt) {
    if (world.isClientSide) {
      return;
    }

    int[] stateIDs = new int[4096];
    int index = 0;
    int diffX = 16 - (int) (box.maxX - box.minX);
    int diffZ = 16 - (int) (box.maxZ - box.minZ);
    int halfDiffX = diffX / 2;
    int halfDiffZ = diffZ / 2;
    int minX = (int) (box.minX - halfDiffX);
    int maxX = (int) (box.maxX + (diffX - halfDiffX));
    int minZ = (int) (box.minZ - halfDiffZ);
    int maxZ = (int) (box.maxZ + (diffZ - halfDiffZ));
    BlockState airState = Blocks.AIR.defaultBlockState();
    for (int x = minX; x < maxX; x++) {
      for (int y = (int) box.minY; y < box.minY + 16; y++) {
        for (int z = minZ; z < maxZ; z++) {
          BlockState state;
          if (y <= box.maxY && x >= box.minX && x <= box.maxX && z >= box.minZ && z <= box.maxZ) {
            BlockPos pos = new BlockPos(x, y, z);
            state = world.getBlockState(pos);
            if (api.isBlockChiseled(world, pos) && state.getBlock() instanceof IMultiStateBlock) {
              state = ((IMultiStateBlock) state.getBlock()).getPrimaryState(world, pos);
            }
          } else {
            state = airState;
          }
          stateIDs[index++] = Block.getId(state);
        }
      }
    }
    String key = NBTKeys.SAVED_STATES;
    writeObjectToNBT(nbt, key, stateIDs);
    player.containerMenu.sendAllDataToRemote();
  }

  public static boolean isAir(BlockState state) {
    return state.equals(Blocks.AIR.defaultBlockState());
  }

  public static boolean isAir(Block block) {
    return block.equals(Blocks.AIR);
  }

  public static void stateToBytes(ByteBuf buffer, BlockState state) {
    buffer.writeInt(Block.getId(state));
  }

  public static BlockState stateFromBytes(ByteBuf buffer) {
    return Block.stateById(buffer.readInt());
  }

  public static BlockState getStateFromString(String stateString) {
    if (stateString.isEmpty()) {
      return null;
    }

    int meta = -1;
    int i = stateString.lastIndexOf(":");
    if (i >= 0 && i < stateString.length() - 1) {
      try {
        meta = Integer.parseInt(stateString.substring(i + 1));
        stateString = stateString.substring(0, i);
      } catch (NumberFormatException e) {
      }
    }
    Block block = BuiltInRegistries.BLOCK.get(new ResourceLocation(stateString));
    if (block == null) {
      LogHelper.getLogger().error("Block failed to load from the following string: " + stateString);
      return null;
    }

    return meta < 0 ? block.defaultBlockState() : getStateFromMeta(block, meta);
  }

  @SuppressWarnings("deprecation")
  public static BlockState getStateFromMeta(Block block, int meta) {
    return Block.stateById(meta);
  }

  public static String getStringFromState(BlockState state) {
    if (state == null) {
      return "minecraft:air";
    }

    Block block = state.getBlock();
    ResourceLocation regName = BuiltInRegistries.BLOCK.getKey(block);
    if (regName == null) {
      return "minecraft:air";
    }

    String valueString = regName.getNamespace() + ":" + regName.getPath();
    if (!state.equals(block.defaultBlockState())) {
      valueString += ":" + Block.getId(block.defaultBlockState());
    }

    return valueString;
  }

  public static boolean hasBitMapsInNbt(ItemStack stack) {
    CompoundTag nbt = ItemStackHelper.getNBT(stack);
    for (int i = 0; i < 4; i++) {
      if (nbt.contains(NBTKeys.STATE_TO_BIT_MAP_PERMANENT + i) ||
          nbt.contains(NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT + i)) {
        return true;
      }
    }
    return false;
  }

  public static void clearAllBitMapsFromNbt(ItemStack stack) {
    CompoundTag nbt = ItemStackHelper.getNBT(stack);
    for (int i = 0; i < 4; i++) {
      nbt.remove(NBTKeys.STATE_TO_BIT_MAP_PERMANENT + i);
      nbt.remove(NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT + i);
    }
  }

  public static boolean areSortedBitMapsIdentical(Map<BlockState, IBitBrush> map1,
                                                  Map<BlockState, IBitBrush> map2) {
    int n = map1.size();
    if (n != map2.size()) {
      return false;
    }

    int matches = 0;
    Iterator<Entry<BlockState, IBitBrush>> iterator1 = map1.entrySet().iterator();
    Iterator<Entry<BlockState, IBitBrush>> iterator2 = map2.entrySet().iterator();
    while (iterator1.hasNext() && iterator2.hasNext()) {
      Entry<BlockState, IBitBrush> entry1 = iterator1.next();
      Entry<BlockState, IBitBrush> entry2 = iterator2.next();
      if (getModelBitMapEntryString(entry1).equals(getModelBitMapEntryString(entry2))) {
        matches++;
      }
    }
    return matches == n;
  }

  private static String getModelBitMapEntryString(Entry<BlockState, IBitBrush> entry) {
    return getStringFromState(entry.getKey()) + "-" +
        getStringFromState(entry.getValue().getState());
  }

  public static boolean notNullToBuffer(ByteBuf buffer, Object object) {
    boolean notNull = object != null;
    buffer.writeBoolean(notNull);
    return notNull;
  }

}