package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class PacketArmorSlotInt implements FabricPacket {

  protected ArmorType armorType;
  protected int indexArmorSet;
  protected int value;

  public PacketArmorSlotInt(FriendlyByteBuf buffer) {
    value = buffer.readInt();
    indexArmorSet = buffer.readInt();
    if (buffer.readBoolean()) {
      armorType = ArmorType.values()[buffer.readInt()];
    }
  }

  public PacketArmorSlotInt(@Nullable ArmorType armorType, int indexArmorSet, int value) {
    this.armorType = armorType;
    this.indexArmorSet = indexArmorSet;
    this.value = value;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(value);
    buffer.writeInt(indexArmorSet);
    if (BitIOHelper.notNullToBuffer(buffer, armorType)) {
      buffer.writeInt(armorType.ordinal());
    }

  }

  protected static ItemStack getArmorStack(Player player, PacketArmorSlotInt message) {
    ItemStack stack =
        ItemStackHelper.getChiseledArmorStack(player, message.armorType, message.indexArmorSet);
    return ItemStackHelper.isChiseledArmorStack(stack) ? stack : ItemStack.EMPTY;
  }
}
