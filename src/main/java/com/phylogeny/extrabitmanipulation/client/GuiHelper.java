package com.phylogeny.extrabitmanipulation.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.phys.AABB;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.phylogeny.extrabitmanipulation.client.gui.button.GuiButtonBase;

public class GuiHelper
{
	
	private static Minecraft getMinecraft()
	{
		return Minecraft.getMinecraft();
	}
	
	public static void glScissor(int x, int y, int width, int height)
	{
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		int scaleFactor = getScaleFactor();
		GL11.glScissor(x * scaleFactor, getMinecraft().displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
	}
	
	public static void glScissorDisable()
	{
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}
	
	public static int getScaleFactor()
	{
		return (new ScaledResolution(getMinecraft())).getScaleFactor();
	}
	
	public static GuiScreen getOpenGui()
	{
		return getMinecraft().screen;
	}
	
	public static boolean isCursorInsideBox(AABB box, int mouseX, int mouseY)
	{
		return box.inflate(1).contains(new Vec3d(mouseX, mouseY, 0));
	}
	
	public static void drawRect(double left, double top, double right, double bottom, int color)
	{
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, (color >> 24 & 255) / 255.0F);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
		buffer.vertex(left, bottom, 0.0D).endVertex();
		buffer.vertex(right, bottom, 0.0D).endVertex();
		buffer.vertex(right, top, 0.0D).endVertex();
		buffer.vertex(left, top, 0.0D).endVertex();
		tessellator.end();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	
	public static void drawTexturedRect(double left, double top, double right, double bottom)
	{
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
		GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		Tesselator t = Tesselator.getInstance();
		BufferBuilder buffer = t.getBuilder();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(left, top, 0).uv(0, 0).endVertex();
		buffer.vertex(left, bottom, 0).uv(0, 1).endVertex();
		buffer.vertex(right, bottom, 0).uv(1, 1).endVertex();
		buffer.vertex(right, top, 0).uv(1, 0).endVertex();
		t.end();
		GlStateManager.disableBlend();
	}
	
	public static void drawHoveringTextForButtons(GuiScreen gui, List<GuiButton> buttonList, int mouseX, int mouseY)
	{
		for (GuiButton button : buttonList)
		{
			if (!(button instanceof GuiButtonBase))
				continue;
			
			if (button.isMouseOver() && button.visible)
			{
				List<String> text = ((GuiButtonBase) button).getHoverText();
				if (!text.isEmpty())
					gui.drawHoveringText(text, mouseX, mouseY);
				
				break;
			}
		}
	}
	
	public static Pair<Float, Boolean> changeScale(float scale, float amount, float max)
	{
		amount *= scale;
		float previewStackInitialScale = scale;
		scale += amount;
		if (scale < 0.1)
		{
			scale = 0.1F;
			return new ImmutablePair<Float, Boolean>(previewStackInitialScale, true);
		}
		if (scale > max)
		{
			scale = max;
			return new ImmutablePair<Float, Boolean>(previewStackInitialScale, true);
		}
		return new ImmutablePair<Float, Boolean>(scale, false);
	}

	public static Triple<Vec3d, Vec3d, Float> dragObject(int clickedMouseButton, float deltaX, float deltaY, Vec3d translationInitialVec,
			Vec3d rotationVec, float scale, float scaleMax, float rotationMultiplierX, float rotationMultiplierY, boolean affectRotation)
	{
		MutableTriple<Vec3d, Vec3d, Float> triple = new MutableTriple<Vec3d, Vec3d, Float>(translationInitialVec, rotationVec, scale);
		if (clickedMouseButton == 0)
		{
			if (GuiScreen.isShiftKeyDown() || GuiScreen.isCtrlKeyDown())
			{
				triple.setRight(changeScale(scale, deltaY * 0.05F, scaleMax).getLeft());
			}
			else if (affectRotation)
			{
				double angleX = rotationVec.x - (deltaY / scale) * rotationMultiplierX;
				double angleY = rotationVec.y - (deltaX / scale) * rotationMultiplierY;
				if (angleX < -90 || angleX > 90)
					angleX = 90 * (angleX > 0 ? 1 : -1);
				
				triple.setMiddle(new Vec3d(angleX, angleY, 0));
			}
		}
		else if (clickedMouseButton == 1)
		{
			triple.setLeft(new Vec3d(translationInitialVec.x - deltaX, translationInitialVec.y - deltaY, 0));
		}
		return triple;
	}

	public static Pair<Vec3d, Float> scaleObjectWithMouseWheel(GuiScreen screen, AABB box,
			Vec3d translationVec, float scale, float scaleMax, float yOffset)
	{
		MutablePair<Vec3d, Float> pair = new MutablePair<Vec3d, Float>(translationVec, scale);
		if (Mouse.getEventDWheel() == 0)
			return pair;
		
		int mouseX = Mouse.getEventX() * screen.width / screen.mc.displayWidth;
		int mouseY = screen.height - Mouse.getEventY() * screen.height / screen.mc.displayHeight - 1;
		if (!GuiHelper.isCursorInsideBox(box, mouseX, mouseY))
			return pair;
		
		float amount = Mouse.getEventDWheel();
		Pair<Float, Boolean> scaleNew = changeScale(scale, amount * 0.005F, scaleMax);
		if (scaleNew.getRight())
			return pair;
		
		pair.setRight(scaleNew.getLeft());
		float x = mouseX - (int) (translationVec.x + (box.maxX + box.minX) * 0.5F);
		float y = mouseY - (int) (translationVec.y + yOffset + (box.maxY + box.minY) * 0.5F);
		float offset = (amount / -30) * 0.15F;
		pair.setLeft(translationVec.addVector(x * offset, y * offset, 0));
		return pair;
	}
	
}