package com.github.kyazuki.dietmod;

import com.github.kyazuki.dietmod.capabilities.IScale;
import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import com.github.kyazuki.dietmod.network.CapabilityPacket;
import com.github.kyazuki.dietmod.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = DietMod.MODID)
public class EventSubscriber {
  public static final ResourceLocation SCALE_CAP_RESOURCE = new ResourceLocation(DietMod.MODID, "capabilities");
  public static final UUID FatHealth = UUID.fromString("7f73c613-1c4f-45df-8176-6e3f9590347b");
  public static final UUID FatMovenentSpeed = UUID.fromString("bba5aa63-ec4b-481f-9e2f-18d0c0a8e615");
  public static DamageSource Malnutrition = (new DamageSource("dietmod:malnutrition")).setDamageBypassesArmor();
  public static final float maxScale = 2.0f;
  public static final float food_modifier = 10.0f;

  // Utils

  public static IScale getCap(PlayerEntity player) {
    return player.getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new);
  }

  public static void resetAttributePlayer(PlayerEntity player) {
    player.getAttribute(Attributes.MAX_HEALTH).removeModifier(FatHealth);
    if (DietModConfig.change_max_health) {
      player.getAttribute(Attributes.MAX_HEALTH).applyNonPersistentModifier(new AttributeModifier(FatHealth, "FatMaxHealth", maxScale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
    player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FatMovenentSpeed);
    if (DietModConfig.change_speed)
      player.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(new AttributeModifier(FatMovenentSpeed, "FatMovementSpeed", 1.0f / maxScale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
  }

  public static void resetPlayer(PlayerEntity player) {
    resetAttributePlayer(player);
    IScale cap = getCap(player);
    cap.setPrevWalkDistance(player.distanceWalkedModified);
    cap.resetWalkDistance();
  }

  // Server Only

  @SubscribeEvent
  public static void calc(TickEvent.PlayerTickEvent event) {
    if (!event.player.getEntityWorld().isRemote()) {
      IScale cap = getCap(event.player);
      float walkDistance = cap.calcWalkDistance(event.player.distanceWalkedModified / 0.6f);
      float scale = MathHelper.clamp(maxScale - walkDistance / DietModConfig.distanceToNormal, 0.2f, maxScale);
      boolean changed = cap.setScale(scale);
      if (changed) PacketHandler.sendTo(new CapabilityPacket(scale), event.player);
    }
  }

  @SubscribeEvent
  public static void setHealthPlayer(TickEvent.PlayerTickEvent event) {
    if (DietModConfig.change_max_health && !event.player.getEntityWorld().isRemote() && event.player.isAlive()) {
      float scale = getCap(event.player).getScale();
      float scaleHealth = Math.round(scale * 10) / 10.0f;
      if (event.player.getMaxHealth() != scaleHealth * 20.0f) {
        ModifiableAttributeInstance player_max_health = event.player.getAttribute(Attributes.MAX_HEALTH);
        player_max_health.removeModifier(FatHealth);
        player_max_health.applyNonPersistentModifier(new AttributeModifier(FatHealth, "FatMaxHealth", scaleHealth - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (event.player.getHealth() > event.player.getMaxHealth())
          event.player.setHealth(event.player.getMaxHealth());
      }
    }
  }

  @SubscribeEvent
  public static void setSpeedPlayer(TickEvent.PlayerTickEvent event) {
    if (DietModConfig.change_speed && !event.player.getEntityWorld().isRemote() && event.player.isAlive()) {
      float scale = getCap(event.player).getScale();
      double speed = MathHelper.clamp(1.0f / scale, 0.8f, 1.5f);
      ModifiableAttributeInstance player_movement_speed = event.player.getAttribute(Attributes.MOVEMENT_SPEED);
      player_movement_speed.removeModifier(FatMovenentSpeed);
      player_movement_speed.applyNonPersistentModifier(new AttributeModifier(FatMovenentSpeed, "FatMovementSpeed", speed - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
  }

  @SubscribeEvent
  public static void killPlayer(TickEvent.PlayerTickEvent event) {
    if (!event.player.getEntityWorld().isRemote()) {
      float walkDistance = getCap(event.player).getWalkDistance();
      if (maxScale - walkDistance / DietModConfig.distanceToNormal < DietModConfig.killHealth) {
        if (event.player.isAlive()) {
          event.player.attackEntityFrom(Malnutrition, 20.0f);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onEat(LivingEntityUseItemEvent.Finish event) {
    if (DietModConfig.count_food && !event.getEntity().getEntityWorld().isRemote()) {
      if (event.getEntity() instanceof PlayerEntity) {
        PlayerEntity player = (PlayerEntity) event.getEntity();
        if (event.getItem().getItem().isFood()) {
          float food_heal = event.getItem().getItem().getFood().getHealing() * food_modifier;
          getCap(player).eatFoods(food_heal);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onClonePlayer(PlayerEvent.Clone event) {
    if (!event.getPlayer().getEntityWorld().isRemote() && event.isWasDeath()) {
      IScale newCap = getCap(event.getPlayer());
      IScale oldCap = getCap(event.getOriginal());
      newCap.copy(oldCap);
    }
  }

  // Server & Client

  @SubscribeEvent
  public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    if (!(event.getObject() instanceof PlayerEntity)) return;

    event.addCapability(SCALE_CAP_RESOURCE, new ScaleProvider());
  }

  @SubscribeEvent
  public static void onCommandsRegister(RegisterCommandsEvent event) {
    SetDistanceCommand.register(event.getDispatcher());
  }

  @SubscribeEvent
  public static void setPlayerHitbox(TickEvent.PlayerTickEvent event) {
    if (DietModConfig.change_hitbox && event.player.isAlive()) {
      float scale = getCap(event.player).getScale();
      float hitboxScale = MathHelper.clamp(0.6f * scale, 0.2f, 0.6f * maxScale);
      AxisAlignedBB playerBoundingBox = event.player.getBoundingBox();
      event.player.setBoundingBox(new AxisAlignedBB(playerBoundingBox.minX, playerBoundingBox.minY, playerBoundingBox.minZ, playerBoundingBox.minX + hitboxScale, playerBoundingBox.maxY, playerBoundingBox.minZ + hitboxScale));
    }
  }

  @SubscribeEvent
  public static void onJumpPlayer(LivingEvent.LivingJumpEvent event) {
    if (DietModConfig.change_jump_boost && event.getEntity() instanceof PlayerEntity) {
      PlayerEntity player = (PlayerEntity) event.getEntity();
      float scale = getCap(player).getScale();
      if (scale < 1.0f) {
        double motionY = MathHelper.clamp(player.getMotion().y / scale, player.getMotion().y, player.getMotion().y * 1.5f);
        player.setMotion(player.getMotion().x, motionY, player.getMotion().z);
      }
    }
  }

  @SubscribeEvent
  public static void onHarvest(PlayerEvent.BreakSpeed event) {
    IScale cap = getCap(event.getPlayer());
    float scale = cap.getScale();
    event.setNewSpeed(event.getOriginalSpeed() * scale);
  }

  @SubscribeEvent
  public static void onRespawnPlayer(PlayerEvent.PlayerRespawnEvent event) {
    if (!event.isEndConquered()) {
      PlayerEntity player = event.getPlayer();
      resetPlayer(player);
      player.setHealth(player.getMaxHealth());
    }
  }

  // Client Only

  @SubscribeEvent
  public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
    float scale = getCap(event.getPlayer()).getScale();
    event.getMatrixStack().push();
    event.getMatrixStack().scale(scale, 1.0f, scale);
  }

  @SubscribeEvent
  public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
    event.getMatrixStack().pop();
  }

  @SubscribeEvent
  public static void onFOVChange(FOVUpdateEvent event) {
    PlayerEntity player = event.getEntity();
    EffectInstance speed = player.getActivePotionEffect(Effects.SPEED);
    float fov = 0.9f, sprint_fov = 1.02f;
    if (player.isSprinting())
      event.setNewfov(speed != null ? sprint_fov + ((0.104f * (speed.getAmplifier() + 1))) : sprint_fov);
    else
      event.setNewfov(speed != null ? fov + (0.08f * (speed.getAmplifier() + 1)) : fov);
  }
}
