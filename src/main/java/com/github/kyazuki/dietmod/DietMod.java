package com.github.kyazuki.dietmod;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mod(DietMod.MODID)
@Mod.EventBusSubscriber
public class DietMod {
  public static final String MODID = "dietmod";
  public static final Logger LOGGER = LogManager.getLogger(MODID);
  public static final UUID FatHealth = UUID.fromString("7f73c613-1c4f-45df-8176-6e3f9590347b");
  public static final UUID FatMovenentSpeed = UUID.fromString("bba5aa63-ec4b-481f-9e2f-18d0c0a8e615");
  public static final float maxScale = 2.0f;
  public static final float food_modifier = 10.0f;
  public static DamageSource Malnutrition = (new DamageSource("dietmod:malnutrition")).setDamageBypassesArmor();
  public static float scale = 2.0f;
  public static float hitboxScale = 0.6f * maxScale;
  public static float prevWalkDistance = 0.0f;
  public static float walkDistance = 0.0f;
  public static float prevMaxHealth = maxScale * 20.0f;
  public static float food_heal = 0.0f;
  public static ModifiableAttributeInstance player_max_health = null;
  public static ModifiableAttributeInstance player_movement_speed = null;

  public DietMod() {
    LOGGER.debug("DietMod Loaded.");
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, DietModConfig.CLIENT_SPEC);
  }

  @SubscribeEvent
  public static void onCommandsRegister(RegisterCommandsEvent event) {
    SetDistanceCommand.register(event.getDispatcher());
  }

  public static void setFatPlayer(PlayerEntity player) {
    player.getAttribute(Attributes.MAX_HEALTH).removeModifier(FatHealth);
    if (DietModConfig.change_max_health) {
      player.getAttribute(Attributes.MAX_HEALTH).applyNonPersistentModifier(new AttributeModifier(FatHealth, "FatMaxHealth", maxScale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
      player.setHealth(maxScale * 20.0f);
    }
    player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FatMovenentSpeed);
    if (DietModConfig.change_speed)
      player.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(new AttributeModifier(FatMovenentSpeed, "FatMovementSpeed", 1.0f / maxScale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
  }

  public static void resetPlayer(PlayerEntity player) {
    setFatPlayer(player);
    prevWalkDistance = -player.distanceWalkedModified;
    walkDistance = 0.0f;
  }

  @SubscribeEvent
  public static void calc(TickEvent.PlayerTickEvent event) {
    if (!event.player.world.isRemote()) {
      if (DietModConfig.count_food)
        walkDistance = prevWalkDistance + event.player.distanceWalkedModified / 0.6f - food_heal;
      else
        walkDistance = prevWalkDistance + event.player.distanceWalkedModified / 0.6f;
      scale = MathHelper.clamp(maxScale - walkDistance / DietModConfig.distanceToNormal, 0.2f, maxScale);
    }
  }

  @SubscribeEvent
  public static void sethitboxPlayer(TickEvent.PlayerTickEvent event) {
    if (DietModConfig.change_hitbox && event.player.isAlive()) {
      if (scale > 1.2f)
        hitboxScale = 1.2f;
      else
        hitboxScale = MathHelper.clamp(0.6f * scale, 0.2f, 0.6f * maxScale);
      AxisAlignedBB playerBoundingBox = event.player.getBoundingBox();
      event.player.setBoundingBox(new AxisAlignedBB(playerBoundingBox.minX, playerBoundingBox.minY, playerBoundingBox.minZ, playerBoundingBox.minX + hitboxScale, playerBoundingBox.maxY, playerBoundingBox.minZ + hitboxScale));
    }
  }

  @SubscribeEvent
  public static void setHealthPlayer(TickEvent.PlayerTickEvent event) {
    if (DietModConfig.change_max_health && event.player.isAlive() && !event.player.world.isRemote()) {
      float scaleHealth = Math.round(scale * 10) / 10.0f;
      if (prevMaxHealth != scaleHealth * 20.0f) {
        player_max_health = event.player.getAttribute(Attributes.MAX_HEALTH);
        player_max_health.removeModifier(FatHealth);
        player_max_health.applyNonPersistentModifier(new AttributeModifier(FatHealth, "FatMaxHealth", scaleHealth - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
        prevMaxHealth = event.player.getMaxHealth();
        if (event.player.getHealth() > event.player.getMaxHealth())
          event.player.setHealth(event.player.getMaxHealth());
      }
    }
  }

  @SubscribeEvent
  public static void setSpeedPlayer(TickEvent.PlayerTickEvent event) {
    if (DietModConfig.change_speed && event.player.isAlive() && !event.player.world.isRemote()) {
      double speed = MathHelper.clamp(1.0f / scale, 0.8f, 1.5f);
      player_movement_speed = event.player.getAttribute(Attributes.MOVEMENT_SPEED);
      player_movement_speed.removeModifier(FatMovenentSpeed);
      player_movement_speed.applyNonPersistentModifier(new AttributeModifier(FatMovenentSpeed, "FatMovementSpeed", speed - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
  }

  @SubscribeEvent
  public static void onFOVChange(FOVUpdateEvent event) {
    if (event.getEntity() instanceof PlayerEntity) {
      PlayerEntity player = event.getEntity();
      EffectInstance speed = player.getActivePotionEffect(Effects.SPEED);
      float fov = 0.9f, sprint_fov = 1.02f;
      if (player.isSprinting())
        event.setNewfov(speed != null ? sprint_fov + ((0.104f * (speed.getAmplifier() + 1))) : sprint_fov);
      else
        event.setNewfov(speed != null ? fov + (0.08f * (speed.getAmplifier() + 1)) : fov);
    }
  }

  @SubscribeEvent
  public static void onJumpPlayer(LivingEvent.LivingJumpEvent event) {
    if (DietModConfig.change_jump_boost && event.getEntity() instanceof PlayerEntity) {
      if (scale < 1.0f) {
        PlayerEntity player = (PlayerEntity) event.getEntity();
        double motionY = MathHelper.clamp(player.getMotion().y / scale, player.getMotion().y, player.getMotion().y * 1.5f);
        player.setMotion(player.getMotion().x, motionY, player.getMotion().z);
      }
    }
  }

  @SubscribeEvent
  public static void onkillPlayer(TickEvent.PlayerTickEvent event) {
    if (!event.player.world.isRemote()) {
      if (maxScale - walkDistance / DietModConfig.distanceToNormal < DietModConfig.killHealth) {
        if (event.player.isAlive()) {
          event.player.attackEntityFrom(Malnutrition, 20.0f);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onHarvest(PlayerEvent.BreakSpeed event) {
    event.setNewSpeed(event.getOriginalSpeed() * scale);
  }

  @SubscribeEvent
  public static void onEat(LivingEntityUseItemEvent.Finish event) {
    if (DietModConfig.count_food && event.getEntity().world.isRemote()) {
      if (event.getEntity() instanceof PlayerEntity) {
        if (event.getItem().getItem().isFood()) {
          food_heal += event.getItem().getItem().getFood().getHealing() * food_modifier;
        }
      }
    }
  }

  @SubscribeEvent
  public static void onloginPlayer(PlayerEvent.PlayerLoggedInEvent event) {
    resetPlayer(event.getPlayer());
  }

  @SubscribeEvent
  public static void onrespawnPlayer(PlayerEvent.PlayerRespawnEvent event) {
    resetPlayer(event.getPlayer());
  }

  @SubscribeEvent
  public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
    event.getMatrixStack().push();
    event.getMatrixStack().scale(scale, 1.0f, scale);
  }

  @SubscribeEvent
  public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
    event.getMatrixStack().pop();
  }
}
