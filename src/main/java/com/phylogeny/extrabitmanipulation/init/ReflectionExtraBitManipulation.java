package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.mixin.accessors.InventoryScreenAccessor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.commands.arguments.SlotArgument;

public class ReflectionExtraBitManipulation {
  private static Field oldMouseX, oldMouseY, smallArms, buttonList, shortcuts;

  public static void initReflectionFieldsClient() {
    // No op
  }

  public static void initReflectionFieldsCommon() {
    String name = FabricLoader.getInstance().getMappingResolver()
        .mapFieldName("intermediary", "net/minecraft/class_2240", "field_9957",
            "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>");

    try {
      shortcuts = SlotArgument.class.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }


  public static void setCursorPosition(InventoryScreen gui, float mouseX, float mouseY) {
    ((InventoryScreenAccessor) gui).setMouseX(mouseX);
    ((InventoryScreenAccessor) gui).setMouseY(mouseY);
  }

  public static boolean areArmsSmall(PlayerModel<?> model) {
    try {
      return smallArms.getBoolean(model);
    } catch (IllegalArgumentException | IllegalAccessException e) {
    }
    return false;
  }

  public static List<Button> getButtonList(Screen gui) {
    try {
      return (List<Button>) buttonList.get(gui);
    } catch (IllegalArgumentException | IllegalAccessException e) {
    }
    return Collections.emptyList();
  }

  public static void addShortcutsToCommandReplaceItem(
      Map<String, Integer> shortcutsNew) {
    try {
      Map<String, Integer> SHORTCUTS = (Map<String, Integer>) shortcuts.get(null);
      SHORTCUTS.putAll(shortcutsNew);
      shortcuts.set(null, SHORTCUTS);
    } catch (IllegalArgumentException | IllegalAccessException e) {
    }
  }

}