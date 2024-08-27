package com.phylogeny.extrabitmanipulation.client;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleSplash;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSplashBit extends ParticleSplash
{
	
	protected ParticleSplashBit(Level worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
	}
	
	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		motionY -= particleGravity;
		move(motionX, motionY, motionZ);
		motionX *= 0.9800000190734863D;
		motionY *= 0.9800000190734863D;
		motionZ *= 0.9800000190734863D;
		if (particleMaxAge-- <= 0)
			setExpired();
		
		if (onGround)
		{
			motionX *= 0.699999988079071D;
			motionZ *= 0.699999988079071D;
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class Factory implements ParticleProvider
	{
		@Override
		public Particle createParticle(int particleID, Level worldIn, double xCoordIn, double yCoordIn,
				double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
		{
			return new ParticleSplashBit(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		}
	}
	
}