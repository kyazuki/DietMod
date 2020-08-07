package com.github.kyazuki.dietmod.capabilities;

public class Scale implements IScale {
  private float scale;
  private float prevWalkDistance;

  public Scale(float scale) {
    this.scale = scale;
    this.prevWalkDistance = 0.0f;
  }

  @Override
  public void setScale(float value) {
    this.scale = value;
  }

  @Override
  public float getScale() {
    return this.scale;
  }

  @Override
  public void setPrevWalkDistance(float value) {
    this.prevWalkDistance = value;
  }

  @Override
  public float getPrevWalkDistance() {
    return this.prevWalkDistance;
  }

  @Override
  public void copy(IScale cap) {
    this.scale = cap.getScale();
    this.prevWalkDistance = cap.getPrevWalkDistance();
  }
}