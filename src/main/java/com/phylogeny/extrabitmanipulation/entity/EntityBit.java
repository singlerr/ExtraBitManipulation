package com.phylogeny.extrabitmanipulation.entity;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.packet.PacketBitParticles;
import com.phylogeny.extrabitmanipulation.packet.PacketPlaceEntityBit;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

public class EntityBit extends Projectile {
  private ItemStack bitStack = ItemStack.EMPTY;
  protected boolean inGround;
  public Entity shootingEntity;

  public EntityBit(Level worldIn) {
    super(worldIn);
    setBoundingBox(Shapes.block().);
    setSize(Utility.PIXEL_F, Utility.PIXEL_F);
  }

  public EntityBit(Level worldIn, double x, double y, double z, ItemStack bitStack) {
    this(worldIn);
    setPos(x, y, z);
    this.bitStack = bitStack.copy();
    this.bitStack.setCount(1);
  }

  public EntityBit(Level worldIn, LivingEntity shooter, ItemStack bitStack) {
    this(worldIn, shooter.getX(), shooter.getY() + shooter.getEyeHeight() - 0.10000000149011612D,
        shooter.getZ(), bitStack);
    shootingEntity = shooter;
  }

  public ItemStack getBitStack() {
    return bitStack;
  }

  @Override
  protected void defineSynchedData() {

  }

  @Override
  @Environment(EnvType.CLIENT)
  public boolean shouldRenderAtSqrDistance(double distance) {
    double range = getBoundingBox().getSize() * 10.0D;
    if (Double.isNaN(range)) {
      range = 4.0D;
    }

    range *= 64.0D;
    return distance < range * range;
  }

  public void setAim(Entity shooter, float pitch, float yaw, float velocity, float inaccuracy) {
    pitch = (float) Math.toRadians(pitch);
    yaw = (float) Math.toRadians(yaw);
    float x = -Mth.sin(yaw) * Mth.cos(pitch);
    float y = -Mth.sin(pitch);
    float z = Mth.cos(yaw) * Mth.cos(pitch);
    setAim(shooter, pitch, y, velocity, inaccuracy);
//    setThrowableHeading(x, y, z, velocity, inaccuracy);

    double motionX = shooter.getDeltaMovement().x;
    double motionZ = shooter.getDeltaMovement().z;
    double motionY = 0;
    if (!shooter.onGround()) {
      motionY += shooter.getDeltaMovement().y;
    }

    addDeltaMovement(new Vec3(motionX, motionY, motionZ));
  }

  @Override
  public void shootFromRotation(Entity entity, float x, float y, float z, float velocity,
                                float inaccuracy) {
    float f = Mth.sqrt(x * x + y * y + z * z);
    x /= f;
    y /= f;
    z /= f;
    x += (float) (random.nextGaussian() * 0.007499999832361937D * inaccuracy);
    y += (float) (random.nextGaussian() * 0.007499999832361937D * inaccuracy);
    z += (float) (random.nextGaussian() * 0.007499999832361937D * inaccuracy);
    x *= velocity;
    y *= velocity;
    z *= velocity;
    setDeltaMovement(x, y, z);

    float f1 = Mth.sqrt(x * x + z * z);
    setYRot((float) (Mth.atan2(x, z) * (180D / Math.PI)));
    setXRot((float) (Mth.atan2(y, f1) * (180D / Math.PI)));
    yRotO = getYRot();
    xRotO = getXRot();

  }


  @Override
  @Environment(EnvType.CLIENT)
  public void lerpMotion(double x, double y, double z) {
    setDeltaMovement(x, y, z);
    if (xRotO == 0.0F && yRotO == 0.0F) {
      setRotation(x, y, z);
      moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
    }
  }

  private void setRotation(double x, double y, double z) {
    float f = Mth.sqrt((float) (x * x + z * z));
    setXRot((float) (Mth.atan2(y, f) * (180D / Math.PI)));
    setYRot((float) (Mth.atan2(x, z) * (180D / Math.PI)));
    xRotO = getXRot();
    yRotO = getYRot();
  }

