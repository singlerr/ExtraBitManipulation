package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PacketSyncArmorSlot implements FabricPacket {

  public static final PacketType<PacketSyncArmorSlot> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "sync_armor_slot"), PacketSyncArmorSlot::new);

  private final UUID playerID;
  private final ItemStack stack;
  private final int index;

  public PacketSyncArmorSlot(FriendlyByteBuf buffer) {
    playerID = UUID.fromString(buffer.readUtf());
    stack = buffer.readItem();
    index = buffer.readInt();
  }

  public PacketSyncArmorSlot(UUID playerID, ItemStack stack, int index) {
    this.playerID = playerID;
    this.stack = stack;
    this.index = index;
  }

  @Override
  public void write(FriendlyByteBuf buf) {
    buf.writeUtf(playerID.toString());
    buf.writeItem(stack);
    buf.writeInt(index);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ClientPlayNetworking.PlayPacketHandler<PacketSyncArmorSlot> {

    @Override
    public void receive(PacketSyncArmorSlot packet, LocalPlayer player,
                        PacketSender responseSender) {
      ClientHelper.getThreadListener().execute(new Runnable() {
        @Override
        public void run() {
          Player player = ClientHelper.getWorld().getPlayerByUUID(packet.playerID);
          if (player == null) {
            return;
          }

          IChiseledArmorSlotsHandler cap =
              ChiseledArmorSlotsHandler.getCapability(player).orElse(null);
          if (cap != null) {
            cap.setStackInSlot(packet.index, packet.stack);
          }
        }
      });
    }

  }

}