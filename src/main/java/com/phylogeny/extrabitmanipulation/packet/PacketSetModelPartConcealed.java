package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ModelMovingPart;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketSetModelPartConcealed extends PacketArmorSlotInt {

  public static final PacketType<PacketSetModelPartConcealed> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "set_model_part_concealed"), PacketSetModelPartConcealed::new);

  private final boolean isOverlay;
  private final boolean remove;

  public PacketSetModelPartConcealed(FriendlyByteBuf buffer) {
    super(buffer);
    isOverlay = buffer.readBoolean();
    remove = buffer.readBoolean();
  }

  public PacketSetModelPartConcealed(@Nullable ArmorType armorType, int indexArmorSet,
                                     ModelMovingPart part, boolean isOverlay, boolean remove) {
    super(armorType, indexArmorSet, part.ordinal());
    this.isOverlay = isOverlay;
    this.remove = remove;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    super.write(buffer);
    buffer.writeBoolean(isOverlay);
    buffer.writeBoolean(remove);
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler
      implements ServerPlayNetworking.PlayPacketHandler<PacketSetModelPartConcealed> {

    @Override
    public void receive(PacketSetModelPartConcealed message, ServerPlayer player,
                        PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          ItemStack stack = getArmorStack(player, message);
          if (stack.isEmpty()) {
            return;
          }

          CompoundTag nbt = stack.getTag();
          if (nbt == null) {
            return;
          }

          ModelPartConcealer modelPartConcealer = ModelPartConcealer.loadFromNBT(nbt);
          if (modelPartConcealer == null) {
            modelPartConcealer = new ModelPartConcealer();
          }

          modelPartConcealer.addOrRemove(message.value, message.isOverlay, message.remove);
          modelPartConcealer.saveToNBT(nbt);
          player.inventoryMenu.sendAllDataToRemote();
          IChiseledArmorSlotsHandler cap =
              ChiseledArmorSlotsHandler.getCapability(player).orElse(null);
          if (cap != null) {
            cap.onContentsChanged(message.armorType.getSlotIndex(message.indexArmorSet));
          }
        }
      });
    }


  }

}