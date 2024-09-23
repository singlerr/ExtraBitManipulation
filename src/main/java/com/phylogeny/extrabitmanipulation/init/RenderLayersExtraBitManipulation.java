package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.armor.ChiseledArmorStackHandler;
import com.phylogeny.extrabitmanipulation.armor.LayerChiseledArmor;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.reference.CustomNPCsReferences;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityIllusionIllager;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;

public class RenderLayersExtraBitManipulation {
  private static final List<LayerChiseledArmor> armorLayers = new ArrayList<LayerChiseledArmor>();
  private static final List<LayerChiseledArmor> armorLayersMob =
      new ArrayList<LayerChiseledArmor>();
  private static final List<LayerChiseledArmor> armorLayersPlayer =
      new ArrayList<LayerChiseledArmor>();
  private static boolean layersInitializedPlayerCNPC;

  public static void initLayers() {
    addLayerChiseledArmorToEntityRender(EntityArmorStand.class);
    addLayerChiseledArmorToEntityRender(EntityVillager.class);
    addLayerChiseledArmorToEntityRender(EntityZombieVillager.class);
    addLayerChiseledArmorToEntityRender(EntityZombie.class);
    addLayerChiseledArmorToEntityRender(EntityGiantZombie.class);
    addLayerChiseledArmorToEntityRender(EntityPigZombie.class);
    addLayerChiseledArmorToEntityRender(EntitySkeleton.class);
    addLayerChiseledArmorToEntityRender(EntityWitherSkeleton.class);
    addLayerChiseledArmorToEntityRender(EntityHusk.class);
    addLayerChiseledArmorToEntityRender(EntityStray.class);
    addLayerChiseledArmorToEntityRender(EntityVex.class);
    addLayerChiseledArmorToEntityRender(EntityVindicator.class);
    addLayerChiseledArmorToEntityRender(EntityEvoker.class);
    addLayerChiseledArmorToEntityRender(EntityIllusionIllager.class);
    armorLayers.addAll(armorLayersMob);
    for (RenderPlayer renderPlayer : ClientHelper.getRenderManager().getSkinMap().values()) {
      LayerChiseledArmor layer = new LayerChiseledArmor(renderPlayer);
      renderPlayer.addLayer(layer);
      armorLayersPlayer.add(layer);
    }
    armorLayers.addAll(armorLayersPlayer);
    if (CustomNPCsReferences.isLoaded) {
      MinecraftForge.EVENT_BUS.register(new RenderLayersExtraBitManipulation());
    }
  }

//  @SubscribeEvent
//  public void initLayersCNPCs(
//      @SuppressWarnings("unused") RenderLivingEvent.Pre<EntityCustomNpc> event) {
//    if (layersInitializedPlayerCNPC) {
//      return;
//    }
//
//    addLayerChiseledArmorToEntityRender(EntityCustomNpc.class);
//    addLayerChiseledArmorToEntityRender(EntityNPC64x32.class);
//    layersInitializedPlayerCNPC = true;
//  }

  private static <T extends LivingEntity> void addLayerChiseledArmorToEntityRender(
      Class<? extends Entity> entityClass) {
    EntityRenderer<T> renderer = ClientHelper.getRenderManager().getRenderer(entityClass);
    LayerChiseledArmor layer = new LayerChiseledArmor(renderer);
    ((RenderLivingBase<T>) renderer).addLayer(layer);
    armorLayersMob.add(layer);
  }

  public static void clearRenderMaps() {
    ChiseledArmorStackHandler.clearModelMap();
    for (LayerChiseledArmor layer : armorLayers) {
      layer.clearDisplayListsMap();
    }
  }

  public static void removeFromRenderMaps(NBTTagCompound nbt) {
    ChiseledArmorStackHandler.removeFromModelMap(nbt);
    for (LayerChiseledArmor layer : armorLayers) {
      layer.removeFromDisplayListsMap(nbt);
    }
  }

  public static void forceUpdateModels(boolean isPlayerModel) {
    for (LayerChiseledArmor layer : isPlayerModel ? armorLayersPlayer : armorLayersMob) {
      layer.updateModelAndRenderers(true);
    }
  }

}