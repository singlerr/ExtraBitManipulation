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

public class PacketOpenChiseledArmorGui extends PacketEmpty {

  public static final PacketType<PacketOpenChiseledArmorGui> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "open_chiseled_armor_gui"), PacketOpenChiseledArmorGui::new);

  public PacketOpenChiseledArmorGui(FriendlyByteBuf buffer) {
    super(buffer);
  }

  public PacketOpenChiseledArmorGui() {
    super();
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketOpenChiseledArmorGui> {

    @Override
    public void receive(PacketOpenChiseledArmorGui packet, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          player.openMenu(ExtraBitManipulation.instance, GuiIDs.CHISELED_ARMOR.getID(),
              player.level(),
              0, 0, 0);
        }
      });
    }
  }

}