package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.GuiIDs;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketOpenInventoryGui extends PacketBoolean {

  public static final PacketType<PacketOpenInventoryGui> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "open_inventory_gui"), PacketOpenInventoryGui::new);

  public PacketOpenInventoryGui(FriendlyByteBuf friendlyByteBuf) {
    super(friendlyByteBuf);
  }

  public PacketOpenInventoryGui(boolean openVanilla) {
    super(openVanilla);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketOpenInventoryGui> {

    @Override
    public void receive(PacketOpenInventoryGui packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          player.inventoryMenu.removed(player);
          if (packet.value) {
            player.containerMenu = player.inventoryMenu;
          } else {
            player.openGui(ExtraBitManipulation.instance, GuiIDs.CHISELED_ARMOR_SLOTS.getID(),
                player.world, 0, 0, 0);
          }
        }
      });

    }

  }

}