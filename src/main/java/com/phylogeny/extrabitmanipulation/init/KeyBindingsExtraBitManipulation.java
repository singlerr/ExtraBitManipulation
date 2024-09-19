package com.phylogeny.extrabitmanipulation.init;

import com.mojang.blaze3d.platform.InputConstants;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.mixin.accessors.KeyMappingAccessor;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import committee.nova.mkb.api.IKeyBinding;
import committee.nova.mkb.api.IKeyConflictContext;
import committee.nova.mkb.keybinding.KeyConflictContext;
import committee.nova.mkb.keybinding.KeyModifier;
import mod.chiselsandbits.api.ItemType;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public enum KeyBindingsExtraBitManipulation implements IKeyConflictContext {
  EDIT_DESIGN("design", InputConstants.KEY_R) {
    @Override
    public boolean isActive() {
      return ItemStackHelper.isDesignStack(getHeldItemMainhandSafe());
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return conflictsInGame(other);
    }
  },

  THROW_BIT("throw.bit", InputConstants.KEY_R) {
    @Override
    public boolean isActive() {
      return ChiselsAndBitsAPIAccess.apiInstance.getItemType(getHeldItemMainhandSafe()) ==
          ItemType.CHISELED_BIT;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return conflictsInGame(other);
    }
  },

  THROW_BIT_BIT_BAG("throw.bit.bitbag", InputConstants.KEY_R) {
    @Override
    public boolean isActive() {
      return ChiselsAndBitsAPIAccess.apiInstance.getItemType(getHeldItemMainhandSafe()) ==
          ItemType.BIT_BAG;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return conflictsInGame(other);
    }
  },

  OPEN_CHISELED_ARMOR_GUI("chiseledarmor", InputConstants.KEY_G) {
    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return conflictsInGame(other);
    }
  },

  OPEN_CHISELED_ARMOR_SLOTS_GUI("chiseledarmor.slots", InputConstants.KEY_H) {
    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
      return conflictsInGame(other);
    }
  },

  OPEN_BIT_MAPPING_GUI("bitmapping", InputConstants.KEY_R, false) {
    @Override
    public boolean isActive() {
      return ItemStackHelper.isModelingToolStack(getHeldItemMainhandSafe());
    }
  },

  SHIFT("Shift", InputConstants.UNKNOWN.getValue(), true) {
    @Override
    public boolean isKeyDown() {
      return isKeyDown(Screen.hasShiftDown());
    }

    @Override
    public boolean isActive() {
      ItemStack stack = getHeldItemMainhandSafe();
      return ItemStackHelper.isSculptingToolStack(stack) ||
          ItemStackHelper.isModelingToolStack(stack) || ItemStackHelper.isBitWrenchStack(stack);
    }
  },

  CONTROL("Control", InputConstants.UNKNOWN.getValue(), true) {
    @Override
    public boolean isKeyDown() {
      return isKeyDown(Screen.hasControlDown());
    }

    @Override
    public boolean isActive() {
      ItemStack stack = getHeldItemMainhandSafe();
      return ItemStackHelper.isSculptingToolStack(stack) ||
          ItemStackHelper.isModelingToolStack(stack) || ItemStackHelper.isChiseledArmorStack(stack);
    }
  },

  ALT("Alt", InputConstants.KEY_X, false) {
    @Override
    public boolean isKeyDown() {
      return isKeyDown(Screen.hasAltDown());
    }

    @Override
    public boolean isActive() {
      ItemStack stack = getHeldItemMainhandSafe();
      return ItemStackHelper.isSculptingToolStack(stack) ||
          ItemStackHelper.isChiseledArmorStack(stack);
    }

    @Override
    public String getText() {

      return ((KeyMappingAccessor) keyBinding).getKey() == InputConstants.UNKNOWN ?
          description.toUpperCase() :
          ("[" + keyBinding.getName() + "]");
    }
  };

  protected KeyMapping keyBinding;
  protected String description = "";
  private final boolean anyConflicts;

  KeyBindingsExtraBitManipulation(String description, int defaultKeyCode) {
    this(description, defaultKeyCode, false);
  }

  KeyBindingsExtraBitManipulation(String description, int defaultKeyCode,
                                  boolean anyConflicts) {
    this.description = description;
    this.anyConflicts = anyConflicts;

    keyBinding = new KeyMapping("keybinding." + Reference.MOD_ID + "." + description.toLowerCase(),
        InputConstants.Type.KEYSYM, defaultKeyCode, "itemGroup." + Reference.MOD_ID);
    if (keyBinding instanceof IKeyBinding binding) {
      binding.setKeyModifierAndCode(getModifier(), InputConstants.getKey(defaultKeyCode, 0));
    }
  }

  public boolean isKeyDown() {
    return getKeyBinding().isDown();
  }

  protected boolean isKeyDown(boolean defaultCheck) {
    return ((KeyMappingAccessor) getKeyBinding()).getKey() == InputConstants.UNKNOWN ?
        defaultCheck :
        getKeyBinding().isDown();
  }

  public static void init() {
    for (KeyBindingsExtraBitManipulation keyBinding : values()) {
      keyBinding.registerKeyBinding();
    }
  }

  protected KeyModifier getModifier() {
    return KeyModifier.NONE;
  }

  private void registerKeyBinding() {
    KeyBindingHelper.registerKeyBinding(keyBinding);
  }

  public String getText() {
    return keyBinding.isDefault() ? description.toUpperCase() :
        ("[" + keyBinding.getName() + "]");
  }

  public KeyMapping getKeyBinding() {
    return keyBinding;
  }

  @Override
  public boolean conflicts(IKeyConflictContext other) {
    return conflictsInGame(other) || other == SHIFT || other == CONTROL ||
        (anyConflicts && (other == ALT || other == OPEN_BIT_MAPPING_GUI));
  }

  protected boolean conflictsInGame(IKeyConflictContext other) {
    return other == this || other == KeyConflictContext.IN_GAME;
  }

  private static ItemStack getHeldItemMainhandSafe() {
    return ClientHelper.getPlayer() == null ? ItemStack.EMPTY : ClientHelper.getHeldItemMainhand();
  }

}