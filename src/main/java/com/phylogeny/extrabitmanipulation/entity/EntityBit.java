package com.phylogeny.extrabitmanipulation.entity;

import io.netty.buffer.ByteBuf;

import java.util.List;

import javax.annotation.Nullable;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.packet.PacketBitParticles;
import com.phylogeny.extrabitmanipulation.packet.PacketPlaceEntityBit;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;

public class EntityBit extends Entity implements IProjectile, IEntityAdditionalSpawnData
{
	private ItemStack bitStack = ItemStack.EMPTY;
	protected boolean inGround;
	public Entity shootingEntity;
	
	public EntityBit(Level worldIn)
	{
		super(worldIn);
		setSize(Utility.PIXEL_F, Utility.PIXEL_F);
	}
	
	public EntityBit(Level worldIn, double x, double y, double z, ItemStack bitStack)
	{
		this(worldIn);
		setPos(x, y, z);
		this.bitStack = bitStack.copy();
		this.bitStack.setCount(1);
	}
	
	public EntityBit(Level worldIn, EntityLivingBase shooter, ItemStack bitStack)
	{
		this(worldIn, shooter.posX, shooter.posY + shooter.getEyeHeight() - 0.10000000149011612D, shooter.posZ, bitStack);
		shootingEntity = shooter;
	}
	
	@Override
	protected void entityInit() {}
	
	public ItemStack getBitStack()
	{
		return bitStack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance)
	{
		double range = getEntityBoundingBox().getAverageEdgeLength() * 10.0D;
		if (Double.isNaN(range))
			range = 4.0D;
		
		range *= 64.0D;
		return distance < range * range;
	}
	
	public void setAim(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy)
	{
		pitch = (float) Math.toRadians(pitch);
		yaw = (float) Math.toRadians(yaw);
		float x = -Mth.sin(yaw) * Mth.cos(pitch);
		float y = -Mth.sin(pitch);
		float z = Mth.cos(yaw) * Mth.cos(pitch);
		setThrowableHeading(x, y, z, velocity, inaccuracy);
		motionX += shooter.motionX;
		motionZ += shooter.motionZ;
		if (!shooter.onGround)
			motionY += shooter.motionY;
	}
	
