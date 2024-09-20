package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketSetTabAndStateBlockButton implements FabricPacket {

  public static final PacketType<PacketSetTabAndStateBlockButton> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
              Reference.MOD_ID, "set_tab_and_state_block_button"),
          PacketSetTabAndStateBlockButton::new);

  private final int tabSelected;
  private final boolean stateButtonSelected;

  public PacketSetTabAndStateBlockButton(FriendlyByteBuf buffer) {
    this.tabSelected = buffer.readInt();
    this.stateButtonSelected = buffer.readBoolean();
  }

  public PacketSetTabAndStateBlockButton(int tabSelected, boolean stateButtonSelected) {
    this.tabSelected = tabSelected;
    this.stateButtonSelected = stateButtonSelected;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeInt(tabSelected);
    buf.writeBoolean(stateButtonSelected);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetTabAndStateBlockButton> {

    @Override
    public void receive(PacketSetTabAndStateBlockButton packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isModelingToolStack(stack)) {
            CompoundTag nbt = ItemStackHelper.getNBT(stack);
            nbt.putInt(NBTKeys.TAB_SETTING, packet.tabSelected);
            nbt.putBoolean(NBTKeys.BUTTON_STATE_BLOCK_SETTING, packet.stateButtonSelected);
            player.inventoryMenu.sendAllDataToRemote();
          }
        }
      });
    }
  }

}