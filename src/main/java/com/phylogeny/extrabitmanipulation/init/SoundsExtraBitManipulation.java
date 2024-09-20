package com.phylogeny.extrabitmanipulation.init;

import com.phylogeny.extrabitmanipulation.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundsExtraBitManipulation {
  public static SoundEvent boxCheck, boxUncheck;

  public static void registerSounds() {
    boxCheck = registerSound("box_check");
    boxUncheck = registerSound("box_uncheck");
  }

  private static SoundEvent registerSound(String soundName) {
    ResourceLocation soundNameResLoc = new ResourceLocation(Reference.MOD_ID + ":" + soundName);

    SoundEvent sound = SoundEvent.createVariableRangeEvent(soundNameResLoc);
    Registry.register(BuiltInRegistries.SOUND_EVENT, soundName, sound);
    return sound;
  }

  public static void playSound(SoundEvent sound) {
    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
  }

}