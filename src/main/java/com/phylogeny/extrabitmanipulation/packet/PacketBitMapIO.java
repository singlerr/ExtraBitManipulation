package com.phylogeny.extrabitmanipulation.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.network.FriendlyByteBuf;

public abstract class PacketBitMapIO implements FabricPacket {
  protected String nbtKey;
  protected boolean saveStatesById;

  public PacketBitMapIO(FriendlyByteBuf buf) {
    nbtKey = buf.readUtf();
    saveStatesById = buf.readBoolean();
  }

  public PacketBitMapIO(String nbtKey, boolean saveStatesById) {
    this.nbtKey = nbtKey;
    this.saveStatesById = saveStatesById;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeUtf(nbtKey);
    buf.writeBoolean(saveStatesById);
  }
}