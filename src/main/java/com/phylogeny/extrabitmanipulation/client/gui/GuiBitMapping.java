package com.phylogeny.extrabitmanipulation.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.GuiHelper;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonGradient;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonSelect;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonTab;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonTextured;
import com.phylogeny.extrabitmanipulation.client.render.RenderState;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.helper.BitIOHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool.BitCount;
import com.phylogeny.extrabitmanipulation.packet.PacketAddBitMapping;
import com.phylogeny.extrabitmanipulation.packet.PacketBitMappingsPerTool;
import com.phylogeny.extrabitmanipulation.packet.PacketClearStackBitMappings;
import com.phylogeny.extrabitmanipulation.packet.PacketCursorStack;
import com.phylogeny.extrabitmanipulation.packet.PacketOverwriteStackBitMappings;
import com.phylogeny.extrabitmanipulation.packet.PacketSetDesign;
import com.phylogeny.extrabitmanipulation.packet.PacketSetTabAndStateBlockButton;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

public class GuiBitMapping extends ContainerScreen {
  public static final ResourceLocation GUI_TEXTURE =
      new ResourceLocation(Reference.MOD_ID, "textures/guis/modeling_tool.png");
  public static final ResourceLocation SETTINGS_MAIN =
      new ResourceLocation(Reference.MOD_ID, "textures/guis/settings_main.png");
  public static final ResourceLocation SETTINGS_BACK =
      new ResourceLocation(Reference.MOD_ID, "textures/guis/settings_back.png");
  private final IChiselAndBitsAPI api;
  private GuiListBitMapping bitMappingList;
  private ItemStack previewStack, previewResultStack;
  private BlockState[][][] stateArray;
  private Map<BlockState, Integer> stateMap;
  private Map<BlockState, ArrayList<BitCount>> stateToBitCountArray;
  private Map<BlockState, IBitBrush> stateToBitMapPermanent, stateToBitMapManual,
      blockToBitMapPermanent, blockToBitMapManual, blockToBitMapAllBlocks;
  private GuiButtonSelect buttonStates, buttonBlocks;
  private GuiButtonTextured buttonSettings, buttonBitMapPerTool;
  private GuiButtonGradient buttonOverwriteStackMapsWithConfig, buttonOverwriteConfigMapsWithStack,
      buttonRestoreConfigMaps, buttonClearStackMaps;
  private final GuiButtonTab[] tabButtons = new GuiButtonTab[4];
  private static final String[] TAB_HOVER_TEXT =
      new String[] {"Current Model", "All Saved Mappings", "All Minecraft Blocks", "Model Result"};
  private int savedTab, mouseInitialX, mouseInitialY;
  private boolean stateMauallySelected;
  private boolean showSettings;
  private boolean bitMapPerTool;
  private final boolean designMode;
  private boolean previewStackBoxClicked;
  private String searchText = "";
  private EditBox searchField;
  private float previewStackScale;
  private Vec3 previewStackRotation, previewStackTranslation, previewStackTranslationInitial;
  private AABB previewStackBox;

  public GuiBitMapping(Player player, boolean designMode) {
    super(ExtraBitManipulation.createBitMappingContainer(player), player.getInventory(),
        Component.literal("GuiBitMapping"));
    this.designMode = designMode;
    api = ChiselsAndBitsAPIAccess.apiInstance;

    imageWidth = 254;
    imageHeight = 219;
    previewStackScale = 3.8F;
    previewStackRotation = new Vec3(30, 225, 0);
    previewStackTranslation = Vec3.ZERO;
    previewStackTranslationInitial = Vec3.ZERO;
    if (designMode) {
      return;
    }

    CompoundTag nbt = ItemStackHelper.getNBTOrNew(player.getInventory().getSelected());
    stateMauallySelected = nbt.getBoolean(NBTKeys.BUTTON_STATE_BLOCK_SETTING);
    savedTab = nbt.getInt(NBTKeys.TAB_SETTING);
    bitMapPerTool = nbt.getBoolean(NBTKeys.BIT_MAPS_PER_TOOL);
    previewStack = previewResultStack = ItemStack.EMPTY;
  }

