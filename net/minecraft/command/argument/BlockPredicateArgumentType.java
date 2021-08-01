package net.minecraft.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagManager;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class BlockPredicateArgumentType implements ArgumentType<BlockPredicateArgumentType.BlockPredicate> {
   private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
   private static final DynamicCommandExceptionType UNKNOWN_TAG_EXCEPTION = new DynamicCommandExceptionType((id) -> {
      return new TranslatableText("arguments.block.tag.unknown", new Object[]{id});
   });

   public static BlockPredicateArgumentType blockPredicate() {
      return new BlockPredicateArgumentType();
   }

   public BlockPredicateArgumentType.BlockPredicate parse(StringReader stringReader) throws CommandSyntaxException {
      BlockArgumentParser blockArgumentParser = (new BlockArgumentParser(stringReader, true)).parse(true);
      if (blockArgumentParser.getBlockState() != null) {
         BlockPredicateArgumentType.StatePredicate statePredicate = new BlockPredicateArgumentType.StatePredicate(blockArgumentParser.getBlockState(), blockArgumentParser.getBlockProperties().keySet(), blockArgumentParser.getNbtData());
         return (manager) -> {
            return statePredicate;
         };
      } else {
         Identifier identifier = blockArgumentParser.getTagId();
         return (manager) -> {
            Tag<Block> tag = manager.getTag(Registry.BLOCK_KEY, identifier, (id) -> {
               return UNKNOWN_TAG_EXCEPTION.create(id.toString());
            });
            return new BlockPredicateArgumentType.TagPredicate(tag, blockArgumentParser.getProperties(), blockArgumentParser.getNbtData());
         };
      }
   }

   public static Predicate<CachedBlockPosition> getBlockPredicate(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
      return ((BlockPredicateArgumentType.BlockPredicate)context.getArgument(name, BlockPredicateArgumentType.BlockPredicate.class)).create(((ServerCommandSource)context.getSource()).getServer().getTagManager());
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      StringReader stringReader = new StringReader(builder.getInput());
      stringReader.setCursor(builder.getStart());
      BlockArgumentParser blockArgumentParser = new BlockArgumentParser(stringReader, true);

      try {
         blockArgumentParser.parse(true);
      } catch (CommandSyntaxException var6) {
      }

      return blockArgumentParser.getSuggestions(builder, BlockTags.getTagGroup());
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   private static class StatePredicate implements Predicate<CachedBlockPosition> {
      private final BlockState state;
      private final Set<Property<?>> properties;
      @Nullable
      private final NbtCompound nbt;

      public StatePredicate(BlockState state, Set<Property<?>> properties, @Nullable NbtCompound nbt) {
         this.state = state;
         this.properties = properties;
         this.nbt = nbt;
      }

      public boolean test(CachedBlockPosition cachedBlockPosition) {
         BlockState blockState = cachedBlockPosition.getBlockState();
         if (!blockState.isOf(this.state.getBlock())) {
            return false;
         } else {
            Iterator var3 = this.properties.iterator();

            while(var3.hasNext()) {
               Property<?> property = (Property)var3.next();
               if (blockState.get(property) != this.state.get(property)) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity blockEntity = cachedBlockPosition.getBlockEntity();
               return blockEntity != null && NbtHelper.matches(this.nbt, blockEntity.writeNbt(new NbtCompound()), true);
            }
         }
      }
   }

   public interface BlockPredicate {
      Predicate<CachedBlockPosition> create(TagManager manager) throws CommandSyntaxException;
   }

   private static class TagPredicate implements Predicate<CachedBlockPosition> {
      private final Tag<Block> tag;
      @Nullable
      private final NbtCompound nbt;
      private final Map<String, String> properties;

      TagPredicate(Tag<Block> tag, Map<String, String> properties, @Nullable NbtCompound nbt) {
         this.tag = tag;
         this.properties = properties;
         this.nbt = nbt;
      }

      public boolean test(CachedBlockPosition cachedBlockPosition) {
         BlockState blockState = cachedBlockPosition.getBlockState();
         if (!blockState.isIn(this.tag)) {
            return false;
         } else {
            Iterator var3 = this.properties.entrySet().iterator();

            while(var3.hasNext()) {
               Entry<String, String> entry = (Entry)var3.next();
               Property<?> property = blockState.getBlock().getStateManager().getProperty((String)entry.getKey());
               if (property == null) {
                  return false;
               }

               Comparable<?> comparable = (Comparable)property.parse((String)entry.getValue()).orElse((Object)null);
               if (comparable == null) {
                  return false;
               }

               if (blockState.get(property) != comparable) {
                  return false;
               }
            }

            if (this.nbt == null) {
               return true;
            } else {
               BlockEntity blockEntity = cachedBlockPosition.getBlockEntity();
               return blockEntity != null && NbtHelper.matches(this.nbt, blockEntity.writeNbt(new NbtCompound()), true);
            }
         }
      }
   }
}