	@Override
	public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy)
	{
		float f = Mth.sqrt(x * x + y * y + z * z);
		x /= f;
		y /= f;
		z /= f;
		x += random.nextGaussian() * 0.007499999832361937D * inaccuracy;
		y += random.nextGaussian() * 0.007499999832361937D * inaccuracy;
		z += random.nextGaussian() * 0.007499999832361937D * inaccuracy;
		x *= velocity;
		y *= velocity;
		z *= velocity;
		motionX = x;
		motionY = y;
		motionZ = z;
		float f1 = Mth.sqrt(x * x + z * z);
		yRot = (float)(Mth.atan2(x, z) * (180D / Math.PI));
		xRot = (float)(Mth.atan2(y, f1) * (180D / Math.PI));
		yRotO = yRot;
		xRotO = xRot;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void lerpMotion(double x, double y, double z)
	{
		motionX = x;
		motionY = y;
		motionZ = z;
		if (xRotO == 0.0F && yRotO == 0.0F)
		{
			setRotation(x, y, z);
			moveTo(posX, posY, posZ, yRot, xRot);
		}
	}
	
	private void setRotation(double x, double y, double z)
	{
		float f = Mth.sqrt(x * x + z * z);
		xRot = (float)(Mth.atan2(y, f) * (180D / Math.PI));
		yRot = (float)(Mth.atan2(x, z) * (180D / Math.PI));
		xRotO = xRot;
		yRotO = yRot;
	}
	
	@Override
	public void onUpdate()
	{
		if (inGround)
			return;
		
		super.onUpdate();
		if (xRotO == 0.0F && yRotO == 0.0F)
			setRotation(motionX, motionY, motionZ);
		
		Vec3d start = new Vec3d(posX, posY, posZ);
		Vec3d end = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
		HitResult result = level.clipWithInteractionOverride(start, end, false, true, false);
		start = new Vec3d(posX, posY, posZ);
		end = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
		if (result != null)
			end = new Vec3d(result.hitVec.x, result.hitVec.y, result.hitVec.z);
		
		Entity entity = findEntityOnPath(start, end);
		if (entity != null)
			result = new HitResult(entity);
		
		if (result != null)
			onHit(result);
		
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		float f4 = Mth.sqrt(motionX * motionX + motionZ * motionZ);
		yRot = (float)(Mth.atan2(motionX, motionZ) * (180D / Math.PI));
		for (xRot = (float)(Mth.atan2(motionY, f4) * (180D / Math.PI));
				xRot - xRotO < -180.0F; xRotO -= 360.0F) {}
		
		while (xRot - xRotO >= 180.0F)
			xRotO += 360.0F;
		
		while (yRot - yRotO < -180.0F)
			yRotO -= 360.0F;
		
		while (yRot - yRotO >= 180.0F)
			yRotO += 360.0F;
		
		xRot = xRotO + (xRot - xRotO) * 0.2F;
		yRot = yRotO + (yRot - yRotO) * 0.2F;
		float attenuation = 0.99F;
		if (isInWater())
		{
			for (int i = 0; i < 4; ++i)
			{
				level.spawnParticle(EnumParticleTypes.WATER_BUBBLE, posX - motionX * 0.25D,
						posY - motionY * 0.25D, posZ - motionZ * 0.25D, motionX, motionY, motionZ, new int[0]);
			}
			attenuation = 0.6F;
		}
		if (isInWaterOrRain())
			clearFire();
		
		motionX *= attenuation;
		motionY *= attenuation;
		motionZ *= attenuation;
		if (!isNoGravity())
			motionY -= 0.05000000074505806D;
		
		setPos(posX, posY, posZ);
		checkInsideBlocks();
	}
	
	protected void onHit(HitResult result)
	{
		if (bitStack.isEmpty())
			return;
		
		IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
		boolean drop = true;
		boolean isLava = false;
		try
		{
			IBlockState state = api.createBrush(bitStack).getState();
			if (state != null)
			{
				isLava = state.getMaterial() != Material.WATER;
				drop = isLava && state.getMaterial() != Material.LAVA;
			}
		}
		catch (InvalidBitItem e) {}
		Entity entity = result.entityHit;
		if (entity != null)
		{
			if (!level.isClientSide)
			{
				if ((isLava ? Configs.disableIgniteEntities : Configs.disableExtinguishEntities) || drop)
				{
					if (!Configs.thrownBitDamageDisable)
						entity.hurt(DamageSource.thrown(this, shootingEntity), Configs.thrownBitDamage);
					
					drop = true;
				}
				else 
				{
					if (isLava)
					{
						playSound(SoundEvents.BLOCK_FIRE_AMBIENT, 1.0F, 3.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
					}
					else
					{
						playSound(getSwimSound(), 0.2F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
					}
					int flag = isLava ? 0 : 1;
					if (entity.isOnFire() != isLava)
					{
						if (isLava)
						{
							entity.setSecondsOnFire(Configs.thrownLavaBitBurnTime);
						}
						else
						{
							entity.clearFire();
							playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
							flag = 2;
						}
					}
					if (!isLava && entity instanceof EntityBlaze)
					{
						if (!Configs.thrownWaterBitBlazeDamageDisable)
							entity.hurt(DamageSource.thrown(this, shootingEntity), Configs.thrownWaterBitBlazeDamage);
						
						flag = 2;
					}
					updateClients(new PacketBitParticles(flag, this, entity));
				}
			}
		}
		else
		{
			BlockPos pos = result.getBlockPos();
			if (!(isLava ? Configs.disableIgniteBlocks : Configs.disableExtinguishBlocks) && !drop)
			{
				if (!level.isClientSide)
				{
					pos = pos.relative(result.sideHit);
					if (isLava)
					{
						if (level.isEmptyBlock(pos))
						{
							playSound(SoundEvents.BLOCK_FIRE_AMBIENT, 1.0F, 3.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
							level.setBlock(pos, Blocks.FIRE.getDefaultState(), 11);
						}
					}
					else
					{
						playSound(getSwimSound(), 0.2F, 1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
						int flag = 3;
						if (level.getBlockState(pos).getBlock() == Blocks.FIRE)
						{
							playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
							level.setBlock(pos, Blocks.AIR.getDefaultState(), 2);
							flag = 4;
						}
						Vec3d hit = result.hitVec.addVector(Utility.PIXEL_D * result.sideHit.getFrontOffsetY() * 2,
								Utility.PIXEL_D * result.sideHit.getFrontOffsetX() * 2,
								Utility.PIXEL_D * result.sideHit.getFrontOffsetZ() * 2);
						updateClients(new PacketBitParticles(flag, hit, pos));
					}
					removeAfterChangingDimensions();
				}
				return;
			}
			if (!level.isClientSide)
			{
				float volume = Mth.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ) * 0.2F;
				if (volume > 1.0F)
					volume = 1.0F;
				
				SoundEvent sound = SoundEvents.BLOCK_METAL_HIT;
				IBlockState state = level.getBlockState(pos);
				if (state != null)
				{
					SoundType soundType = state.getBlock().getSoundType(state, level, pos, this);
					if (soundType != null)
						sound = soundType.getFallSound();
				}
				playSound(sound, volume, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
			}
			drop = !placeBit(level, bitStack, pos, result.hitVec, result.sideHit, level.isClientSide);
			if (!level.isClientSide && !drop)
				updateClients(new PacketPlaceEntityBit(bitStack, pos, result));
		}
		if (!level.isClientSide)
		{
			if (drop)
				spawnAtLocation(bitStack, 0);
			
			removeAfterChangingDimensions();
		}
	}
	
	private void updateClients(IMessage message)
	{
		ExtraBitManipulation.packetNetwork.sendToAllAround(message, new TargetPoint(world.provider.getDimension(), posX, posY, posZ, 100));
	}
	
	public static boolean placeBit(Level world, ItemStack bitStack, BlockPos pos, Vec3d hitVec, EnumFacing sideHit, boolean simulate)
	{
		try
		{
			IChiselAndBitsAPI api2 = ChiselsAndBitsAPIAccess.apiInstance;
			IBitLocation bitLoc = api2.getBitPos((float) hitVec.x - pos.getX(), (float) hitVec.y - pos.getY(),
					(float) hitVec.z - pos.getZ(), sideHit, pos, false);
			Vec3d center = new Vec3d(bitLoc.getBitX() * Utility.PIXEL_D + pos.getX() + Utility.PIXEL_D * sideHit.getFrontOffsetX(),
					bitLoc.getBitY() * Utility.PIXEL_D + pos.getY() + Utility.PIXEL_D * sideHit.getFrontOffsetY(),
					bitLoc.getBitZ() * Utility.PIXEL_D + pos.getZ() + Utility.PIXEL_D * sideHit.getFrontOffsetZ());
			pos = new BlockPos(center);
			IBitAccess bitAccess = api2.getBitAccess(world, pos);
			if (api2.canBeChiseled(world, pos))
			{
				int x = (int) (Math.ceil((int) ((center.x - pos.getX()) / Utility.PIXEL_D)));
				int y = (int) (Math.ceil((int) ((center.y - pos.getY()) / Utility.PIXEL_D)));
				int z = (int) (Math.ceil((int) ((center.z - pos.getZ()) / Utility.PIXEL_D)));
				if (bitAccess.getBitAt(x, y, z).isAir())
				{
					bitAccess.setBitAt(x, y, z, api2.createBrush(bitStack));
					if (!simulate)
						bitAccess.commitChanges(true);
					
					return true;
				}
			}
		}
		catch (CannotBeChiseled e) {}
		catch (SpaceOccupied e) {}
		catch (InvalidBitItem e) {}
		return false;
	}
	
	@Nullable
	protected Entity findEntityOnPath(Vec3d start, Vec3d end)
	{
		Entity entity = null;
		List<Entity> list = level.getEntities(this, getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0D));
		double d0 = 0.0D;
		for (int i = 0; i < list.size(); ++i)
		{
			Entity entity1 = list.get(i);
			if (!entity1.isPickable() || (entity1 == shootingEntity && tickCount < 5))
				continue;
			
			AABB axisalignedbb = entity1.getEntityBoundingBox().grow(0.30000001192092896D);
			HitResult raytraceresult = axisalignedbb.calculateIntercept(start, end);
			if (raytraceresult != null)
			{
				double d1 = start.squareDistanceTo(raytraceresult.hitVec);
				if (d1 < d0 || d0 == 0.0D)
				{
					entity = entity1;
					d0 = d1;
				}
			}
		}
		return entity;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		compound.setByte("inGround", (byte)(inGround ? 1 : 0));
		NBTTagCompound nbt = new NBTTagCompound();
		bitStack.writeToNBT(nbt);
		compound.setTag(NBTKeys.ENTITY_BIT_STACK, nbt);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		inGround = compound.getByte("inGround") == 1;
		bitStack = new ItemStack(compound.getCompoundTag(NBTKeys.ENTITY_BIT_STACK));
	}
	
	@Override
	public void writeSpawnData(ByteBuf buffer)
	{
		ByteBufUtils.writeItemStack(buffer, bitStack);
		buffer.writeDouble(motionX);
		buffer.writeDouble(motionY);
		buffer.writeDouble(motionZ);
	}
	
	@Override
	public void readSpawnData(ByteBuf buffer)
	{
		bitStack = ByteBufUtils.readItemStack(buffer);
		motionX = buffer.readDouble();
		motionY = buffer.readDouble();
		motionZ = buffer.readDouble();
	}
	
	@Override
	public boolean isAttackable()
	{
		return false;
	}

	@Override
	public float getEyeHeight()
	{
		return 0.0F;
	}
	
	@Override
	public boolean isControlledByLocalInstance()
	{
		return true;
	}
	
}