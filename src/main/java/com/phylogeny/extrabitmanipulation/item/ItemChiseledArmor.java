package com.phylogeny.extrabitmanipulation.item;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.armor.DataChiseledArmorPiece;
import com.phylogeny.extrabitmanipulation.armor.GlOperation;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitInventoryHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorBodyPartTemplateData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorCollectionData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ModelRegistration;
import com.phylogeny.extrabitmanipulation.packet.PacketCreateBodyPartTemplate;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.APIExceptions.SpaceOccupied;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.KeyBindingContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@KeyBindingContext("menuitem")
public class ItemChiseledArmor extends ArmorItem {
  public static final String[] MODE_TITLES = new String[] {"Template Creation", "Block Collection"};
  public static final String[] SCALE_TITLES = new String[] {"1:1", "1:2", "1:4"};
  public final ArmorMovingPart[] MOVING_PARTS;
  public final String[] MOVING_PART_TITLES;
  public final ArmorType armorType;
  @Environment(EnvType.CLIENT)
  private ModelResourceLocation itemModelLocation;

  private final ResourceLocation registryName;

  @SuppressWarnings("null")
  public ItemChiseledArmor(Properties properties, String name, ArmorMaterials material,
                           ArmorType armorType,
                           ArmorMovingPart... movingParts) {
    super(material, Type.valueOf(armorType.name()), properties);
//    setCreativeTab(CreativeTabExtraBitManipulation.CREATIVE_TAB);
    registryName = new ResourceLocation(Reference.MOD_ID, name);
    this.armorType = armorType;
    MOVING_PARTS = movingParts;
    MOVING_PART_TITLES = new String[MOVING_PARTS.length];
    for (int i = 0; i < MOVING_PARTS.length; i++) {
      MOVING_PART_TITLES[i] = MOVING_PARTS[i].getName();
    }
  }

  public ResourceLocation getRegistryName() {
    return registryName;
  }

  @SuppressWarnings("null")
  @Environment(EnvType.CLIENT)
  public ResourceLocation initItemModelLocation() {
    ResourceLocation loc = new ResourceLocation(registryName.getNamespace(),
        registryName.getPath() + "_default");
    itemModelLocation = new ModelResourceLocation(loc, "inventory");
    return loc;
  }

  @Environment(EnvType.CLIENT)
  public ModelResourceLocation getItemModelLocation() {
    return itemModelLocation;
  }

  @Environment(EnvType.CLIENT)
  public BakedModel getItemModel() {
    return ClientHelper.getBlockModelShapes().getModelManager().getModel(itemModelLocation);
  }

