package com.github.kyazuki.dietmod;

import com.github.kyazuki.dietmod.capabilities.IScale;
import com.github.kyazuki.dietmod.capabilities.Scale;
import com.github.kyazuki.dietmod.capabilities.ScaleStorage;
import com.github.kyazuki.dietmod.network.PacketHandler;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DietMod.MODID)
public class DietMod {
  public static final String MODID = "dietmod";
  public static final Logger LOGGER = LogManager.getLogger(MODID);

  public DietMod() {
    LOGGER.debug("DietMod Loaded.");
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, DietModConfig.COMMON_SPEC);
    FMLJavaModLoadingContext.get().getModEventBus().addListener(DietMod::onFMLCommonSetup);
    PacketHandler.register();
  }

  public static void onFMLCommonSetup(FMLCommonSetupEvent event) {
    CapabilityManager.INSTANCE.register(IScale.class, new ScaleStorage(), () -> new Scale(2.0f));
  }
}
