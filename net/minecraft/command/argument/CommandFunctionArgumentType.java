package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class CommandFunctionArgumentType implements ArgumentType<CommandFunctionArgumentType.FunctionArgument> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
   private static final DynamicCommandExceptionType UNKNOWN_FUNCTION_TAG_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return new TranslatableText("arguments.function.tag.unknown", new Object[]{id});
   });
   private static final DynamicCommandExceptionType UNKNOWN_FUNCTION_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return new TranslatableText("arguments.function.unknown", new Object[]{id});
   });

   public static CommandFunctionArgumentType commandFunction() {
      return new CommandFunctionArgumentType();
   }

   public CommandFunctionArgumentType.FunctionArgument parse(StringReader stringReader) throws CommandSyntaxException {
      final Identifier identifier2;
      if (stringReader.canRead() && stringReader.peek() == '#') {
         stringReader.skip();
         identifier2 = Identifier.fromCommandInput(stringReader);
         return new CommandFunctionArgumentType.FunctionArgument() {
            public Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
               Tag<CommandFunction> tag = CommandFunctionArgumentType.getFunctionTag(context, identifier2);
               return tag.values();
            }

            public Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
               return Pair.of(identifier2, Either.right(CommandFunctionArgumentType.getFunctionTag(context, identifier2)));
            }
         };
      } else {
         identifier2 = Identifier.fromCommandInput(stringReader);
         return new CommandFunctionArgumentType.FunctionArgument() {
            public Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
               return Collections.singleton(CommandFunctionArgumentType.getFunction(context, identifier2));
            }

            public Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
               return Pair.of(identifier2, Either.left(CommandFunctionArgumentType.getFunction(context, identifier2)));
            }
         };
      }
   }

   static CommandFunction getFunction(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
      return (CommandFunction)((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager().getFunction(id).orElseThrow(() -> {
         return UNKNOWN_FUNCTION_EXCEPTION.create(id.toString());
      });
   }

   static Tag<CommandFunction> getFunctionTag(CommandContext<ServerCommandSource> context, Identifier id) throws CommandSyntaxException {
      Tag<CommandFunction> tag = ((ServerCommandSource)context.getSource()).getServer().getCommandFunctionManager().getTag(id);
      if (tag == null) {
         throw UNKNOWN_FUNCTION_TAG_EXCEPTION.create(id.toString());
      } else {
         return tag;
      }
   }

   public static Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
      return ((CommandFunctionArgumentType.FunctionArgument)context.getArgument(name, CommandFunctionArgumentType.FunctionArgument.class)).getFunctions(context);
   }

   public static Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
      return ((CommandFunctionArgumentType.FunctionArgument)context.getArgument(name, CommandFunctionArgumentType.FunctionArgument.class)).getFunctionOrTag(context);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public interface FunctionArgument {
      Collection<CommandFunction> getFunctions(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;

      Pair<Identifier, Either<CommandFunction, Tag<CommandFunction>>> getFunctionOrTag(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
   }
}
