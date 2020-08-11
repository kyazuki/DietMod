package com.github.kyazuki.dietmod.events;

import com.github.kyazuki.dietmod.DietMod;
import com.github.kyazuki.dietmod.DietModConfig;
import com.github.kyazuki.dietmod.SetDistanceCommand;
import com.github.kyazuki.dietmod.capabilities.IScale;
import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import com.github.kyazuki.dietmod.network.CapabilityPacket;
import com.github.kyazuki.dietmod.network.PacketHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = DietMod.MODID)
public class CommonEventHandler {
  public static final ResourceLocation SCALE_CAP_RESOURCE = new ResourceLocation(DietMod.MODID, "capabilities");
  public static final UUID FatHealth = UUID.fromString("7f73c613-1c4f-45df-8176-6e3f9590347b");
  public static final UUID FatMovenentSpeed = UUID.fromString("bba5aa63-ec4b-481f-9e2f-18d0c0a8e615");
  public static DamageSource Malnutrition = (new DamageSource("dietmod:malnutrition")).setDamageBypassesArmor();
  private static final EntitySize DEFAULT_STANDING_SIZE = EntitySize.flexible(0.6F, 1.8F);
  private static final Map<Pose, EntitySize> DEFAULT_SIZE_BY_POSE = ImmutableMap.<Pose, EntitySize>builder().put(Pose.STANDING, PlayerEntity.STANDING_SIZE).put(Pose.SLEEPING, EntitySize.fixed(0.2F, 0.2F)).put(Pose.FALL_FLYING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SWIMMING, EntitySize.flexible(0.6F, 0.6F)).put(Pose.SPIN_ATTACK, EntitySize.flexible(0.6F, 0.6F)).put(Pose.CROUCHING, EntitySize.flexible(0.6F, 1.5F)).put(Pose.DYING, EntitySize.fixed(0.2F, 0.2F)).build();

  // Utils

