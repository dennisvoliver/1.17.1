package net.minecraft.server.dedicated.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;

public class PardonCommand {
   private static final SimpleCommandExceptionType ALREADY_UNBANNED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.pardon.failed"));

   public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("pardon").requires((source) -> {
         return source.hasPermissionLevel(3);
      })).then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
         return CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getUserBanList().getNames(), builder);
      }).executes((context) -> {
         return pardon((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"));
      })));
   }

   private static int pardon(ServerCommandSource source, Collection<GameProfile> targets) throws CommandSyntaxException {
      BannedPlayerList bannedPlayerList = source.getServer().getPlayerManager().getUserBanList();
      int i = 0;
      Iterator var4 = targets.iterator();

      while(var4.hasNext()) {
         GameProfile gameProfile = (GameProfile)var4.next();
         if (bannedPlayerList.contains(gameProfile)) {
            bannedPlayerList.remove(gameProfile);
            ++i;
            source.sendFeedback(new TranslatableText("commands.pardon.success", new Object[]{Texts.toText(gameProfile)}), true);
         }
      }

      if (i == 0) {
         throw ALREADY_UNBANNED_EXCEPTION.create();
      } else {
         return i;
      }
   }
}
