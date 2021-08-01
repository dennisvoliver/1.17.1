package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

public class OperationArgumentType implements ArgumentType<OperationArgumentType.Operation> {
   private static final Collection<String> EXAMPLES = Arrays.asList("=", ">", "<");
   private static final SimpleCommandExceptionType INVALID_OPERATION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.invalid"));
   private static final SimpleCommandExceptionType DIVISION_ZERO_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("arguments.operation.div0"));

   public static OperationArgumentType operation() {
      return new OperationArgumentType();
   }

   public static OperationArgumentType.Operation getOperation(CommandContext<ServerCommandSource> context, String name) {
      return (OperationArgumentType.Operation)context.getArgument(name, OperationArgumentType.Operation.class);
   }

   public OperationArgumentType.Operation parse(StringReader stringReader) throws CommandSyntaxException {
      if (!stringReader.canRead()) {
         throw INVALID_OPERATION.create();
      } else {
         int i = stringReader.getCursor();

         while(stringReader.canRead() && stringReader.peek() != ' ') {
            stringReader.skip();
         }

         return getOperator(stringReader.getString().substring(i, stringReader.getCursor()));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(new String[]{"=", "+=", "-=", "*=", "/=", "%=", "<", ">", "><"}, builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static OperationArgumentType.Operation getOperator(String operator) throws CommandSyntaxException {
      return (OperationArgumentType.Operation)(operator.equals("><") ? (a, b) -> {
         int i = a.getScore();
         a.setScore(b.getScore());
         b.setScore(i);
      } : getIntOperator(operator));
   }

   private static OperationArgumentType.IntOperator getIntOperator(String operator) throws CommandSyntaxException {
      byte var2 = -1;
      switch(operator.hashCode()) {
      case 60:
         if (operator.equals("<")) {
            var2 = 6;
         }
         break;
      case 61:
         if (operator.equals("=")) {
            var2 = 0;
         }
         break;
      case 62:
         if (operator.equals(">")) {
            var2 = 7;
         }
         break;
      case 1208:
         if (operator.equals("%=")) {
            var2 = 5;
         }
         break;
      case 1363:
         if (operator.equals("*=")) {
            var2 = 3;
         }
         break;
      case 1394:
         if (operator.equals("+=")) {
            var2 = 1;
         }
         break;
      case 1456:
         if (operator.equals("-=")) {
            var2 = 2;
         }
         break;
      case 1518:
         if (operator.equals("/=")) {
            var2 = 4;
         }
      }

      switch(var2) {
      case 0:
         return (a, b) -> {
            return b;
         };
      case 1:
         return (a, b) -> {
            return a + b;
         };
      case 2:
         return (a, b) -> {
            return a - b;
         };
      case 3:
         return (a, b) -> {
            return a * b;
         };
      case 4:
         return (a, b) -> {
            if (b == 0) {
               throw DIVISION_ZERO_EXCEPTION.create();
            } else {
               return MathHelper.floorDiv(a, b);
            }
         };
      case 5:
         return (a, b) -> {
            if (b == 0) {
               throw DIVISION_ZERO_EXCEPTION.create();
            } else {
               return MathHelper.floorMod(a, b);
            }
         };
      case 6:
         return Math::min;
      case 7:
         return Math::max;
      default:
         throw INVALID_OPERATION.create();
      }
   }

   @FunctionalInterface
   public interface Operation {
      void apply(ScoreboardPlayerScore a, ScoreboardPlayerScore b) throws CommandSyntaxException;
   }

   @FunctionalInterface
   private interface IntOperator extends OperationArgumentType.Operation {
      int apply(int a, int b) throws CommandSyntaxException;

      default void apply(ScoreboardPlayerScore scoreboardPlayerScore, ScoreboardPlayerScore scoreboardPlayerScore2) throws CommandSyntaxException {
         scoreboardPlayerScore.setScore(this.apply(scoreboardPlayerScore.getScore(), scoreboardPlayerScore2.getScore()));
      }
   }
}
