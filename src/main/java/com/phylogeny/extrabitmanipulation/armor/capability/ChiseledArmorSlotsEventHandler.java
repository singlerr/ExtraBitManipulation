package com.phylogeny.extrabitmanipulation.armor.capability;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiButtonArmorSlots;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiInventoryArmorSlots;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.mixin.events.PlayerTickEvents;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandReplaceItem;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class ChiseledArmorSlotsEventHandler {
  private static final Map<String, Integer> COMMAND_VANITY_SLOTS = Maps.newHashMap();

  static {
    for (int i = 0; i < ChiseledArmorSlotsHandler.COUNT_SLOTS_TOTAL; i++) {

      COMMAND_VANITY_SLOTS.put("slot.vanity.set" + i / ChiseledArmorSlotsHandler.COUNT_TYPES +
          "." + EquipmentSlot.values()[5 - i % ChiseledArmorSlotsHandler.COUNT_TYPES].toString()
          .toLowerCase(), i);
    }
  }

  public static void addCommandTabCompletions() {
    ReflectionExtraBitManipulation.addShortcutsToCommandReplaceItem(
        COMMAND_VANITY_SLOTS);
  }

  public static void registerEventListeners() {
    TODO("Register")
    AttachCapabilitiesEvent.REGISTER_CAPS.register(
        event -> onEntityConstruct((AttachCapabilitiesEvent<Entity>) event));
    ClientEntityEvents.ENTITY_LOAD.register(ChiseledArmorSlotsEventHandler::markPlayerSlotsDirty);
    EntityTrackingEvents.START_TRACKING.register(
        ChiseledArmorSlotsEventHandler::markPlayerSlotsDirty);
    PlayerTickEvents.END.register(ChiseledArmorSlotsEventHandler::syncPlayerSlots);
    ServerPlayerEvents.COPY_FROM.register(ChiseledArmorSlotsEventHandler::syncDataForClonedPlayers);
    ServerLivingEntityEvents.AFTER_DEATH.register(ChiseledArmorSlotsEventHandler::dropArmorOnDeath);
    ScreenEvents.AFTER_INIT.register(ChiseledArmorSlotsEventHandler::addArmorButtonToGui);
  }

  private static void onEntityConstruct(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
      event.addCapability(new ResourceLocation(Reference.MOD_ID, "chiseled_armor_slots"),
          new ChiseledArmorSlotsHandler());
    }
  }

  private static void markPlayerSlotsDirty(Entity entity, Level level) {
    markPlayerSlotsDirty(entity);
  }

  private static void markPlayerSlotsDirty(Entity trackedEntity, ServerPlayer player) {
    markPlayerSlotsDirty(trackedEntity);
  }

  private static void markPlayerSlotsDirty(Entity player) {
    if (!(player instanceof ServerPlayer)) {
      return;
    }

    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
    if (cap != null) {
      cap.markAllSlotsDirty();
    }
  }

  private static void syncPlayerSlots(Player player) {
    if (!(player instanceof ServerPlayer)) {
      return;
    }

    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
    if (cap != null) {
      cap.syncAllSlots(player);
    }
  }

  private static void syncDataForClonedPlayers(ServerPlayer oldPlayer, ServerPlayer newPlayer,
                                               boolean alive) {
    if (alive) {
      return;
    }


    IChiseledArmorSlotsHandler capOld =
        ChiseledArmorSlotsHandler.getCapability(oldPlayer);
    if (capOld != null) {
      IChiseledArmorSlotsHandler capNew =
          ChiseledArmorSlotsHandler.getCapability(newPlayer);
      if (capNew != null) {
        ((ChiseledArmorSlotsHandler) capNew).deserializeNBT(
            ((ChiseledArmorSlotsHandler) capOld).serializeNBT());
      }
    }
  }

  private static void dropArmorOnDeath(LivingEntity entity, DamageSource damageSource) {
    if (!(entity instanceof Player player)) {
      return;
    }
    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
    if (cap == null) {
      return;
    }
    for (int i = 0; i < cap.getSlots(); i++) {
      if (!cap.getStackInSlot(i).isEmpty()) {
        player.drop(cap.getStackInSlot(i).copy(), true, false);
        cap.setStackInSlot(i, ItemStack.EMPTY);
      }
    }
  }

  private static void vanitySlotCommandAccess(CommandEvent event) {
    TODO("Not Implemented");
    if (!(event.getCommand() instanceof CommandReplaceItem)) {
      return;
    }
    CommandContext<CommandSourceStack> a;
    a.
        String[] args = event.getParameters();
    if (args.length < 4 || !"entity".equals(args[0])) {
      return;
    }

    int i = 2;
    String slotName = args[i];
    if (!slotName.contains("vanity")) {
      return;
    }

    event.setCanceled(true);
    if (!COMMAND_VANITY_SLOTS.containsKey(slotName)) {
      notifyCommandListener(event, "commands.generic.parameter.invalid", slotName);
      return;
    }
    ICommandSender sender = event.getSender();
    int slot = COMMAND_VANITY_SLOTS.get(args[i++]);
    Item item;
    try {
      item = CommandBase.getItemByText(sender, args[i++]);
    } catch (NumberInvalidException e) {
      notifyCommandListener(event, e);
      return;
    }
    ItemStack stack = new ItemStack(item);
    if (args.length > i) {
      String nbtTagJson = CommandBase.buildString(args, args.length > 5 ? 6 : 4);
      try {
        stack.setTagCompound(TagParser.parseTag(nbtTagJson));
      } catch (NBTException e) {
        notifyCommandListener(event, "commands.replaceitem.tagError", e.getMessage());
        return;
      }
    } else {
      notifyCommandListener(event, "nbt");
      return;
    }
    if (!stack.isEmpty() && !ChiseledArmorSlotsHandler.isItemValidStatic(slot, stack)) {
      notifyCommandListener(event, "commands.replaceitem.failed", slotName, 1,
          stack.getDisplayName());
      return;
    }
    Entity entity;
    try {
      entity = CommandBase.getEntity(sender.getServer(), sender, args[1]);
    } catch (CommandRuntimeException e) {
      notifyCommandListener(event, e);
      return;
    }
    if (!(entity instanceof EntityPlayer player)) {
      notifyCommandListener(event, "player");
      return;
    }
    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
    if (cap == null) {
      notifyCommandListener(event, "capability");
      return;
    }
    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);
    player.openContainer.detectAndSendChanges();
    cap.setStackInSlot(slot, stack);
    player.openContainer.detectAndSendChanges();
    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
    notifyCommandListener(event, "commands.replaceitem.success", slotName, 1,
        stack.isEmpty() ? "Air" : stack.getDisplayName());
  }

  private void notifyCommandListener(CommandEvent event, String suffix) {
    notifyCommandListener(event, "command." + Reference.MOD_ID + ".vanity.failure." + suffix,
        new Object[0]);
  }

  private void notifyCommandListener(CommandEvent event, CommandRuntimeException e) {
    notifyCommandListener(event, e.getMessage(), e.getErrorObjects());
  }

  private void notifyCommandListener(CommandEvent event, String translationKey,
                                     Object... translationArgs) {
    CommandBase.notifyCommandListener(event.getSender(), event.getCommand(), translationKey,
        translationArgs);
  }

  @Environment(EnvType.CLIENT)
  private static void addArmorButtonToGui(Minecraft client, Screen screen, int scaledWidth,
                                          int scaledHeight) {
    Player player = ClientHelper.getPlayer();
    if (player == null || player.isCreative()) {
      return;
    }

    boolean isArmorSlots = screen instanceof GuiInventoryArmorSlots;
    if (!isArmorSlots && !(screen instanceof InventoryScreen)) {
      return;
    }

    ContainerScreen gui = (ContainerScreen) screen;
    boolean add = false;
    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player);
    if (cap != null) {
      for (int i = 0; i < cap.getSlots(); i++) {
        if (!cap.getStackInSlot(i).isEmpty()) {
          add = true;
          break;
        }
      }
    }
    if (!add) {
      ArmorButtonVisibiltyMode mode = Configs.armorButtonVisibiltyMode;
      add = isArmorSlots || mode == ArmorButtonVisibiltyMode.ALWAYS;
      if (!isArmorSlots && mode != ArmorButtonVisibiltyMode.NEVER &&
          mode != ArmorButtonVisibiltyMode.ALWAYS) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
          ItemStack stack = player.inventory.getStackInSlot(i);
          if (stack.isEmpty()) {
            continue;
          }

          if (mode == ArmorButtonVisibiltyMode.ANY_ITEMS ||
              mode == ArmorButtonVisibiltyMode.CHISELED_ARMOR_ITEMS) {
            if (mode == ArmorButtonVisibiltyMode.ANY_ITEMS ||
                ItemStackHelper.isChiseledArmorStack(stack)) {
              add = true;
              break;
            }
            continue;
          }
          Item item = stack.getItem();
          if (item != null) {
            ResourceLocation name = item.getRegistryName();
            if (name != null && (name.getResourceDomain().equals(Reference.MOD_ID)
                || (mode == ArmorButtonVisibiltyMode.EBM_OR_CNB_ITEMS &&
                name.getResourceDomain().equals(ChiselsAndBitsReferences.MOD_ID)))) {
              add = true;
              break;
            }
          }
        }
      }
    }
    if (add) {
      GuiButtonArmorSlots button =
          new GuiButtonArmorSlots(gui, isArmorSlots ? "Back" : "Chiseled Armor");
      if (isArmorSlots) {
        button.setHelpMode(((GuiInventoryArmorSlots) gui).isInHelpMode());
      }

      event.getButtonList().add(button);
    }
  }

  @Environment(EnvType.CLIENT)
  @SubscribeEvent
  public void resetArmorButtonPosition(GuiScreenEvent.KeyboardInputEvent.Post event) {
    if (!Keyboard.isKeyDown(Keyboard.KEY_R) || !GuiButtonArmorSlots.shouldMoveButton() ||
        !(event.getGui() instanceof GuiInventoryArmorSlots) &&
            !(event.getGui() instanceof GuiInventory) ||
        (Configs.armorButtonX.isAtDefaultValue() && Configs.armorButtonY.isAtDefaultValue())) {
      return;
    }

    List<GuiButton> buttonList = ReflectionExtraBitManipulation.getButtonList(event.getGui());
    for (GuiButton button : buttonList) {
      if (button instanceof GuiButtonArmorSlots) {
        BitToolSettingsHelper.setArmorButtonPosition(Configs.armorButtonX.getDefaultValue(),
            Configs.armorButtonY.getDefaultValue());
        ((GuiButtonArmorSlots) button).setPosition();
        break;
      }
    }
  }

  public enum ArmorButtonVisibiltyMode {
    CHISELED_ARMOR_ITEMS, EBM_ITEMS, EBM_OR_CNB_ITEMS, ANY_ITEMS, ALWAYS, NEVER
  }

}