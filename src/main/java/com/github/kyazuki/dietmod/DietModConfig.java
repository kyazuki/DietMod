package com.github.kyazuki.dietmod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = DietMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DietModConfig {
  public static final ClientConfig CLIENT;
  public static final ForgeConfigSpec CLIENT_SPEC;
  static {
    final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
    CLIENT_SPEC = specPair.getRight();
    CLIENT = specPair.getLeft();
  }

  public static int distanceToDeath;

  @SubscribeEvent
  public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
    if (configEvent.getConfig().getSpec() == DietModConfig.CLIENT_SPEC) {
      bakeConfig();
    }
  }

  public static void bakeConfig() {
    distanceToDeath = CLIENT.distanceToDeath.get();
  }

  public static class ClientConfig {

    public final ForgeConfigSpec.IntValue distanceToDeath;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
      builder.push("DietMod Config");
      distanceToDeath = builder
              .comment("A default distance to die.")
              .translation(DietMod.MODID + ".config." + "distanceToDeath")
              .defineInRange("distanceToDeath", 1000, 100, 100000);
      builder.pop();
    }

  }

}