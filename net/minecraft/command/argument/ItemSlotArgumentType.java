package net.minecraft.command.argument;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class ItemSlotArgumentType implements ArgumentType<Integer> {
   private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "12", "weapon");
   private static final DynamicCommandExceptionType UNKNOWN_SLOT_EXCEPTION = new DynamicCommandExceptionType((name) -> {
      return new TranslatableText("slot.unknown", new Object[]{name});
   });
   private static final Map<String, Integer> slotNamesToSlotCommandId = (Map)Util.make(Maps.newHashMap(), (map) -> {
      int n;
      for(n = 0; n < 54; ++n) {
         map.put("container." + n, n);
      }

      for(n = 0; n < 9; ++n) {
         map.put("hotbar." + n, n);
      }

      for(n = 0; n < 27; ++n) {
         map.put("inventory." + n, 9 + n);
      }

      for(n = 0; n < 27; ++n) {
         map.put("enderchest." + n, 200 + n);
      }

      for(n = 0; n < 8; ++n) {
         map.put("villager." + n, 300 + n);
      }

      for(n = 0; n < 15; ++n) {
         map.put("horse." + n, 500 + n);
      }

      map.put("weapon", EquipmentSlot.MAINHAND.getOffsetEntitySlotId(98));
      map.put("weapon.mainhand", EquipmentSlot.MAINHAND.getOffsetEntitySlotId(98));
      map.put("weapon.offhand", EquipmentSlot.OFFHAND.getOffsetEntitySlotId(98));
      map.put("armor.head", EquipmentSlot.HEAD.getOffsetEntitySlotId(100));
      map.put("armor.chest", EquipmentSlot.CHEST.getOffsetEntitySlotId(100));
      map.put("armor.legs", EquipmentSlot.LEGS.getOffsetEntitySlotId(100));
      map.put("armor.feet", EquipmentSlot.FEET.getOffsetEntitySlotId(100));
      map.put("horse.saddle", 400);
      map.put("horse.armor", 401);
      map.put("horse.chest", 499);
   });

   public static ItemSlotArgumentType itemSlot() {
      return new ItemSlotArgumentType();
   }

   public static int getItemSlot(CommandContext<ServerCommandSource> context, String name) {
      return (Integer)context.getArgument(name, Integer.class);
   }

   public Integer parse(StringReader stringReader) throws CommandSyntaxException {
      String string = stringReader.readUnquotedString();
      if (!slotNamesToSlotCommandId.containsKey(string)) {
         throw UNKNOWN_SLOT_EXCEPTION.create(string);
      } else {
         return (Integer)slotNamesToSlotCommandId.get(string);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching((Iterable)slotNamesToSlotCommandId.keySet(), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
