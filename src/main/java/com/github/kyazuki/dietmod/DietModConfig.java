package com.github.kyazuki.dietmod;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = DietMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DietModConfig {
  public static final CommonConfig COMMON;
  public static final ForgeConfigSpec COMMON_SPEC;

  static {
    final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
    COMMON_SPEC = specPair.getRight();
    COMMON = specPair.getLeft();
  }

  public static double maxScale;
  public static int distanceToNormal;
  public static double killHealth;
  public static boolean count_food;
  public static double food_modifier;
  public static boolean change_hitbox;
  public static boolean change_max_health;
  public static boolean change_speed;
  public static boolean change_jump_boost;

  @SubscribeEvent
  public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
    if (configEvent.getConfig().getSpec() == DietModConfig.COMMON_SPEC) {
      bakeConfig();
    }
  }

  public static void bakeConfig() {
    maxScale = COMMON.maxScale.get();
    distanceToNormal = COMMON.distanceToNormal.get();
    killHealth = COMMON.killHealth.get();
    count_food = COMMON.count_food.get();
    food_modifier = COMMON.food_modifier.get();
    change_hitbox = COMMON.change_hitbox.get();
    change_max_health = COMMON.change_max_health.get();
    change_speed = COMMON.change_speed.get();
    change_jump_boost = COMMON.change_jump_boost.get();
  }

  public static class CommonConfig {

    public final ForgeConfigSpec.DoubleValue maxScale;
    public final ForgeConfigSpec.IntValue distanceToNormal;
    public final ForgeConfigSpec.DoubleValue killHealth;
    public final ForgeConfigSpec.BooleanValue count_food;
    public final ForgeConfigSpec.DoubleValue food_modifier;
    public final ForgeConfigSpec.BooleanValue change_hitbox;
    public final ForgeConfigSpec.BooleanValue change_max_health;
    public final ForgeConfigSpec.BooleanValue change_speed;
    public final ForgeConfigSpec.BooleanValue change_jump_boost;

    public CommonConfig(ForgeConfigSpec.Builder builder) {
      builder.push("DietMod Config");
      maxScale = builder
              .comment("Max size of player.")
              .translation(DietMod.MODID + ".config." + "maxScale")
              .defineInRange("maxScale", 2.0d, 1.0d, 10.0d);
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
      food_modifier = builder
              .comment("Fat amount by hunger.")
              .translation(DietMod.MODID + ".config." + "food_modifier")
              .defineInRange("food_modifier", 0.025d, 0.0d, 10.0d);
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