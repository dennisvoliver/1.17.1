package net.minecraft.loot.provider.nbt;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;
import org.jetbrains.annotations.Nullable;

public class ContextLootNbtProvider implements LootNbtProvider {
   private static final String BLOCK_ENTITY_TARGET_NAME = "block_entity";
   private static final ContextLootNbtProvider.Target BLOCK_ENTITY_TARGET = new ContextLootNbtProvider.Target() {
      public NbtElement getNbt(LootContext context) {
         BlockEntity blockEntity = (BlockEntity)context.get(LootContextParameters.BLOCK_ENTITY);
         return blockEntity != null ? blockEntity.writeNbt(new NbtCompound()) : null;
      }

      public String getName() {
         return "block_entity";
      }

      public Set<LootContextParameter<?>> getRequiredParameters() {
         return ImmutableSet.of(LootContextParameters.BLOCK_ENTITY);
      }
   };
   public static final ContextLootNbtProvider BLOCK_ENTITY;
   final ContextLootNbtProvider.Target target;

   private static ContextLootNbtProvider.Target getTarget(final LootContext.EntityTarget entityTarget) {
      return new ContextLootNbtProvider.Target() {
         @Nullable
         public NbtElement getNbt(LootContext context) {
            Entity entity = (Entity)context.get(entityTarget.getParameter());
            return entity != null ? NbtPredicate.entityToNbt(entity) : null;
         }

         public String getName() {
            return entityTarget.name();
         }

         public Set<LootContextParameter<?>> getRequiredParameters() {
            return ImmutableSet.of(entityTarget.getParameter());
         }
      };
   }

   private ContextLootNbtProvider(ContextLootNbtProvider.Target target) {
      this.target = target;
   }

   public LootNbtProviderType getType() {
      return LootNbtProviderTypes.CONTEXT;
   }

   @Nullable
   public NbtElement getNbt(LootContext context) {
      return this.target.getNbt(context);
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return this.target.getRequiredParameters();
   }

   public static LootNbtProvider fromTarget(LootContext.EntityTarget target) {
      return new ContextLootNbtProvider(getTarget(target));
   }

   static ContextLootNbtProvider fromTarget(String target) {
      if (target.equals("block_entity")) {
         return new ContextLootNbtProvider(BLOCK_ENTITY_TARGET);
      } else {
         LootContext.EntityTarget entityTarget = LootContext.EntityTarget.fromString(target);
         return new ContextLootNbtProvider(getTarget(entityTarget));
      }
   }

   static {
      BLOCK_ENTITY = new ContextLootNbtProvider(BLOCK_ENTITY_TARGET);
   }

   private interface Target {
      @Nullable
      NbtElement getNbt(LootContext context);

      String getName();

      Set<LootContextParameter<?>> getRequiredParameters();
   }

   public static class CustomSerializer implements JsonSerializing.ElementSerializer<ContextLootNbtProvider> {
      public JsonElement toJson(ContextLootNbtProvider contextLootNbtProvider, JsonSerializationContext jsonSerializationContext) {
         return new JsonPrimitive(contextLootNbtProvider.target.getName());
      }

      public ContextLootNbtProvider fromJson(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
         String string = jsonElement.getAsString();
         return ContextLootNbtProvider.fromTarget(string);
      }
   }

   public static class Serializer implements JsonSerializer<ContextLootNbtProvider> {
      public void toJson(JsonObject jsonObject, ContextLootNbtProvider contextLootNbtProvider, JsonSerializationContext jsonSerializationContext) {
         jsonObject.addProperty("target", contextLootNbtProvider.target.getName());
      }

      public ContextLootNbtProvider fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
         String string = JsonHelper.getString(jsonObject, "target");
         return ContextLootNbtProvider.fromTarget(string);
      }
   }
}
