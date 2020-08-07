package com.github.kyazuki.dietmod.capabilities;

public class Scale implements IScale {
  private float scale;
  private float walkDistance;
  private float prevWalkDistance;

  public Scale(float scale) {
    this.scale = scale;
    this.walkDistance = 0.0f;
    this.prevWalkDistance = 0.0f;
  }

  @Override
  public boolean setScale(float value) {
    boolean changed = false;
    if (this.scale != value) {
      this.scale = value;
      changed = true;
    }
    return changed;
  }

  @Override
  public float getScale() {
    return this.scale;
  }

  @Override
  public void setWalkDistance(float value) {
    this.walkDistance = value;
  }

  @Override
  public float getWalkDistance() {
    return this.walkDistance;
  }

  @Override
  public float calcWalkDistance(float blocks) {
    this.walkDistance = blocks - this.prevWalkDistance;
    return this.walkDistance;
  }

  @Override
  public void resetWalkDistance() {
    this.walkDistance = 0.0f;
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
  public void eatFoods(float value) {
    this.prevWalkDistance += value;
  }

  @Override
  public void copy(IScale cap) {
    this.scale = cap.getScale();
    this.walkDistance = cap.getWalkDistance();
    this.prevWalkDistance = cap.getPrevWalkDistance();
  }
}