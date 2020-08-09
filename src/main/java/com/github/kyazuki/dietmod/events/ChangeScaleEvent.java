package com.github.kyazuki.dietmod.events;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class ChangeScaleEvent extends PlayerEvent {
  private final float scale;

  public ChangeScaleEvent(PlayerEntity player, float scale) {
    super(player);
    this.scale = scale;
  }

  public float getScale() {
    return scale;
  }
}
