package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PacketSetCollectionBox extends PacketBlockInteraction {

  public static final PacketType<PacketSetCollectionBox> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_collection_box"), PacketSetCollectionBox::new);

  private final float playerYaw;
  private final boolean useBitGrid;
  private final Direction facingBox;

  public PacketSetCollectionBox(FriendlyByteBuf buffer) {
    super(buffer);
    playerYaw = buffer.readFloat();
    useBitGrid = buffer.readBoolean();
    facingBox = Direction.from3DDataValue(buffer.readInt());
  }

  public PacketSetCollectionBox(float playerYaw, boolean useBitGrid, Direction facingBox,
                                BlockPos pos, Direction facingPlacement, Vec3 hit) {
    super(pos, facingPlacement, hit);
    this.playerYaw = playerYaw;
    this.useBitGrid = useBitGrid;
    this.facingBox = facingBox;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    buffer.writeFloat(playerYaw);
    buffer.writeBoolean(useBitGrid);
    buffer.writeInt(facingBox.ordinal());
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetCollectionBox> {

    @Override
    public void receive(PacketSetCollectionBox message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isChiseledArmorStack(stack)) {
            CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
            ItemChiseledArmor.writeCollectionBoxToNBT(nbt, message.playerYaw, message.useBitGrid,
                message.facingBox, message.pos, message.side, message.hit);
            stack.setTag(nbt);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });
    }

  }

}