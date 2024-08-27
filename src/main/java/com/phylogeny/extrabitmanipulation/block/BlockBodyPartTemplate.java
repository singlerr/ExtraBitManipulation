package com.phylogeny.extrabitmanipulation.block;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBodyPartTemplate extends BlockExtraBitManipulationBase
{
	
	public BlockBodyPartTemplate(String name)
	{
		super(Material.GROUND, name);
		setHardness(0.2F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable Level player, List<String> tooltip, TooltipFlag advanced)
	{
		tooltip.add("The bits of this block are used as bodypart placeholders in the creation of chiseled armor.");
	}
	
}