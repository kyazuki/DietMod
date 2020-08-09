package com.github.kyazuki.dietmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class SetDistanceCommand {
  public static void register(CommandDispatcher<CommandSource> dispatcher) {
    dispatcher.register(Commands.literal("setdistance")
            .requires(source -> source.hasPermissionLevel(2))
            .then(Commands.argument("blocks", IntegerArgumentType.integer(10, 100000))
                    .executes(context -> {
                      int value = IntegerArgumentType.getInteger(context, "blocks");
                      DietModConfig.distanceToNormal = value;
                      context.getSource().sendFeedback(new TranslationTextComponent("commands.dietmod.setdistance", value), true);
                      return 0;
                    })));
  }
}