  public static IScale getCap(PlayerEntity player) {
    return player.getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new);
  }

  public static void resetAttributePlayer(PlayerEntity player) {
    player.getAttribute(Attributes.MAX_HEALTH).removeModifier(FatHealth);
    if (DietModConfig.change_max_health) {
      player.getAttribute(Attributes.MAX_HEALTH).applyNonPersistentModifier(new AttributeModifier(FatHealth, "FatMaxHealth", DietModConfig.maxScale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
    player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(FatMovenentSpeed);
    if (DietModConfig.change_speed)
      player.getAttribute(Attributes.MOVEMENT_SPEED).applyNonPersistentModifier(new AttributeModifier(FatMovenentSpeed, "FatMovementSpeed", 1.0f / DietModConfig.maxScale - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
  }

  public static EntitySize getScaledPlayerSize(PlayerEntity player, Pose poseIn) {
    if (DietModConfig.change_hitbox) {
      float scale;
      try {
        scale = getCap(player).getScale();
      } catch (IllegalArgumentException e) {
        scale = 1.0f;
      } catch (Exception e) {
        throw e;
      }

      switch (poseIn) {
        case STANDING:
          return EntitySize.flexible(0.6F * scale, 1.8F);
        case SLEEPING:
          return EntitySize.fixed(0.2F, 0.2F);
        case CROUCHING:
          return EntitySize.flexible(0.6F * scale, 1.5F);
        case DYING:
          return EntitySize.flexible(0.2F, 0.2F);
        default:
          return EntitySize.flexible(0.6F, 0.6F * scale);
      }
    }
    return DEFAULT_SIZE_BY_POSE.getOrDefault(poseIn, DEFAULT_STANDING_SIZE);
  }

  // Server Only

  @SubscribeEvent
  public static void onCommandsRegister(RegisterCommandsEvent event) {
    SetDistanceCommand.register(event.getDispatcher());
  }

  @SubscribeEvent
  public static void calc(TickEvent.PlayerTickEvent event) {
    if (!event.player.getEntityWorld().isRemote() && event.player.isAlive()) {
      IScale cap = getCap(event.player);
      float distanceWalked = event.player.distanceWalkedModified / 0.6f;
      float prevWalkDistance = cap.getPrevWalkDistance();
      if (distanceWalked > prevWalkDistance) {
        float distance = distanceWalked - prevWalkDistance;
        float prevScale = cap.getScale();
        float scale = MathHelper.clamp(prevScale - distance / DietModConfig.distanceToNormal, 0.2f, (float) DietModConfig.maxScale);
        cap.setScale(scale);
        MinecraftForge.EVENT_BUS.post(new DietModEvents.ChangedScaleEvent(event.player, scale));
        MinecraftForge.EVENT_BUS.post(new DietModEvents.UpdatePlayerSizeEvent(event.player));
        PacketHandler.sendToTrackersAndSelf(new CapabilityPacket(event.player.getEntityId(), scale), event.player);
        cap.setPrevWalkDistance(distanceWalked);
      }
    }
  }

  @SubscribeEvent
  public static void setHealthPlayer(DietModEvents.ChangedScaleEvent event) {
    if (DietModConfig.change_max_health && !event.getPlayer().getEntityWorld().isRemote()) {
      PlayerEntity player = event.getPlayer();
      float scale = event.getScale();
      float scaleHealth = Math.round(scale * 10) / 10.0f;
      if (player.getMaxHealth() != scaleHealth * 20.0f) {
        ModifiableAttributeInstance player_max_health = player.getAttribute(Attributes.MAX_HEALTH);
        player_max_health.removeModifier(FatHealth);
        player_max_health.applyNonPersistentModifier(new AttributeModifier(FatHealth, "FatMaxHealth", scaleHealth - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (player.getHealth() > player.getMaxHealth())
          player.setHealth(player.getMaxHealth());
      }
    }
  }

  @SubscribeEvent
  public static void setSpeedPlayer(DietModEvents.ChangedScaleEvent event) {
    if (DietModConfig.change_speed && !event.getPlayer().getEntityWorld().isRemote()) {
      PlayerEntity player = event.getPlayer();
      float scale = event.getScale();
      double speed = MathHelper.clamp(1.0f / scale, 0.8f, 1.5f);
      ModifiableAttributeInstance player_movement_speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
      player_movement_speed.removeModifier(FatMovenentSpeed);
      player_movement_speed.applyNonPersistentModifier(new AttributeModifier(FatMovenentSpeed, "FatMovementSpeed", speed - 1.0f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }
  }

  @SubscribeEvent
  public static void killPlayer(DietModEvents.ChangedScaleEvent event) {
    if (!event.getPlayer().getEntityWorld().isRemote()) {
      PlayerEntity player = event.getPlayer();
      if (event.getScale() < DietModConfig.killHealth) {
        if (player.isAlive()) {
          player.attackEntityFrom(Malnutrition, 20.0f);
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
          float food_heal = event.getItem().getItem().getFood().getHealing() * (float) DietModConfig.food_modifier;
          IScale cap = getCap(player);
          float scale = MathHelper.clamp(cap.getScale() + food_heal, 0.2f, (float) DietModConfig.maxScale);
          cap.setScale(scale);
          MinecraftForge.EVENT_BUS.post(new DietModEvents.ChangedScaleEvent(player, scale));
          MinecraftForge.EVENT_BUS.post(new DietModEvents.UpdatePlayerSizeEvent(player));
          PacketHandler.sendToTrackersAndSelf(new CapabilityPacket(player.getEntityId(), scale), player);
        }
      }
    }
  }

  @SubscribeEvent
  public static void onLoggedInPlayer(PlayerEvent.PlayerLoggedInEvent event) {
    PlayerEntity player = event.getPlayer();
    IScale cap = getCap(player);
    PacketHandler.sendTo(new CapabilityPacket(player.getEntityId(), cap.getScale()), player);
    cap.setPrevWalkDistance(0.0f);
  }

  @SubscribeEvent
  public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    PlayerEntity player = event.getPlayer();
    PacketHandler.sendTo(new CapabilityPacket(player.getEntityId(), getCap(player).getScale()), player);
  }

  @SubscribeEvent
  public static void onRespawnPlayer(PlayerEvent.PlayerRespawnEvent event) {
    PlayerEntity player = event.getPlayer();

    if (!event.isEndConquered()) {
      resetAttributePlayer(player);
      player.setHealth(player.getMaxHealth());
    } else {
      PacketHandler.sendTo(new CapabilityPacket(player.getEntityId(), getCap(player).getScale()), player);
    }
  }

  @SubscribeEvent
  public static void onStartTracking(PlayerEvent.StartTracking event) {
    if (!(event.getTarget() instanceof PlayerEntity)) return;

    PlayerEntity trackedPlayer = (PlayerEntity) event.getTarget();
    PacketHandler.sendTo(new CapabilityPacket(trackedPlayer.getEntityId(), getCap(trackedPlayer).getScale()), event.getPlayer());
  }

  @SubscribeEvent
  public static void onClonePlayer(PlayerEvent.Clone event) {
    if (event.isWasDeath()) return;

    PlayerEntity newPlayer = event.getPlayer();
    PlayerEntity oldPlayer = event.getOriginal();
    IScale newCap = getCap(newPlayer);
    IScale oldCap = getCap(oldPlayer);
    newCap.copy(oldCap);
    newCap.setPrevWalkDistance(0.0f);
    newPlayer.setHealth(oldPlayer.getHealth());
  }

  // Server & Client

  @SubscribeEvent
  public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
    if (!(event.getObject() instanceof PlayerEntity)) return;

    event.addCapability(SCALE_CAP_RESOURCE, new ScaleProvider());
  }

  @SubscribeEvent
  public static void setPlayerHitbox(DietModEvents.UpdatePlayerSizeEvent event) {
    if (DietModConfig.change_hitbox)
      event.getPlayer().recalculateSize();
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
}
