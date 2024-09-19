package com.phylogeny.extrabitmanipulation;

import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsEventHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.armor.capability.ChiseledArmorSlotsStorage;
import com.phylogeny.extrabitmanipulation.armor.capability.IChiseledArmorSlotsHandler;
import com.phylogeny.extrabitmanipulation.client.CreativeTabExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.config.ConfigHandlerExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.init.BlocksExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.PacketRegistration;
import com.phylogeny.extrabitmanipulation.init.RecipesExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.proxy.ProxyCommon;
import com.phylogeny.extrabitmanipulation.reference.BaublesReferences;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import com.phylogeny.extrabitmanipulation.reference.JeiReferences;
import com.phylogeny.extrabitmanipulation.reference.MorePlayerModelsReference;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import java.io.File;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Reference.MOD_ID,
    version = Reference.VERSION,
    guiFactory = Reference.GUI_FACTORY_CLASSPATH,
    acceptedMinecraftVersions = Reference.MC_VERSIONS_ACCEPTED,
    updateJSON = Reference.UPDATE_JSON,
    dependencies = Reference.DEPENDENCIES)
public class ExtraBitManipulation implements ModInitializer {
  @Mod.Instance(Reference.MOD_ID)
  public static ExtraBitManipulation instance;

  public static ProxyCommon proxy;

  public ExtraBitManipulation() {
    // Pre Init
    BlocksExtraBitManipulation.blocksInit();
    ItemsExtraBitManipulation.itemsInit(event);
    BaublesReferences.isLoaded = FabricLoader.getInstance().isModLoaded(BaublesReferences.MOD_ID);
    JeiReferences.isLoaded = FabricLoader.getInstance().isModLoaded(JeiReferences.MOD_ID);
    MorePlayerModelsReference.isLoaded =
        FabricLoader.getInstance().isModLoaded(MorePlayerModelsReference.MOD_ID);
    CustomNPCsReferences.isLoaded =
        FabricLoader.getInstance().isModLoaded(CustomNPCsReferences.MOD_ID);

    MinecraftForge.EVENT_BUS.register(new ConfigHandlerExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new ItemsExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new BlocksExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new RecipesExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new ChiseledArmorSlotsEventHandler());
    CapabilityManager.INSTANCE.register(
        IChiseledArmorSlotsHandler.class, new ChiseledArmorSlotsStorage(),
        ChiseledArmorSlotsHandler::new);
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
    // Init
    RecipesExtraBitManipulation.registerOres();
    NetworkRegistry.INSTANCE.registerGuiHandler(ExtraBitManipulation.instance, new ProxyCommon());
    // Post Init
  }

  @Override
  public void onInitialize() {
    Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
        new ResourceLocation(Reference.MOD_ID, "tab"),
        CreativeTabExtraBitManipulation.CREATIVE_TAB);
    ConfigHandlerExtraBitManipulation.setUpConfigs(
        new File(Minecraft.getInstance().gameDirectory, "config"));
  }
}