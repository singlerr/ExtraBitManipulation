package com.phylogeny.extrabitmanipulation.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.phylogeny.extrabitmanipulation.client.ClientHelper;
import com.phylogeny.extrabitmanipulation.entity.EntityBit;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RenderEntityBit extends EntityRenderer<EntityBit> {

  public RenderEntityBit(EntityRendererProvider.Context context) {
    super(context);

  }


  @Override
  public void render(EntityBit entity, float f, float g, PoseStack poseStack,
                     MultiBufferSource multiBufferSource, int i) {

    if (entity.getBitStack() != null) {
      if (renderOutlines) {
        GlStateManager.enableColorMaterial();
        GlStateManager.enableOutlineMode(getTeamColor(entity));
      }
      GlStateManager.pushMatrix();
//      GlStateManager.translate((float) x, (float) y, (float) z);
      ClientHelper.getRenderItem().renderItem(entity.getBitStack(), TransformType.GROUND);
      GlStateManager.popMatrix();
      if (renderOutlines) {
        GlStateManager.disableOutlineMode();
        GlStateManager.disableColorMaterial();
      }
    }
  }


  @Override
  public ResourceLocation getTextureLocation(EntityBit entity) {
    return null;
  }

  @Override
  public void doRender(EntityBit entity, double x, double y, double z, float entityYaw,
                       float partialTicks) {

    super.doRender(entity, x, y, z, entityYaw, partialTicks);
  }

  @Override
  @Nullable
  protected ResourceLocation getEntityTexture(EntityBit entity) {
    return null;
  }

}