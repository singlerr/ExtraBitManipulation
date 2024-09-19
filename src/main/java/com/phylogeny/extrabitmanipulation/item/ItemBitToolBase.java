package com.phylogeny.extrabitmanipulation.item;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.config.ConfigBitToolSettingBase;
import com.phylogeny.extrabitmanipulation.config.ConfigProperty;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import java.awt.Color;
import java.util.List;
import mod.chiselsandbits.api.KeyBindingContext;
import mod.chiselsandbits.api.ModKeyBinding;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@KeyBindingContext(value = {"menuitem"}, applyToSubClasses = true)
public class ItemBitToolBase extends ItemExtraBitManipulationBase {

  public ItemBitToolBase(Item.Properties properties, String name) {
    super(properties, name);
  }

  public boolean initialize(ItemStack stack) {
    if (stack.hasTag()) {
      return false;
    }

    stack.setTag(new CompoundTag());
    return true;
  }

  // Forge only features, so disabling it.
//  @Override
//  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
//                                             boolean slotChanged) {
//    return slotChanged;
//  }

  protected void damageTool(ItemStack stack, Player player) {
    ConfigProperty config = (ConfigProperty) Configs.itemPropertyMap.get(this);
    if (config.takesDamage) {
      stack.hurtAndBreak(1, player, (e) -> {
        if (stack.getDamageValue() > config.maxDamage) {
          player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
          player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
//          ForgeEventFactory.onPlayerDestroyItem(player, stack, EnumHand.MAIN_HAND);
        }
      });

    }
  }

  protected void initInt(CompoundTag nbt, String nbtKey, int initInt) {
    if (!nbt.contains(nbtKey)) {
      nbt.putInt(nbtKey, initInt);
    }
  }

  protected void initBoolean(CompoundTag nbt, String nbtKey, boolean initBoolean) {
    if (!nbt.contains(nbtKey)) {
      nbt.putBoolean(nbtKey, initBoolean);
    }
  }

  public static MutableComponent colorSettingText(String text, ConfigBitToolSettingBase setting) {
    return Component.literal(text)
        .withStyle((setting.isPerTool() ? ChatFormatting.GREEN : ChatFormatting.BLUE));
  }

  public static void addColorInformation(List<Component> tooltip, boolean shiftDown) {
    if (shiftDown) {
      tooltip.add(Component.literal("Blue = data stored/accessed per client")
          .withColor(Color.BLUE.getRGB()));
      tooltip.add(Component.literal("Green = data stored/accessed per tool")
          .withColor(Color.GREEN.getRGB()));
      tooltip.add(Component.empty());
    }
  }

  public static void addKeyInformation(List<Component> tooltip, boolean hasSettings) {
    if (hasSettings) {
      tooltip.add(Component.literal("Hold SHIFT for settings."));
    }

    tooltip.add(Component.literal("Hold CONTROL for controls."));
    tooltip.add(Component.literal("Use the Chisels & Bits radial").withStyle(ChatFormatting.AQUA));
    tooltip.add(Component.literal("    menu key [" +
        (ChiselsAndBitsAPIAccess.apiInstance == null ? "null"
            : ChiselsAndBitsAPIAccess.apiInstance.getKeyBinding(ModKeyBinding.MODE_MENU)
            .getTranslatedKeyMessage().getString()) + "] or the").withStyle(ChatFormatting.AQUA));
    tooltip.add(Component.literal("    controls listed above").withStyle(ChatFormatting.AQUA));
    tooltip.add(Component.literal("    to change tool settings.").withStyle(ChatFormatting.AQUA));
  }

  public static void addKeybindReminders(List<Component> tooltip,
                                         KeyBindingsExtraBitManipulation... keyBinds) {
    tooltip.add(Component.empty());
    tooltip.add(Component.literal(">>Replacable with " +
            (keyBinds.length > 1 ? "Keybinds" : "a Keybind") + "<<")
        .withStyle(ChatFormatting.DARK_AQUA));
  }

  public static String getColoredKeyBindText(KeyBindingsExtraBitManipulation keyBind) {
    return ChatFormatting.DARK_AQUA + keyBind.getText() + ChatFormatting.GRAY;
  }

}
