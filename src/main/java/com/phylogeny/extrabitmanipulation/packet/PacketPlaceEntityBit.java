package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PacketPlaceEntityBit implements FabricPacket {

  public static final PacketType<PacketPlaceEntityBit> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "place_entity_bit"), PacketPlaceEntityBit::new);

  private final ItemStack bitStack;
  private final BlockPos pos;
  private final Vec3 hitVec;
  private final Direction sideHit;

  public PacketPlaceEntityBit(FriendlyByteBuf buffer) {
    bitStack = buffer.readItem();
    pos = BlockPos.of(buffer.readLong());
    hitVec = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    sideHit = Direction.from3DDataValue(buffer.readInt());
  }

  public PacketPlaceEntityBit(ItemStack bitStack, BlockPos pos, HitResult result) {
    this.bitStack = bitStack;
    this.pos = pos;
    this.hitVec = result.getLocation();
    this.sideHit = ((BlockHitResult) result).getDirection();
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeItem(bitStack);
    buffer.writeLong(pos.asLong());
    buffer.writeDouble(hitVec.x);
    buffer.writeDouble(hitVec.y);
    buffer.writeDouble(hitVec.z);
    buffer.writeInt(sideHit.ordinal());
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ClientPlayNetworking.PlayPacketHandler<PacketPlaceEntityBit> {
    @Override
    public void receive(PacketPlaceEntityBit message, LocalPlayer player,
                        PacketSender responseSender) {
      ClientHelper.getThreadListener().execute(new Runnable() {
        @Override
        public void run() {
          EntityBit.placeBit(ClientHelper.getWorld(), message.bitStack, message.pos, message.hitVec,
              message.sideHit, false);
        }
      });
    }


  }

}