  private void constructManualMaps() {
    stateToBitMapManual = new LinkedHashMap<BlockState, IBitBrush>();
    blockToBitMapManual = new LinkedHashMap<BlockState, IBitBrush>();
    blockToBitMapAllBlocks = new LinkedHashMap<BlockState, IBitBrush>();
    if (stateMap.isEmpty()) {
      return;
    }

    if (!designMode && tabButtons[2].selected) {
      for (Block block : BuiltInRegistries.BLOCK) {
        ResourceLocation regName = BuiltInRegistries.BLOCK.getKey(block);
        if (regName == null) {
          continue;
        }

        if (regName.getNamespace().equals(ChiselsAndBitsReferences.MOD_ID)) {
          Item item = Item.byBlock(block);
          if (item != Items.AIR) {
            ItemType itemType = api.getItemType(new ItemStack(item));
            if (itemType != null && itemType == ItemType.CHISELED_BLOCK) {
              continue;
            }
          }
        }
        if (BitIOHelper.isAir(block)) {
          continue;
        }

        BlockState state = block.defaultBlockState();
        addBitToManualMap(state.getBlock().defaultBlockState(), blockToBitMapPermanent,
            blockToBitMapAllBlocks);
      }
      blockToBitMapAllBlocks = getSortedLinkedBitMap(blockToBitMapAllBlocks);
    }
    for (BlockState state : stateMap.keySet()) {
      addBitToManualMap(state, stateToBitMapPermanent, stateToBitMapManual);
      if (!designMode) {
        addBitToManualMap(state.getBlock().defaultBlockState(), blockToBitMapPermanent,
            blockToBitMapManual);
      }
    }
    stateToBitMapManual = getSortedLinkedBitMap(stateToBitMapManual);
    blockToBitMapManual = getSortedLinkedBitMap(blockToBitMapManual);
  }

  private void addBitToManualMap(BlockState state, Map<BlockState, IBitBrush> bitMapPermanent,
                                 Map<BlockState, IBitBrush> bitMapManual) {
    IBitBrush bit = null;
    if (bitMapPermanent.containsKey(state)) {
      bit = bitMapPermanent.get(state);
    } else {
      try {
        bit = api.createBrushFromState(state);
      } catch (InvalidBitItem e) {
      }
    }
    bitMapManual.put(state, bit);
  }

  public void addPermanentMapping(BlockState state, IBitBrush bit) {
    Map<BlockState, IBitBrush> bitMapPermanent = getBitMapPermanent();
    if (bit != null) {
      bitMapPermanent.put(state, bit);
      Map<BlockState, IBitBrush> blockToBitMap = getBitMapManual();
      if (blockToBitMap.containsKey(state)) {
        blockToBitMap.put(state, bit);
      }
    } else {
      bitMapPermanent.remove(state);
      constructManualMaps();
    }
    if (designMode) {
      refreshList();
      return;
    }
    if (bitMapPerTool) {
      String nbtKey = buttonStates.selected ? NBTKeys.STATE_TO_BIT_MAP_PERMANENT :
          NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT;
      ClientPlayNetworking.send(
          new PacketAddBitMapping(nbtKey, state, bit, Configs.saveStatesById));
    } else {
      Map<BlockState, IBitBrush> bitMap =
          buttonStates.selected ? Configs.modelStateToBitMap : Configs.modelBlockToBitMap;
      if (bit != null) {
        bitMap.put(state, bit);
      } else {
        bitMap.remove(state);
      }
      String[] entryStrings = BitIOHelper.getEntryStringsFromModelBitMap(bitMap);
      if (buttonStates.selected) {
        Configs.modelStateToBitMapEntryStrings = entryStrings;
      } else {
        Configs.modelBlockToBitMapEntryStrings = entryStrings;
      }
      BitToolSettingsHelper.setBitMapProperty(buttonStates.selected, entryStrings);
    }
    refreshList();
  }

  private void refreshList() {
    constructStateToBitCountArray();
    Map<BlockState, IBitBrush> bitMapPermanent = getBitMapPermanent();
    bitMappingList.refreshList(
        designMode || tabButtons[0].selected || tabButtons[2].selected ? getBitMapManual()
            : (isResultsTabSelected() ? null : bitMapPermanent), bitMapPermanent,
        isResultsTabSelected() ? stateToBitCountArray : null,
        searchField.getValue(), designMode || buttonStates.selected);
    setPreviewStack();
    if (!designMode) {
      tabButtons[0].setIconStack(previewStack);
    }
  }

  @SuppressWarnings("null")
  private void constructStateToBitCountArray() {
    stateToBitCountArray = new LinkedHashMap<BlockState, ArrayList<BitCount>>();
    if (designMode) {
      for (Entry<BlockState, Integer> entry : stateMap.entrySet()) {
        ArrayList<BitCount> bitCountArray = new ArrayList<BitCount>();
        try {
          bitCountArray.add(
              new BitCount(api.createBrushFromState(entry.getKey()), entry.getValue()));
          stateToBitCountArray.put(entry.getKey(), bitCountArray);
        } catch (InvalidBitItem e) {
        }
      }
      return;
    }
    Map<IBitBrush, Integer> bitMap = new HashMap<IBitBrush, Integer>();
    Player player = minecraft.player;
    ItemModelingTool itemModelingTool = (ItemModelingTool) getHeldStack().getItem();
    if (itemModelingTool.mapBitsToStates(api, Configs.replacementBitsUnchiselable,
            Configs.replacementBitsInsufficient,
            BitInventoryHelper.getInventoryBitCounts(api, player), stateMap, stateToBitCountArray,
            stateToBitMapPermanent, blockToBitMapPermanent, bitMap, player.isCreative())
        .isEmpty()) {
      stateToBitCountArray = getSortedLinkedBitMap(stateToBitCountArray);
      IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
      Map<BlockState, ArrayList<BitCount>> stateToBitCountArrayCopy =
          new HashMap<BlockState, ArrayList<BitCount>>();
      for (Entry<BlockState, ArrayList<BitCount>> entry : stateToBitCountArray.entrySet()) {
        ArrayList<BitCount> bitCountArray = new ArrayList<BitCount>();
        for (BitCount bitCount : entry.getValue()) {
          bitCountArray.add(new BitCount(bitCount.getBit(), bitCount.getCount()));
        }

        stateToBitCountArrayCopy.put(entry.getKey(), bitCountArray);
      }
      previewResultStack = itemModelingTool.createModel(null, null, getHeldStack(), stateArray,
          stateToBitCountArrayCopy, bitAccess)
          ? bitAccess.getBitsAsItem(null, ItemType.CHISELED_BIT, false) : ItemStack.EMPTY;
    } else {
      previewResultStack = ItemStack.EMPTY;
    }
  }

