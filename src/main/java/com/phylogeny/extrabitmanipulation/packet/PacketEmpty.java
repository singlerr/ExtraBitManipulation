package com.phylogeny.extrabitmanipulation.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.network.FriendlyByteBuf;

public abstract class PacketEmpty implements FabricPacket {
  public PacketEmpty() {
  }

  public PacketEmpty(FriendlyByteBuf buf) {

  }


  @Override
  public void write(FriendlyByteBuf buf) {

  }
}