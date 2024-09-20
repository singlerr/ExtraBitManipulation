package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.SculptingData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PacketSculpt extends PacketBlockInteraction {

  public static final PacketType<PacketSculpt> PACKET_TYPE = PacketType.create(new ResourceLocation(
      Reference.MOD_ID, "sculpt"), PacketSculpt::new);

  private Vec3 drawnStartPoint;
  private SculptingData sculptingData = new SculptingData();

  public PacketSculpt(FriendlyByteBuf buffer) {
    super(buffer);
    if (buffer.readBoolean()) {
      drawnStartPoint = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    sculptingData.fromBytes(buffer);
  }

  public PacketSculpt(BlockPos pos, Direction side, Vec3 hit, Vec3 drawnStartPoint,
                      SculptingData sculptingData) {
    super(pos, side, hit);
    this.drawnStartPoint = drawnStartPoint;
    this.sculptingData = sculptingData;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    if (BitIOHelper.notNullToBuffer(buffer, drawnStartPoint)) {
      buffer.writeDouble(drawnStartPoint.x);
      buffer.writeDouble(drawnStartPoint.y);
      buffer.writeDouble(drawnStartPoint.z);
    }
    sculptingData.toBytes(buffer);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketSculpt> {
    @Override
    public void receive(PacketSculpt message, ServerPlayer player, PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isSculptingToolStack(stack)) {
            ((ItemSculptingTool) stack.getItem()).sculptBlocks(stack, player, player.level(),
                message.pos,
                message.side, message.hit, message.drawnStartPoint, message.sculptingData);
          }
        }
      });

    }
  }

}