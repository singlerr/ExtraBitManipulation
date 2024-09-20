package com.phylogeny.extrabitmanipulation.packet;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.ArrayList;
import java.util.List;
import mod.chiselsandbits.api.IBitBag;
import mod.chiselsandbits.api.ItemType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class PacketThrowBit extends PacketEmpty {

  public static final PacketType<PacketThrowBit> PACKET_TYPE =
      PacketType.create(new ResourceLocation(
          Reference.MOD_ID, "throw_bit"), PacketThrowBit::new);

  public PacketThrowBit(FriendlyByteBuf buf) {
    super(buf);
  }

  public PacketThrowBit() {
  }

  @Override
  public PacketType<?> getType() {
    return PACKET_TYPE;
  }

  public static class Handler implements ServerPlayNetworking.PlayPacketHandler<PacketThrowBit> {
    @Override
    public void receive(PacketThrowBit packet, ServerPlayer player, PacketSender responseSender) {
      MinecraftServer mainThread = player.level().getServer();
      mainThread.execute(new Runnable() {
        @Override
        public void run() {
          if (!Configs.thrownBitsEnabled) {
            MinecraftServer server = player.getServer();
            player.sendSystemMessage(Component.literal("Bit throwing is disabled"
                + (server != null &&
                (server.isDedicatedServer() || server.getPlayerList().getPlayers().size() > 1) ?
                " on this server." : ".")));
            return;
          }
          ItemStack stack = player.getMainHandItem();
          boolean isBit =
              ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.CHISELED_BIT;
          if (!isBit) {
            IBitBag bitBag = ChiselsAndBitsAPIAccess.apiInstance.getBitbag(stack);
            if (bitBag == null) {
              return;
            }

            stack = ItemStack.EMPTY;
            int start, end, inc;
            if (Configs.bitBagBitSelectionMode == BitBagBitSelectionMode.END_TO_BEGINNING) {
              start = bitBag.getSlots() - 1;
              end = inc = -1;
            } else {
              start = 0;
              end = bitBag.getSlots();
              inc = 1;
            }
            List<Pair<ItemStack, Integer>> stacks = new ArrayList<Pair<ItemStack, Integer>>();
            boolean isRandom = Configs.bitBagBitSelectionMode == BitBagBitSelectionMode.RANDOM;
            for (int i = start; i != end; i += inc) {
              stack = bitBag.extractItem(i, 1, isRandom);
              if (!stack.isEmpty()) {
                if (isRandom) {
                  stacks.add(new ImmutablePair<ItemStack, Integer>(stack, i));
                } else {
                  break;
                }
              }
            }
            if (isRandom && !stacks.isEmpty()) {
              Pair<ItemStack, Integer> pair =
                  stacks.get(player.level().random.nextInt(stacks.size()));
              stack = pair.getLeft();
              bitBag.extractItem(pair.getRight(), 1, false);
            }
            if (stack.isEmpty()) {
              return;
            }
          }

          EntityBit entityBit = new EntityBit(player.level(), player, stack);
          entityBit.setAim(player, player.getXRot(), player.getYRot(),
              isBit ? Configs.thrownBitVelocity : Configs.thrownBitVelocityBitBag,
              isBit ? Configs.thrownBitInaccuracy : Configs.thrownBitInaccuracyBitBag);
          player.level().addFreshEntity(entityBit);
          player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
              SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F,
              0.4F / (player.level().random.nextFloat() * 0.4F + 0.8F));
          if (isBit && !player.isCreative()) {
            stack.shrink(1);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
          }
        }
      });
    }
  }

  public enum BitBagBitSelectionMode {
    RANDOM, BEGINNING_TO_END, END_TO_BEGINNING
  }

}