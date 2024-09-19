package com.phylogeny.extrabitmanipulation.armor.capability;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor.ArmorType;
import com.phylogeny.extrabitmanipulation.mixin.accessors.ServerLevelAccessor;
import com.phylogeny.extrabitmanipulation.packet.PacketSyncArmorSlot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ChiseledArmorSlotsHandler extends ItemStackHandler
    implements ICapabilityProvider, IChiseledArmorSlotsHandler {
  public static final int COUNT_TYPES = 4;
  public static final int COUNT_SETS = 4;
  public static final int COUNT_SLOTS_TOTAL = COUNT_TYPES * COUNT_SETS;
  private boolean[] syncedSlots;
  private final boolean[] hasArmorSet;
  private final boolean[] hasArmorType;
  private boolean hasArmor;
  private ModelPartConcealer modelPartConcealer;

  //  @CapabilityInject(IChiseledArmorSlotsHandler.class)
  public static final Capability<IChiseledArmorSlotsHandler> ARMOR_SLOTS_CAP = null;

  public ChiseledArmorSlotsHandler() {
    super(COUNT_SLOTS_TOTAL);
    syncedSlots = new boolean[COUNT_SLOTS_TOTAL];
    hasArmorSet = new boolean[COUNT_SETS];
    hasArmorType = new boolean[COUNT_TYPES];
  }

  @Override
  public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
    return ICapabilityProvider.super.getCapability(cap);
  }

//  @Override
//  public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
//    return ARMOR_SLOTS_CAP != null && capability == ARMOR_SLOTS_CAP;
//  }
//
//  @Override
//  public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
//    return capability == ARMOR_SLOTS_CAP ? ARMOR_SLOTS_CAP.<T>cast(this) : null;
//  }

  public static LazyOptional<IChiseledArmorSlotsHandler> getCapability(Player player) {
    return player.getCapability(ARMOR_SLOTS_CAP, null);
  }

  @Override
  public void syncAllSlots(Player player) {
    Collection<Player> players = null;
    for (int i = 0; i < COUNT_SLOTS_TOTAL; i++) {
      if (syncedSlots[i]) {
        continue;
      }

      if (players == null) {
        players = new HashSet<>();

        PersistentEntitySectionManager<Entity> tracker =
            ((ServerLevelAccessor) player.level()).getEntityManager();

        List<Player> trackedEntities = Collections.synchronizedList(new ArrayList<>());
        tracker.getEntityGetter().get(new EntityTypeTest<>() {
          @Override
          public @org.jetbrains.annotations.Nullable Entity tryCast(Entity object) {
            if (!(object instanceof Player)) {
              return null;
            }
            return object;
          }

          @Override
          public Class<? extends Entity> getBaseClass() {
            return Player.class;
          }
        }, object -> {
          trackedEntities.add((Player) object);
          return AbortableIterationConsumer.Continuation.CONTINUE;
        });
        players.addAll(trackedEntities);
        players.add(player);
      }

      for (Player player2 : players) {
        ExtraBitManipulation.packetNetwork.sendTo(
            new PacketSyncArmorSlot(player.getUniqueID(), getStackInSlot(i), i),
            (EntityPlayerMP) player2);
      }

      syncedSlots[i] = true;
    }
  }

  @Override
  public void markAllSlotsDirty() {
    syncedSlots = new boolean[COUNT_SLOTS_TOTAL];
  }

  @Override
  public void markSlotDirty(int index) {
    syncedSlots[index] = false;
  }

  @Override
  @Nullable
  public ModelPartConcealer getAndApplyModelPartConcealer(ModelPart model) {
    return modelPartConcealer == null ? null : modelPartConcealer.copy().applyToModel(model);
  }

  @Override
  public void onContentsChanged(int slot) {
    markSlotDirty(slot);
    int index = slot / COUNT_TYPES;
    hasArmorSet[index] = false;
    int start = index * COUNT_TYPES;
    for (int i = start; i < start + COUNT_TYPES; i++) {
      if (!stacks.get(i).isEmpty()) {
        hasArmorSet[index] = true;
        break;
      }
    }
    hasArmor = false;
    for (boolean setHasArmor : hasArmorSet) {
      if (setHasArmor) {
        hasArmor = true;
        break;
      }
    }
    index = slot % COUNT_TYPES;
    hasArmorType[index] = !stacks.get(index).isEmpty();
    ModelPartConcealer modelPartConcealer = new ModelPartConcealer();
    for (int i = 0; i < COUNT_SLOTS_TOTAL && !modelPartConcealer.isFull(); i++) {
      ItemStack stack = stacks.get(i);
      CompoundTag nbt = stack.getTag();
      if (nbt == null) {
        continue;
      }

      modelPartConcealer.merge(ModelPartConcealer.loadFromNBT(nbt));
    }
    this.modelPartConcealer = !modelPartConcealer.isEmpty() ? modelPartConcealer : null;
  }

  public static int findNextArmorSetIndex(int startIndex) {
    int indexNext = startIndex;
    do {
      indexNext = (indexNext + 1) % (ChiseledArmorSlotsHandler.COUNT_TYPES + 1);
      if (setHasArmor(indexNext)) {
        return indexNext;
      }
    }
    while (indexNext != startIndex);
    return -1;
  }

  public static boolean setHasArmor(int index) {
    for (ArmorType armorType : ArmorType.values()) {
      if (ItemStackHelper.isChiseledArmorStack(
          ItemStackHelper.getChiseledArmorStack(ClientHelper.getPlayer(), armorType, index))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasArmor() {
    return hasArmor;
  }

  @Override
  public boolean hasArmorSet(int indexSet) {
    return hasArmorSet[indexSet];
  }

  @Override
  public boolean hasArmorType(int indexType) {
    return hasArmorType[indexType];
  }

  @Override
  public int getSlotLimit(int slot) {
    return 1;
  }

  @Override
  public void setSize(int size) {
    super.setSize(COUNT_SLOTS_TOTAL);
  }


  @Override
  public boolean isItemValid(int slot, @NotNull ItemStack stack) {
    return isItemValidStatic(slot, stack);
  }

  public static boolean isItemValidStatic(int slot, ItemStack stack) {
    return ItemStackHelper.isChiseledArmorStack(stack) &&
        ((ItemChiseledArmor) stack.getItem()).armorType.ordinal() == slot % COUNT_TYPES
        && ItemStackHelper.isChiseledArmorNotEmpty(stack);
  }

  @Override
  public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
    if (stack.isEmpty() || isItemValidStatic(slot, stack)) {
      super.setStackInSlot(slot, stack);
    }
  }

  @Override
  @Nonnull
  public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
    return isItemValidStatic(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
  }

}