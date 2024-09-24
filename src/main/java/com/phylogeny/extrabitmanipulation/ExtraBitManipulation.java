package com.phylogeny.extrabitmanipulation;

import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsEventHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.CreativeTabExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.client.gui.GuiBitMapping;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiChiseledArmor;
import com.phylogeny.extrabitmanipulation.client.gui.armor.GuiInventoryArmorSlots;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.container.ContainerHeldItem;
import com.phylogeny.extrabitmanipulation.container.ContainerPlayerArmorSlots;
import com.phylogeny.extrabitmanipulation.container.ContainerPlayerInventory;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.helper.ItemStackHelper;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.PacketRegistration;
import com.phylogeny.extrabitmanipulation.init.RecipesExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.proxy.ProxyCommon;
import com.phylogeny.extrabitmanipulation.reference.BaublesReferences;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.GuiIDs;
import com.phylogeny.extrabitmanipulation.reference.JeiReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.io.File;
import mod.chiselsandbits.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

//@Mod(modid = Reference.MOD_ID,
//    version = Reference.VERSION,
//    guiFactory = Reference.GUI_FACTORY_CLASSPATH,
//    acceptedMinecraftVersions = Reference.MC_VERSIONS_ACCEPTED,
//    updateJSON = Reference.UPDATE_JSON,
//    dependencies = Reference.DEPENDENCIES)
public class ExtraBitManipulation implements ModInitializer, MenuProvider {
  public static ExtraBitManipulation instance;

  public static ProxyCommon proxy;

  public ExtraBitManipulation() {
    // Pre Init
    BlocksExtraBitManipulation.blocksInit();
    BaublesReferences.isLoaded = FabricLoader.getInstance().isModLoaded(BaublesReferences.MOD_ID);
    JeiReferences.isLoaded = FabricLoader.getInstance().isModLoaded(JeiReferences.MOD_ID);
    MorePlayerModelsReference.isLoaded =
        FabricLoader.getInstance().isModLoaded(MorePlayerModelsReference.MOD_ID);
    CustomNPCsReferences.isLoaded =
        FabricLoader.getInstance().isModLoaded(CustomNPCsReferences.MOD_ID);

    ItemsExtraBitManipulation.itemsInit();
    BlocksExtraBitManipulation.blocksInit();
    MinecraftForge.EVENT_BUS.register(new ConfigHandlerExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new RecipesExtraBitManipulation());
    RecipesExtraBitManipulation.registerRecipes();
    ChiseledArmorSlotsEventHandler.registerEventListeners();
    RegisterCapabilitiesEvent.REGISTER_CAPS.register(new RegisterCapabilitiesEvent.Register() {
      @Override
      public void accept(RegisterCapabilitiesEvent registerCapabilitiesEvent) {
        registerCapabilitiesEvent.register(IChiseledArmorSlotsHandler.class);
      }
    });
//    CapabilityManager.INSTANCE.register(
//        IChiseledArmorSlotsHandler.class, new ChiseledArmorSlotsStorage(),
//        ChiseledArmorSlotsHandler::new);

    PacketRegistration.registerPackets();
//    ResourceLocation name = new ResourceLocation(Reference.MOD_ID, "entity_bit");
//
//    EntityRegistry.registerModEntity(name, EntityBit.class, name.toString(), 0,
//        ExtraBitManipulation.instance, 64, 3, false);
    DispenserBlock.registerBehavior(ModItems.ITEM_BLOCK_BIT.get(),
        new AbstractProjectileDispenseBehavior() {
          @Override
          protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
            return new EntityBit(level, position.x(), position.y(), position.z(), itemStack);
          }
        });

    ReflectionExtraBitManipulation.initReflectionFieldsCommon();
    ChiseledArmorSlotsEventHandler.addCommandTabCompletions();
    // Init
    RecipesExtraBitManipulation.registerOres();
  }

  @Override
  public void onInitialize() {
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
        new ResourceLocation(Reference.MOD_ID, "tab"),
        CreativeTabExtraBitManipulation.CREATIVE_TAB);
    ConfigHandlerExtraBitManipulation.setUpConfigs(
        new File(Minecraft.getInstance().gameDirectory, "config"));
  }

  public static ContainerHeldItem createBitMappingContainer(Player player) {
    return new ContainerHeldItem(player, 60, 137);
  }

  public static ContainerPlayerInventory createArmorContainer(Player player) {
    return new ContainerPlayerInventory(player, 57, 148);
  }

  public static ContainerPlayerArmorSlots createArmorSlotsContainer(Player player) {
    return new ContainerPlayerArmorSlots(player.getInventory(), !player.level().isClientSide,
        player);
  }


  private AbstractContainerMenu getServerGui(int id, Player player) {
    if (openBitMappingGui(id, player.getMainHandItem())) {
      return createBitMappingContainer(player);
    }

    if (id == GuiIDs.CHISELED_ARMOR.getID()) {
      return createArmorContainer(player);
    }

    if (id == GuiIDs.CHISELED_ARMOR_SLOTS.getID()) {
      return createArmorSlotsContainer(player);
    }

    return null;
  }

  private AbstractContainerMenu getClientGui(int id, Player player) {
    if (openBitMappingGui(id, player.getMainHandItem())) {
      return new GuiBitMapping(player,
          ItemStackHelper.isDesignStack(player.getMainHandItem()));
    }

    if (id == GuiIDs.CHISELED_ARMOR.getID()) {
      return new GuiChiseledArmor(player);
    }

    if (id == GuiIDs.CHISELED_ARMOR_SLOTS.getID()) {
      return new GuiInventoryArmorSlots(createArmorSlotsContainer(player));
    }

    return null;
  }

  private boolean openBitMappingGui(int id, ItemStack stack) {
    return id == GuiIDs.BIT_MAPPING.getID() &&
        (ItemStackHelper.isModelingToolStack(stack) || ItemStackHelper.isDesignStack(stack));
  }


  @Override
  public Component getDisplayName() {
    return Component.empty();
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      return getClientGui(i, player);
    } else {
      return getServerGui(i, player);
    }
  }
}