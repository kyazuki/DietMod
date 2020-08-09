package com.github.kyazuki.dietmod.events;

import com.github.kyazuki.dietmod.DietMod;
import com.github.kyazuki.dietmod.capabilities.ScaleProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DietMod.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
  @SubscribeEvent
  public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
    float scale = event.getPlayer().getCapability(ScaleProvider.SCALE_CAP).orElseThrow(IllegalArgumentException::new).getScale();
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
