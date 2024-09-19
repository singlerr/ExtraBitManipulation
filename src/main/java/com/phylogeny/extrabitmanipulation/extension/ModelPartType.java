package com.phylogeny.extrabitmanipulation.extension;

public enum ModelPartType {
  HEAD("head"),
  BODY("body"),
  RIGHT_LEG("right_leg"),
  LEFT_LEG("left_leg"),
  RIGHT_ARM("right_arm"),
  LEFT_ARM("left_arm");

  private final String name;

  ModelPartType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
