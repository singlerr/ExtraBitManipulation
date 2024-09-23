package com.phylogeny.extrabitmanipulation.client;

import com.phylogeny.extrabitmanipulation.mixin.accessors.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientHelper {

  private static Minecraft getMinecraft() {
    return Minecraft.getInstance();
  }

  public static ReentrantBlockableEventLoop<Runnable> getThreadListener() {
    return getMinecraft();
  }

  public static Level getWorld() {
    return getMinecraft().level;
  }

  public static Player getPlayer() {
    return getMinecraft().player;
  }

  public static ItemStack getHeldItemMainhand() {
    return getPlayer().getMainHandItem();
  }

  public static HitResult getObjectMouseOver() {
    return getMinecraft().hitResult;
  }

  public static <T extends ParticleOptions> void spawnParticle(Level worldIn, Vec3 particlePos,
                                                               ParticleProvider<T> particleFactory,
                                                               T particleOption) {
    getMinecraft().particleEngine.add(
        particleFactory.createParticle(particleOption, (ClientLevel) worldIn, particlePos.x,
            particlePos.y, particlePos.z, 0,
            0, 0));
  }

  public static void printChatMessageWithDeletion(String text) {
    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(text));
//    Minecraft.getInstance().ingameGUI.getChatGUI()
//        .printChatMessageWithOptionalDeletion(new TextComponentString(text), 627250);
  }

  public static void bindTexture(ResourceLocation resource) {
    getMinecraft().getTextureManager().bind(resource);
  }

  public static ItemRenderer getRenderItem() {
    return getMinecraft().getItemRenderer();
  }

  public static EntityRenderDispatcher getRenderManager() {
    return getMinecraft().getEntityRenderDispatcher();
  }

  public static BlockModelShaper getBlockModelShapes() {
    return getMinecraft().getBlockRenderer().getBlockModelShaper();
  }

  public static ItemColors getItemColors() {
    return ((MinecraftAccessor) getMinecraft()).getItemColors();
  }

}