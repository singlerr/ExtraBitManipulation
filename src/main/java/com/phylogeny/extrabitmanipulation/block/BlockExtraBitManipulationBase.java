package com.phylogeny.extrabitmanipulation.block;

import com.phylogeny.extrabitmanipulation.client.CreativeTabExtraBitManipulation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;

public class BlockExtraBitManipulationBase extends Block
{
	private String name;
	
	@SuppressWarnings("null")
	public BlockExtraBitManipulationBase(Material material, String name)
	{
		super(material);
		this.name = name;
		setRegistryName(name);
		setUnlocalizedName(getRegistryName().toString());
		setCreativeTab(CreativeTabExtraBitManipulation.CREATIVE_TAB);
		setHardness(0.5F);
	}
	
	public String getName()
	{
		return name;
	}
	
}