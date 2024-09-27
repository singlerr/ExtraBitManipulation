package com.phylogeny.extrabitmanipulation.client;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.phylogeny.extrabitmanipulation.api.ChiselsAndBitsAPIAccess;
import com.phylogeny.extrabitmanipulation.armor.LayerChiseledArmor;
import com.phylogeny.extrabitmanipulation.armor.ModelPartConcealer;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.gui.GuiBitToolSettingsMenu;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRender;
import com.phylogeny.extrabitmanipulation.config.ConfigShapeRenderPair;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper;
import com.phylogeny.extrabitmanipulation.helper.BitAreaHelper.ModelingBoxSet;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorBodyPartTemplateBoxData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ArmorCollectionData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.ModelReadData;
import com.phylogeny.extrabitmanipulation.helper.BitToolSettingsHelper.SculptingData;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.item.ItemChiseledArmor;
import com.phylogeny.extrabitmanipulation.item.ItemModelingTool;
import com.phylogeny.extrabitmanipulation.item.ItemSculptingTool;
import com.phylogeny.extrabitmanipulation.mixin.events.ClientMouseEvents;
import com.phylogeny.extrabitmanipulation.mixin.events.LivingEntityRenderEvents;
import com.phylogeny.extrabitmanipulation.mixin.events.PlayerTickEvents;
import com.phylogeny.extrabitmanipulation.mixin.events.TextureStitchCallback;
import com.phylogeny.extrabitmanipulation.mixin.events.UniversalKeyInputEvent;
import com.phylogeny.extrabitmanipulation.packet.PacketCollectArmorBlocks;
import com.phylogeny.extrabitmanipulation.packet.PacketCycleBitWrenchMode;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenBitMappingGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenChiseledArmorGui;
import com.phylogeny.extrabitmanipulation.packet.PacketOpenInventoryGui;
import com.phylogeny.extrabitmanipulation.packet.PacketReadBlockStates;
import com.phylogeny.extrabitmanipulation.packet.PacketSculpt;
import com.phylogeny.extrabitmanipulation.packet.PacketSetCollectionBox;
import com.phylogeny.extrabitmanipulation.packet.PacketThrowBit;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import com.phylogeny.extrabitmanipulation.reference.NBTKeys;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import com.phylogeny.extrabitmanipulation.reference.Utility;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.api.ModKeyBinding;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;
import org.lwjgl.util.glu.Disk;
import org.lwjgl.util.glu.Quadric;
import org.lwjgl.util.glu.Sphere;

public class ClientEventHandler {
  private static float millisecondsElapsed;
  private static Stopwatch timer;
  private static Vec3 drawnStartPoint = null;
  private static Vec3i drawnStartPointModelingTool = null;
  private static final ResourceLocation ARROW_HEAD =
      new ResourceLocation(Reference.MOD_ID, "textures/overlays/arrow_head.png");
  private static final ResourceLocation ARROW_BIDIRECTIONAL =
      new ResourceLocation(Reference.MOD_ID, "textures/overlays/arrow_bidirectional.png");
  private static final ResourceLocation ARROW_CYCLICAL =
      new ResourceLocation(Reference.MOD_ID, "textures/overlays/arrow_cyclical.png");
  private static final ResourceLocation CIRCLE =
      new ResourceLocation(Reference.MOD_ID, "textures/overlays/circle.png");
  private static final ResourceLocation INVERSION =
      new ResourceLocation(Reference.MOD_ID, "textures/overlays/inversion.png");
  private static final int[] DIRECTION_FORWARD = new int[] {2, 0, 5, 4, 1, 3};
  private static final int[] DIRECTION_BACKWARD = new int[] {1, 4, 0, 5, 3, 2};
  private static final int[] AXIS_FORWARD = new int[] {2, 3, 4, 5, 0, 1};
  private static final int[] AXIS_BACKWARD = new int[] {4, 5, 0, 1, 2, 3};
  private static final int[] SHAPE_CURVED = new int[] {1, 2, 0, 0, 0, 0, 0};
  //	private static final int[] SHAPE_FLAT = new int[]{3, 3, 3, 4, 5, 6, 3}; TODO
  private static final int[] SHAPE_FLAT = new int[] {3, 3, 3, 6, 3, 3, 3};
  private static boolean keyThrowBitIsDown;
  private static final double BOUNDING_BOX_OFFSET = 0.0020000000949949026D;
  private static final Map<UUID, ItemStack[]> invisibleArmorMap = new HashMap<>();
  private static final Map<UUID, ModelPartConcealer> concealedModelPartsMap = new HashMap<>();

