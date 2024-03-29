package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Nameable;

public class CopyNameLootFunction extends ConditionalLootFunction {
   final CopyNameLootFunction.Source source;

   CopyNameLootFunction(LootCondition[] lootConditions, CopyNameLootFunction.Source source) {
      super(lootConditions);
      this.source = source;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.COPY_NAME;
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return ImmutableSet.of(this.source.parameter);
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Object object = context.get(this.source.parameter);
      if (object instanceof Nameable) {
         Nameable nameable = (Nameable)object;
         if (nameable.hasCustomName()) {
            stack.setCustomName(nameable.getDisplayName());
         }
      }

      return stack;
   }

   public static ConditionalLootFunction.Builder<?> builder(CopyNameLootFunction.Source source) {
      return builder((conditions) -> {
         return new CopyNameLootFunction(conditions, source);
      });
   }

   public static enum Source {
      THIS("this", LootContextParameters.THIS_ENTITY),
      KILLER("killer", LootContextParameters.KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootContextParameters.LAST_DAMAGE_PLAYER),
      BLOCK_ENTITY("block_entity", LootContextParameters.BLOCK_ENTITY);

      public final String name;
      public final LootContextParameter<?> parameter;

      private Source(String name, LootContextParameter<?> parameter) {
         this.name = name;
         this.parameter = parameter;
      }

      public static CopyNameLootFunction.Source get(String name) {
         CopyNameLootFunction.Source[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            CopyNameLootFunction.Source source = var1[var3];
            if (source.name.equals(name)) {
               return source;
            }
         }

         throw new IllegalArgumentException("Invalid name source " + name);
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer<CopyNameLootFunction> {
      public void toJson(JsonObject jsonObject, CopyNameLootFunction copyNameLootFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)copyNameLootFunction, jsonSerializationContext);
         jsonObject.addProperty("source", copyNameLootFunction.source.name);
      }

      public CopyNameLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         CopyNameLootFunction.Source source = CopyNameLootFunction.Source.get(JsonHelper.getString(jsonObject, "source"));
         return new CopyNameLootFunction(lootConditions, source);
      }
   }
}
