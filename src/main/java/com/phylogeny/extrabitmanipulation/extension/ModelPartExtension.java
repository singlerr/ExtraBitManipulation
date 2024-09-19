package com.phylogeny.extrabitmanipulation.extension;

import net.minecraft.client.model.geom.ModelPart;

public interface ModelPartExtension {

  void ebm$putChild(ModelPartType type, ModelPart child);


  ModelPart ebm$getChild(ModelPartType type);
}