  public static void registerEventListeners() {
    TextureStitchCallback.POST.register(ClientEventHandler::registerTextures);
    ClientPlayConnectionEvents.JOIN.register(ClientEventHandler::clearDisplayListsMaps);
    LivingEntityRenderEvents.BEFORE.register(
        ClientEventHandler::preventArmorAndPlayerModelPartRendering_pre);
    LivingEntityRenderEvents.AFTER.register(
        ClientEventHandler::preventArmorAndPlayerModelPartRendering_post);
    UniversalKeyInputEvent.EVENT.register(ClientEventHandler::onKeyInput);
    PlayerTickEvents.START.register(ClientEventHandler::throwBit);
    ClientMouseEvents.EVENT.register(ClientEventHandler::interceptMouseInput);
    WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ClientEventHandler::cancelBoundingBoxDraw);
    WorldRenderEvents.LAST.register(ClientEventHandler::renderBoxesSpheresAndOverlays);
  }

  private static void registerTextures(@SuppressWarnings("unused") TextureAtlas event) {
    registerTexture(ARROW_HEAD);
    registerTexture(ARROW_BIDIRECTIONAL);
    registerTexture(ARROW_CYCLICAL);
    registerTexture(CIRCLE);
    registerTexture(INVERSION);
    RenderLayersExtraBitManipulation.clearRenderMaps();
  }

  private static void registerTexture(ResourceLocation resourceLocation) {
    SimpleTexture texture = new SimpleTexture(resourceLocation);
    Minecraft.getInstance().getTextureManager().register(resourceLocation, texture);
  }

  private static void clearDisplayListsMaps(
      ClientPacketListener handler, PacketSender sender, Minecraft client) {
    ClientHelper.getThreadListener().execute(
        RenderLayersExtraBitManipulation::clearRenderMaps);
  }

  private static boolean preventArmorAndPlayerModelPartRendering_pre(LivingEntity livingEntity,
                                                                     float partialTicks,
                                                                     LivingEntityRenderer<?, ?> renderer) {
    boolean isPlayerModelAlt =
        LayerChiseledArmor.isPlayerModelAlt(livingEntity, partialTicks);
    Entity entity = isPlayerModelAlt ? Minecraft.getInstance().player : livingEntity;
    if (!(entity instanceof Player player)) {
      return false;
    }

    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player).orElse(null);
    if (cap == null || !cap.hasArmor() || !cap.hasArmorSet(0)) {
      return false;
    }

    ItemStack[] armor = new ItemStack[4];
    NonNullList<ItemStack> armorInventory = player.getInventory().armor;
    boolean found = false;
    for (int i = 0; i < 4; i++) {
      ItemStack stack = armorInventory.get(i);
      ItemStack stackVanity = cap.getStackInSlot(3 - i);
      if (!stackVanity.isEmpty() && !stack.isEmpty()) {
        armor[i] = stack;
        armorInventory.set(i, ItemStack.EMPTY);
        found = true;
      }
    }
    if (found) {
      invisibleArmorMap.put(player.getUUID(), armor);
    }

    Model model = renderer.getModel();
    if (!(model instanceof HumanoidModel<?>)) {
      return false;
    }

    ModelPartConcealer modelPartConcealer = cap.getAndApplyModelPartConcealer(model);
    if (modelPartConcealer != null && !modelPartConcealer.isEmpty()) {
      concealedModelPartsMap.put(player.getUUID(), modelPartConcealer);
      RenderLayersExtraBitManipulation.forceUpdateModels(!isPlayerModelAlt);
    }

    return false;
  }

  private static void preventArmorAndPlayerModelPartRendering_post(LivingEntity livingEntity,
                                                                   float partialTicks,
                                                                   LivingEntityRenderer<?, ?> renderer) {
    boolean isPlayerModelAlt =
        LayerChiseledArmor.isPlayerModelAlt(livingEntity, partialTicks);
    Entity entity = isPlayerModelAlt ? Minecraft.getInstance().player : livingEntity;
    if (!(entity instanceof Player player)) {
      return;
    }

    IChiseledArmorSlotsHandler cap = ChiseledArmorSlotsHandler.getCapability(player).orElse(null);
    if (cap == null) {
      return;
    }

    ItemStack[] armor = invisibleArmorMap.get(player.getUUID());
    if (armor != null) {
      for (int i = 0; i < 4; i++) {
        if (armor[i] != null) {
          player.getInventory().armor.set(i, armor[i]);
        }
      }
      invisibleArmorMap.remove(player.getUUID());
    }
    ModelPartConcealer modelPartConcealer = concealedModelPartsMap.get(player.getUUID());
    if (modelPartConcealer == null) {
      return;
    }

    Model model = renderer.getModel();
    if (model instanceof HumanoidModel<?>) {
      modelPartConcealer.restoreModelPartVisibility((HumanoidModel<?>) model);
    }

    concealedModelPartsMap.remove(player.getUUID());
    RenderLayersExtraBitManipulation.forceUpdateModels(!isPlayerModelAlt);
  }

  private static void onKeyInput() {
    if (timer == null) {
      timer = Stopwatch.createStarted();
    }

    ItemStack stack = ClientHelper.getHeldItemMainhand();
    keyThrowBitIsDown =
        (ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.CHISELED_BIT ?
            KeyBindingsExtraBitManipulation.THROW_BIT :
            KeyBindingsExtraBitManipulation.THROW_BIT_BIT_BAG).isKeyDown();
    if (ChiselsAndBitsAPIAccess.apiInstance.getKeyBinding(ModKeyBinding.MODE_MENU).isDown()
        || KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.isKeyDown()
        || KeyBindingsExtraBitManipulation.EDIT_DESIGN.isKeyDown()
        || KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_GUI.isKeyDown()
        || KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_SLOTS_GUI.isKeyDown()) {
      if (KeyBindingsExtraBitManipulation.OPEN_BIT_MAPPING_GUI.isKeyDown()) {
        if (ItemStackHelper.isModelingToolStack(stack) &&
            ItemStackHelper.hasKey(stack, NBTKeys.SAVED_STATES)) {
          openBitMappingGui();
        }
      } else if (KeyBindingsExtraBitManipulation.EDIT_DESIGN.isKeyDown()) {
        if (stack.hasTag() && ItemStackHelper.isDesignStack(stack)) {
          openBitMappingGui();
        }
      } else if (KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_GUI.isKeyDown()) {
        if (ChiseledArmorSlotsHandler.findNextArmorSetIndex(ChiseledArmorSlotsHandler.COUNT_SETS) >=
            0) {
          ClientPlayNetworking.send(new PacketOpenChiseledArmorGui());
        } else {
          ClientHelper.printChatMessageWithDeletion(
              "You must be wearing at least one piece of Chiseled Armor to open the Chiseled Armor GUI.");
        }
      } else if (KeyBindingsExtraBitManipulation.OPEN_CHISELED_ARMOR_SLOTS_GUI.isKeyDown()) {
        ClientPlayNetworking.send(new PacketOpenInventoryGui(false));
      } else if (ItemStackHelper.isBitToolStack(stack) ||
          ItemStackHelper.isChiseledArmorStack(stack)) {
        Minecraft.getInstance().setScreen(new GuiBitToolSettingsMenu());
      }
    }
  }

  private static void throwBit(Player player) {
    if (!keyThrowBitIsDown) {
      return;
    }

    ItemStack stack = ClientHelper.getHeldItemMainhand();
    boolean isBit = ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.CHISELED_BIT;
    if (!stack.isEmpty() &&
        (ChiselsAndBitsAPIAccess.apiInstance.getItemType(stack) == ItemType.BIT_BAG
            || (timer.elapsed(TimeUnit.MILLISECONDS) > 150 && isBit))) {
      if (isBit) {
        timer = Stopwatch.createStarted();
      }

      ClientPlayNetworking.send(new PacketThrowBit());
    }
  }

  private static void openBitMappingGui() {
    ClientPlayNetworking.send(new PacketOpenBitMappingGui());

  }

  private static boolean interceptMouseInput(double deltaX, double deltaY, int button) {

    boolean cancel = false;
    Player player = ClientHelper.getPlayer();
    if (deltaY != 0) {
      ItemStack stack = player.getMainHandItem();
      if (ItemStackHelper.isBitToolStack(stack)) {
        boolean forward = deltaX < 0;
        if (KeyBindingsExtraBitManipulation.SHIFT.isKeyDown()) {
          if (ItemStackHelper.isBitWrenchItem(stack.getItem())) {
            ClientPlayNetworking.send(new PacketCycleBitWrenchMode(forward));
          } else {
            cycleSemiDiameter(player, stack, forward);
          }
          cancel = true;
        } else if (ItemStackHelper.isSculptingToolItem(stack.getItem())
            && (KeyBindingsExtraBitManipulation.CONTROL.isKeyDown()
            || KeyBindingsExtraBitManipulation.ALT.isKeyDown())) {
          if (KeyBindingsExtraBitManipulation.CONTROL.isKeyDown()) {
            cycleDirection(player, stack, forward);
          } else {
            cycleWallThickness(player, stack, forward);
          }
          cancel = true;
        }
      } else {
        drawnStartPoint = null;
      }
    } else if ((KeyBindingsExtraBitManipulation.CONTROL.isKeyDown() ||
        KeyBindingsExtraBitManipulation.ALT.isKeyDown()) && button != -1) {
      ItemStack stack = player.getMainHandItem();
      Item item = stack.getItem();
      if (ItemStackHelper.isSculptingToolItem(item)) {
        if (KeyBindingsExtraBitManipulation.CONTROL.isKeyDown()) {
          if (button == 1) {
            cycleShapeType(player, stack, item);
          }

          if (button == 0) {
            toggleBitGridTargeted(player, stack);
          }
        } else {
          if (button == 1) {
            toggleHollowShape(player, stack, item);
          }

          if (button == 0) {
            toggleOpenEnds(player, stack);
          }
        }
        cancel = true;
      }
    } else if (button == 0) {

//      if (!player.capabilities.allowEdit) {
////        return cancel;
////      }

      ItemStack stack = player.getMainHandItem();
      Item item = stack.getItem();
      if (ItemStackHelper.isChiseledArmorItem(item)) {
        HitResult target = ClientHelper.getObjectMouseOver();
        if (target != null && target.getType() == HitResult.Type.BLOCK) {
          CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
          int mode = BitToolSettingsHelper.getArmorMode(nbt);
          if (mode == 0) {
            BlockHitResult blockHitResult = (BlockHitResult) target;
            ClientPlayNetworking.send(
                new PacketSetCollectionBox(player.yHeadRot, player.isShiftKeyDown(),
                    player.getDirection().getOpposite(), blockHitResult.getBlockPos(),
                    blockHitResult.getDirection(), target.getLocation()));
            ClientHelper.printChatMessageWithDeletion("Set collection reference area for a " +
                ItemChiseledArmor.getPartAndScaleText(BitToolSettingsHelper.getArmorMovingPart(nbt,
                    (ItemChiseledArmor) item), BitToolSettingsHelper.getArmorScale(nbt)));
            cancel = true;
          }
        }
      }
      if (button != -1 && ItemStackHelper.isBitWrenchItem(item)) {
        cancel = true;
      } else if ((ItemStackHelper.isChiseledArmorItem(item) &&
          BitToolSettingsHelper.getArmorMode(ItemStackHelper.getNBTOrNew(stack)) == 1)
          || ItemStackHelper.isSculptingToolItem(item)) {
        boolean isArmor = ItemStackHelper.isChiseledArmorItem(item);
        boolean drawnMode =
            isArmor || BitToolSettingsHelper.getSculptMode(stack.getTag()) == 2;
        if (!drawnMode) {
          drawnStartPoint = null;
        }

        if (button != -1 || (drawnMode && drawnStartPoint != null)) {
          boolean removeBits = isArmor || ((ItemSculptingTool) item).removeBits();
          HitResult target = ClientHelper.getObjectMouseOver();
          boolean shiftDown = KeyBindingsExtraBitManipulation.SHIFT.isKeyDown();
          if (target != null && target.getType() != HitResult.Type.MISS) {
            if (target.getType() == HitResult.Type.BLOCK) {
              BlockHitResult blockHitResult = (BlockHitResult) target;
              BlockPos pos = blockHitResult.getBlockPos();
              Direction side = blockHitResult.getDirection();
              Vec3 hit = target.getLocation();
              boolean swingTool = true;
              if (drawnMode && button != -1 && drawnStartPoint != null) {
                cancel = true;
                return cancel;
              }
              if ((isArmor || !shiftDown) && drawnMode && button != -1) {
                if (isArmor && !ItemStackHelper.getNBTOrNew(stack).contains(NBTKeys.ARMOR_HIT)) {
                  ClientHelper.printChatMessageWithDeletion(
                      "You must set a bodypart template reference area in 'Template Creation' " +
                          "mode before collecting block copies.");
                  swingTool = false;
                } else {
                  IBitLocation bitLoc =
                      ChiselsAndBitsAPIAccess.apiInstance.getBitPos((float) hit.x - pos.getX(),
                          (float) hit.y - pos.getY(), (float) hit.z - pos.getZ(), side, pos, false);
                  if (bitLoc != null) {
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    float x2 = x + bitLoc.getBitX() * Utility.PIXEL_F;
                    float y2 = y + bitLoc.getBitY() * Utility.PIXEL_F;
                    float z2 = z + bitLoc.getBitZ() * Utility.PIXEL_F;
                    if (!removeBits) {
                      x2 += side.getStepX() * Utility.PIXEL_F;
                      y2 += side.getStepY() * Utility.PIXEL_F;
                      z2 += side.getStepZ() * Utility.PIXEL_F;
                    }
                    drawnStartPoint = new Vec3(x2, y2, z2);
                    if (isArmor && player.isShiftKeyDown()) {
                      Vec3 vec = new Vec3(side.getStepX(), side.getStepY(),
                          side.getStepZ());
                      if (BitToolSettingsHelper.areArmorBitsTargeted(
                          ItemStackHelper.getNBTOrNew(stack))) {
                        vec = vec.multiply(Utility.PIXEL_D, Utility.PIXEL_D, Utility.PIXEL_D);
                      }

                      drawnStartPoint = drawnStartPoint.add(vec);
                    }
                  } else {
                    drawnStartPoint = null;
                    swingTool = false;
                  }
                }
              } else {
                if (shiftDown && !isArmor) {
                  IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
                  IBitLocation bitLoc =
                      api.getBitPos((float) hit.x - pos.getX(), (float) hit.y - pos.getY(),
                          (float) hit.z - pos.getZ(), side, pos, false);
                  if (bitLoc != null) {
                    try {
                      IBitAccess bitAccess = api.getBitAccess(player.level(), pos);
                      IBitBrush bit =
                          bitAccess.getBitAt(bitLoc.getBitX(), bitLoc.getBitY(), bitLoc.getBitZ());
                      BitToolSettingsHelper.setBitStack(player, stack, removeBits, bit,
                          removeBits ? Configs.sculptSetBitWire : Configs.sculptSetBitSpade);
                      if ((removeBits ? Configs.sculptSetBitWire :
                          Configs.sculptSetBitSpade).shouldDisplayInChat()) {
                        ClientHelper.printChatMessageWithDeletion(
                            (removeBits ? "Removing only " : "Sculpting with ")
                                + BitToolSettingsHelper.getBitName(bit.getItemStack(1)));
                      }
                    } catch (CannotBeChiseled e) {
                      cancel = true;
                      return cancel;
                    }
                  }
                } else if (!shiftDown || removeBits || drawnMode) {
                  if (isArmor) {
                    CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
                    ArmorCollectionData collectionData =
                        new ArmorCollectionData(nbt, (ItemChiseledArmor) item,
                            getDrawnArmorCollectionBox(player, nbt, side, pos, hit));
                    swingTool = ItemChiseledArmor.collectArmorBlocks(player, collectionData);
                    ClientPlayNetworking.send(new PacketCollectArmorBlocks(collectionData));
                  } else {
                    SculptingData sculptingData =
                        new SculptingData(stack.getTag(), (ItemSculptingTool) item);
                    swingTool =
                        ((ItemSculptingTool) item).sculptBlocks(stack, player, player.level(), pos,
                            side, hit, drawnStartPoint, sculptingData);
                    ClientPlayNetworking.send(
                        new PacketSculpt(pos, side, hit, drawnStartPoint, sculptingData));
                  }
                }
                if (drawnMode) {
                  drawnStartPoint = null;
                }
              }
              if (swingTool) {
                player.swing(InteractionHand.MAIN_HAND);
              }

              cancel = true;
            }
          } else if (shiftDown && button != -1 && removeBits && !isArmor) {
            BitToolSettingsHelper.setBitStack(player, stack, true, null, Configs.sculptSetBitWire);
            if ((removeBits ? Configs.sculptSetBitWire :
                Configs.sculptSetBitSpade).shouldDisplayInChat()) {
              ClientHelper.printChatMessageWithDeletion("Removing any/all bits");
            }
          } else if (drawnMode) {
            drawnStartPoint = null;
          }
        }
      }
    } else if (button == 1) {
      ItemStack stack = player.getMainHandItem();
      if (ItemStackHelper.isModelingToolStack(stack) &&
          ItemStackHelper.hasKey(stack, NBTKeys.SAVED_STATES)
          && KeyBindingsExtraBitManipulation.SHIFT.isKeyDown()) {
        openBitMappingGui();
        cancel = true;
      }
    }
    if (!cancel && button == 1) {
      ItemStack stack = player.getMainHandItem();
      if (ItemStackHelper.isSculptingToolStack(stack)) {
        cycleSculptMode(player, stack, !KeyBindingsExtraBitManipulation.SHIFT.isKeyDown());
      }
    }
    if (deltaY != 0) {
      ItemStack stack = player.getMainHandItem();
      boolean isArmor = ItemStackHelper.isChiseledArmorStack(stack);
      if (isArmor || ItemStackHelper.isModelingToolStack(stack)) {
        boolean forward = deltaY < 0;
        boolean controlDown = KeyBindingsExtraBitManipulation.CONTROL.isKeyDown();
        if (controlDown || (isArmor ? KeyBindingsExtraBitManipulation.ALT.isKeyDown() :
            KeyBindingsExtraBitManipulation.SHIFT.isKeyDown())) {
          if (controlDown) {
            if (isArmor) {
              cycleArmorScale(player, stack, forward);
            } else {
              cycleModelSnapMode(player, stack, forward);
            }
          } else {
            if (isArmor) {
              cycleArmorMovingPart(player, stack, forward);
            } else {
              cycleModelAreaMode(player, stack, forward);
            }
          }
          cancel = true;
        }
      } else {
        drawnStartPointModelingTool = null;
      }
    } else if ((KeyBindingsExtraBitManipulation.CONTROL.isKeyDown() ||
        KeyBindingsExtraBitManipulation.ALT.isKeyDown())) {
      ItemStack stack = player.getMainHandItem();
      boolean isArmor = ItemStackHelper.isChiseledArmorStack(stack);
      boolean controlDown = KeyBindingsExtraBitManipulation.CONTROL.isKeyDown();
      if (isArmor || (ItemStackHelper.isModelingToolStack(stack) && controlDown)) {
        if (button == 1) {
          if (isArmor) {
            if (controlDown) {
              toggleArmorMode(player, stack);
            } else {
              toggleArmorBitsTargeted(player, stack);
            }
          } else {
            toggleModelGuiOpen(player, stack);
          }
        }
        cancel = true;
      }
    } else if (button == 0) {
      ItemStack stack = player.getMainHandItem();
      if (ItemStackHelper.isModelingToolStack(stack)) {
        Item item = stack.getItem();
        if (item != null) {
          boolean drawnMode = BitToolSettingsHelper.getModelAreaMode(stack.getTag()) == 2;
          if (!drawnMode) {
            drawnStartPointModelingTool = null;
          }

          if ((drawnMode && drawnStartPointModelingTool != null)) {
            HitResult target = ClientHelper.getObjectMouseOver();
            if (target != null && target.getType() != HitResult.Type.MISS) {
              if (target.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) target;
                BlockPos pos = blockHitResult.getBlockPos();
                Vec3 hit = blockHitResult.getLocation();
                boolean swingTool = true;
                if (drawnMode && drawnStartPointModelingTool != null) {
                  cancel = true;
                  return cancel;
                }
                if (!KeyBindingsExtraBitManipulation.SHIFT.isKeyDown() && drawnMode) {
                  drawnStartPointModelingTool = new Vec3i(pos.getX(), pos.getY(), pos.getZ());
                } else {
                  if (!KeyBindingsExtraBitManipulation.SHIFT.isKeyDown() || drawnMode) {
                    ModelReadData modelingData = new ModelReadData(stack.getTag());
                    swingTool =
                        BitAreaHelper.readBlockStates(stack, player, player.level(), pos, hit,
                            drawnStartPointModelingTool, modelingData);
                    ClientPlayNetworking.send(new PacketReadBlockStates(pos, hit,
                        drawnStartPointModelingTool, modelingData));
                  }
                  if (drawnMode) {
                    drawnStartPointModelingTool = null;
                  }
                }
                if (swingTool) {
                  player.swing(InteractionHand.MAIN_HAND);
                }

                cancel = true;
              }
            } else if (drawnMode) {
              drawnStartPointModelingTool = null;
            }
          }
        }
      }
    }

    return cancel;
  }

  private static void toggleArmorMode(Player player, ItemStack stack) {
    int mode =
        BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getArmorMode(stack.getTag()),
            true, ItemChiseledArmor.MODE_TITLES.length);
    BitToolSettingsHelper.setArmorMode(player, stack, mode, Configs.armorMode);
    if (Configs.armorMode.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getArmorModeText(mode));
    }
  }

  private static void toggleArmorBitsTargeted(Player player, ItemStack stack) {
    boolean targetBits = !BitToolSettingsHelper.areArmorBitsTargeted(stack.getTag());
    BitToolSettingsHelper.setArmorBitsTargeted(player, stack, targetBits, Configs.armorTargetBits);
    if (Configs.armorTargetBits.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getArmorBitsTargetedText(targetBits));
    }
  }

  private static void cycleArmorScale(Player player, ItemStack stack, boolean forward) {
    int scale =
        BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getArmorScale(stack.getTag()),
            forward, ItemChiseledArmor.SCALE_TITLES.length);
    BitToolSettingsHelper.setArmorScale(player, stack, scale, Configs.armorScale);
    if (Configs.armorScale.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getArmorScaleText(scale));
    }
  }

  private static void cycleArmorMovingPart(Player player, ItemStack stack, boolean forward) {
    ItemChiseledArmor armorPiece = (ItemChiseledArmor) stack.getItem();
    int partIndex =
        BitToolSettingsHelper.getArmorMovingPart(stack.getTag(), armorPiece).getPartIndex();
    partIndex =
        BitToolSettingsHelper.cycleData(partIndex, forward, armorPiece.MOVING_PART_TITLES.length);
    BitToolSettingsHelper.setArmorMovingPart(player, stack, armorPiece, partIndex);
    if (BitToolSettingsHelper.getArmorMovingPartConfig(armorPiece.armorType)
        .shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getArmorMovingPartText(armorPiece.MOVING_PARTS[partIndex],
              armorPiece));
    }
  }

  private static void cycleModelAreaMode(Player player, ItemStack stack, boolean forward) {
    int mode = BitToolSettingsHelper.cycleData(
        BitToolSettingsHelper.getModelAreaMode(stack.getTag()),
        forward, ItemModelingTool.AREA_MODE_TITLES.length);
    BitToolSettingsHelper.setModelAreaMode(player, stack, mode, Configs.modelAreaMode);
    if (Configs.modelAreaMode.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getModelAreaModeText(mode));
    }
  }

  private static void cycleModelSnapMode(Player player, ItemStack stack, boolean forward) {
    int mode = BitToolSettingsHelper.cycleData(
        BitToolSettingsHelper.getModelSnapMode(stack.getTag()),
        forward, ItemModelingTool.SNAP_MODE_TITLES.length);
    BitToolSettingsHelper.setModelSnapMode(player, stack, mode, Configs.modelSnapMode);
    if (Configs.modelSnapMode.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getModelSnapModeText(mode));
    }
  }

  private static void toggleModelGuiOpen(Player player, ItemStack stack) {
    boolean modelGuiOpen = !BitToolSettingsHelper.getModelGuiOpen(stack.getTag());
    BitToolSettingsHelper.setModelGuiOpen(player, stack, modelGuiOpen, Configs.modelGuiOpen);
    if (Configs.modelGuiOpen.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getModelGuiOpenText(modelGuiOpen));
    }
  }

  private static void cycleSculptMode(Player player, ItemStack stack, boolean forward) {
    int mode =
        BitToolSettingsHelper.cycleData(BitToolSettingsHelper.getSculptMode(stack.getTag()),
            forward, ItemSculptingTool.MODE_TITLES.length);
    BitToolSettingsHelper.setSculptMode(player, stack, mode, Configs.sculptMode);
    if (Configs.sculptMode.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getSculptModeText(mode));
    }
  }

  private static void cycleDirection(Player player, ItemStack stack, boolean forward) {
    CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
    int direction = BitToolSettingsHelper.getDirection(nbt);
    int shapeType =
        BitToolSettingsHelper.getShapeType(nbt, ((ItemSculptingTool) stack.getItem()).isCurved());
    int rotation = direction / 6;
    direction %= 6;
    if (!(shapeType == 4 && (forward ? rotation != 1 : rotation != 0)) &&
        !(shapeType == 5 && (forward ? rotation != 3 : rotation != 0))) {
      direction = shapeType == 2 || shapeType > 3 ?
          (forward ? DIRECTION_FORWARD[direction] : DIRECTION_BACKWARD[direction])
          : (forward ? AXIS_FORWARD[direction] : AXIS_BACKWARD[direction]);
      rotation = forward ? 0 : (shapeType == 4 ? 1 : 3);
    } else {
      rotation = shapeType == 4 ? (rotation == 0 ? 1 : 0) :
          BitToolSettingsHelper.cycleData(rotation, forward, 4);
    }
    direction += 6 * rotation;
    BitToolSettingsHelper.setDirection(player, stack, direction, Configs.sculptDirection);
    if (Configs.sculptDirection.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getDirectionText(direction, shapeType == 4 || shapeType == 5));
    }
  }

  private static void cycleShapeType(Player player, ItemStack stack, Item item) {
    boolean isCurved = ((ItemSculptingTool) item).isCurved();
    CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
    int shapeType = BitToolSettingsHelper.getShapeType(nbt, isCurved);
    shapeType = isCurved ? SHAPE_CURVED[shapeType] : SHAPE_FLAT[shapeType];
    BitToolSettingsHelper.setShapeType(player, stack, isCurved, shapeType,
        isCurved ? Configs.sculptShapeTypeCurved : Configs.sculptShapeTypeFlat);
    if ((isCurved ? Configs.sculptShapeTypeCurved :
        Configs.sculptShapeTypeFlat).shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getShapeTypeText(shapeType));
    }
  }

  private static void toggleBitGridTargeted(Player player, ItemStack stack) {
    boolean targetBitGrid = !BitToolSettingsHelper.isBitGridTargeted(stack.getTag());
    BitToolSettingsHelper.setBitGridTargeted(player, stack, targetBitGrid,
        Configs.sculptTargetBitGridVertexes);
    if (Configs.sculptTargetBitGridVertexes.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getBitGridTargetedText(targetBitGrid));
    }
  }

  private static void cycleSemiDiameter(Player player, ItemStack stack, boolean forward) {
    int semiDiameter = BitToolSettingsHelper.cycleData(
        BitToolSettingsHelper.getSemiDiameter(stack.getTag()),
        forward, Configs.maxSemiDiameter);
    BitToolSettingsHelper.setSemiDiameter(player, stack, semiDiameter, Configs.sculptSemiDiameter);
    if (Configs.sculptSemiDiameter.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getSemiDiameterText(stack.getTag(), semiDiameter));
    }
  }

  private static void toggleHollowShape(Player player, ItemStack stack, Item item) {
    boolean isWire = ((ItemSculptingTool) item).removeBits();
    boolean isHollowShape = !BitToolSettingsHelper.isHollowShape(stack.getTag(), isWire);
    BitToolSettingsHelper.setHollowShape(player, stack, isWire, isHollowShape,
        isWire ? Configs.sculptHollowShapeWire : Configs.sculptHollowShapeSpade);
    if ((isWire ? Configs.sculptHollowShapeWire :
        Configs.sculptHollowShapeSpade).shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getHollowShapeText(isHollowShape));
    }
  }

  private static void toggleOpenEnds(Player player, ItemStack stack) {
    boolean areEndsOpen = !BitToolSettingsHelper.areEndsOpen(stack.getTag());
    BitToolSettingsHelper.setEndsOpen(player, stack, areEndsOpen, Configs.sculptOpenEnds);
    if (Configs.sculptOpenEnds.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(BitToolSettingsHelper.getOpenEndsText(areEndsOpen));
    }
  }

  private static void cycleWallThickness(Player player, ItemStack stack, boolean forward) {
    int wallThickness = BitToolSettingsHelper.cycleData(
        BitToolSettingsHelper.getWallThickness(stack.getTag()),
        forward, Configs.maxWallThickness);
    BitToolSettingsHelper.setWallThickness(player, stack, wallThickness,
        Configs.sculptWallThickness);
    if (Configs.sculptWallThickness.shouldDisplayInChat()) {
      ClientHelper.printChatMessageWithDeletion(
          BitToolSettingsHelper.getWallThicknessText(wallThickness));
    }
  }

  private static boolean cancelBoundingBoxDraw(WorldRenderContext context,
                                               @Nullable HitResult hitResult) {
    ItemStack stack = ClientHelper.getPlayer().getMainHandItem();
    return !ItemStackHelper.isSculptingToolStack(stack) ||
        BitToolSettingsHelper.getSculptMode(stack.getTag()) != 1;
  }

  private static void renderBoxesSpheresAndOverlays(WorldRenderContext context) {
    Player player = ClientHelper.getPlayer();
    Level world = player.level();
    ItemStack stack = player.getMainHandItem();
    PoseStack poseStack = context.matrixStack();
    RenderType renderType = ItemBlockRenderTypes.getRenderType(stack, false);
    VertexConsumer vertexConsumer = context.consumers().getBuffer(renderType);
    if (stack.isEmpty()) {
      return;
    }
    HitResult target = ClientHelper.getObjectMouseOver();
    Item item = stack.getItem();
    boolean hitBlock = target != null && target.getType().equals(HitResult.Type.BLOCK);
    boolean isArmor = ItemStackHelper.isChiseledArmorItem(item);
    if (!isArmor && (!hitBlock || !ItemStackHelper.isBitToolItem(item))) {
      return;
    }

    IChiselAndBitsAPI api = ChiselsAndBitsAPIAccess.apiInstance;
    float ticks = context.tickDelta();

    double playerX = player.xOld + (player.getX() - player.xOld) * ticks;
    double playerY = player.yOld + (player.getY() - player.yOld) * ticks;
    double playerZ = player.zOld + (player.getZ() - player.zOld) * ticks;
    Tesselator t = Tesselator.getInstance();
    BufferBuilder buffer = t.getBuilder();
    if (isArmor) {
      CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
      if (nbt.contains(NBTKeys.ARMOR_HIT)) {
        ArmorBodyPartTemplateBoxData boxData =
            new ArmorBodyPartTemplateBoxData(nbt, (ItemChiseledArmor) item);
        renderBodyPartTemplate(poseStack, vertexConsumer, playerX, playerY, playerZ,
            boxData.getFacingBox(), t, buffer,
            boxData.getBox(), 0.0F);
      }
      if (!hitBlock) {
        return;
      }
    }
    @SuppressWarnings("null")
    BlockHitResult blockHitResult = (BlockHitResult) target;
    Direction dir = blockHitResult.getDirection();
    BlockPos pos = blockHitResult.getBlockPos();
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
    double diffX = playerX - x;
    double diffY = playerY - y;
    double diffZ = playerZ - z;
    Vec3 hit = target.getLocation();
    if (ItemStackHelper.isBitWrenchItem(item) &&
        api.isBlockChiseled(world, blockHitResult.getBlockPos()) &&
        !Configs.disableOverlays) {
      int mode = ItemStackHelper.getNBTOrNew(stack).getInt(NBTKeys.WRENCH_MODE);
      if (timer == null) {
        timer = Stopwatch.createStarted();
      }

      millisecondsElapsed = timer.elapsed(TimeUnit.MILLISECONDS);
      int side = dir.ordinal();
      boolean upDown = side <= 1;
      boolean eastWest = side >= 4;
      boolean northSouth = !upDown && !eastWest;
      AABB box = new AABB(eastWest ? hit.x : x, upDown ? hit.y : y, northSouth ? hit.z : z,
          eastWest ? hit.x : x + 1, upDown ? hit.y : y + 1, northSouth ? hit.z : z + 1);

      int offsetX = Math.abs(dir.getStepX());
      int offsetY = Math.abs(dir.getStepY());
      int offsetZ = Math.abs(dir.getStepZ());
      double invOffsetX = offsetX ^ 1;
      double invOffsetY = offsetY ^ 1;
      double invOffsetZ = offsetZ ^ 1;


      boolean invertDirection = KeyBindingsExtraBitManipulation.SHIFT.isKeyDown();
      poseStack.pushPose();
      GlStateManager.disableLighting();
      GlStateManager.enableAlpha();
      GlStateManager._enableBlend();
      GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GlStateManager.enableTexture2D();
      poseStack.pushPose();
      double angle = getInitialAngle(mode);
      if (mode == 3) {
        if (side % 2 == 1) {
          angle += 180;
        }

        if (side >= 4) {
          angle -= 90;
        }
      } else {
        if (mode == 0) {
          if (side % 2 == (invertDirection ? 0 : 1)) {
            angle *= -1;
          }
        } else {
          if (side < 2 || side > 3) {
            angle *= -1;
          }
        }
        if (eastWest) {
          angle += 90;
        }

        if (side == (mode == 1 ? 1 : 0) || side == 3 || side == 4) {
          angle += 180;
        }
      }
      double offsetX2 = 0.5 * invOffsetX;
      double offsetY2 = 0.5 * invOffsetY;
      double offsetZ2 = 0.5 * invOffsetZ;

      double mirTravel = mode == 1 ? Configs.mirrorAmplitude *
          Math.cos(Math.PI * 2 * millisecondsElapsed / Configs.mirrorPeriod) : 0;
      double mirTravel1 = mirTravel;
      double mirTravel2 = 0;

      boolean mirrorInversion = invertDirection && mode == 1;
      if (mirrorInversion && side <= 1 && player.getDirection().ordinal() > 3) {
        angle += 90;
        mirTravel1 = 0;
        mirTravel2 = mirTravel;
      }

      translateAndRotateTexture(playerX, playerY, playerZ, dir, upDown, eastWest, offsetX, offsetY,
          offsetZ, angle, diffX, diffY, diffZ, offsetX2, offsetY2, offsetZ2, mirTravel1,
          mirTravel2);


      RenderSystem.setShaderTexture(0, mode == 0 ? ARROW_CYCLICAL
          : (mode == 1 ? ARROW_BIDIRECTIONAL : (mode == 2 ? CIRCLE : INVERSION)));
      float minU = 0;
      float maxU = 1;
      float minV = 0;
      float maxV = 1;
      if (mode == 0) {
        if (invertDirection) {
          float minU2 = minU;
          minU = maxU;
          maxU = minU2;
        }
      } else if (mode == 2) {
        Direction dir2 =
            side <= 1 ? Direction.WEST : (side <= 3 ? Direction.WEST : Direction.DOWN);
        box = contractBoxOrRenderArrows(true, t, buffer, side, northSouth, dir2, box, invOffsetX,
            invOffsetY, invOffsetZ, invertDirection, minU, maxU, minV, maxV);
      }

      renderTexturedSide(t, buffer, side, northSouth, box, minU, maxU, minV, maxV, 1);
      poseStack.popPose();

      AABB box3 = world.getBlockState(pos).getBlockSupportShape(world, pos).bounds();
      for (int s = 0; s < 6; s++) {
        if (s != side) {
          poseStack.pushPose();
          upDown = s <= 1;
          eastWest = s >= 4;

          northSouth = !upDown && !eastWest;
          dir = Direction.from3DDataValue(s);
          box = new AABB(eastWest ? (s == 5 ? box3.maxX : box3.minX) : x,
              upDown ? (s == 1 ? box3.maxY : box3.minY) : y,
              northSouth ? (s == 3 ? box3.maxZ : box3.minZ) : z,
              eastWest ? (s == 4 ? box3.minX : box3.maxX) : x + 1,
              upDown ? (s == 0 ? box3.minY : box3.maxY) : y + 1,
              northSouth ? (s == 2 ? box3.minZ : box3.maxZ) : z + 1);
          angle = getInitialAngle(mode);

          boolean oppRotation = false;
          int mode2 = mode;
          if (mode != 3) {
            oppRotation = dir == Direction.from3DDataValue(side).getOpposite();
            if (mode == 0) {
              if (!oppRotation) {
                RenderSystem.setShaderTexture(0, ARROW_HEAD);
//                Minecraft.getInstance().renderEngine.bindTexture(ARROW_HEAD);
                angle = 90;
                if (side % 2 == 0) {
                  angle += 180;
                }

                if (invertDirection) {
                  angle += 180;
                }

                mode2 = 2;
              } else {
                RenderSystem.setShaderTexture(0, ARROW_CYCLICAL);
//                Minecraft.getInstance().renderEngine.bindTexture(ARROW_CYCLICAL);
                mode2 = 0;
              }
            } else if (mode == 2) {
              if (!oppRotation) {
                RenderSystem.setShaderTexture(0, ARROW_HEAD);
//                Minecraft.getInstance().renderEngine.bindTexture(ARROW_HEAD);
                if (side == 0 ? s == 2 || s == 5 :
                    (side == 1 ? s == 3 || s == 4 : (side == 2 ? s == 1 || s == 5
                        : (side == 3 ? s == 0 || s == 4 :
                        (side == 4 ? s == 1 || s == 2 : s == 0 || s == 3))))) {
                  angle += 180;
                }

                if (invertDirection) {
                  angle += 180;
                }
              } else {
                RenderSystem.setShaderTexture(0, CIRCLE);
//                Minecraft.getInstance().renderEngine.bindTexture(CIRCLE);
              }
            }
          }
          mirTravel1 = mirTravel;
          mirTravel2 = 0;
          if (mode != 3 && ((((side <= 1 && mirrorInversion) == (side > 1)) && s > 1)
              || ((mirrorInversion ?
              (oppRotation ? player.getDirection().ordinal() > 3 : side > 3)
              : (side == 2 || side == 3)) && s <= 1))) {
            angle += 90;
            mirTravel1 = 0;
            mirTravel2 = mirTravel;
          }
          if (mode == 3) {
            if (s % 2 == 1) {
              angle += 180;
            }

            if (s >= 4) {
              angle -= 90;
            }
          } else {
            if (mode2 == 0) {
              if (s % 2 == (invertDirection ? 0 : 1)) {
                angle *= -1;
              }

              if (oppRotation) {
                angle *= -1;
              }
            } else {
              if (s < 2 || s > 3) {
                angle *= -1;
              }
            }
            if (eastWest) {
              angle -= 90;
            }

            if (s == (mode2 == 1 ? 1 : 0) || s == 3 || s == 5) {
              angle += 180;
            }
          }
          offsetX = Math.abs(dir.getStepX());
          offsetY = Math.abs(dir.getStepY());
          offsetZ = Math.abs(dir.getStepZ());
          invOffsetX = offsetX ^ 1;
          invOffsetY = offsetY ^ 1;
          invOffsetZ = offsetZ ^ 1;
          offsetX2 = 0.5 * invOffsetX;
          offsetY2 = 0.5 * invOffsetY;
          offsetZ2 = 0.5 * invOffsetZ;
          translateAndRotateTexture(playerX, playerY, playerZ, dir, upDown, eastWest, offsetX,
              offsetY,
              offsetZ, angle, diffX, diffY, diffZ, offsetX2, offsetY2, offsetZ2, mirTravel1,
              mirTravel2);
          minU = 0;
          maxU = 1;
          minV = 0;
          maxV = 1;
          if (mode2 == 0) {
            if (oppRotation) {
              minU = 1;
              maxU = 0;
            }
            if (invertDirection) {
              float minU2 = minU;
              minU = maxU;
              maxU = minU2;
            }
          } else if (mode2 == 2) {
            Direction dir2 = side <= 1 ? (s == 2 || s == 3 ? Direction.WEST : Direction.DOWN)
                : (side >= 4 ? Direction.WEST : (s <= 1 ? Direction.WEST : Direction.DOWN));
            box = contractBoxOrRenderArrows(oppRotation, t, buffer, side, northSouth, dir2, box,
                invOffsetX,
                invOffsetY, invOffsetZ, invertDirection, minU, maxU, minV, maxV);
          }
          if (mode2 != 2 || oppRotation) {
            renderTexturedSide(t, buffer, s, northSouth, box, minU, maxU, minV, maxV, 1);
          }

          poseStack.popPose();
        }
      }

      GlStateManager.enableLighting();
      GlStateManager.disableBlend();
      GlStateManager.enableTexture2D();
      poseStack.popPose();
    } else if (ItemStackHelper.isSculptingToolItem(item)) {
      ItemSculptingTool toolItem = (ItemSculptingTool) item;
      boolean removeBits = toolItem.removeBits();
      int mode = BitToolSettingsHelper.getSculptMode(stack.getTag());
      if (!removeBits || mode > 0 ||
          api.canBeChiseled(world, ((BlockHitResult) target).getBlockPos())) {
        float hitX = (float) hit.x - pos.getX();
        float hitY = (float) hit.y - pos.getY();
        float hitZ = (float) hit.z - pos.getZ();
        IBitLocation bitLoc = api.getBitPos(hitX, hitY, hitZ, dir, pos, false);
        if (bitLoc != null) {
          CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
          int x2 = bitLoc.getBitX();
          int y2 = bitLoc.getBitY();
          int z2 = bitLoc.getBitZ();
          if (!toolItem.removeBits()) {
            x2 += dir.getStepX();
            y2 += dir.getStepY();
            z2 += dir.getStepZ();
          }
          boolean isDrawn = drawnStartPoint != null;
          boolean drawnBox = mode == 2 && isDrawn;
          int shapeType = BitToolSettingsHelper.getShapeType(nbt, toolItem.isCurved());
          boolean fixedNotSym = !drawnBox && shapeType == 2 || shapeType > 4;
          glStart();
          double r = BitToolSettingsHelper.getSemiDiameter(nbt) * Utility.PIXEL_D;
          ConfigShapeRenderPair configPair = Configs.itemShapeMap.get(toolItem);
          ConfigShapeRender configBox = configPair.boundingBox;
          AABB box = null, shapeBox = null;
          double x3 = x + x2 * Utility.PIXEL_D;
          double y3 = y + y2 * Utility.PIXEL_D;
          double z3 = z + z2 * Utility.PIXEL_D;
          if (configBox.renderInnerShape || configBox.renderOuterShape) {
            poseStack.pushPose();
            GL11.glLineWidth(configBox.lineWidth);
            boolean inside = ItemSculptingTool.wasInsideClicked(dir, hit, pos);
            if (drawnBox) {
              double x4 = drawnStartPoint.x;
              double y4 = drawnStartPoint.y;
              double z4 = drawnStartPoint.z;
              if (Math.max(x3, x4) == x3) {
                x3 += Utility.PIXEL_D;
              } else {
                x4 += Utility.PIXEL_D;
              }
              if (Math.max(y3, y4) == y3) {
                y3 += Utility.PIXEL_D;
              } else {
                y4 += Utility.PIXEL_D;
              }
              if (Math.max(z3, z4) == z3) {
                z3 += Utility.PIXEL_D;
              } else {
                z4 += Utility.PIXEL_D;
              }
              box = new AABB(x4, y4, z4, x3, y3, z3);
            } else {
              double f = 0;
              Vec3 vecOffset = new Vec3(0, 0, 0);
              boolean targetBitGrid = BitToolSettingsHelper.isBitGridTargeted(nbt);
              if (mode == 2) {
                r = 0;
              } else if (targetBitGrid) {
                f = Utility.PIXEL_D * 0.5;
                vecOffset =
                    BitAreaHelper.getBitGridOffset(dir, inside, hitX, hitY, hitZ, removeBits);
                r -= f;
              }
              box = new AABB(x - r, y - r, z - r, x + r + Utility.PIXEL_D, y + r + Utility.PIXEL_D,
                  z + r + Utility.PIXEL_D)
                  .move(x2 * Utility.PIXEL_D + f * vecOffset.x,
                      y2 * Utility.PIXEL_D + f * vecOffset.y,
                      z2 * Utility.PIXEL_D + f * vecOffset.z);
              boolean placementOffset =
                  BitToolSettingsHelper.isShapeOffset(nbt) && !removeBits && mode != 2;
              double r2 = r + (targetBitGrid ? Utility.PIXEL_D * 0.5 : 0);
              if (placementOffset) {
                box = box.move(dir.getStepX() * r2, dir.getStepY() * r2,
                    dir.getStepZ() * r2);
              }

              if (targetBitGrid && mode != 2) {
                x3 = (box.maxX + box.minX) * 0.5 - f;
                y3 = (box.maxY + box.minY) * 0.5 - f;
                z3 = (box.maxZ + box.minZ) * 0.5 - f;
              }
              if (!targetBitGrid && placementOffset) {
                x3 += dir.getStepX() * r2;
                y3 += dir.getStepY() * r2;
                z3 += dir.getStepZ() * r2;
              }
            }
            if (fixedNotSym) {
              shapeBox = box.inflate(0);
            }

            if (mode == 0) {
              BlockPos pos2 = !removeBits && !inside ? pos.relative(dir) : pos;
              AABB box2 = !removeBits ? new AABB(pos2) :
                  world.getBlockState(pos2).getBlockSupportShape(world, pos2).bounds();
              box = limitBox(box, box2);
            }
            if (configBox.renderOuterShape) {
              LevelRenderer.renderLineBox(context.matrixStack(), context.consumers().getBuffer(
                      RenderType.LINES),
                  box.inflate(BOUNDING_BOX_OFFSET).move(-playerX, -playerY, -playerZ),
                  configBox.red, configBox.green, configBox.blue, configBox.outerShapeAlpha);
            }

            if (configBox.renderInnerShape) {
              RenderSystem.depthFunc(GL11.GL_GREATER);
              LevelRenderer.renderLineBox(context.matrixStack(),
                  context.consumers().getBuffer(RenderType.LINES),
                  box.inflate(BOUNDING_BOX_OFFSET).move(-playerX, -playerY, -playerZ),
                  configBox.red, configBox.green, configBox.blue, configBox.innerShapeAlpha);
              RenderSystem.depthFunc(GL11.GL_LEQUAL);
            }
            poseStack.popPose();
          }
          if (!fixedNotSym && box != null) {
            shapeBox = box.inflate(0);
          }

          boolean isHollow = BitToolSettingsHelper.isHollowShape(nbt, removeBits);
          boolean isOpen = isHollow && BitToolSettingsHelper.areEndsOpen(nbt);
          renderEnvelopedShapes(shapeType, nbt, playerX, playerY, playerZ, isDrawn,
              drawnBox, r, configPair, shapeBox, x3, y3, z3, 0, isOpen);
          float wallThickness = BitToolSettingsHelper.getWallThickness(nbt) * Utility.PIXEL_F;
          if (wallThickness > 0 && isHollow && !(mode == 2 && !drawnBox)) {
            renderEnvelopedShapes(shapeType, nbt, playerX, playerY, playerZ, isDrawn, drawnBox, r,
                configPair, shapeBox,
                x3, y3, z3, wallThickness, isOpen);
          }

          glEnd();
        }
      }
    } else if (ItemStackHelper.isModelingToolItem(item)) {
      glStart();
      ModelingBoxSet boxSet = BitAreaHelper.getModelingToolBoxSet(player, x, y, z, hit,
          drawnStartPointModelingTool, true,
          BitToolSettingsHelper.getModelAreaMode(stack.getTag()),
          BitToolSettingsHelper.getModelSnapMode(stack.getTag()));
      if (!boxSet.isEmpty()) {
        renderBoundingBox(poseStack, vertexConsumer,
            boxSet.getBoundingBox().move(-playerX, -playerY, -playerZ), 1, 1, 1, 115);
        if (boxSet.hasPoint()) {
          renderBoundingBox(poseStack, vertexConsumer,
              boxSet.getPoint().move(-playerX, -playerY, -playerZ), 1, 1, 1, 155);
        }
      }
      glEnd();
    } else if (ItemStackHelper.isChiseledArmorItem(item)) {
      CompoundTag nbt = ItemStackHelper.getNBTOrNew(stack);
      int mode = BitToolSettingsHelper.getArmorMode(nbt);
      if (hitBlock) {
        if (mode == 0) {
          Direction facingBox = player.getDirection().getOpposite();
          AABB box = ItemChiseledArmor.getBodyPartTemplateBox(player, dir, pos, hit,
              BitToolSettingsHelper.getArmorScale(nbt),
              BitToolSettingsHelper.getArmorMovingPart(nbt, (ItemChiseledArmor) item));
          if (box != null) {
            renderBodyPartTemplate(poseStack, vertexConsumer, playerX, playerY, playerZ, facingBox,
                t, buffer, box, 1.0F);
          }
        } else {
          glStart();
          renderBoundingBox(poseStack, vertexConsumer,
              getDrawnArmorCollectionBox(player, nbt, dir, pos, hit)
                  .move(-playerX, -playerY, -playerZ).inflate(BOUNDING_BOX_OFFSET), 0, 0, 0, 155);
          glEnd();
        }
      }
    }
  }

  private static AABB getDrawnArmorCollectionBox(Player player, CompoundTag nbt,
                                                 Direction dir,
                                                 BlockPos pos, Vec3 hit) {
    boolean targetBits = BitToolSettingsHelper.areArmorBitsTargeted(nbt);
    double x3 = 0, y3 = 0, z3 = 0;
    if (targetBits) {
      float hitX = (float) hit.x - pos.getX();
      float hitY = (float) hit.y - pos.getY();
      float hitZ = (float) hit.z - pos.getZ();
      IBitLocation bitLoc =
          ChiselsAndBitsAPIAccess.apiInstance.getBitPos(hitX, hitY, hitZ, dir, pos, false);
      if (bitLoc != null) {
        int x2 = bitLoc.getBitX();
        int y2 = bitLoc.getBitY();
        int z2 = bitLoc.getBitZ();
        x3 = pos.getX() + x2 * Utility.PIXEL_D;
        y3 = pos.getY() + y2 * Utility.PIXEL_D;
        z3 = pos.getZ() + z2 * Utility.PIXEL_D;
        if (player.isShiftKeyDown()) {
          x3 += dir.getStepX() * Utility.PIXEL_D;
          y3 += dir.getStepY() * Utility.PIXEL_D;
          z3 += dir.getStepZ() * Utility.PIXEL_D;
        }
      }
    } else {
      x3 = pos.getX();
      y3 = pos.getY();
      z3 = pos.getZ();
      if (player.isShiftKeyDown()) {
        x3 += dir.getStepX();
        y3 += dir.getStepY();
        z3 += dir.getStepZ();
      }
    }
    double x4, y4, z4;
    if (drawnStartPoint != null) {
      x4 = drawnStartPoint.x;
      y4 = drawnStartPoint.y;
      z4 = drawnStartPoint.z;
    } else {
      x4 = x3;
      y4 = y3;
      z4 = z3;
    }
    double offset;
    if (!targetBits) {
      x4 = Math.floor(x4);
      y4 = Math.floor(y4);
      z4 = Math.floor(z4);
      offset = 1;
    } else {
      offset = Utility.PIXEL_D;
    }
    if (Math.max(x3, x4) == x3) {
      x3 += offset;
    } else {
      x4 += offset;
    }
    if (Math.max(y3, y4) == y3) {
      y3 += offset;
    } else {
      y4 += offset;
    }
    if (Math.max(z3, z4) == z3) {
      z3 += offset;
    } else {
      z4 += offset;
    }
    return new AABB(x4, y4, z4, x3, y3, z3);
  }

  private static void renderBodyPartTemplate(PoseStack poseStack, VertexConsumer vertexConsumer,
                                             double playerX, double playerY, double playerZ,
                                             Direction facingBox, Tesselator t,
                                             BufferBuilder buffer,
                                             AABB box, float redBlue) {
    glStart();
    box = box.move(-playerX, -playerY, -playerZ).inflate(BOUNDING_BOX_OFFSET);
    renderBoundingBox(poseStack, vertexConsumer, box, redBlue, 1, redBlue, 155);
    for (Direction face : Direction.values()) {
      boolean isFront = face == facingBox;
      if (isFront) {
        GL11.glColor4d(0, 0, 1, 0.5);
      } else {
        GL11.glColor4d(1, 1, 1, 0.5);
      }

      boolean northSouth = face.getAxis() == Direction.Axis.Z;
      double minX = box.minX;
      double minY = box.minY;
      double minZ = box.minZ;
      double maxX = box.maxX;
      double maxY = box.maxY;
      double maxZ = box.maxZ;
      if (face.getAxis() == Direction.Axis.X) {

        minX = maxX =
            face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.maxX : box.minX;
      } else if (face.getAxis() == Direction.Axis.Y) {
        minY = maxY =
            face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.maxY : box.minY;
      } else {
        minZ = maxZ =
            face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? box.maxZ : box.minZ;
      }

      boolean flag = face.getAxisDirection() ==
          (face.getAxis() == Direction.Axis.Y ? Direction.AxisDirection.NEGATIVE :
              Direction.AxisDirection.POSITIVE);
      if (flag || isFront) {
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(minX, minY, minZ).endVertex();
        buffer.vertex(maxX, northSouth ? minY : maxY, minZ).endVertex();
        buffer.vertex(maxX, maxY, maxZ).endVertex();
        buffer.vertex(minX, northSouth ? maxY : minY, maxZ).endVertex();
        t.end();
      }
      if (!flag || isFront) {
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(minX, northSouth ? maxY : minY, maxZ).endVertex();
        buffer.vertex(maxX, maxY, maxZ).endVertex();
        buffer.vertex(maxX, northSouth ? minY : maxY, minZ).endVertex();
        buffer.vertex(minX, minY, minZ).endVertex();
        t.end();
      }
    }
    glEnd();
  }

  private static void glStart() {
    GlStateManager._enableBlend();
    GlStateManager._blendFuncSeparate(770, 771, 1, 0);
    GlStateManager.disableTexture2D();
    GlStateManager._depthMask(false);
  }

  private static void glEnd() {
    GlStateManager._depthMask(true);
    GlStateManager.enableTexture2D();
    GlStateManager._disableBlend();
  }

  private static void renderBoundingBox(PoseStack poseStack, VertexConsumer vertexConsumer,
                                        AABB boxBounding, float red, float green, float blue,
                                        int outerAlpha) {
    LevelRenderer.renderLineBox(poseStack, vertexConsumer, boxBounding, red, green, blue,
        outerAlpha / 255.0F);
    GlStateManager._depthFunc(GL11.GL_GREATER);
    LevelRenderer.renderLineBox(poseStack, vertexConsumer, boxBounding, red, green, blue,
        28 / 255.0F);
    GlStateManager._depthFunc(GL11.GL_LEQUAL);
  }

  private static void renderEnvelopedShapes(int shapeType, CompoundTag nbt, double playerX,
                                            double playerY, double playerZ, boolean isDrawn,
                                            boolean drawnBox, double r,
                                            ConfigShapeRenderPair configPair,
                                            AABB box, double x, double y, double z,
                                            double contraction,
                                            boolean isOpen) {
    ConfigShapeRender configShape = configPair.envelopedShape;
    if (configShape.renderInnerShape || configShape.renderOuterShape) {
      double a = 0, b = 0, c = 0;
      /* 0 = sphere
       * 1 = cylinder
       * 2 = cone
       * 3 = cube
       * 4 = triangular prism
       * 5 = triangular pyramid
       * 6 = square pyramid
       */
      int dir = BitToolSettingsHelper.getDirection(nbt);
//			int rotation = dir / 6;
      dir %= 6;
      boolean notFullSym = shapeType != 0 && shapeType != 3;
      boolean notSym = shapeType == 2 || shapeType > 4;
      double ri = r + Utility.PIXEL_D * 0.5;
      r = Math.max(ri - contraction, 0);
      boolean drawnNotSym = notSym && drawnBox;
      double base = 0;
      double v;
      if (drawnBox || notSym) {
        double f = 0.5;
        double minX = box.minX * f;
        double minY = box.minY * f;
        double minZ = box.minZ * f;
        double maxX = box.maxX * f;
        double maxY = box.maxY * f;
        double maxZ = box.maxZ * f;
        double x2 = maxX - minX;
        double y2 = maxY - minY;
        double z2 = maxZ - minZ;
        if (drawnNotSym) {
          if (dir == 2 || dir == 3) {
            v = y2;
            y2 = z2;
            z2 = v;
          } else if (dir > 3) {
            v = y2;
            y2 = x2;
            x2 = v;
          }
        }
        if (notSym && contraction > 0) {
          if (!isOpen) {
            base = contraction;
          }

          y2 *= 2;
          double y2sq = y2 * y2;
          double aInset = (Math.sqrt(x2 * x2 + y2sq) * contraction) / x2 + base;
          double cInset = (Math.sqrt(z2 * z2 + y2sq) * contraction) / z2 + base;
          a = Math.max((y2 - aInset) * (x2 / y2), 0);
          c = Math.max((y2 - cInset) * (z2 / y2), 0);
          contraction = Math.min(aInset - base, cInset - base);
          b = Math.max(y2 * 0.5 - contraction * 0.5 - base * 0.5, 0);
        } else {
          a = Math.max(x2 - (!isOpen || !notFullSym || dir < 4 ? contraction : 0), 0);
          c = Math.max(z2 - (!isOpen || !notFullSym || dir != 2 && dir != 3 ? contraction : 0), 0);
          b = Math.max(y2 - (!isOpen || !notFullSym || dir > 1 ? contraction : 0), 0);
        }
        r = Math.max(Math.max(a, b), c);
        x = maxX + minX;
        y = maxY + minY;
        z = maxZ + minZ;
        if (drawnBox) {
          if (notSym || !notFullSym) {
            if (dir < 2 || dir > 3 || !notFullSym) {
              v = b;
              b = c;
              c = v;
            }
          } else {
            if (dir < 2) {
              v = b;
              b = c;
              c = v;
            } else if (dir > 3) {
              v = a;
              a = c;
              c = v;
            } else {
              v = b;
              b = a;
              a = v;
            }
          }
        }
      } else {
        a = b = c = r;
        if (b > 0 && notFullSym && isOpen) {
          b += contraction * (isDrawn ? 0 : 1);
        }
      }

      Quadric shape = shapeType > 2 ? new Prism(shapeType > 4, shapeType == 4 || shapeType == 5) :
          (notFullSym ? new Cylinder() : new Sphere());
      shape.setDrawStyle(Quadric.GLU_LINE);
      Quadric lid = new Disk();
      lid.setDrawStyle(Quadric.GLU_LINE);
      GL11.glPushMatrix();
      GL11.glLineWidth(configShape.lineWidth);
      double x2 = x - playerX;
      double y2 = y - playerY;
      double z2 = z - playerZ;
      if (!notSym && !isDrawn) {
        double hp = Utility.PIXEL_D * 0.5;
        x2 += hp;
        y2 += hp;
        z2 += hp;
      }
      if (notFullSym) {
        if (isOpen && contraction > 0 && !notSym) {
          double offset = contraction * (notSym ? 0.5 : (drawnBox ? 0 : -1));
          if (dir != 3) {
            y2 += dir == 0 ? offset : -offset;
          }

          if (dir > 2) {
            x2 += dir == 5 ? -offset : offset;
          }

          if (dir == 2 || dir == 3) {
            z2 += dir == 2 ? offset : -offset;
          }
        }
      }

      GL11.glTranslated(x2, y2, z2);
      int rot2 = dir;
      if (!(drawnNotSym && dir == 2)) {
        if (notFullSym && rot2 != 1) {
          int angle = 90;
          if (rot2 == 3) {
            rot2 = 0;
            angle = 180;
            if (!(drawnNotSym && dir == 3)) {
              GL11.glRotated(90, 0, 0, 1);
            }
          } else if (rot2 > 1) {
            rot2 %= 4;
          } else {
            rot2 = rot2 ^ 1 + 4;
          }
          Vec3i vec = Direction.from2DDataValue(rot2).getOpposite().getNormal();
          GL11.glRotated(angle, vec.getX(), vec.getY(), vec.getZ());
        } else {
          GL11.glRotated(90, 1, 0, 0);
        }
      }
      boolean openSym = notFullSym && !notSym && isOpen && !isDrawn;
      if (notFullSym) {
        double offset1 = 0;
        double offset2 = 0;
        double r2 = r;
        if (notSym) {
          r2 -= contraction * 0.5 - base * 0.5;
        } else if (openSym) {
          double m = -contraction;
          if (dir == 0) {
            m *= 2;
          }

          if (dir != 1) {
            r -= m;
          }

          if (dir > 1) {
            if (dir < 3) {
              offset1 = m;
            } else {
              offset2 = m;
            }
          }
        }
        GL11.glTranslated(offset1, offset2, -r2);
      }
      if (openSym) {
        v = b;
        b = c;
        c = v;
      }
      if (drawnNotSym) {
        if (dir == 2 || dir == 3) {
          v = b;
          b = c;
          c = v;
        } else if (dir > 3) {
          v = b;
          b = a;
          a = v;
        }
      }
      if (notFullSym && drawnBox) {
        if (b > c && b > a) {
          GL11.glTranslated(0, 0, b - c);
        } else if (a > c && a >= b) {
          GL11.glTranslated(0, 0, a - c);
        }
      }
      GL11.glScaled(a / ri, b / ri, c / ri);
      if (configShape.renderOuterShape) {
        drawEnvelopedShapes(ri, configShape, shapeType, shape, lid, true, notSym, isOpen);
      }

      if (configShape.renderInnerShape) {
        GlStateManager._depthFunc(GL11.GL_GREATER);
        drawEnvelopedShapes(ri, configShape, shapeType, shape, lid, false, notSym, isOpen);
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
      }
      GL11.glPopMatrix();
    }
  }

  private static void drawEnvelopedShapes(double r, ConfigShapeRender configShape, int shapeType,
                                          Quadric shape,
                                          Quadric lid, boolean isOuter, boolean isCylinder,
                                          boolean isOpen) {
    GL11.glPushMatrix();
    drawEnvelopedShape(shape, r, isOuter, configShape, isCylinder, isOpen);
    if (shapeType > 0 && shapeType < 3 && !isOpen) {
      if (shapeType == 1) {
        drawEnvelopedShape(lid, r, isOuter, configShape, isCylinder, isOpen);
      }

      GL11.glTranslated(0, 0, r * 2);
      drawEnvelopedShape(lid, r, isOuter, configShape, isCylinder, isOpen);
    }
    GL11.glPopMatrix();
  }

  private static void drawEnvelopedShape(Quadric shape, double radius, boolean isOuter,
                                         ConfigShapeRender configShape, boolean isCone,
                                         boolean isOpen) {
    GL11.glPushMatrix();
    GlStateManager.color(configShape.red, configShape.green,
        configShape.blue, isOuter ? configShape.outerShapeAlpha : configShape.innerShapeAlpha);
    float r = (float) radius;
    if (shape instanceof Prism) {
      ((Prism) shape).draw(r, isOpen);
    } else if (shape instanceof Sphere) {
      ((Sphere) shape).draw(r, 32, 32);
    } else if (shape instanceof Cylinder) {
      ((Cylinder) shape).draw(isCone ? 0 : r, r, r * 2, 32, 32);
    } else if (shape instanceof Disk) {
      ((Disk) shape).draw(0, r, 32, 32);
    }
    GL11.glPopMatrix();
  }

  private static AABB limitBox(AABB box, AABB mask) {
    double d0 = Math.max(box.minX, mask.minX);
    double d1 = Math.max(box.minY, mask.minY);
    double d2 = Math.max(box.minZ, mask.minZ);
    double d3 = Math.min(box.maxX, mask.maxX);
    double d4 = Math.min(box.maxY, mask.maxY);
    double d5 = Math.min(box.maxZ, mask.maxZ);
    return new AABB(d0, d1, d2, d3, d4, d5);
  }

  private static double getInitialAngle(int mode) {
    return mode == 0 ? (millisecondsElapsed * (360.0 / Configs.rotationPeriod)) % 360 : 0;
  }

  private static void translateAndRotateTexture(double playerX, double playerY, double playerZ,
                                                Direction dir, boolean upDown,
                                                boolean eastWest, int offsetX, int offsetY,
                                                int offsetZ,
                                                double angle, double diffX, double diffY,
                                                double diffZ,
                                                double offsetX2, double offsetY2, double offsetZ2,
                                                double mirTravel1, double mirTravel2) {
    double cos = Math.cos(Math.toRadians(angle));
    double sin = Math.sin(Math.toRadians(angle));
    if (upDown) {
      GL11.glTranslated(diffX * cos + diffZ * sin - diffX + mirTravel1, 0,
          -diffX * sin + diffZ * cos - diffZ + mirTravel2);
    } else if (eastWest) {
      GL11.glTranslated(0, diffY * cos - diffZ * sin - diffY + mirTravel2,
          diffY * sin + diffZ * cos - diffZ + mirTravel1);
    } else {
      GL11.glTranslated(diffX * cos - diffY * sin - diffX + mirTravel1,
          diffX * sin + diffY * cos - diffY + mirTravel2, 0);
    }
    GL11.glTranslated(offsetX2, offsetY2, offsetZ2);
    GL11.glRotated(angle, offsetX, offsetY, offsetZ);
    GL11.glTranslated(-offsetX2, -offsetY2, -offsetZ2);
    GL11.glTranslated(-playerX + 0.002 * dir.getStepX(),
        -playerY + 0.002 * dir.getStepY(), -playerZ + 0.002 * dir.getStepZ());
  }

  private static AABB contractBoxOrRenderArrows(boolean contractBox, Tesselator t,
                                                BufferBuilder buffer,
                                                int side, boolean northSouth, Direction dir,
                                                AABB box, double invOffsetX, double invOffsetY,
                                                double invOffsetZ, boolean invertDirection,
                                                float minU,
                                                float maxU, float minV, float maxV) {
    if (contractBox) {
      double amount =
          (millisecondsElapsed % Configs.translationScalePeriod) / Configs.translationScalePeriod;
      amount /= invertDirection ? -2 : 2;
      if (invertDirection && Configs.translationScalePeriod > 1) {
        amount += 0.5;
      }

      box = box.inflate(-amount * invOffsetX, -amount * invOffsetY, -amount * invOffsetZ);
    } else if (Configs.translationDistance > 0) {
      double distance = Configs.translationDistance;
      double fadeDistance = Configs.translationFadeDistance;
      double period = Configs.translationMovementPeriod;
      double offsetDistance = Configs.translationOffsetDistance;
      int timeOffset = offsetDistance > 0 ? (int) (period / (distance / offsetDistance)) : 0;
      if (timeOffset > period / 3.0) {
        timeOffset = (int) (period / 3.0);
      }

      if (fadeDistance > distance / 2.0) {
        fadeDistance = distance / 2.0;
      }

      int n = offsetDistance == 0 || period == 1 ? 1 : 3;
      for (int i = 0; i < n; i++) {
        double amount = ((millisecondsElapsed + timeOffset * i) % period) /
            (period / (distance * 100.0) * 100.0);
        double alpha = 1;
        if (period > 1) {
          if (amount < fadeDistance) {
            alpha = amount / fadeDistance;
          } else if (amount > distance - fadeDistance) {
            alpha = (distance - amount) / fadeDistance;
          }
          amount -= distance / 2.0;
        }
        AABB box2 = new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
            .move(amount * dir.getStepX(), amount * dir.getStepY(),
                amount * dir.getStepZ());
        renderTexturedSide(t, buffer, side, northSouth, box2, minU, maxU, minV, maxV, alpha);
      }
    } else {
      renderTexturedSide(t, buffer, side, northSouth, box, minU, maxU, minV, maxV, 1);
    }
    return box;
  }

  private static void renderTexturedSide(Tesselator t, BufferBuilder buffer, int side,
                                         boolean northSouth,
                                         AABB box, float minU, float maxU, float minV, float maxV,
                                         double alpha) {
    GL11.glColor4d(1, 1, 1, alpha);
    if (side == 1 || side == 3 || side ==
        4) {
      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.vertex(box.minX, box.minY, box.maxZ).uv(maxU, minV).endVertex();
      buffer.vertex(box.maxX, northSouth ? box.minY : box.maxY, box.maxZ).uv(minU, minV)
          .endVertex();
      buffer.vertex(box.maxX, box.maxY, box.minZ).uv(minU, maxV).endVertex();
      buffer.vertex(box.minX, northSouth ? box.maxY : box.minY, box.minZ).uv(maxU, maxV)
          .endVertex();
      t.end();

      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.vertex(box.maxX, northSouth ? box.minY : box.maxY, box.maxZ).uv(minU, minV)
          .endVertex();
      buffer.vertex(box.minX, box.minY, box.maxZ).uv(maxU, minV).endVertex();
      buffer.vertex(box.minX, northSouth ? box.maxY : box.minY, box.minZ).uv(maxU, maxV)
          .endVertex();
      buffer.vertex(box.maxX, box.maxY, box.minZ).uv(minU, maxV).endVertex();
      t.end();
    } else {

      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.vertex(box.minX, northSouth ? box.maxY : box.minY, box.minZ).uv(maxU, minV)
          .endVertex();
      buffer.vertex(box.maxX, box.maxY, box.minZ).uv(minU, minV).endVertex();
      buffer.vertex(box.maxX, northSouth ? box.minY : box.maxY, box.maxZ).uv(minU, maxV)
          .endVertex();
      buffer.vertex(box.minX, box.minY, box.maxZ).uv(maxU, maxV).endVertex();
      t.end();

      buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      buffer.vertex(box.maxX, box.maxY, box.minZ).uv(minU, minV).endVertex();
      buffer.vertex(box.minX, northSouth ? box.maxY : box.minY, box.minZ).uv(maxU, minV)
          .endVertex();
      buffer.vertex(box.minX, box.minY, box.maxZ).uv(maxU, maxV).endVertex();
      buffer.vertex(box.maxX, northSouth ? box.minY : box.maxY, box.maxZ).uv(minU, maxV)
          .endVertex();
      t.end();
    }
  }

}