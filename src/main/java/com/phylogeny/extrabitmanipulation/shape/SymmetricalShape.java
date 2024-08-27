package com.phylogeny.extrabitmanipulation.shape;

import net.minecraft.world.phys.AABB;

public abstract class SymmetricalShape extends Shape
{
	protected float semiDiameter, semiDiameterInset;
	
	public void init(float centerX, float centerY, float centerZ, float semiDiameter, int direction,
			boolean sculptHollowShape, float wallThickness, boolean openEnds)
	{
		init(centerX, centerY, centerZ, direction, sculptHollowShape, wallThickness, openEnds);
		this.semiDiameter = semiDiameter;
		semiDiameterInset = reduceLength(semiDiameter);
	}
	
	@Override
	protected AABB getBoundingBox()
	{
		return new AABB(centerX - semiDiameter, centerY - semiDiameter, centerZ - semiDiameter,
				centerX + semiDiameter, centerY + semiDiameter, centerZ + semiDiameter);
	}
	
}