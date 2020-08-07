package com.github.kyazuki.dietmod.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ScaleProvider implements ICapabilitySerializable<INBT> {
  @CapabilityInject(IScale.class)
  public static final Capability<IScale> SCALE_CAP = null;

  private LazyOptional<IScale> instance = LazyOptional.of(SCALE_CAP::getDefaultInstance);

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    return SCALE_CAP.orEmpty(cap, instance);
  }

  @Override
  public INBT serializeNBT() {
    return SCALE_CAP.getStorage().writeNBT(SCALE_CAP, instance.orElseThrow(() -> new IllegalArgumentException("at serialize")), null);
  }

  @Override
  public void deserializeNBT(INBT nbt) {
    SCALE_CAP.getStorage().readNBT(SCALE_CAP, instance.orElseThrow(() -> new IllegalArgumentException("at deserialize")), null, nbt);
  }
}
