package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemBitWrench;
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

public class PacketUseWrench extends PacketBlockInteraction {

  public static final PacketType<PacketUseWrench> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "use_wrench"), PacketUseWrench::new);

  private final boolean bitRequirement;
  private final boolean invertDirection;

  public PacketUseWrench(FriendlyByteBuf buf) {
    super(buf);
    bitRequirement = buf.readBoolean();
    invertDirection = buf.readBoolean();
  }

  public PacketUseWrench(BlockPos pos, Direction side, boolean bitRequirement,
                         boolean invertDirection) {
    super(pos, side, new Vec3(0, 0, 0));
    this.bitRequirement = bitRequirement;
    this.invertDirection = invertDirection;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    buffer.writeBoolean(bitRequirement);
    buffer.writeBoolean(invertDirection);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketUseWrench> {

    @Override
    public void receive(PacketUseWrench message, ServerPlayer player, PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isBitWrenchStack(stack)) {
            ((ItemBitWrench) stack.getItem()).useWrench(stack, player, player.level(), message.pos,
                message.side, message.bitRequirement, message.invertDirection);
          }
        }
      });

    }
  }

}