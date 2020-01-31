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

  public static int distanceToNormal;
  public static double killHealth;
  public static boolean count_food;
  public static boolean change_hitbox;
  public static boolean change_max_health;
  public static boolean change_speed;
  public static boolean change_jump_boost;

  @SubscribeEvent
  public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
    if (configEvent.getConfig().getSpec() == DietModConfig.CLIENT_SPEC) {
      bakeConfig();
    }
  }

  public static void bakeConfig() {
    distanceToNormal = CLIENT.distanceToNormal.get();
    killHealth = CLIENT.killHealth.get();
    count_food = CLIENT.count_food.get();
    change_hitbox = CLIENT.change_hitbox.get();
    change_max_health = CLIENT.change_max_health.get();
    change_speed = CLIENT.change_speed.get();
    change_jump_boost = CLIENT.change_jump_boost.get();
  }

  public static class ClientConfig {

    public final ForgeConfigSpec.IntValue distanceToNormal;
    public final ForgeConfigSpec.DoubleValue killHealth;
    public final ForgeConfigSpec.BooleanValue count_food;
    public final ForgeConfigSpec.BooleanValue change_hitbox;
    public final ForgeConfigSpec.BooleanValue change_max_health;
    public final ForgeConfigSpec.BooleanValue change_speed;
    public final ForgeConfigSpec.BooleanValue change_jump_boost;

    public ClientConfig(ForgeConfigSpec.Builder builder) {
      builder.push("DietMod Config");
      distanceToNormal = builder
              .comment("A default distance to normal.")
              .translation(DietMod.MODID + ".config." + "distanceToNormal")
              .defineInRange("distanceToNormal", 1000, 100, 100000);
      killHealth = builder
              .comment("Kill players by malnutrition when their max health is under this value.")
              .translation(DietMod.MODID + ".config." + "killHealth")
              .defineInRange("killHealth", 0.45d, 0.1d, 20.0d);
      count_food = builder
              .comment("Players are fat when they eat foods.")
              .translation(DietMod.MODID + ".config" + "count_food")
              .define("count_food", true);
      change_hitbox = builder
              .comment("Whether Player's hitbox is changed.")
              .translation(DietMod.MODID + ".config" + "change_hitbox")
              .define("change_hitbox", true);
      change_max_health = builder
              .comment("Whether Player's max health is changed.")
              .translation(DietMod.MODID + ".config" + "change_max_health")
              .define("change_max_health", true);
      change_speed = builder
              .comment("Whether Player's spped is changed.")
              .translation(DietMod.MODID + ".config" + "change_speed")
              .define("change_speed", true);
      change_jump_boost = builder
              .comment("Whether Player's jump power is changed.")
              .translation(DietMod.MODID + ".config" + "change_jump_boost")
              .define("change_jump_boost", true);
      builder.pop();
    }

  }

}