package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelWriteData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
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

public class PacketCreateModel extends PacketBlockInteraction {

  public static final PacketType<PacketCreateModel> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "create_model"), PacketCreateModel::new);

  private ModelWriteData modelingData = new ModelWriteData();

  public PacketCreateModel(FriendlyByteBuf buffer) {
    super(buffer);
    modelingData.fromBytes(buffer);
  }

  public PacketCreateModel(BlockPos pos, Direction side, ModelWriteData modelingData) {
    super(pos, side, new Vec3(0, 0, 0));
    this.modelingData = modelingData;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    modelingData.toBytes(buffer);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketCreateModel> {
    @Override
    public void receive(PacketCreateModel message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            ((ItemModelingTool) stack.getItem()).createModel(stack, player, player.level(),
                message.pos, message.side, message.modelingData);
          }
        }
      });

    }


  }

}