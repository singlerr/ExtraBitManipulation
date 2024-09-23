package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.ParticleSplashBit;
import com.phylogeny.extrabitmanipulation.client.ParticleSplashBit.Factory;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class PacketBitParticles implements FabricPacket {

  public static final PacketType<PacketBitParticles> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "bit_particles"), PacketBitParticles::new);

  private int flag;
  private Vec3 locBit, locEntity;
  private double width, height;

  public PacketBitParticles(FriendlyByteBuf buffer) {
    flag = buffer.readInt();
    locBit = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    locEntity = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    width = buffer.readDouble();
    height = buffer.readDouble();
  }

  public PacketBitParticles(int flag, @Nullable Entity entityBit, @Nullable Entity entity) {
    this.flag = flag;
    if (entityBit == null || entity == null) {
      locBit = new Vec3(0, 0, 0);
      locEntity = new Vec3(0, 0, 0);
      return;
    }
    double x = entityBit.getX();
    double y = entityBit.getY();
    double z = entityBit.getZ();
    Vec3 start = new Vec3(x, y, z);
    Vec3 end = new Vec3(x + entityBit.getDeltaMovement().x, y + entityBit.getDeltaMovement().y,
        z + entityBit.getDeltaMovement().z);
    Optional<Vec3> result =
        entity.getBoundingBox().inflate(0.30000001192092896D).clip(start, end);
    locBit = result.orElse(start);
    locEntity = new Vec3(entity.getX(), entity.getY(), entity.getZ());
    width = (flag == 0 ? entityBit.getBbWidth() : entity.getBbWidth()) + 0.2;
    height = (flag == 0 ? entityBit.getBbHeight() : entity.getBbHeight()) + 0.2;
  }

  public PacketBitParticles(int flag, Vec3 locBit, BlockPos pos) {
    this(flag, null, (Entity) null);
    this.locBit = locBit;
    locEntity = new Vec3(pos.getX(), pos.getY(), pos.getZ());
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(flag);
    buffer.writeDouble(locBit.x);
    buffer.writeDouble(locBit.y);
    buffer.writeDouble(locBit.z);
    buffer.writeDouble(locEntity.x);
    buffer.writeDouble(locEntity.y);
    buffer.writeDouble(locEntity.z);
    buffer.writeDouble(width);
    buffer.writeDouble(height);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ClientPlayNetworking.PlayPacketHandler<PacketBitParticles> {

    @Override
    public void receive(PacketBitParticles message, LocalPlayer player,
                        PacketSender responseSender) {
      ClientHelper.getThreadListener().execute(new Runnable() {
        @Override
        public void run() {
          Level world = ClientHelper.getWorld();
          double width = message.width;
          double height = message.height;
          double x = message.locBit.x;
          double y = message.locBit.y;
          double z = message.locBit.z;
          double x2, y2, z2;
          if (message.flag == 0) {
            for (int i = 0; i < 3; i++) {
              x2 = x - width * 0.5 + width * world.random.nextDouble();
              y2 = y - height * 0.5 + height * world.random.nextDouble();
              z2 = z - width * 0.5 + width * world.random.nextDouble();
              world.addParticle(ParticleTypes.FLAME, x2, y2, z2, 0, 0, 0);
            }
          } else if (message.flag == 3 || message.flag == 4) {
            Factory particleFactory = new ParticleSplashBit.Factory();
            for (int i = 0; i < 8; i++) {
              ClientHelper.spawnParticle(world, message.locBit, particleFactory);
              if (message.flag == 4) {
                world.addParticle(ParticleTypes.CLOUD, message.locEntity.x + Math.random(),
                    message.locEntity.y + Math.random(), message.locEntity.z + Math.random(), 0, 0,
                    0);
              }
            }
          } else {
            Factory particleFactory = new ParticleSplashBit.Factory();
            for (int i = 0; i < 8; i++) {
              ClientHelper.spawnParticle(world, message.locBit, particleFactory);
            }

            if (message.flag != 2) {
              return;
            }

            int count = Mth.clamp((int) (width * width * height * 6.25), 1, 50);
            for (int i = 0; i < count; i++) {
              ClientHelper.spawnParticle(world, message.locBit, particleFactory);
              if (message.flag == 2) {
                x2 = message.locEntity.x - width * 0.5 + width * world.random.nextDouble();
                y2 = message.locEntity.y + height * world.random.nextDouble();
                z2 = message.locEntity.z - width * 0.5 + width * world.random.nextDouble();
                world.addParticle(ParticleTypes.CLOUD, x2, y2, z2, 0, 0, 0);
              }
            }
          }
        }
      });

    }
  }

}