  @Override
  @Environment(EnvType.CLIENT)
  @Nullable
  public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack,
                                  EntityEquipmentSlot slot, ModelBiped modeldefault) {
    return ModelRegistration.getArmorModel(stack, slot, entity);
  }

  @Override
  @Nullable
  public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot,
                                String type) {
    return ModelRegistration.getArmorTexture(stack, getArmorMaterial());
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
                                             boolean slotChanged) {
    return slotChanged || oldStack.hasTagCompound() != newStack.hasTagCompound() ||
        (oldStack.hasTagCompound() && newStack.hasTagCompound()
            && !ItemStackHelper.getArmorData(oldStack.getTagCompound())
            .equals(ItemStackHelper.getArmorData(newStack.getTagCompound())));
  }

  @Override
  public InteractionResult useOn(UseOnContext useOnContext) {
    Player player = useOnContext.getPlayer();
    ItemStack stack = player.getItemInHand(useOnContext.getHand());
    if (useOnContext.getLevel().isClientSide) {
      ArmorBodyPartTemplateData templateData =
          new ArmorBodyPartTemplateData(ItemStackHelper.getNBTOrNew(stack), this);
      Vec3 hit = useOnContext.getClickLocation();
      if (createBodyPartTemplate(player, useOnContext.getLevel(), useOnContext.getClickedPos(),
          useOnContext.getClickedFace(), hit, templateData) ==
          InteractionResult.SUCCESS) {
        ExtraBitManipulation.packetNetwork.sendToServer(
            new PacketCreateBodyPartTemplate(pos, facing, hit, templateData));
      }
    }
    return InteractionResult.SUCCESS;
  }

  public static InteractionResult createBodyPartTemplate(Player player, Level world,
                                                         BlockPos pos,
                                                         Direction facing, Vec3 hit,
                                                         ArmorBodyPartTemplateData templateData) {
    CompoundTag nbt = ItemStackHelper.getNBTOrNew(player.getMainHandItem());
    if (templateData.getMode() != 0) {
      return InteractionResult.PASS;
    }

    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    IBitBrush bitBodyPartTemplate = null;
    try {
      bitBodyPartTemplate =
          api.createBrushFromState(BlocksExtraBitManipulation.bodyPartTemplate.defaultBlockState());
    } catch (InvalidBitItem e) {
      return InteractionResult.FAIL;
    }
    ItemStack bitStack = bitBodyPartTemplate.getItemStack(1);
    hit = hit.add(pos.getX(), pos.getY(), pos.getZ());
    AABB box = getBodyPartTemplateBox(player, facing, pos, hit, templateData.getScale(),
        templateData.getMovingPart());
    boolean creativeMode = player.isCreative();
    if (!creativeMode) {
      int bitsMissing = (int) (Math.round((box.maxX - box.minX) / Utility.PIXEL_D) *
          Math.round((box.maxY - box.minY) / Utility.PIXEL_D)
          * Math.round((box.maxZ - box.minZ) / Utility.PIXEL_D)) -
          BitInventoryHelper.countInventoryBits(api, player, bitStack.copy())
          - BitInventoryHelper.countInventoryBlocks(player,
          BlocksExtraBitManipulation.bodyPartTemplate) * 4096;
      if (bitsMissing > 0) {
        if (world.isClientSide) {
          ClientHelper.printChatMessageWithDeletion(
              "There are insufficient Bodypart Template blocks/bits in your inventory. Obtain " +
                  bitsMissing
                  + " Bodypart Template bits or blocks worth of bits (1 block = 4096 bits).");
        }

        return InteractionResult.FAIL;
      }
    }
    int bitsPlaced = 0;
    AABB boxBlocks = new AABB(Math.floor(box.minX), Math.floor(box.minY), Math.floor(box.minZ),
        Math.ceil(box.maxX), Math.ceil(box.maxY), Math.ceil(box.maxZ));
    try {
      api.beginUndoGroup(player);
      for (int i = (int) boxBlocks.minX; i <= boxBlocks.maxX; i++) {
        for (int j = (int) boxBlocks.minY; j <= boxBlocks.maxY; j++) {
          for (int k = (int) boxBlocks.minZ; k <= boxBlocks.maxZ; k++) {
            bitsPlaced = placeBodyPartTemplateBits(world, new BlockPos(i, j, k), api, box,
                bitBodyPartTemplate, bitsPlaced);
          }
        }
      }
    } finally {
      api.endUndoGroup(player);
      if (!world.isClientSide && !creativeMode) {
        bitsPlaced =
            BitInventoryHelper.removeOrAddInventoryBits(api, player, bitStack.copy(), bitsPlaced,
                false);
        BitInventoryHelper.removeBitsFromBlocks(api, player, bitStack,
            BlocksExtraBitManipulation.bodyPartTemplate, bitsPlaced);
        player.containerMenu.sendAllDataToRemote();
      }
      if (bitsPlaced > 0) {
        ItemSculptingTool.playPlacementSound(player, world, pos, 1.0F);
        if (world.isClientSide) {
          ClientHelper.printChatMessageWithDeletion("Created a " +
              getPartAndScaleText(templateData.getMovingPart(), templateData.getScale()) +
              " and set collection reference area");
        }
      }
    }
    if (!world.isClientSide) {
      writeCollectionBoxToNBT(nbt, player.getYRot(), player.isShiftKeyDown(),
          player.getDirection().getOpposite(), pos, facing, hit);
      player.getMainHandItem().setTag(nbt);
    }
    return bitsPlaced > 0 ? InteractionResult.SUCCESS : InteractionResult.FAIL;
  }

  public static String getPartAndScaleText(ArmorMovingPart part, int scale) {
    return SCALE_TITLES[scale] + " scale " + part.getBodyPartTemplate().getName().toLowerCase() +
        " template";
  }

  public static void writeCollectionBoxToNBT(CompoundTag nbt, float playerYaw,
                                             boolean useBitGrid,
                                             Direction facingBox, BlockPos pos,
                                             Direction facingPlacement, Vec3 hit) {
    BitAreaHelper.writeFacingToNBT(facingBox, nbt, NBTKeys.ARMOR_FACING_BOX);
    BitAreaHelper.writeFacingToNBT(facingPlacement, nbt, NBTKeys.ARMOR_FACING_PLACEMENT);
    BitAreaHelper.writeBlockPosToNBT(pos, nbt, NBTKeys.ARMOR_POS);
    BitAreaHelper.writeVecToNBT(hit, nbt, NBTKeys.ARMOR_HIT);
    nbt.putFloat(NBTKeys.ARMOR_YAW_PLAYER, playerYaw);
    nbt.putBoolean(NBTKeys.ARMOR_USE_BIT_GRID, useBitGrid);
  }

  private static int placeBodyPartTemplateBits(Level world, BlockPos pos, IChiselAndBitsAPI api,
                                               AABB box, IBitBrush bitBodyPartTemplate,
                                               int bitsPlaced) {
    IBitAccess bitAccess;
    try {
      bitAccess = api.getBitAccess(world, pos);
    } catch (CannotBeChiseled e) {
      return bitsPlaced;
    }
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          IBitBrush bit = bitAccess.getBitAt(i, j, k);
          if (!bit.isAir()) {
            continue;
          }

          double x = pos.getX() + i * Utility.PIXEL_D + 0.5 * Utility.PIXEL_D;
          double y = pos.getY() + j * Utility.PIXEL_D + 0.5 * Utility.PIXEL_D;
          double z = pos.getZ() + k * Utility.PIXEL_D + 0.5 * Utility.PIXEL_D;
          if (x < box.minX || x > box.maxX || y < box.minY || y > box.maxY || z < box.minZ ||
              z > box.maxZ) {
            continue;
          }

          try {
            bitAccess.setBitAt(i, j, k, bitBodyPartTemplate);
            bitsPlaced++;
          } catch (SpaceOccupied e) {
          }
        }
      }
    }
    bitAccess.commitChanges(true);
    return bitsPlaced;
  }

  public static AABB getBodyPartTemplateBox(Player player, Direction facingPlacement,
                                            BlockPos pos, Vec3 hit, int scale,
                                            ArmorMovingPart part) {
    return getBodyPartTemplateBox(player.getYHeadRot(), player.isShiftKeyDown(),
        player.getDirection(), facingPlacement, pos, hit, scale, part);
  }

  public static AABB getBodyPartTemplateBox(float playerYaw, boolean useBitGrid,
                                            Direction facingBox,
                                            Direction facingPlacement, BlockPos pos, Vec3d hit,
                                            int scale, ArmorMovingPart part) {
    scale = (int) Math.pow(2, scale);
    BodyPartTemplate bodyPart = part.getBodyPartTemplate();
    boolean isHead = bodyPart == BodyPartTemplate.HEAD;
    double semiDiameterX = (bodyPart == BodyPartTemplate.LIMB ? 2 : 4) * scale * Utility.PIXEL_D;
    double semiDiameterY = (isHead ? 4 : 6) * scale * Utility.PIXEL_D;
    double semiDiameterZ = (isHead ? 4 : 2) * scale * Utility.PIXEL_D;
    if (facingBox.getAxis() == Direction.Axis.X) {
      double tempX = semiDiameterX;
      semiDiameterX = semiDiameterZ;
      semiDiameterZ = tempX;
    }
    int offsetX = facingPlacement.getStepX();
    int offsetY = facingPlacement.getStepY();
    int offsetZ = facingPlacement.getStepZ();
    double x2, y2, z2;
    AABB box = null;
    if (useBitGrid) {
      float hitX = (float) hit.x - pos.getX();
      float hitY = (float) hit.y - pos.getY();
      float hitZ = (float) hit.z - pos.getZ();
      IBitLocation bitLoc =
          ChiselsAndBitsAPIAccess.apiInstance.getBitPos(hitX, hitY, hitZ, facingPlacement, pos,
              false);
      if (bitLoc != null) {
        x2 = bitLoc.getBitX() * Utility.PIXEL_D;
        y2 = bitLoc.getBitY() * Utility.PIXEL_D;
        z2 = bitLoc.getBitZ() * Utility.PIXEL_D;
        double offset =
            facingPlacement.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                Utility.PIXEL_D : 0;
        box =
            new AABB(x2 - semiDiameterX, y2 - semiDiameterY, z2 - semiDiameterZ, x2 + semiDiameterX,
                y2 + semiDiameterY,
                z2 + semiDiameterZ).move((semiDiameterX + offset) * offsetX,
                (semiDiameterY + offset) * offsetY,
                (semiDiameterZ + offset) * offsetZ).move(pos);
      }
    } else {
      x2 = pos.getX() + 0.5;
      y2 = pos.getY() + 0.5;
      z2 = pos.getZ() + 0.5;
      box = new AABB(x2 - semiDiameterX, y2 - semiDiameterY, z2 - semiDiameterZ, x2 + semiDiameterX,
          y2 + semiDiameterY,
          z2 + semiDiameterZ).move(0, (semiDiameterY - 0.5) * (offsetY != 0 ? offsetY : 1), 0)
          .move(offsetX, offsetY, offsetZ);
      if (scale == 4 && bodyPart != BodyPartTemplate.LIMB) {
        if (facingBox.getAxis() != Direction.Axis.X || isHead) {
          box = box.move((facingPlacement.getAxis() == Direction.Axis.X ?
              (facingPlacement.getAxisDirection() == Direction.AxisDirection.POSITIVE)
              : (playerYaw % 360 > (playerYaw > 0 ? 180 : -180))) ? 0.5 : -0.5, 0, 0);
        }
        if (facingBox.getAxis() == Direction.Axis.X || isHead) {
          box = box.move(0, 0, (facingPlacement.getAxis() == Direction.Axis.Z ?
              (facingPlacement.getAxisDirection() == Direction.AxisDirection.POSITIVE)
              : ((playerYaw - 90) % 360 > (playerYaw > 90 ? 180 : -180))) ? 0.5 : -0.5);
        }
      }
    }
    return box;
  }

  public static boolean collectArmorBlocks(Player player,
                                           ArmorCollectionData collectionData) {
    ItemStack stack = player.getMainHandItem();
    CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
    DataChiseledArmorPiece armorPiece =
        new DataChiseledArmorPiece(nbt, ((ItemChiseledArmor) stack.getItem()).armorType);
    Level world = player.level();
    AABB boxCollection = collectionData.getCollectionBox();
    AABB boxBlocks = new AABB(Math.floor(boxCollection.minX), Math.floor(boxCollection.minY),
        Math.floor(boxCollection.minZ),
        Math.ceil(boxCollection.maxX), Math.ceil(boxCollection.maxY),
        Math.ceil(boxCollection.maxZ));
    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    ArmorMovingPart movingPart = collectionData.getMovingPart();
    Direction facingBox = collectionData.getFacing();
    Vec3 orginBox = collectionData.getOriginBodyPart();
    float scale = 1 / (float) Math.pow(2, collectionData.getScale());
    int blocksCollected = 0;
    for (int i = (int) boxBlocks.minX; i <= boxBlocks.maxX; i++) {
      for (int j = (int) boxBlocks.minY; j <= boxBlocks.maxY; j++) {
        for (int k = (int) boxBlocks.minZ; k <= boxBlocks.maxZ; k++) {
          blocksCollected = collectBits(world, new BlockPos(i, j, k), api, boxCollection,
              facingBox, orginBox, scale, armorPiece, movingPart, blocksCollected);
        }
      }
    }
    if (blocksCollected > 0) {
      if (world.isClientSide) {
        ClientHelper.printChatMessageWithDeletion(
            "Imported " + blocksCollected + " block cop" + (blocksCollected > 1 ? "ies" : "y") +
                " at " + SCALE_TITLES[collectionData.getScale()] + " scale into the " +
                collectionData.getMovingPart().getName().toLowerCase());
      } else {
        armorPiece.saveToNBT(nbt);
        stack.setTag(nbt);
        player.inventoryMenu.sendAllDataToRemote();
      }
    }
    return blocksCollected > 0;
  }

  private static int collectBits(Level world, BlockPos pos, IChiselAndBitsAPI api,
                                 AABB boxCollection, Direction facingBox,
                                 Vec3 orginBox, float scale, DataChiseledArmorPiece armorPiece,
                                 ArmorMovingPart movingPart, int blocksCollected) {
    IBitAccess bitAccess;
    try {
      bitAccess = api.getBitAccess(world, pos);
    } catch (CannotBeChiseled e) {
      return blocksCollected;
    }
    IBitAccess bitAccessNew = api.createBitItem(ItemStack.EMPTY);
    if (bitAccessNew == null) {
      return blocksCollected;
    }

    boolean bitsCollected = false;
    for (int i = 0; i < 16; i++) {
      for (int j = 0; j < 16; j++) {
        for (int k = 0; k < 16; k++) {
          IBitBrush bit = bitAccess.getBitAt(i, j, k);
          if (bit.isAir() ||
              bit.getState() == BlocksExtraBitManipulation.bodyPartTemplate.defaultBlockState()) {
            continue;
          }

          double x = pos.getX() + i * Utility.PIXEL_D + 0.5 * Utility.PIXEL_D;
          double y = pos.getY() + j * Utility.PIXEL_D + 0.5 * Utility.PIXEL_D;
          double z = pos.getZ() + k * Utility.PIXEL_D + 0.5 * Utility.PIXEL_D;
          if (x < boxCollection.minX || x > boxCollection.maxX || y < boxCollection.minY
              || y > boxCollection.maxY || z < boxCollection.minZ || z > boxCollection.maxZ) {
            continue;
          }

          try {
            if (!world.isClientSide) {
              bitAccessNew.setBitAt(i, j, k, bit);
            }

            bitsCollected = true;
          } catch (SpaceOccupied e) {
          }
        }
      }
    }
    if (!world.isClientSide && bitsCollected) {
      com.phylogeny.extrabitmanipulation.armor.ArmorItem armorItem =
          new com.phylogeny.extrabitmanipulation.armor.ArmorItem(
              bitAccessNew.getBitsAsItem(null, ItemType.CHISELED_BLOCK, false));
      if (facingBox != Direction.NORTH) {

        armorItem.addGlOperation(
            GlOperation.createRotation((facingBox.toYRot() + 180) % 360, 0, 1, 0));
      }

      if (scale != 1) {
        armorItem.addGlOperation(GlOperation.createScale(scale, scale, scale));
      }

      AABB box = new AABB(pos);
      float x = (float) (box.minX - orginBox.x);
      float y = (float) (box.minY - orginBox.y);
      float z = (float) (box.minZ - orginBox.z);
      if (x != 0 || y != 0 || z != 0) {
        armorItem.addGlOperation(GlOperation.createTranslation(x, y, z));
      }

      armorPiece.addItemToPart(movingPart.getPartIndex(), armorItem);
    }
    if (bitsCollected) {
      blocksCollected++;
    }

    return blocksCollected;
  }

  @Override
  public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level,
                              List<Component> tooltip, TooltipFlag tooltipFlag) {
    boolean shiftDown = Screen.hasShiftDown();
    boolean ctrlDown = Screen.hasControlDown();
    ItemBitToolBase.addColorInformation(tooltip, shiftDown);
    CompoundTag nbt = stack.getTag();
    int mode = BitToolSettingsHelper.getArmorMode(nbt);
    boolean targetBits = BitToolSettingsHelper.areArmorBitsTargeted(nbt);
    if (shiftDown) {
      tooltip.add(Component.literal(
          ItemBitToolBase.colorSettingText(BitToolSettingsHelper.getArmorModeText(mode),
              Configs.armorMode)));
      tooltip.add(Component.literal(
          ItemBitToolBase.colorSettingText(BitToolSettingsHelper.getArmorScaleText(nbt),
              Configs.armorScale)));
      tooltip.add(Component.literal(ItemBitToolBase.colorSettingText(
          BitToolSettingsHelper.getArmorBitsTargetedText(targetBits), Configs.armorTargetBits)));
    }
    if (!ctrlDown || shiftDown) {
      tooltip.add(
          ItemBitToolBase.colorSettingText(BitToolSettingsHelper.getArmorMovingPartText(nbt, this),
              BitToolSettingsHelper.getArmorMovingPartConfig(armorType)));
    }
    if (shiftDown) {
      return;
    }

    if (!ctrlDown) {
      ItemBitToolBase.addKeyInformation(tooltip, true);
      return;
    }
    if (mode == 1) {
      String target = targetBits ? "bit" : "block";
      tooltip.add(Component.literal("Left click a " + target + ", drag to another "));
      tooltip.add(Component.literal("    " + target + ", then release to import copies"));
      if (targetBits) {
        tooltip.add(Component.literal("    of all intersecting bits into the"));
        tooltip.add(Component.literal("    selected moving part as blocks."));
      } else {
        tooltip.add(Component.literal("    of all intersecting blocks into the"));
        tooltip.add(Component.literal("    selected moving part."));
      }
    } else {
      tooltip.add(Component.literal("Left click a block to set the collection"));
      tooltip.add(Component.literal("    reference area for the bodypart"));
      tooltip.add(Component.literal("    template of the selected moving part."));
      tooltip.add(Component.literal("    (sneaking will allow the area to be"));
      tooltip.add(Component.literal("    placed outside of the block grid)"));
    }
    if (mode == 0) {
      tooltip.add(Component.literal("Right click to do so and fill that area"));
      tooltip.add(Component.literal("    with bits of bodypart template blocks."));
    }
    tooltip.add(Component.empty());
    String controlText =
        ItemBitToolBase.getColoredKeyBindText(KeyBindingsExtraBitManipulation.CONTROL);
    if (KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.getKeyBinding()
        .isDefault()) {
      tooltip.add(controlText + " right click to toggle mode.");
    } else {
      tooltip.add(controlText + " right click or press " +
          KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.getText());
      tooltip.add("    to open mapping/preview GUI.");
    }
    tooltip.add(controlText + " mouse wheel to cycle scale.");
    tooltip.add("");
    String altText = ItemBitToolBase.getColoredKeyBindText(KeyBindingsExtraBitManipulation.ALT);
    tooltip.add(altText + " right click to toggle collection");
    tooltip.add("     target between bits & blocks.");
    tooltip.add(altText + " mouse wheel to cycle moving part.");
    ItemBitToolBase.addKeybindReminders(tooltip, KeyBindingsExtraBitManipulation.SHIFT,
        KeyBindingsExtraBitManipulation.CONTROL);

  }

  public enum ArmorType {
    HELMET("Helmet", EntityEquipmentSlot.HEAD, 1),
    CHESTPLATE("Chestplate", EntityEquipmentSlot.CHEST, 3),
    LEGGINGS("Leggings", EntityEquipmentSlot.LEGS, 3),
    BOOTS("Boots", EntityEquipmentSlot.FEET, 2);

    private final String name;
    private final int movingpartCount;
    private final EntityEquipmentSlot equipmentSlot;
    @Environment(EnvType.CLIENT)
    private ItemStack iconStack;

    ArmorType(String name, int movingpartCount) {
      this(name, null, movingpartCount);
    }

    ArmorType(String name, @Nullable EntityEquipmentSlot equipmentSlot,
              int movingpartCount) {
      this.name = name;
      this.movingpartCount = movingpartCount;
      this.equipmentSlot = equipmentSlot;
    }

    public String getName() {
      return name;
    }

    public int getMovingpartCount() {
      return movingpartCount;
    }

    public @Nullable EquipmentSlot getEquipmentSlot() {
      return equipmentSlot;
    }

    @Environment(EnvType.CLIENT)
    public void initIconStack(Item item) {
      iconStack = new ItemStack(item);
    }

    @Environment(EnvType.CLIENT)
    public IBakedModel getIconModel() {
      return ClientHelper.getRenderItem()
          .getItemModelWithOverrides(iconStack, null, ClientHelper.getPlayer());
    }

    public int getSlotIndex(int indexArmorSet) {
      return ordinal() + (indexArmorSet - 1) * ChiseledArmorSlotsHandler.COUNT_TYPES;
    }

  }

  public enum BodyPartTemplate {
    HEAD("Head"),
    TORSO("Torso"),
    LIMB("Limb");

    private final String name;

    BodyPartTemplate(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

  }

  public enum ModelMovingPart {
    HEAD(BodyPartTemplate.HEAD, 6, 12, "Head"),
    BODY(BodyPartTemplate.TORSO, 1, 13, "Body"),
    ARM_RIGHT(BodyPartTemplate.LIMB, 3, 17, "Right Arm"),
    ARM_LEFT(BodyPartTemplate.LIMB, 2, 16, "Left Arm"),
    LEG_RIGHT(BodyPartTemplate.LIMB, 5, 19, "Right Leg"),
    LEG_LEFT(BodyPartTemplate.LIMB, 4, 18, "Left Leg");

    private final BodyPartTemplate template;
    private final int partIndexOverlay;
    private final int partIndexNoppes;
    private final String name;

    ModelMovingPart(BodyPartTemplate template, int partIndexOverlay, int partIndexNoppes,
                    String name) {
      this.template = template;
      this.partIndexOverlay = partIndexOverlay;
      this.partIndexNoppes = partIndexNoppes;
      this.name = name;
    }

    public BodyPartTemplate getBodyPartTemplate() {
      return template;
    }

    public int getPartIndexNoppes() {
      return partIndexNoppes;
    }

    public String getName() {
      return name;
    }

    public String getOverlayName() {
      return EnumPlayerModelParts.values()[partIndexOverlay].getName().getFormattedText();
    }

  }

  public enum ArmorMovingPart {
    HEAD(ModelMovingPart.HEAD, 0, 1, "Head"),
    TORSO(ModelMovingPart.BODY, 0, 2, "Torso"),
    PELVIS(ModelMovingPart.BODY, 0, 1, "Pelvis"),
    ARM_RIGHT(ModelMovingPart.ARM_RIGHT, 1, 1, "Right Arm"),
    ARM_LEFT(ModelMovingPart.ARM_LEFT, 2, 2, "Left Arm"),
    LEG_RIGHT(ModelMovingPart.LEG_RIGHT, 1, 3, 1, "Right Leg"),
    LEG_LEFT(ModelMovingPart.LEG_LEFT, 2, 2, "Left Leg"),
    FOOT_RIGHT(ModelMovingPart.LEG_RIGHT, 0, 1, "Right Foot"),
    FOOT_LEFT(ModelMovingPart.LEG_LEFT, 1, 2, "Left Foot");

    private final ModelMovingPart modelPart;
    private final int partIndex;
    private final int modelCount;
    private final int opaqueIndex;
    private final String name;
    @Environment(EnvType.CLIENT)
    private ModelResourceLocation[] iconModelLocationsDiamond, iconModelLocationsIron;

    ArmorMovingPart(ModelMovingPart modelPart, int partIndex, int modelCount, String name) {
      this(modelPart, partIndex, modelCount, 0, name);
    }

    ArmorMovingPart(ModelMovingPart modelPart, int partIndex, int modelCount,
                    int opaqueIndex, String name) {
      this.modelPart = modelPart;
      this.partIndex = partIndex;
      this.modelCount = modelCount;
      this.opaqueIndex = opaqueIndex;
      this.name = name;
    }

    public ModelMovingPart getModelMovingPart() {
      return modelPart;
    }

    public BodyPartTemplate getBodyPartTemplate() {
      return modelPart.getBodyPartTemplate();
    }

    public int getPartIndex() {
      return partIndex;
    }

    public String getName() {
      return name;
    }

    @Environment(EnvType.CLIENT)
    public IBakedModel[] getIconModels(ArmorMaterial material) {
      ModelResourceLocation[] iconModelLocations =
          material == ArmorMaterial.DIAMOND ? iconModelLocationsDiamond : iconModelLocationsIron;
      IBakedModel[] models = new IBakedModel[iconModelLocations.length];
      for (int i = 0; i < iconModelLocations.length; i++) {
        models[i] =
            ClientHelper.getBlockModelShapes().getModelManager().getModel(iconModelLocations[i]);
      }
      return models;
    }

    @Environment(EnvType.CLIENT)
    public static ResourceLocation[] initAndGetIconModelLocations() {
      List<ResourceLocation> modelLocations = new ArrayList<>();
      for (ArmorMovingPart part : ArmorMovingPart.values()) {
        part.iconModelLocationsDiamond = new ModelResourceLocation[part.modelCount];
        part.iconModelLocationsIron = new ModelResourceLocation[part.modelCount];
        for (int i = 0; i < part.modelCount; i++) {
          part.iconModelLocationsDiamond[i] =
              createIconModelLocation(part, i, ArmorMaterial.DIAMOND);
          part.iconModelLocationsIron[i] = createIconModelLocation(part, i, ArmorMaterial.IRON);
          modelLocations.add(part.iconModelLocationsDiamond[i]);
          modelLocations.add(part.iconModelLocationsIron[i]);
        }
      }
      return modelLocations.toArray(new ResourceLocation[modelLocations.size()]);
    }

    private static ModelResourceLocation createIconModelLocation(ArmorMovingPart part, int index,
                                                                 ArmorMaterial material) {
      return new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID, "moving_part_"
          + (index == part.opaqueIndex ? material.toString().toLowerCase() + "_" : "") +
          part.name.toLowerCase().replace(" ", "_") + "_" + index), null);
    }

  }

}