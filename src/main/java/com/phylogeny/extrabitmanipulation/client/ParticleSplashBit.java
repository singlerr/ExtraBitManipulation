package com.phylogeny.extrabitmanipulation.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ParticleSplashBit extends SplashParticle {

  protected ParticleSplashBit(ClientLevel worldIn, double xCoordIn, double yCoordIn,
                              double zCoordIn,
                              double xSpeedIn, double ySpeedIn, double zSpeedIn) {
    super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
  }

  @Override
  public void tick() {
    xo = x;
    yo = y;
    zo = z;

    yd -= gravity;
    move(xd, yd, zd);
    xd *= 0.9800000190734863D;
    yd *= 0.9800000190734863D;
    zd *= 0.9800000190734863D;
    if (age-- <= 0) {
      remove();
    }

    if (onGround) {
      xd *= 0.699999988079071D;
      zd *= 0.699999988079071D;
    }
  }

  @Environment(EnvType.CLIENT)
  public static class Factory implements ParticleProvider {
    @Override
    public @Nullable Particle createParticle(ParticleOptions particleOptions,
                                             ClientLevel clientLevel, double x, double y, double z,
                                             double xSpeed, double ySpeed, double zSpeed) {
      return new ParticleSplashBit(clientLevel, x, y, z, xSpeed, ySpeed, zSpeed);
    }
  }

}