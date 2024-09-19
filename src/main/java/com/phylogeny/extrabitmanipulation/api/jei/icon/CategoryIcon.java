package com.phylogeny.extrabitmanipulation.api.jei.icon;

import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class CategoryIcon extends CategoryIconResourceBase {
  private static ResourceLocation image;

  public CategoryIcon(int u, int v, int width, int height, int textureWidth, int textureHeight,
                      String imagePath) {
    super(u, v, width, height, textureWidth, textureHeight);
    image = new ResourceLocation(Reference.MOD_ID, imagePath + ".png");
  }

  @Override
  protected void bindTexture(Minecraft minecraft) {
    ClientHelper.bindTexture(image);
  }

}