  private Map<BlockState, IBitBrush> getBitMapManual() {
    if (designMode) {
      return stateToBitMapManual;
    }

    return tabButtons[2].selected ? blockToBitMapAllBlocks :
        (buttonStates.selected ? stateToBitMapManual : blockToBitMapManual);
  }

  private Map<BlockState, IBitBrush> getBitMapPermanent() {
    return designMode || buttonStates.selected ? stateToBitMapPermanent : blockToBitMapPermanent;
  }

  @SuppressWarnings("null")
  public void setPreviewStack() {
    IBitAccess bitAccess = api.createBitItem(ItemStack.EMPTY);
    IBitBrush defaultBit = null;
    try {
      defaultBit = api.createBrushFromState(
          (Configs.replacementBitsUnchiselable.getDefaultReplacementBit().getDefaultState()));
    } catch (InvalidBitItem e) {
    }
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          BlockState state = stateArray[i][j][k];
          if (designMode) {
            try {
              bitAccess.setBitAt(i, j, k, stateToBitMapManual.get(state));
            } catch (SpaceOccupied e) {
            }
            continue;
          }
          BlockState state2 = state.getBlock().defaultBlockState();
          boolean stateFound = stateToBitMapManual.containsKey(state);
          boolean savedStateFound = stateToBitMapPermanent.containsKey(state);
          boolean savedBlockFound = blockToBitMapPermanent.containsKey(state2);
          if (stateFound || savedBlockFound) {
            IBitBrush bit =
                savedBlockFound && !savedStateFound ? blockToBitMapPermanent.get(state2) :
                    stateToBitMapManual.get(state);
            try {
              bitAccess.setBitAt(i, j, k, bit != null ? bit : defaultBit);
            } catch (SpaceOccupied e) {
            }
          }
        }
      }
    }
    previewStack = bitAccess.getBitsAsItem(null, ItemType.CHISELED_BLOCK, false);
  }

  public ItemStack getHeldStack() {
    return Minecraft.getInstance().player.getMainHandItem();
  }


  public int getGuiLeft() {
    return leftPos + 24;
  }

  @Override
  protected void init() {
    super.init();
    leftPos -= 12;
    int l = leftPos + 128;
    int t = topPos + 21;
    previewStackBox = new AABB(l, t, -1, l + 107, t + 100, 1);
    searchField = new EditBox(font, 6, leftPos + 44, topPos + 8, 65, Component.empty());
//    searchField.setEnableBackgroundDrawing(false);
    searchField.setTextColor(-1);
    searchField.setValue(searchText);
    int slotHeight = 24;
    if (designMode) {
      stateToBitMapPermanent = new HashMap<BlockState, IBitBrush>();
      blockToBitMapPermanent = new HashMap<BlockState, IBitBrush>();
      initDesignMode();
      String buttonText = "Save Changes";
      int buttonWidth = font.width(buttonText) + 6;
      buttonList.add(
          new GuiButtonExt(0, guiLeft + xSize - buttonWidth - 5, guiTop + 5, buttonWidth, 14,
              buttonText));
    } else {
      buttonSettings =
          new GuiButtonTextured(7, guiLeft + 237, guiTop + 6, 12, 12, "Bit Mapping Settings",
              SETTINGS_BACK, SETTINGS_MAIN, null, null);
      buttonSettings.setHoverTextSelected("Back To Preview");
      buttonBitMapPerTool = GuiButtonTextured.createCheckBox(8, guiLeft + 143, guiTop + 26, 12, 12,
          "Save/access mappings per tool or per client config");
      if (showSettings) {
        buttonSettings.selected = true;
      }

      int y = topPos + 44;
      int offsetY = 19;
      String hovertext = "Overwrite mappings saved in 1 with the mappings saved in 2";
      String stackText = "this Modeling Tool's NBT";
      String configText = "the client config file";
      buttonOverwriteStackMapsWithConfig = new GuiButtonGradient(9, guiLeft + 130, y, 102, 14,
          "Write Config->Stack", hovertext.replace("1", stackText).replace("2", configText));
      buttonOverwriteConfigMapsWithStack =
          new GuiButtonGradient(10, guiLeft + 130, y + offsetY, 102, 14,
              "Write Stack->Config", hovertext.replace("2", stackText).replace("1", configText));
      buttonRestoreConfigMaps = new GuiButtonGradient(11, guiLeft + 130, y + offsetY * 2, 102, 14,
          "Reset Config Maps", "Reset " + configText + " mapping data to their default values");
      buttonClearStackMaps = new GuiButtonGradient(12, guiLeft + 130, y + offsetY * 3, 102, 14,
          "Clear Stack Data", "Delete all saved mappping data from " + stackText);
      updateButtons();
      if (bitMapPerTool) {
        buttonBitMapPerTool.selected = true;
        stateToBitMapPermanent = BitIOHelper.readStateToBitMapFromNBT(api, getHeldStack(),
            NBTKeys.STATE_TO_BIT_MAP_PERMANENT);
        blockToBitMapPermanent = BitIOHelper.readStateToBitMapFromNBT(api, getHeldStack(),
            NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT);
      } else {
        stateToBitMapPermanent = Configs.modelStateToBitMap;
        blockToBitMapPermanent = Configs.modelBlockToBitMap;
      }
      stateToBitMapPermanent = getSortedLinkedBitMap(stateToBitMapPermanent);
      blockToBitMapPermanent = getSortedLinkedBitMap(blockToBitMapPermanent);
      buttonList.add(buttonSettings);
      buttonList.add(buttonBitMapPerTool);
      buttonList.add(buttonOverwriteStackMapsWithConfig);
      buttonList.add(buttonOverwriteConfigMapsWithStack);
      buttonList.add(buttonRestoreConfigMaps);
      buttonList.add(buttonClearStackMaps);
      stateMap = new HashMap<BlockState, Integer>();
      stateArray = new BlockState[16][16][16];
      BitIOHelper.readStatesFromNBT(ItemStackHelper.getNBTOrNew(getHeldStack()), stateMap,
          stateArray);
      constructStateToBitCountArray();
      for (int i = 0; i < tabButtons.length; i++) {
        ItemStack iconStack = i == 0 ? previewStack :
            (i == 1 ? ItemStack.EMPTY : (i == 2 ? new ItemStack(Blocks.GRASS) : ItemStack.EMPTY));
        float u = 0;
        float v = 0;
        int uWidth = 0;
        int vHeight = 0;
        if (i == 1 || i == 3) {
          u = i == 1 ? 104 : 67;
          v = 219;
          uWidth = 36;
          vHeight = 36;
        }
        GuiButtonTab tab = new GuiButtonTab(i, guiLeft, guiTop + 21 + i * 25, 24, 25,
            TAB_HOVER_TEXT[i], true, iconStack, u, v, uWidth, vHeight, 141, 219, 0, 256,
            GUI_TEXTURE);
        if (i == savedTab) {
          tab.selected = true;
        }

        tabButtons[i] = tab;
        buttonList.add(tab);
      }

      int x = guiLeft + 42;
      y = guiTop + 122;
      int colorSelected = -16726016;
      int colorDeselected = -8882056;
      buttonStates =
          new GuiButtonSelect(4, x, y, 37, 12, "States", "Map bits to individual block states",
              colorSelected, colorDeselected);
      buttonBlocks = new GuiButtonSelect(5, x + 37, y, 36, 12, "Blocks",
          "Map bits to all posible states of a given block", colorSelected, colorDeselected);
      buttonStates.enabled = !tabButtons[2].selected;
      buttonBlocks.enabled = !isResultsTabSelected();
      int selectedTab = getSelectedTab();
      boolean buttonStatesSlected = selectedTab > 1 ? selectedTab == 3 : stateMauallySelected;
      buttonStates.selected = buttonStatesSlected;
      buttonBlocks.selected = !buttonStatesSlected;
      buttonList.add(buttonStates);
      buttonList.add(buttonBlocks);
    }
    bitMappingList =
        new GuiListBitMapping(this, 150, height, guiTop + 21, guiTop + 121, slotHeight, designMode);
    constructManualMaps();
    refreshList();
  }

  private boolean isResultsTabSelected() {
    return designMode || tabButtons[3].selected;
  }

  private LinkedHashMap getSortedLinkedBitMap(Map bitMap) {
    return BitInventoryHelper.getSortedLinkedHashMap(bitMap, new Comparator<Object>() {
      @Override
      public int compare(Object object1, Object object2) {
        return getName(object1).compareTo(getName(object2));
      }

      @SuppressWarnings("unchecked")
      private String getName(Object object) {
        BlockState state = (BlockState) ((Map.Entry) object).getKey();
        Block block = state.getBlock();
        ResourceLocation regName = block.getRegistryName();
        return regName == null ? "" : (regName.getResourceDomain() + regName.getResourcePath() +
            block.getMetaFromState(state));
      }
    });
  }

  private void updateButtons() {
    if (designMode) {
      return;
    }

    buttonBitMapPerTool.visible =
        buttonOverwriteStackMapsWithConfig.visible = buttonOverwriteConfigMapsWithStack.visible
            = buttonOverwriteConfigMapsWithStack.visible =
            buttonRestoreConfigMaps.visible = buttonClearStackMaps.visible = showSettings;
    if (!showSettings) {
      return;
    }

    LinkedHashMap stateToBitMapSorted = getSortedLinkedBitMap(Configs.modelStateToBitMap);
    LinkedHashMap blockToBitMapSorted = getSortedLinkedBitMap(Configs.modelBlockToBitMap);
    buttonOverwriteStackMapsWithConfig.enabled = buttonOverwriteConfigMapsWithStack.enabled
        = !BitIOHelper.areSortedBitMapsIdentical(stateToBitMapSorted,
        getSortedLinkedBitMap(BitIOHelper.readStateToBitMapFromNBT(api, getHeldStack(),
            NBTKeys.STATE_TO_BIT_MAP_PERMANENT)))
        || !BitIOHelper.areSortedBitMapsIdentical(blockToBitMapSorted,
        getSortedLinkedBitMap(BitIOHelper.readStateToBitMapFromNBT(api, getHeldStack(),
            NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT)));
    buttonRestoreConfigMaps.enabled = !BitIOHelper.areSortedBitMapsIdentical(stateToBitMapSorted,
        getSortedLinkedBitMap(BitIOHelper.getModelBitMapFromEntryStrings(
            ConfigHandlerExtraBitManipulation.STATE_TO_BIT_MAP_DEFAULT_VALUES)))
        || !BitIOHelper.areSortedBitMapsIdentical(blockToBitMapSorted,
        getSortedLinkedBitMap(BitIOHelper.getModelBitMapFromEntryStrings(
            ConfigHandlerExtraBitManipulation.BLOCK_TO_BIT_MAP_DEFAULT_VALUES)));
    buttonClearStackMaps.enabled = BitIOHelper.hasBitMapsInNbt(getHeldStack());
  }

  @Override
  protected void keyTyped(char typedChar, int keyCode) throws IOException {
    if (searchField.textboxKeyTyped(typedChar, keyCode)) {
      refreshList();
      searchText = searchField.getText();
    } else if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
      previewStackTranslation = Vec3d.ZERO;
    } else if (showSettings) {
      if (keyCode == Keyboard.KEY_ESCAPE ||
          mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
        showSettings = buttonSettings.selected = false;
        updateButtons();
      }
    } else {
      super.keyTyped(typedChar, keyCode);
    }
  }

  @Override
  public void handleMouseInput() throws IOException {
    super.handleMouseInput();
    if (!previewStackBoxClicked) {
      bitMappingList.handleMouseInput();
    }

    Pair<Vec3d, Float> pair =
        GuiHelper.scaleObjectWithMouseWheel(this, previewStackBox, previewStackTranslation,
            previewStackScale, 30.0F, 0.0F);
    previewStackTranslation = pair.getLeft();
    previewStackScale = pair.getRight();
  }

  @Override
  protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton,
                                long timeSinceLastClick) {
    super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    if (!previewStackBoxClicked) {
      return;
    }

    float deltaX = mouseInitialX - mouseX;
    float deltaY = mouseInitialY - mouseY;
    if (clickedMouseButton == 0) {
      mouseInitialX = mouseX;
      mouseInitialY = mouseY;
    }
    Triple<Vec3d, Vec3d, Float> triple = GuiHelper.dragObject(clickedMouseButton, deltaX, deltaY,
        previewStackTranslationInitial, previewStackRotation, previewStackScale, 30.0F, 4.5F, 4.5F,
        true);
    previewStackTranslation = triple.getLeft();
    previewStackRotation = triple.getMiddle();
    previewStackScale = triple.getRight();
  }

  @Override
  public void updateScreen() {
    super.updateScreen();
    searchField.updateCursorCounter();
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    searchField.mouseClicked(mouseX, mouseY, mouseButton);
    bitMappingList.mouseClicked(mouseX, mouseY, mouseButton);
    previewStackBoxClicked = GuiHelper.isCursorInsideBox(previewStackBox, mouseX, mouseY);
    mouseInitialX = mouseX;
    mouseInitialY = mouseY;
    previewStackTranslationInitial =
        new Vec3d(previewStackTranslation.x, previewStackTranslation.y, 0);
    if (mc.player.inventory.getItemStack().isEmpty() && mouseButton == 2 &&
        mc.player.capabilities.isCreativeMode && previewStackBoxClicked) {
      ItemStack previewStack = getPreviewStack();
      if (previewStack.isEmpty()) {
        return;
      }

      ItemStack stack = previewStack.copy();
      mc.player.inventory.setItemStack(stack);
      ExtraBitManipulation.packetNetwork.sendToServer(new PacketCursorStack(stack));
    }
  }

  @Override
  protected void mouseReleased(int mouseX, int mouseY, int state) {
    super.mouseReleased(mouseX, mouseY, state);
    bitMappingList.mouseReleased(mouseX, mouseY, state);
    updateButtons();
    mouseInitialX = 0;
    mouseInitialY = 0;
    previewStackBoxClicked = false;
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    super.drawScreen(mouseX, mouseY, partialTicks);
    if (previewStackBoxClicked) {
      return;
    }

    renderHoveredToolTip(mouseX, mouseY);
    for (int i = 0; i < bitMappingList.getSize(); i++) {
      GuiListBitMappingEntry entry = bitMappingList.getListEntry(i);
      if (mouseY >= bitMappingList.top && mouseY <= bitMappingList.bottom) {
        Lighting.enableGUIStandardItemLighting();
        int slotWidth = 19;
        int k = bitMappingList.left + bitMappingList.width / 2 - bitMappingList.width / 2 + 5;
        int l = bitMappingList.top + 4 + i * (bitMappingList.slotHeight) -
            bitMappingList.getAmountScrolled();
        AABB slot = new AABB(k, l, -1, k + slotWidth, l + bitMappingList.slotHeight - 5, 1);
        Vec3d mousePos = new Vec3d(mouseX, mouseY, 0);
        if (slot.move(38, 0, 0).contains(mousePos)) {
          ArrayList<String> hoverTextList = new ArrayList<String>();
          final String unmappedText =
              "The blockstate is currently mapped to nothing, as it cannot be chiseled.";
          ArrayList<BitCount> bitCountArray = entry.getBitCountArray();
          for (int j = 0; j < bitCountArray.size(); j++) {
            BitCount bitCount = bitCountArray.get(j);
            IBitBrush bit = bitCount.getBit();
            ItemStack bitStack = bit != null ? bit.getItemStack(1) : ItemStack.EMPTY;
            boolean isAir = bit != null && bit.isAir();
            String text = !bitStack.isEmpty() ? BitToolSettingsHelper.getBitName(bitStack) :
                (isAir ? "Empty / Air" : unmappedText);
            if (!bitStack.isEmpty() || entry.isAir()) {
              String text2 =
                  ChatFormatting.DARK_RED + (j == 0 ? "Bit:" : "	") + " " + ChatFormatting.RESET;
              if (bitCountArray.size() > 1) {
                text2 = (j == 0 ? "" : " ") + text2.replace("Bit:", "Bits:");
              }

              text = text2 + text;
              if (designMode || !entry.isInteractive()) {
                text += " (" + bitCount.getCount() + ")";
              }
            }
            if (!designMode &&
                (buttonStates.selected ? stateToBitMapPermanent.containsKey(entry.getState())
                    : blockToBitMapPermanent.containsKey(entry.getState()))) {
              text += ChatFormatting.BLUE + " (manually mapped)";
            }

            hoverTextList.add(text);
          }
          if (entry.getBitCountArray().isEmpty()) {
            hoverTextList.add(unmappedText);
          }
          if (entry.isInteractive()) {
            hoverTextList.add(
                !isShiftKeyDown() ? ChatFormatting.AQUA + "  Hold SHIFT for usage instructions."
                    :
                    ChatFormatting.AQUA + "  - Click with bit or block on cursor to add mapping.");
            if (isShiftKeyDown()) {
              hoverTextList.add(
                  ChatFormatting.AQUA + "  - Shift click with empty cursor to map to air.");
              hoverTextList.add(
                  ChatFormatting.AQUA + "  - Control click with empty cursor to remove mapping.");
              hoverTextList.add(ChatFormatting.AQUA +
                  "  - Middle mouse click blocks or bits in creative mode to get stack.");
            }
          }
          drawHoveringText(hoverTextList, mouseX, mouseY, mc.fontRenderer);
        } else if (slot.contains(mousePos)) {
          boolean stateMode = designMode || buttonStates.selected;
          drawHoveringText(Arrays.<String>asList(
                  new String[] {ChatFormatting.DARK_RED + (stateMode ? "State" : "Block")
                      + ": " + ChatFormatting.RESET + (stateMode ? entry.getState().toString()
                      : Block.REGISTRY.getNameForObject(entry.getState().getBlock()))}), mouseX, mouseY,
              mc.fontRenderer);
        }
        Lighting.turnOff();
      }
    }

    for (GuiButton button : buttonList) {
      if (!(button instanceof GuiButtonBase buttonBase)) {
        continue;
      }

      if (button.isMouseOver() && button.visible) {
        drawHoveringText(buttonBase.getHoverText(), mouseX, mouseY, mc.fontRenderer);
      }
    }
    if (!designMode) {
      for (int i = 0; i < tabButtons.length; i++) {
        if (tabButtons[i].isMouseOver()) {
          drawHoveringText(tabButtons[i].getHoverText(), mouseX, mouseY, mc.fontRenderer);
        }
      }
    }
    GlStateManager.enableLighting();
    GlStateManager.enableDepth();
    Lighting.turnBackOn();
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    ClientHelper.bindTexture(GUI_TEXTURE);
    int i = (width - xSize) / 2;
    int j = (height - ySize) / 2;
    if (designMode) {
      drawTexturedModalRect(i + 12, j, 24, 0, xSize - 24, ySize);
    } else {
      drawTexturedModalRect(i - 12, j, 0, 0, xSize, ySize);
    }
    bitMappingList.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(-guiLeft, -guiTop, 0);
    GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
    GlStateManager.disableLighting();
    searchField.drawTextBox();
    if (!searchField.isFocused() && searchField.getText().isEmpty()) {
      fontRenderer.drawString("search", searchField.x, searchField.y, -10197916);
    }

    if (designMode) {
      fontRenderer.drawString("Design", getGuiLeft() + 103, guiTop + 8, 4210752);
      fontRenderer.drawString(mc.player.inventory.getDisplayName().getUnformattedText(),
          guiLeft + 60, guiTop + ySize - 96 + 2, 4210752);
    } else {
      for (int i = 0; i < tabButtons.length; i++) {
        GuiButtonTab tab = tabButtons[i];
        tab.renderIconStack();
        if (tab.selected) {
          fontRenderer.drawString(TAB_HOVER_TEXT[i], getGuiLeft() + 103, guiTop + 7, 4210752);
        }
      }
    }
    if (!designMode && showSettings) {
      GlStateManager.pushMatrix();
      GlStateManager.translate(0, 0.5, 0);
      fontRenderer.drawString("Map Per Tool", getGuiLeft() + 133, guiTop + 29, 4210752);
      GlStateManager.popMatrix();
    } else {
      ItemStack previewStack = getPreviewStack();
      if (!previewStack.isEmpty()) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiHelper.glScissor((int) previewStackBox.minX, (int) previewStackBox.minY,
            (int) (previewStackBox.maxX - previewStackBox.minX),
            (int) (previewStackBox.maxY - previewStackBox.minY));
        Lighting.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5 + previewStackTranslation.x, previewStackTranslation.y, 0);
        RenderState.renderStateModelIntoGUI(null,
            RenderState.getItemModelWithOverrides(previewStack), previewStack, false,
            guiLeft + 167, guiTop + 61, (float) previewStackRotation.x,
            (float) previewStackRotation.y, previewStackScale);
        GlStateManager.popMatrix();
        Lighting.turnOff();
        GuiHelper.glScissorDisable();
      } else {
        fontRenderer.drawSplitString("No Preview   Available", getGuiLeft() + 131, guiTop + 63, 60,
            4210752);
      }
    }
    if (bitMappingList.getSize() == 0) {
      fontRenderer.drawSplitString(
          "No " + (designMode || buttonStates.selected ? "States" : "Blocks")
              + "      Found", getGuiLeft() + 31, guiTop + 63, 60, 4210752);
    }

    Lighting.enableGUIStandardItemLighting();
    ItemStack stack = mc.player.inventory.getItemStack();
    GlStateManager.translate(0.0F, 0.0F, 32.0F);
    zLevel = 800.0F;
    itemRender.zLevel = 800.0F;
    Font font = null;
    if (!stack.isEmpty()) {
      font = stack.getItem().getFontRenderer(stack);
    }

    if (font == null) {
      font = fontRenderer;
    }

    int x = mouseX - 8;
    int y = mouseY - 8;
    itemRender.renderItemAndEffectIntoGUI(stack, x, y);
    itemRender.renderItemOverlayIntoGUI(font, stack, x, y, null);
    zLevel = 0.0F;
    itemRender.zLevel = 0.0F;
    GlStateManager.popMatrix();
  }

  private ItemStack getPreviewStack() {
    return !designMode && isResultsTabSelected() ? previewResultStack : previewStack;
  }

  @Override
  protected void actionPerformed(GuiButton button) throws IOException {
    if (designMode) {
      if (button.id == 0) {
        BitInventoryHelper.setHeldDesignStack(mc.player, previewStack);
        stateToBitMapPermanent.clear();
        initDesignMode();
        constructManualMaps();
        refreshList();
        ExtraBitManipulation.packetNetwork.sendToServer(new PacketSetDesign(previewStack));
      } else {
        super.actionPerformed(button);
      }
      return;
    }
    int id = button.id;
    if (id >= 0 && id <= 5) {
      if (id > 3) {
        stateMauallySelected = id == 4;
        selectButtonStatesBlocks(stateMauallySelected);
        constructManualMaps();
        refreshList();
      } else {
        if (getSelectedTab() == id) {
          return;
        }

        boolean allBlocksPrev = tabButtons[2].selected;
        boolean resultsPrev = isResultsTabSelected();
        for (GuiButtonTab tab : tabButtons) {
          tab.selected = tab.id == id;
        }

        savedTab = id;
        boolean allBlocks = tabButtons[2].selected;
        boolean results = isResultsTabSelected();
        buttonStates.enabled = !allBlocks;
        buttonBlocks.enabled = !results;
        if (allBlocks) {
          selectButtonStatesBlocks(false);
        } else if (allBlocksPrev && stateMauallySelected) {
          selectButtonStatesBlocks(true);
        }
        if (results) {
          selectButtonStatesBlocks(true);
        } else if (resultsPrev && !stateMauallySelected) {
          selectButtonStatesBlocks(false);
        }
        if (allBlocksPrev != allBlocks) {
          constructManualMaps();
        }

        refreshList();
      }
      ExtraBitManipulation.packetNetwork.sendToServer(
          new PacketSetTabAndStateBlockButton(getSelectedTab(), stateMauallySelected));
    } else if (id == 7) {
      showSettings ^= true;
      buttonSettings.selected = showSettings;
      updateButtons();
    } else if (id == 8) {
      bitMapPerTool ^= true;
      buttonBitMapPerTool.selected = bitMapPerTool;
      ExtraBitManipulation.packetNetwork.sendToServer(new PacketBitMappingsPerTool(bitMapPerTool));
    } else if (id == 9) {
      ItemStack stack = getHeldStack();
      overwriteStackBitMappings(stack, Configs.modelBlockToBitMap,
          NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT);
      overwriteStackBitMappings(stack, Configs.modelStateToBitMap,
          NBTKeys.STATE_TO_BIT_MAP_PERMANENT);
    } else if (id == 10 || id == 11) {
      Configs.modelBlockToBitMapEntryStrings = id == 10 ?
          overwriteConfigMapWithStackMap(Configs.modelBlockToBitMap,
              NBTKeys.BLOCK_TO_BIT_MAP_PERMANENT)
          : ConfigHandlerExtraBitManipulation.BLOCK_TO_BIT_MAP_DEFAULT_VALUES;
      Configs.modelStateToBitMapEntryStrings = id == 10 ?
          overwriteConfigMapWithStackMap(Configs.modelStateToBitMap,
              NBTKeys.STATE_TO_BIT_MAP_PERMANENT)
          : ConfigHandlerExtraBitManipulation.STATE_TO_BIT_MAP_DEFAULT_VALUES;
      if (id == 11) {
        BitToolSettingsHelper.setBitMapProperty(true, Configs.modelStateToBitMapEntryStrings);
        BitToolSettingsHelper.setBitMapProperty(false, Configs.modelBlockToBitMapEntryStrings);
      }
      Configs.initModelingBitMaps();
    } else if (id == 12) {
      BitIOHelper.clearAllBitMapsFromNbt(getHeldStack());
      ExtraBitManipulation.packetNetwork.sendToServer(new PacketClearStackBitMappings());
    } else {
      super.actionPerformed(button);
      return;
    }
    if (id >= 8) {
      setWorldAndResolution(mc, width, height);
    }
  }

  private void initDesignMode() {
    stateMap = new HashMap<BlockState, Integer>();
    stateArray = new BlockState[16][16][16];
    IBitAccess pattern = api.createBitItem(getHeldStack());
    if (pattern == null) {
      pattern = api.createBitItem(ItemStack.EMPTY);
    }

    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          @SuppressWarnings("null")
          IBitBrush bit = pattern.getBitAt(i, j, k);
          BlockState state = bit.getState();
          stateArray[i][j][k] = state;
          if (!bit.isAir()) {
            stateMap.put(state, 1 + (stateMap.containsKey(state) ? stateMap.get(state) : 0));
          }
        }
      }
    }
    constructStateToBitCountArray();
  }

  private void overwriteStackBitMappings(ItemStack stack, Map<BlockState, IBitBrush> bitMap,
                                         String key) {
    BitIOHelper.writeStateToBitMapToNBT(stack, key, bitMap, Configs.saveStatesById);
    ExtraBitManipulation.packetNetwork.sendToServer(
        new PacketOverwriteStackBitMappings(bitMap, key, Configs.saveStatesById));
  }

  private String[] overwriteConfigMapWithStackMap(Map<BlockState, IBitBrush> bitMap, String key) {
    String[] entryStrings = BitIOHelper.getEntryStringsFromModelBitMap(
        BitIOHelper.readStateToBitMapFromNBT(api, getHeldStack(), key));
    BitToolSettingsHelper.setBitMapProperty(bitMap.equals(Configs.modelStateToBitMap),
        entryStrings);
    return entryStrings;
  }

  private int getSelectedTab() {
    for (GuiButtonTab tab : tabButtons) {
      if (tab.selected) {
        return tab.id;
      }
    }
    return 0;
  }

  private void selectButtonStatesBlocks(boolean selectStates) {
    buttonStates.selected = selectStates;
    buttonBlocks.selected = !selectStates;
  }

}