package com.github.kyazuki.dietmod.network;

import com.github.kyazuki.dietmod.DietMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketHandler {
  private static final String PROTOCOL_VERSION = "1.0";
  public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
          .named(new ResourceLocation(DietMod.MODID, "main_channel"))
          .clientAcceptedVersions(PROTOCOL_VERSION::equals)
          .serverAcceptedVersions(PROTOCOL_VERSION::equals)
          .networkProtocolVersion(() -> PROTOCOL_VERSION)
          .simpleChannel();

  public static void register() {
    CHANNEL.registerMessage(0, CapabilityPacket.class, CapabilityPacket::encode, CapabilityPacket::decode, CapabilityPacket::handle);
  }

  public static void sendTo(Object message, PlayerEntity player) {
    CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
  }

  public static void sendToAll(Object message) {
    CHANNEL.send(PacketDistributor.ALL.noArg(), message);
  }
}
