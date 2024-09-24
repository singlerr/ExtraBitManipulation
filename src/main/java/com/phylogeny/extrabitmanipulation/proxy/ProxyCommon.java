package com.phylogeny.extrabitmanipulation.proxy;

import com.phylogeny.extrabitmanipulation.ExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsEventHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsStorage;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.PacketRegistration;
import com.phylogeny.extrabitmanipulation.init.RecipesExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.BaublesReferences;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.JeiReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import mod.chiselsandbits.core.ChiselsAndBits;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockDispenser;
import net.minecraft.core.Position;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.entity.IProjectile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.jetbrains.annotations.Nullable;

public class ProxyCommon {

  public void preinit() {
    BlocksExtraBitManipulation.blocksInit();
    ItemsExtraBitManipulation.itemsInit();
    BaublesReferences.isLoaded = FabricLoader.getInstance().isModLoaded(BaublesReferences.MOD_ID);
    JeiReferences.isLoaded = FabricLoader.getInstance().isModLoaded(JeiReferences.MOD_ID);
    MorePlayerModelsReference.isLoaded =
        FabricLoader.getInstance().isModLoaded(MorePlayerModelsReference.MOD_ID);
    CustomNPCsReferences.isLoaded =
        FabricLoader.getInstance().isModLoaded(CustomNPCsReferences.MOD_ID);
    ConfigHandlerExtraBitManipulation.setUpConfigs(event.getModConfigurationDirectory());
    MinecraftForge.EVENT_BUS.register(new ConfigHandlerExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new ItemsExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new BlocksExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new RecipesExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new ChiseledArmorSlotsEventHandler());
    CapabilityManager.INSTANCE.register(IChiseledArmorSlotsHandler.class,
        new ChiseledArmorSlotsStorage(), ChiseledArmorSlotsHandler::new);
    PacketRegistration.registerPackets();
    ResourceLocation name = new ResourceLocation(Reference.MOD_ID, "entity_bit");
    EntityRegistry.registerModEntity(name, EntityBit.class, name.toString(), 0,
        ExtraBitManipulation.instance, 64, 3, false);
    BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ChiselsAndBits.getItems().itemBlockBit,
        new BehaviorProjectileDispense() {
          @Override
          private IProjectile getProjectileEntity(Level worldIn, Position position,
                                                  ItemStack stackIn) {
            return new EntityBit(worldIn, position.x(), position.y(), position.z(), stackIn);
          }
        });
    ReflectionExtraBitManipulation.initReflectionFieldsCommon();
    ChiseledArmorSlotsEventHandler.addCommandTabCompletions();
  }

  public void init() {
    RecipesExtraBitManipulation.registerOres();
  }

  public void postinit() {
  }


  @Override
  public Component getDisplayName() {
    return Component.empty();
  }

  @Override
  public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
    return null;
  }
}