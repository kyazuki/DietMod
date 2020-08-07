package com.github.kyazuki.dietmod.network;

import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CapabilityPacket {
  private final float scale;

  public CapabilityPacket(float scale) {
    this.scale = scale;
  }

  public static void encode(CapabilityPacket pkt, PacketBuffer buf) {
    buf.writeFloat(pkt.scale);
  }

  public static CapabilityPacket decode(PacketBuffer buf) {
    return new CapabilityPacket(buf.readFloat());
  }

  public static void handle(CapabilityPacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    context.enqueueWork(() -> {
      PlayerEntity player = Minecraft.getInstance().player;
      if (player == null) return;
      player.getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new).setScale(pkt.scale);
    });
    context.setPacketHandled(true);
  }
}
