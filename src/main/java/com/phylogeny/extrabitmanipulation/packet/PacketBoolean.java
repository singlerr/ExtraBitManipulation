package com.phylogeny.extrabitmanipulation.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.network.FriendlyByteBuf;

public abstract class PacketBoolean implements FabricPacket {
  protected boolean value;

  public PacketBoolean(FriendlyByteBuf buf) {
    value = buf.readBoolean();
  }

  public PacketBoolean(boolean value) {
    this.value = value;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeBoolean(value);
  }

}