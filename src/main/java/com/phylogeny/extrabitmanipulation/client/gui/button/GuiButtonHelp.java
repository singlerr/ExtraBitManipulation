package com.phylogeny.extrabitmanipulation.client.gui.button;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GuiButtonHelp extends GuiButtonCustom
{
	List<GuiButton> buttonList;
	
	public GuiButtonHelp(int buttonId, List<GuiButton> buttonList, int x, int y, String hoverText, String hoverTextSelected)
	{
		super(buttonId, x, y, 12, 12, "?", hoverText);
		this.buttonList = buttonList;
		setHoverTextSelected(hoverTextSelected);
		setTextOffsetX(0.5F);
		setTextOffsetY(0.5F);
	}
	
	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		boolean pressed = super.mousePressed(mc, mouseX, mouseY);
		if (pressed)
		{
			boolean helpMode = !selected;
			selected = helpMode;
			for (GuiButton button : buttonList)
			{
				if (button != this && button instanceof GuiButtonBase)
					((GuiButtonBase) button).setHelpMode(helpMode);
			}
		}
		return pressed;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		int x = this.x + 6;
		int y = this.y + 6;
		double radius = 6;
		int red, green, blue;
		if (selected)
		{
			red = blue = 0;
			green = 200;
		}
		else
		{
			red = green = blue = 120;
		}
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(x, y, 0).color(red, green, blue, 255).endVertex();
		double s = 30;
		for(int k = 0; k <= s; k++) 
		{
			double angle = (Math.PI * 2 * k / s) + Math.toRadians(180);
			buffer.vertex(x + Math.sin(angle) * radius, y + Math.cos(angle) * radius, 0).color(red, green, blue, 255).endVertex();
		}
		tessellator.end();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		super.drawButton(mc, mouseX, mouseY, partialTicks);
	}
	
}