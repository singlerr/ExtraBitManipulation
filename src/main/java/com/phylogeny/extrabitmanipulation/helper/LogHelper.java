package com.phylogeny.extrabitmanipulation.helper;

import com.phylogeny.extrabitmanipulation.reference.Reference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogHelper {
  private static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

  public static Logger getLogger() {
    return LOGGER;
  }

}