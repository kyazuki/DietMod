package com.github.kyazuki.dietmod.network;

import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CapabilityPacket {
  private final int playerEntityID;
  private final float scale;

  public CapabilityPacket(int playerEntityID, float scale) {
    this.playerEntityID = playerEntityID;
    this.scale = scale;
  }

  public static void encode(CapabilityPacket pkt, PacketBuffer buf) {
    buf.writeInt(pkt.playerEntityID);
    buf.writeFloat(pkt.scale);
  }

  public static CapabilityPacket decode(PacketBuffer buf) {
    return new CapabilityPacket(buf.readInt(), buf.readFloat());
  }

  public static void handle(CapabilityPacket pkt, Supplier<NetworkEvent.Context> contextSupplier) {
    NetworkEvent.Context context = contextSupplier.get();
    context.enqueueWork(() -> handleClient(pkt));
    context.setPacketHandled(true);
  }

  @OnlyIn(Dist.CLIENT)
  public static void handleClient(CapabilityPacket pkt) {
    PlayerEntity player = (PlayerEntity) Minecraft.getInstance().world.getEntityByID(pkt.playerEntityID);
    if (player == null) return;
    player.getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new).setScale(pkt.scale);
  }
}
