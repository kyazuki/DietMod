package com.github.kyazuki.dietmod.capabilities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class ScaleStorage implements Capability.IStorage<IScale> {
  @Override
  public INBT writeNBT(Capability<IScale> capability, IScale instance, Direction side) {
    CompoundNBT tag = new CompoundNBT();
    tag.putFloat("scale", instance.getScale());
    tag.putFloat("walkDistance", instance.getWalkDistance());
    tag.putFloat("prevWalkDistance", instance.getPrevWalkDistance());
    return tag;
  }

  @Override
  public void readNBT(Capability<IScale> capability, IScale instance, Direction side, INBT nbt) {
    CompoundNBT tag = (CompoundNBT) nbt;
    instance.setScale(tag.getFloat("scale"));
    instance.setWalkDistance(tag.getFloat("walkDistance"));
    instance.setPrevWalkDistance(tag.getFloat("prevWalkDistance"));
  }
}