  @Override
  public void tick() {
    if (inGround) {
      return;
    }

    super.tick();
    if (xRotO == 0.0F && yRotO == 0.0F) {
      setRotation(getDeltaMovement().x, getDeltaMovement().y, getDeltaMovement().z);
    }

    Vec3 start = new Vec3(getX(), getY(), getZ());
    Vec3 end = new Vec3(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y,
        getZ() + getDeltaMovement().z);
    HitResult result = level().clip(
        new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE,
            CollisionContext.empty()));

    start = new Vec3(getX(), getY(), getZ());
    end = new Vec3(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y,
        getZ() + getDeltaMovement().z);
    if (result != null) {
      end = new Vec3(result.getLocation().x, result.getLocation().y, result.getLocation().z);
    }

    Entity entity = findEntityOnPath(start, end);
    if (entity != null) {
      result = new EntityHitResult(entity);
    }


    if (result != null) {
      onHit(result);
    }
    Vec3 pos = new Vec3(getX() + getDeltaMovement().x, getY() + getDeltaMovement().y,
        getZ() + getDeltaMovement().z);
    setPos(pos);

    float f4 = Mth.sqrt((float) (getDeltaMovement().x * getDeltaMovement().x +
        getDeltaMovement().z * getDeltaMovement().z));
    setYRot((float) (Mth.atan2(getDeltaMovement().x, getDeltaMovement().z) * (180D / Math.PI)));
    for (setXRot((float) (Mth.atan2(getDeltaMovement().y, f4) * (180D / Math.PI)));
         getXRot() - xRotO < -180.0F; xRotO -= 360.0F) {
    }

    while (getXRot() - xRotO >= 180.0F) {
      xRotO += 360.0F;
    }

    while (getYRot() - yRotO < -180.0F) {
      yRotO -= 360.0F;
    }

    while (getYRot() - yRotO >= 180.0F) {
      yRotO += 360.0F;
    }

    setXRot(xRotO + (getXRot() - xRotO) * 0.2F);
    setYRot(yRotO + (getYRot() - yRotO) * 0.2F);
    float attenuation = 0.99F;
    if (isInWater()) {
      for (int i = 0; i < 4; ++i) {
        Vec3 motion = getDeltaMovement();
        level().addParticle(ParticleTypes.BUBBLE, getX() - motion.x * 0.25,
            getY() - motion.y * 0.25, getZ() - motion.z * 0.25, motion.x, motion.y, motion.z);
      }
      attenuation = 0.6F;
    }
    if (isInWaterOrRain()) {
      clearFire();
    }
    Vec3 motion = getDeltaMovement();

    double motionX = motion.x;
    double motionY = motion.y;
    double motionZ = motion.z;
    motionX *= attenuation;
    motionY *= attenuation;
    motionZ *= attenuation;
    if (!isNoGravity()) {
      motionY -= 0.05000000074505806D;
    }

