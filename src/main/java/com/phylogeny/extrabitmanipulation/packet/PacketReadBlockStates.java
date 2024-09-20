package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PacketReadBlockStates extends PacketBlockInteraction {

  public static final PacketType<PacketReadBlockStates> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "read_block_states"), PacketReadBlockStates::new);

  private Vec3i drawnStartPoint;
  private ModelReadData modelingData = new ModelReadData();

  public PacketReadBlockStates(FriendlyByteBuf buffer) {
    super(buffer);
    if (buffer.readBoolean()) {
      drawnStartPoint = new Vec3i(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    modelingData.fromBytes(buffer);
  }

  public PacketReadBlockStates(BlockPos pos, Vec3 hit, Vec3i drawnStartPoint,
                               ModelReadData modelingData) {
    super(pos, Direction.UP, hit);
    this.drawnStartPoint = drawnStartPoint;
    this.modelingData = modelingData;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    if (BitIOHelper.notNullToBuffer(buffer, drawnStartPoint)) {
      buffer.writeInt(drawnStartPoint.getX());
      buffer.writeInt(drawnStartPoint.getY());
      buffer.writeInt(drawnStartPoint.getZ());
    }
    modelingData.toBytes(buffer);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketReadBlockStates> {

    @Override
    public void receive(PacketReadBlockStates message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            BitAreaHelper.readBlockStates(stack, player, player.level(), message.pos, message.hit,
                message.drawnStartPoint, message.modelingData);
          }
        }
      });

    }

  }

}