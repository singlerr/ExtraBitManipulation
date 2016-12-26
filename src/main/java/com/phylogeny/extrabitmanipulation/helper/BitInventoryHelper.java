package com.phylogeny.extrabitmanipulation.helper;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.shape.Shape;

import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BitInventoryHelper
{

	public static Map<Integer, Integer> getInventoryBitCounts(IChiselAndBitsAPI api, EntityPlayer player)
	{
		Map<Integer, Integer> bitCounts = new HashMap<Integer, Integer>();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (BitInventoryHelper.isBitStack(api, stack))
			{
				try
				{
					int bitStateID = api.createBrush(stack).getStateID();
					if (!bitCounts.containsKey(bitStateID))
						bitCounts.put(bitStateID, BitInventoryHelper.countInventoryBits(api, player, stack));
				}
				catch (InvalidBitItem e) {}
			}
		}
		return getSortedLinkedHashMap(bitCounts, new Comparator<Object>() {
			@Override
			@SuppressWarnings("unchecked")
			public int compare(Object object1, Object object2)
			{
				return ((Comparable<Integer>) ((Map.Entry<Integer, Integer>) (object2)).getValue())
						.compareTo(((Map.Entry<Integer, Integer>) (object1)).getValue());
			}
		});
	}
	
	public static LinkedHashMap getSortedLinkedHashMap(Map bitCounts, Comparator<Object> comparator)
	{
		List<Map.Entry> bitCountsList = new LinkedList(bitCounts.entrySet());
		Collections.sort(bitCountsList, comparator);
		LinkedHashMap bitCountsSorted = new LinkedHashMap();
		for (Map.Entry entry : bitCountsList)
		{
			bitCountsSorted.put(entry.getKey(), entry.getValue());
		}
		return bitCountsSorted;
	}
	
	public static int countInventoryBits(IChiselAndBitsAPI api, EntityPlayer player, ItemStack setBitStack)
	{
		int count = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			count += BitInventoryHelper.getBitCountFromStack(api, setBitStack, stack);
			if (api.getItemType(stack) == ItemType.BIT_BAG)
			{
				IBitBag bitBag = api.getBitbag(stack);
				if (bitBag == null)
					continue;
				
				for (int j = 0; j < bitBag.getSlots(); j++)
				{
					ItemStack bagStack = bitBag.getStackInSlot(j);
					count += BitInventoryHelper.getBitCountFromStack(api, setBitStack, bagStack);
				}
			}
		}
		return count;
	}
	
	private static int getBitCountFromStack(IChiselAndBitsAPI api, ItemStack setBitStack, ItemStack stack)
	{
		return BitInventoryHelper.areBitStacksEqual(api, setBitStack, stack) ? stack.getCount() : 0;
	}
	
	private static boolean areBitStacksEqual(IChiselAndBitsAPI api, ItemStack bitStack, ItemStack putativeBitStack)
	{
		return BitInventoryHelper.isBitStack(api, putativeBitStack) && ItemStack.areItemStackTagsEqual(putativeBitStack, bitStack);
	}
	
	public static boolean isBitStack(IChiselAndBitsAPI api, ItemStack putativeBitStack)
	{
		return !putativeBitStack.isEmpty() && api.getItemType(putativeBitStack) == ItemType.CHISLED_BIT;
	}
	
	public static void removeOrAddInventoryBits(IChiselAndBitsAPI api, EntityPlayer player, ItemStack setBitStack, int quota, boolean addBits)
	{
		if (quota <= 0)
			return;
		
		InventoryPlayer inventoy = player.inventory;
		for (int i = 0; i < inventoy.getSizeInventory(); i++)
		{
			ItemStack stack = inventoy.getStackInSlot(i);
			if (!addBits)
				quota = BitInventoryHelper.removeBitsFromStack(api, setBitStack, quota, inventoy, null, i, stack);
			
			if (api.getItemType(stack) == ItemType.BIT_BAG)
			{
				IBitBag bitBag = api.getBitbag(stack);
				if (bitBag == null)
					continue;
				
				for (int j = 0; j < bitBag.getSlots(); j++)
				{
					ItemStack bagStack = bitBag.getStackInSlot(j);
					quota = addBits ? BitInventoryHelper.addBitsToBag(quota, bitBag, j, setBitStack)
							: BitInventoryHelper.removeBitsFromStack(api, setBitStack, quota, null, bitBag, j, bagStack);
					if (quota <= 0)
						break;
				}
			}
			if (quota <= 0) break;
		}
	}
	
	private static int addBitsToBag(int quota, IBitBag bitBag, int index, ItemStack stack)
	{
		if (!stack.isEmpty())
		{
			int size = stack.getCount();
			ItemStack remainingStack = bitBag.insertItem(index, stack, false);
			int reduction = size - (!remainingStack.isEmpty() ? remainingStack.getCount() : 0);
			quota -= reduction;
			stack.shrink(reduction);
		}
		return quota;
	}
	
	private static int removeBitsFromStack(IChiselAndBitsAPI api, ItemStack setBitStack,
			int quota, InventoryPlayer inventoy, IBitBag bitBag, int index, ItemStack stack)
	{
		if (areBitStacksEqual(api, setBitStack, stack))
		{
			int size = stack.getCount();
			if (size > quota)
			{
				if (bitBag != null)
				{
					bitBag.extractItem(index, quota, false);
				}
				else
				{
					stack.shrink(quota);
				}
				quota = 0;
			}
			else
			{
				if (bitBag != null)
				{
					bitBag.extractItem(index, size, false);
				}
				else if (inventoy != null)
				{
					inventoy.setInventorySlotContents(index, ItemStack.EMPTY);
				}
				quota -= size;
			}
		}
		return quota;
	}
	
	public static void giveOrDropStacks(EntityPlayer player, World world, BlockPos pos, Shape shape,
			IChiselAndBitsAPI api, Map<IBlockState, Integer> bitTypes)
	{
		if (bitTypes != null)
		{
			Set<IBlockState> keySet = bitTypes.keySet();
			for (IBlockState state : keySet)
			{
				ItemStack bitStack;
				try
				{
					bitStack = api.getBitItem(state);
				}
				catch (InvalidBitItem e)
				{
					continue;
				}
				if (bitStack.getItem() != null)
				{
					IBitBrush bit;
					try
					{
						bit = api.createBrush(bitStack);
					}
					catch (InvalidBitItem e)
					{
						continue;
					}
					int totalBits = bitTypes.get(state);
					if (Configs.dropBitsAsFullChiseledBlocks && totalBits >= 4096)
					{
						IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
						BitInventoryHelper.setAllBits(bitAccess, bit);
						int blockCount = totalBits / 4096;
						totalBits -= blockCount * 4096;
						while (blockCount > 0)
						{
							int stackSize = blockCount > 64 ? 64 : blockCount;
							@SuppressWarnings("null")
							ItemStack stack2 = bitAccess.getBitsAsItem(null, ItemType.CHISLED_BLOCK, false);
							if (!stack2.isEmpty())
							{
								stack2.setCount(stackSize);
								BitInventoryHelper.givePlayerStackOrDropOnGround(player, world, api, pos, shape, stack2);
							}
							blockCount -= stackSize;
						}
					}
					int quota;
					while (totalBits > 0)
					{
						quota = totalBits > 64 ? 64 : totalBits;
						ItemStack bitStack2 = bit.getItemStack(quota);
						BitInventoryHelper.givePlayerStackOrDropOnGround(player, world, api, pos, shape, bitStack2);
						totalBits -= quota;
					}
				}
			}
			bitTypes.clear();
			if (Configs.placeBitsInInventory) player.inventoryContainer.detectAndSendChanges();
		}
	}
	
	private static void givePlayerStackOrDropOnGround(EntityPlayer player, World world, IChiselAndBitsAPI api, BlockPos pos, Shape shape, ItemStack stack)
	{
		if (Configs.placeBitsInInventory)
		{
			removeOrAddInventoryBits(api, player, stack, stack.getCount(), true);
			if (stack.getCount() > 0)
				player.inventory.addItemStackToInventory(stack);
		}
		if (stack.getCount() > 0)
		{
			if (Configs.dropBitsInBlockspace)
			{
				BitInventoryHelper.spawnStacksInShape(world, pos, shape, stack);
			}
			else
			{
				player.dropItem(stack, false, false);
			}
		}
	}
	
	private static void spawnStacksInShape(World world, BlockPos pos, Shape shape, ItemStack stack)
	{
		if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops") && !world.restoringBlockSnapshots)
		{
			Vec3d spawnPoint = shape.getRandomInternalPoint(world, pos);
			EntityItem entityitem = new EntityItem(world, spawnPoint.xCoord, spawnPoint.yCoord - 0.25, spawnPoint.zCoord, stack);
			entityitem.setDefaultPickupDelay();
			world.spawnEntity(entityitem);
		}
	}
	
	private static void setAllBits(IBitAccess bitAccess, IBitBrush bit)
	{
		for (int i = 0; i < 16; i++)
		{
			for (int j = 0; j < 16; j++)
			{
				for (int k = 0; k < 16; k++)
				{
					try
					{
						bitAccess.setBitAt(i, j, k, bit);
					}
					catch (SpaceOccupied e) {}
				}
			}
		}
	}
	
	public static void setHeldDesignStack(EntityPlayer player, ItemStack stackChiseledBlock)
	{
		ItemStack stack = player.getHeldItemMainhand();
		ItemType itemType = ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack);
		if (itemType == null || !ItemStackHelper.isDesignItemType(itemType))
			return;
		
		IBitAccess bitAccess = ChiselsAndBitsAPIAccess.apiInstance.createBitItem(stackChiseledBlock);
		if (bitAccess == null)
			return;
		
		ItemStack stackDesign = bitAccess.getBitsAsItem(EnumFacing.getFront(ItemStackHelper.getNBTOrNew(stack)
				.getInteger(ChiselsAndBitsReferences.NBT_KEY_DESIGN_SIDE)), itemType, false);
		if (stackDesign.isEmpty())
			stackDesign = new ItemStack(Item.getByNameOrId(ChiselsAndBitsReferences.MOD_ID + ":" + (itemType == ItemType.POSITIVE_DESIGN
			? ChiselsAndBitsReferences.ITEM_PATH_DESIGN_POSITIVE : (itemType == ItemType.NEGATIVE_DESIGN
			? ChiselsAndBitsReferences.ITEM_PATH_DESIGN_NEGATIVE : ChiselsAndBitsReferences.ITEM_PATH_DESIGN_MIRROR))));
		
		if (!stack.isEmpty() && stack.hasTagCompound())
		{
			stackDesign.setTagInfo(ChiselsAndBitsReferences.NBT_KEY_DESIGN_MODE,
					new NBTTagString(ItemStackHelper.getNBT(stack).getString(ChiselsAndBitsReferences.NBT_KEY_DESIGN_MODE)));
		}
		player.setHeldItem(EnumHand.MAIN_HAND, stackDesign);
	}
	
}