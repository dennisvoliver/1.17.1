package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {
   private static final DynamicCommandExceptionType FAILURE_EXCEPTION = new DynamicCommandExceptionType((difficulty) -> {
      return new TranslatableText("commands.difficulty.failure", new Object[]{difficulty});
   });

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("difficulty");
      Difficulty[] var2 = Difficulty.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Difficulty difficulty = var2[var4];
         literalArgumentBuilder.then(CommandManager.literal(difficulty.getName()).executes((context) -> {
            return execute((ServerCommandSource)context.getSource(), difficulty);
         }));
      }

      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.requires((source) -> {
         return source.hasPermissionLevel(2);
      })).executes((context) -> {
         Difficulty difficulty = ((ServerCommandSource)context.getSource()).getWorld().getDifficulty();
         ((ServerCommandSource)context.getSource()).sendFeedback(new TranslatableText("commands.difficulty.query", new Object[]{difficulty.getTranslatableName()}), false);
         return difficulty.getId();
      }));
   }

   public static int execute(ServerCommandSource source, Difficulty difficulty) throws CommandSyntaxException {
      MinecraftServer minecraftServer = source.getServer();
      if (minecraftServer.getSaveProperties().getDifficulty() == difficulty) {
         throw FAILURE_EXCEPTION.create(difficulty.getName());
      } else {
         minecraftServer.setDifficulty(difficulty, true);
         source.sendFeedback(new TranslatableText("commands.difficulty.success", new Object[]{difficulty.getTranslatableName()}), true);
         return 0;
      }
   }
}
