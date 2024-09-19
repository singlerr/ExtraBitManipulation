package com.phylogeny.extrabitmanipulation.api.jei;

import com.phylogeny.extrabitmanipulation.helper.LogHelper;
import java.util.IllegalFormatException;
import net.minecraft.client.resources.language.I18n;

/**
 * This class is taken by permission from JEI.
 *
 * @author mezz
 */
@SuppressWarnings("deprecation")
public final class Translator {
  private Translator() {
  }

  public static String translateToLocal(String key) {
    if (I18n.exists(key)) {
      return I18n.get(key);
    }

    return I18n.get(key);
  }

  public static String translateToLocalFormatted(String key, Object... format) {
    String s = translateToLocal(key);
    try {
      return String.format(s, format);
    } catch (IllegalFormatException e) {
      LogHelper.getLogger().error("Format error: {}", s, e);
      return "Format error: " + s;
    }
  }

}