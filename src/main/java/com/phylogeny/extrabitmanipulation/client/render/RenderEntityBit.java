package com.phylogeny.extrabitmanipulation.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.lwjgl.opengl.GL11;

public class RenderEntityBit extends EntityRenderer<EntityBit> {

  public RenderEntityBit(EntityRendererProvider.Context context) {
    super(context);

  }


  @Override
  public void render(EntityBit entity, float f, float g, PoseStack poseStack,
                     MultiBufferSource multiBufferSource, int i) {

    if (entity.getBitStack() != null) {

//      if (renderOutlines) {
//        GlStateManager.enableColorMaterial();
//        GlStateManager.enableOutlineMode(getTeamColor(entity));
//      }
      GL11.glPushMatrix();
//      GlStateManager.translate((float) x, (float) y, (float) z);
      ClientHelper.getRenderItem().renderStatic(entity.getBitStack(), ItemDisplayContext.GROUND, i,
          OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, entity.level(), 0);
      GL11.glPopMatrix();
//      if (renderOutlines) {
//        GlStateManager.disableOutlineMode();
//        GlStateManager.disableColorMaterial();
//      }
    }

    super.render(entity, f, g, poseStack, multiBufferSource, i);
  }


  @Override
  public ResourceLocation getTextureLocation(EntityBit entity) {
    return null;
  }


}