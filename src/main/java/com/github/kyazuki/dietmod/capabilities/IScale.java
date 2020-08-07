package com.github.kyazuki.dietmod.capabilities;

public interface IScale {
  boolean setScale(float value);

  float getScale();

  void setWalkDistance(float value);

  float getWalkDistance();

  float calcWalkDistance(float blocks);

  void resetWalkDistance();

  void setPrevWalkDistance(float value);

  float getPrevWalkDistance();

  void eatFoods(float value);

  void copy(IScale cap);
}
