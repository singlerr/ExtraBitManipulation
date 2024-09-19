package com.phylogeny.extrabitmanipulation.config;

public class ConfigShapeRender extends ConfigNamed {
  public boolean renderOuterShape, renderInnerShape;
  private final boolean renderOuterShapeDefault;
  private final boolean renderInnerShapeDefault;
  public float outerShapeAlpha, innerShapeAlpha, red, green, blue;
  private final int outerShapeAlphaDefault;
  private final int innerShapeAlphaDefault;
  private final int redDefault;
  private final int greenDefault;
  private final int blueDefault;
  public float lineWidth;
  private final float lineWidthDefault;

  public ConfigShapeRender(String title, boolean renderOuterShapeDefault,
                           boolean renderInnerShapeDefault, int outerShapeAlphaDefault,
                           int innerShapeAlphaDefault, int redDefault, int greenDefault,
                           int blueDefault, float lineWidthDefault) {
    super(title);
    this.renderOuterShapeDefault = renderOuterShapeDefault;
    this.renderInnerShapeDefault = renderInnerShapeDefault;
    this.outerShapeAlphaDefault = outerShapeAlphaDefault;
    this.innerShapeAlphaDefault = innerShapeAlphaDefault;
    this.redDefault = redDefault;
    this.greenDefault = greenDefault;
    this.blueDefault = blueDefault;
    this.lineWidthDefault = lineWidthDefault;
  }

  public boolean getRenderOuterShapeDefault() {
    return renderOuterShapeDefault;
  }

  public boolean getRenderInnerShapeDefault() {
    return renderInnerShapeDefault;
  }

  public int getOuterShapeAlphaDefault() {
    return outerShapeAlphaDefault;
  }

  public int getInnerShapeAlphaDefault() {
    return innerShapeAlphaDefault;
  }

  public int getRedDefault() {
    return redDefault;
  }

  public int getGreenDefault() {
    return greenDefault;
  }

  public int getBlueDefault() {
    return blueDefault;
  }

  public float getLineWidthDefault() {
    return lineWidthDefault;
  }

}