package com.phylogeny.extrabitmanipulation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class ClientHelper
{
	
	private static Minecraft getMinecraft()
	{
		return Minecraft.getMinecraft();
	}
	
	public static IThreadListener getThreadListener()
	{
		return getMinecraft();
	}
	
	public static Level getWorld()
	{
		return getMinecraft().level;
	}
	
	public static EntityPlayer getPlayer()
	{
		return getMinecraft().player;
	}
	
	public static ItemStack getHeldItemMainhand()
	{
		return getPlayer().getHeldItemMainhand();
	}
	
	public static HitResult getObjectMouseOver()
	{
		return getMinecraft().hitResult;
	}
	
	public static void spawnParticle(Level worldIn, Vec3d particlePos, ParticleProvider particleFactory)
	{
		getMinecraft().effectRenderer.addEffect(particleFactory.createParticle(0, worldIn, particlePos.x, particlePos.y, particlePos.z, 0, 0, 0));
	}
	
	public static void printChatMessageWithDeletion(String text)
	{
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(text), 627250);
	}
	
	public static void bindTexture(ResourceLocation resource)
	{
		getMinecraft().getTextureManager().bind(resource);
	}
	
	public static RenderItem getRenderItem()
	{
		return getMinecraft().getRenderItem();
	}
	
	public static RenderManager getRenderManager()
	{
		return getMinecraft().getEntityRenderDispatcher();
	}
	
	public static BlockModelShaper getBlockModelShapes()
	{
		return getMinecraft().getBlockRenderer().getBlockModelShaper();
	}
	
	public static ItemColors getItemColors()
	{
		return getMinecraft().getItemColors();
	}
	
}