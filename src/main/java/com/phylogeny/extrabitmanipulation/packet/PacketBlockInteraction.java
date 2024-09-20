package com.phylogeny.extrabitmanipulation.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public abstract class PacketBlockInteraction implements FabricPacket {
  protected BlockPos pos;
  protected Direction side;
  protected Vec3 hit;

  public PacketBlockInteraction(FriendlyByteBuf buffer) {
    pos = BlockPos.of(buffer.readLong());
    side = Direction.from3DDataValue(buffer.readInt());
    hit = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
  }

  public PacketBlockInteraction(BlockPos pos, Direction side, Vec3 hit) {
    this.pos = pos;
    this.side = side;
    this.hit = hit;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeLong(pos.asLong());
    buffer.writeInt(side.ordinal());
    buffer.writeDouble(hit.x);
    buffer.writeDouble(hit.y);
    buffer.writeDouble(hit.z);
  }

}