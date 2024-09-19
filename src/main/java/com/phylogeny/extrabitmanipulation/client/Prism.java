package com.phylogeny.extrabitmanipulation.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Quadric;

public class Prism extends Quadric {
  private final boolean isPryamid;
  private final boolean isTriangular;

  public Prism(boolean isPryamid, boolean isTriangular) {
    this.isPryamid = isPryamid;
    this.isTriangular = isTriangular;
  }

  public void draw(float radius, boolean isOpen) {
    float slope = isPryamid ? radius : 0;
    float slope2 = isTriangular ? radius : 0;
    boolean isCube = !isPryamid && !isTriangular;
    GL11.glPushMatrix();
    GL11.glTranslated(0, radius, isCube ? -radius : 0);
    drawSquare(radius, isPryamid, slope, slope2);
    GL11.glTranslated(0, -radius * 2, 0);
    GL11.glScaled(1, -1, 1);
    drawSquare(radius, isPryamid, slope, slope2);
    GL11.glPopMatrix();

    GL11.glRotated(90, 0, 0, 1);

    GL11.glPushMatrix();
    GL11.glTranslated(0, radius, isCube ? -radius : 0);
    if (!isTriangular) {
      drawSquare(radius, isPryamid, slope, 0);
    }
    GL11.glTranslated(0, -radius * 2, 0);
    GL11.glScaled(1, -1, 1);
    drawSquare(radius, isPryamid, slope, 0);
    GL11.glPopMatrix();

    GL11.glRotated(90, 1, 0, 0);

    if (isCube || !isOpen) {
      GL11.glPushMatrix();
      GL11.glTranslated(0, isCube ? radius : radius * 2, -radius);
      drawSquare(radius, false, isTriangular ? radius : 0, 0);
      GL11.glPopMatrix();
    }

    if (isCube || (!isPryamid && !isOpen)) {
      GL11.glPushMatrix();
      GL11.glTranslated(0, isCube ? -radius : 0, -radius);
      drawSquare(radius, false, isTriangular ? radius : 0, 0);
      GL11.glPopMatrix();
    }
  }

  private void drawSquare(float radius, boolean isSlanted, float slope, float slope2) {
    float height = radius * 2;
    int i;
    float x, y, z;
    float inc = height / 15;
    float ratio = (radius / height) * 4F;
    float halfRadius = -radius * 0.5F;
    GL11.glBegin(GL11.GL_LINES);
    for (i = 0; i <= 15; i++) {
      x = i * inc - radius;
      y = slope2 > 0 ? halfRadius + x / ratio : 0;
      GL11.glVertex3f(slope > 0 ? 0 : x, isSlanted ? -slope : y, 0);
      GL11.glVertex3f(x, y, height);
    }
    GL11.glEnd();
    GL11.glBegin(GL11.GL_LINES);
    for (i = 0; i <= 15; i++) {
      z = i * inc;
      x = z / ratio;
      y = isSlanted ? x - radius : 0;
      if (slope == 0) {
        x = radius;
      }

      float s = slope2;
      if (isPryamid && slope2 > 0) {
        s *= 1 * (z / height);
      }

      GL11.glVertex3f(-x, y - s, z);
      GL11.glVertex3f(x, y, z);
    }
    GL11.glEnd();
  }

}