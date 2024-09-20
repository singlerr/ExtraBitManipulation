package com.phylogeny.extrabitmanipulation.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.network.FriendlyByteBuf;

public abstract class PacketInt implements FabricPacket {
  protected int value;

  public PacketInt(FriendlyByteBuf buffer) {
    this.value = buffer.readInt();
  }

  public PacketInt(int value) {
    this.value = value;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeInt(value);
  }
}