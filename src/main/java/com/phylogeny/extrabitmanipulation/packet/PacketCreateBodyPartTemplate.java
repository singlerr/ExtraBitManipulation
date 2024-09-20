package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorBodyPartTemplateData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
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

public class PacketCreateBodyPartTemplate extends PacketBlockInteraction {

  public static final PacketType<PacketCreateBodyPartTemplate> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "create_body_part_template"), PacketCreateBodyPartTemplate::new);

  private ArmorBodyPartTemplateData templateData = new ArmorBodyPartTemplateData();

  public PacketCreateBodyPartTemplate(FriendlyByteBuf buf) {
    super(buf);
    templateData.fromBytes(buf);
  }

  public PacketCreateBodyPartTemplate(BlockPos pos, Direction side, Vec3 hit,
                                      ArmorBodyPartTemplateData templateData) {
    super(pos, side, hit);
    this.templateData = templateData;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    templateData.toBytes(buffer);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements
      ServerPlayNetworking.PlayPacketHandler<PacketCreateBodyPartTemplate> {
    @Override
    public void receive(PacketCreateBodyPartTemplate message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = player.getMainHandItem();
          if (ItemStackHelper.isChiseledArmorStack(stack)) {
            ItemChiseledArmor.createBodyPartTemplate(player, player.level(), message.pos,
                message.side, message.hit, message.templateData);
          }
        }
      });

    }


  }

}