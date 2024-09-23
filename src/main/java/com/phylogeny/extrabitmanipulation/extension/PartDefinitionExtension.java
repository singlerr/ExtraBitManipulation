package com.phylogeny.extrabitmanipulation.extension;

import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelPart;

public interface PartDefinitionExtension {

  void ebm$registerBakeListener(Consumer<ModelPart> onBake);


}
