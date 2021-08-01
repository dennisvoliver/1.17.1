package net.minecraft.loot.provider.score;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

public class FixedLootScoreProvider implements LootScoreProvider {
   final String name;

   FixedLootScoreProvider(String string) {
      this.name = string;
   }

   public static LootScoreProvider create(String name) {
      return new FixedLootScoreProvider(name);
   }

   public LootScoreProviderType getType() {
      return LootScoreProviderTypes.FIXED;
   }

   public String getName() {
      return this.name;
   }

   @Nullable
   public String getName(LootContext context) {
      return this.name;
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return ImmutableSet.of();
   }

   public static class Serializer implements JsonSerializer<FixedLootScoreProvider> {
      public void toJson(JsonObject jsonObject, FixedLootScoreProvider fixedLootScoreProvider, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("name", fixedLootScoreProvider.name);
      }

      public FixedLootScoreProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         String string = JsonHelper.getString(jsonObject, "name");
         return new FixedLootScoreProvider(string);
      }
   }
}
