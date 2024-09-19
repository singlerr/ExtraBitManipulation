package com.phylogeny.extrabitmanipulation.proxy;

import com.phylogeny.extrabitmanipulation.client.ClientEventHandler;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.client.render.RenderEntityBit;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import com.phylogeny.extrabitmanipulation.init.ItemsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.KeyBindingsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.ModelRegistration;
import com.phylogeny.extrabitmanipulation.init.ReflectionExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.RenderLayersExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.init.SoundsExtraBitManipulation;
import com.phylogeny.extrabitmanipulation.reference.ChiselsAndBitsReferences;
import com.phylogeny.extrabitmanipulation.reference.Configs;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ProxyClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    // Pre Init
    ReflectionExtraBitManipulation.initReflectionFieldsClient();
    MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    MinecraftForge.EVENT_BUS.register(new SoundsExtraBitManipulation());
    MinecraftForge.EVENT_BUS.register(new ModelRegistration());
    RenderingRegistry.registerEntityRenderingHandler(EntityBit.class,
        new IRenderFactory<EntityBit>() {
          @Override
          public Render<EntityBit> createRenderFor(RenderManager manager) {
            return new RenderEntityBit(manager);
          }
        });

    // Init
    KeyBindingsExtraBitManipulation.init();
    FMLInterModComms.sendMessage(ChiselsAndBitsReferences.MOD_ID, "initkeybindingannotations", "");

    // Post Init
    RenderLayersExtraBitManipulation.initLayers();
    Configs.sculptSetBitWire.init();
    Configs.sculptSetBitSpade.init();
    Configs.replacementBitsUnchiselable.initDefaultReplacementBit();
    Configs.replacementBitsInsufficient.initDefaultReplacementBit();
    Configs.initModelingBitMaps();
    ClientHelper.getItemColors().registerItemColorHandler(new ItemColor() {
                                                            @Override
                                                            public int getColorFromItemstack(ItemStack stack, int tintIndex) {
                                                              return tintIndex;
                                                            }
                                                          }, ItemsExtraBitManipulation.chiseledHelmetDiamond,
        ItemsExtraBitManipulation.chiseledChestplateDiamond,
        ItemsExtraBitManipulation.chiseledLeggingsDiamond,
        ItemsExtraBitManipulation.chiseledBootsDiamond,
        ItemsExtraBitManipulation.chiseledHelmetIron,
        ItemsExtraBitManipulation.chiseledChestplateIron,
        ItemsExtraBitManipulation.chiseledLeggingsIron,
        ItemsExtraBitManipulation.chiseledBootsIron);
  }
}