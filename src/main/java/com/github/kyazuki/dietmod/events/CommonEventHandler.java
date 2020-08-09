package com.github.kyazuki.dietmod.events;

import com.github.kyazuki.dietmod.DietMod;
import com.github.kyazuki.dietmod.DietModConfig;
import com.github.kyazuki.dietmod.SetDistanceCommand;
import com.github.kyazuki.dietmod.capabilities.IScale;
import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import com.github.kyazuki.dietmod.network.CapabilityPacket;
import com.github.kyazuki.dietmod.network.PacketHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
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

import java.util.UUID;

@Mod.EventBusSubscriber(modid = DietMod.MODID)
public class CommonEventHandler {
  public static final ResourceLocation SCALE_CAP_RESOURCE = new ResourceLocation(DietMod.MODID, "capabilities");
  public static final UUID FatHealth = UUID.fromString("7f73c613-1c4f-45df-8176-6e3f9590347b");
  public static final UUID FatMovenentSpeed = UUID.fromString("bba5aa63-ec4b-481f-9e2f-18d0c0a8e615");
  public static DamageSource Malnutrition = (new DamageSource("dietmod:malnutrition")).setDamageBypassesArmor();

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

  // Server Only

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
        MinecraftForge.EVENT_BUS.post(new ChangeScaleEvent(event.player, scale));
        PacketHandler.sendToAll(new CapabilityPacket(event.player.getEntityId(), scale));
        cap.setPrevWalkDistance(distanceWalked);
      }
    }
  }

  @SubscribeEvent
  public static void setHealthPlayer(ChangeScaleEvent event) {
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
  public static void setSpeedPlayer(ChangeScaleEvent event) {
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
  public static void killPlayer(ChangeScaleEvent event) {
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
          MinecraftForge.EVENT_BUS.post(new ChangeScaleEvent(player, scale));
          PacketHandler.sendToAll(new CapabilityPacket(player.getEntityId(), scale));
        }
      }
    }
  }

  @SubscribeEvent
  public static void onLoggedInPlayer(PlayerEvent.PlayerLoggedInEvent event) {
    if (!event.getPlayer().getEntityWorld().isRemote()) {
      PlayerEntity player = event.getPlayer();
      IScale cap = getCap(player);
      MinecraftForge.EVENT_BUS.post(new ChangeScaleEvent(player, cap.getScale()));
      PacketHandler.sendToAll(new CapabilityPacket(player.getEntityId(), cap.getScale()));
      cap.setPrevWalkDistance(0.0f);

      for (PlayerEntity otherPlayer : event.getPlayer().getServer().getPlayerList().getPlayers()) {
        if (otherPlayer == player) continue;
        PacketHandler.sendTo(new CapabilityPacket(otherPlayer.getEntityId(), getCap(otherPlayer).getScale()), player);
      }
    }
  }

  @SubscribeEvent
  public static void onRespawnPlayer(PlayerEvent.PlayerRespawnEvent event) {
    if (!event.getPlayer().getEntityWorld().isRemote()) {
      PlayerEntity player = event.getPlayer();

      if (!event.isEndConquered()) {
        resetAttributePlayer(player);
        player.setHealth(player.getMaxHealth());
      } else {
        IScale cap = getCap(player);
        MinecraftForge.EVENT_BUS.post(new ChangeScaleEvent(player, cap.getScale()));
        PacketHandler.sendToAll(new CapabilityPacket(player.getEntityId(), cap.getScale()));
      }

      for (PlayerEntity otherPlayer : event.getPlayer().getServer().getPlayerList().getPlayers()) {
        if (otherPlayer == player) continue;
        PacketHandler.sendTo(new CapabilityPacket(otherPlayer.getEntityId(), getCap(otherPlayer).getScale()), player);
      }
    }
  }

  @SubscribeEvent
  public static void onClonePlayer(PlayerEvent.Clone event) {
    if (!event.getPlayer().getEntityWorld().isRemote() && !event.isWasDeath()) {
      PlayerEntity newPlayer = event.getPlayer();
      PlayerEntity oldPlayer = event.getOriginal();
      IScale newCap = getCap(newPlayer);
      IScale oldCap = getCap(oldPlayer);
      newCap.copy(oldCap);
      newCap.setPrevWalkDistance(0.0f);
      newPlayer.setHealth(oldPlayer.getHealth());
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
      PlayerEntity player = event.player;
      float scale = getCap(player).getScale();
      float hitboxScale = MathHelper.clamp(0.6f * scale, 0.2f, 0.6f * (float) DietModConfig.maxScale);
      AxisAlignedBB playerBoundingBox = player.getBoundingBox();
      player.setBoundingBox(new AxisAlignedBB(playerBoundingBox.minX, playerBoundingBox.minY, playerBoundingBox.minZ, playerBoundingBox.minX + hitboxScale, playerBoundingBox.maxY, playerBoundingBox.minZ + hitboxScale));
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
}