    setDeltaMovement(new Vec3(motionX, motionY, motionZ));
    setPos(getX(), getY(), getZ());
    checkInsideBlocks();

  }


  protected void onHit(HitResult result) {
    if (bitStack.isEmpty()) {
      return;
    }

    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    boolean drop = true;
    boolean isLava = false;
    try {
      BlockState state = api.createBrush(bitStack).getState();
      if (state != null) {
        isLava = !state.getFluidState().isEmpty();
        drop = isLava && state.getBlock() instanceof LiquidBlock;
      }
    } catch (InvalidBitItem e) {
    }
    EntityHitResult entityHit = (EntityHitResult) result;
    Entity entity = entityHit.getEntity();
    if (entity != null) {
      if (!level().isClientSide) {
        if ((isLava ? Configs.disableIgniteEntities : Configs.disableExtinguishEntities) || drop) {
          if (!Configs.thrownBitDamageDisable) {
            entity.hurt(damageSources().thrown(this, shootingEntity), Configs.thrownBitDamage);
          }


          drop = true;
        } else {
          if (isLava) {
            playSound(SoundEvents.FIRE_AMBIENT, 1.0F,
                3.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
          } else {
            playSound(getSwimSound(), 0.2F,
                1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
          }
          int flag = isLava ? 0 : 1;
          if (entity.isOnFire() != isLava) {
            if (isLava) {
              entity.setSecondsOnFire(Configs.thrownLavaBitBurnTime);
            } else {
              entity.clearFire();
              playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.7F,
                  1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
              flag = 2;
            }
          }
          if (!isLava && entity instanceof Blaze) {
            if (!Configs.thrownWaterBitBlazeDamageDisable) {
              entity.hurt(damageSources().thrown(this, shootingEntity),
                  Configs.thrownWaterBitBlazeDamage);
            }

            flag = 2;
          }
          updateClients(new PacketBitParticles(flag, this, entity));
        }
      }
    } else {
      BlockHitResult blockHitResult = (BlockHitResult) result;
      BlockPos pos = blockHitResult.getBlockPos();
      if (!(isLava ? Configs.disableIgniteBlocks : Configs.disableExtinguishBlocks) && !drop) {
        if (!level().isClientSide) {
          pos = pos.relative(blockHitResult.getDirection());
          if (isLava) {
            if (level().isEmptyBlock(pos)) {
              playSound(SoundEvents.FIRE_AMBIENT, 1.0F,
                  3.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
              level().setBlock(pos, Blocks.FIRE.defaultBlockState(), 11);
            }
          } else {
            playSound(getSwimSound(), 0.2F,
                1.6F + (random.nextFloat() - random.nextFloat()) * 0.4F);
            int flag = 3;
            if (level().getBlockState(pos).getBlock() == Blocks.FIRE) {
              playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F,
                  2.6F + (level().random.nextFloat() - level().random.nextFloat()) * 0.8F);
              level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
              flag = 4;
            }
            Vec3 hit =
                blockHitResult.getLocation()
                    .add(Utility.PIXEL_D * blockHitResult.getDirection().getStepY() * 2,
                        Utility.PIXEL_D * blockHitResult.getDirection().getStepX() * 2,
                        Utility.PIXEL_D * blockHitResult.getDirection().getStepZ() * 2);
            updateClients(new PacketBitParticles(flag, hit, pos));
          }
          removeAfterChangingDimensions();
        }
        return;
      }
      if (!level().isClientSide) {
        Vec3 motion = getDeltaMovement();
        float volume = Mth.sqrt(
            (float) (motion.x * motion.x + motion.y * motion.y + motion.z * motion.z)) * 0.2F;
        if (volume > 1.0F) {
          volume = 1.0F;
        }

        SoundEvent sound = SoundEvents.METAL_HIT;
        BlockState state = level().getBlockState(pos);
        if (state != null) {
          SoundType soundType = state.getBlock().getSoundType(state, level(), pos, this);
          if (soundType != null) {
            sound = soundType.getFallSound();
          }
        }
        playSound(sound, volume, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.4F);
      }
      drop = !placeBit(level(), bitStack, pos, blockHitResult.getDirection(),
          blockHitResult.getDirection(), level().isClientSide);
      if (!level().isClientSide && !drop) {
        updateClients(new PacketPlaceEntityBit(bitStack, pos, result));
      }
    }
    if (!level().isClientSide) {
      if (drop) {
        spawnAtLocation(bitStack, 0);
      }

      removeAfterChangingDimensions();
    }
  }

  private void updateClients(IMessage message) {
    ExtraBitManipulation.packetNetwork.sendToAllAround(message,
        new TargetPoint(world.provider.getDimension(), posX, posY, posZ, 100));
  }

  public static boolean placeBit(Level world, ItemStack bitStack, BlockPos pos, Vec3 hitVec,
                                 Direction sideHit, boolean simulate) {
    try {
      IChiselAndBitsAPI api2 = ChiselsAndBitsAPIAccess.apiInstance;
      IBitLocation bitLoc =
          api2.getBitPos((float) hitVec.x - pos.getX(), (float) hitVec.y - pos.getY(),
              (float) hitVec.z - pos.getZ(), sideHit, pos, false);
      Vec3 center = new Vec3(bitLoc.getBitX() * Utility.PIXEL_D + pos.getX() +
          Utility.PIXEL_D * sideHit.getStepX(),
          bitLoc.getBitY() * Utility.PIXEL_D + pos.getY() +
              Utility.PIXEL_D * sideHit.getStepY(),
          bitLoc.getBitZ() * Utility.PIXEL_D + pos.getZ() +
              Utility.PIXEL_D * sideHit.getStepZ());
      pos = BlockPos.containing(center);
      IBitAccess bitAccess = api2.getBitAccess(world, pos);
      if (api2.canBeChiseled(world, pos)) {
        int x = (int) (Math.ceil((int) ((center.x - pos.getX()) / Utility.PIXEL_D)));
        int y = (int) (Math.ceil((int) ((center.y - pos.getY()) / Utility.PIXEL_D)));
        int z = (int) (Math.ceil((int) ((center.z - pos.getZ()) / Utility.PIXEL_D)));
        if (bitAccess.getBitAt(x, y, z).isAir()) {
          bitAccess.setBitAt(x, y, z, api2.createBrush(bitStack));
          if (!simulate) {
            bitAccess.commitChanges(true);
          }

          return true;
        }
      }
    } catch (CannotBeChiseled e) {
    } catch (SpaceOccupied e) {
    } catch (InvalidBitItem e) {
    }
    return false;
  }

  @Nullable
  protected Entity findEntityOnPath(Vec3 start, Vec3 end) {
    Entity entity = null;
    List<Entity> list = level().getEntities(this,
        getBoundingBox().expandTowards(getDeltaMovement().x, getDeltaMovement().y,
            getDeltaMovement().z).inflate(1.0D));
    double d0 = 0.0D;
    for (int i = 0; i < list.size(); ++i) {
      Entity entity1 = list.get(i);
      if (!entity1.isPickable() || (entity1 == shootingEntity && tickCount < 5)) {
        continue;
      }

      AABB axisalignedbb = entity1.getBoundingBox().inflate(0.30000001192092896D);
      Optional<Vec3> raytraceresult = axisalignedbb.clip(start, end);
      if (raytraceresult.isPresent()) {
        double d1 = start.distanceToSqr(raytraceresult.get());
        if (d1 < d0 || d0 == 0.0D) {
          entity = entity1;
          d0 = d1;
        }
      }
    }
    return entity;
  }

  @Override
  public boolean save(CompoundTag compoundTag) {
    boolean result = super.save(compoundTag);
    compoundTag.putByte("inGround", (byte) (inGround ? 1 : 0));
    CompoundTag nbt = new CompoundTag();
    bitStack.save(nbt);
    compoundTag.put(NBTKeys.ENTITY_BIT_STACK, nbt);

    return result;
  }

  @Override
  public void load(CompoundTag compoundTag) {
    inGround = compoundTag.getByte("inGround") == 1;
    bitStack = ItemStack.of(compoundTag.getCompound(NBTKeys.ENTITY_BIT_STACK));
  }

  @Override
  protected void addAdditionalSaveData(CompoundTag compoundTag) {
    super.addAdditionalSaveData(compoundTag);
    ByteBufUtils.writeItemStack(buffer, bitStack);
    buffer.writeDouble(motionX);
    buffer.writeDouble(motionY);
    buffer.writeDouble(motionZ);
  }

  @Override
  protected void readAdditionalSaveData(CompoundTag compoundTag) {
    super.readAdditionalSaveData(compoundTag);
    bitStack = ByteBufUtils.readItemStack(buffer);
    motionX = buffer.readDouble();
    motionY = buffer.readDouble();
    motionZ = buffer.readDouble();
  }

  @Override
  public boolean isAttackable() {
    return false;
  }

  @Override
  protected float getEyeHeight(Pose pose, EntityDimensions entityDimensions) {
    return 0.0F;
  }

  @Override
  public boolean isControlledByLocalInstance() {
    return true;
  }

}