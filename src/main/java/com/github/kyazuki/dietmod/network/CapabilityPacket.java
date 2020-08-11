package com.github.kyazuki.dietmod.network;

import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import com.github.kyazuki.dietmod.events.DietModEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
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
    context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handleClient(pkt.playerEntityID, pkt.scale)));
    context.setPacketHandled(true);
  }

  public static class Handle {
    public static DistExecutor.SafeRunnable handleClient(int playerEntityID, float scale) {
      return new DistExecutor.SafeRunnable() {
        @Override
        public void run() {
          Entity player = Minecraft.getInstance().world.getEntityByID(playerEntityID);
          if (!(player instanceof PlayerEntity)) return;
          player.getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new).setScale(scale);
          MinecraftForge.EVENT_BUS.post(new DietModEvents.UpdatePlayerSizeEvent((PlayerEntity) player));
        }
      };
    }
  }
}
