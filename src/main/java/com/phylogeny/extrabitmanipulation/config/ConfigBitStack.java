package com.phylogeny.extrabitmanipulation.config;

import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ConfigBitStack extends ConfigBitToolSettingBase<ItemStack> {
  private BlockState valueDefault, stateDefault, stateDefaultDefault;
  private String stringDeafult;

  public ConfigBitStack(ItemStack bitStackDefault) {
    super("", false, false);
    defaultValue = bitStackDefault;
  }

  public ConfigBitStack(String name, BlockState bitBlockDefault,
                        BlockState defaultDefaultBitBlock, String stringDefault,
                        BlockState valueDefault) {
    this(name, false, false, bitBlockDefault, defaultDefaultBitBlock, stringDefault, valueDefault);
  }

  public ConfigBitStack(String name, boolean perTool, boolean displayInChat,
                        BlockState stateDefault,
                        BlockState stateDefaultDefault, String stringDefault,
                        BlockState valueDefault) {
    super(name, perTool, displayInChat);
    this.stateDefault = stateDefault;
    this.stateDefaultDefault = stateDefaultDefault;
    this.stringDeafult = stringDefault;
    this.valueDefault = valueDefault;
  }

  public void init() {
    defaultValue = getBitStack(stateDefault);
    value = getBitStack(valueDefault);
  }

  private ItemStack getBitStack(BlockState defaultState) {
    ItemStack bitStack = ItemStack.EMPTY;
    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    if (api != null) {
      try {
        bitStack = api.getBitItem(defaultState != null ? defaultState : stateDefaultDefault);
      } catch (InvalidBitItem e) {
      }
    }
    return bitStack;
  }

  public BlockState getDefaultState() {
    return stateDefault != null ? stateDefault : stateDefaultDefault;
  }

  public String getStringDeafult() {
    return stringDeafult;
  }

  @Override
  public boolean isAtDefaultValue() {
    return super.isAtDefaultValue() || ItemStack.matches(value, defaultValue);
  }

}