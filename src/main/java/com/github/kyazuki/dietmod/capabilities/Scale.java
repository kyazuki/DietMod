package com.github.kyazuki.dietmod.capabilities;

public class Scale implements IScale {
  private float scale;
  private float prevWalkDistance;

  public Scale(float scale) {
    this.scale = scale;
    prevWalkDistance = 0.0f;
  }

  @Override
  public void setScale(float value) {
    scale = value;
  }

  @Override
  public float getScale() {
    return scale;
  }

  @Override
  public void setPrevWalkDistance(float value) {
    prevWalkDistance = value;
  }

  @Override
  public float getPrevWalkDistance() {
    return prevWalkDistance;
  }

  @Override
  public void copy(IScale cap) {
    scale = cap.getScale();
    prevWalkDistance = cap.getPrevWalkDistance();
  }
}