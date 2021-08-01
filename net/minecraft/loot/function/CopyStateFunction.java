package net.minecraft.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class CopyStateFunction extends ConditionalLootFunction {
   final Block block;
   final Set<Property<?>> properties;

   CopyStateFunction(LootCondition[] lootConditions, Block block, Set<Property<?>> set) {
      super(lootConditions);
      this.block = block;
      this.properties = set;
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.COPY_STATE;
   }

   public Set<LootContextParameter<?>> getRequiredParameters() {
      return ImmutableSet.of(LootContextParameters.BLOCK_STATE);
   }

   protected ItemStack process(ItemStack stack, LootContext context) {
      BlockState blockState = (BlockState)context.get(LootContextParameters.BLOCK_STATE);
      if (blockState != null) {
         NbtCompound nbtCompound = stack.getOrCreateNbt();
         NbtCompound nbtCompound3;
         if (nbtCompound.contains("BlockStateTag", 10)) {
            nbtCompound3 = nbtCompound.getCompound("BlockStateTag");
         } else {
            nbtCompound3 = new NbtCompound();
            nbtCompound.put("BlockStateTag", nbtCompound3);
         }

         Stream var10000 = this.properties.stream();
         Objects.requireNonNull(blockState);
         var10000.filter(blockState::contains).forEach((property) -> {
            nbtCompound3.putString(property.getName(), getPropertyName(blockState, property));
         });
      }

      return stack;
   }

   public static CopyStateFunction.Builder getBuilder(Block block) {
      return new CopyStateFunction.Builder(block);
   }

   private static <T extends Comparable<T>> String getPropertyName(BlockState state, Property<T> property) {
      T comparable = state.get(property);
      return property.name(comparable);
   }

   public static class Builder extends ConditionalLootFunction.Builder<CopyStateFunction.Builder> {
      private final Block block;
      private final Set<Property<?>> properties = Sets.newHashSet();

      Builder(Block block) {
         this.block = block;
      }

      public CopyStateFunction.Builder addProperty(Property<?> property) {
         if (!this.block.getStateManager().getProperties().contains(property)) {
            throw new IllegalStateException("Property " + property + " is not present on block " + this.block);
         } else {
            this.properties.add(property);
            return this;
         }
      }

      protected CopyStateFunction.Builder getThisBuilder() {
         return this;
      }

      public LootFunction build() {
         return new CopyStateFunction(this.getConditions(), this.block, this.properties);
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer<CopyStateFunction> {
      public void toJson(JsonObject jsonObject, CopyStateFunction copyStateFunction, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)copyStateFunction, jsonSerializationContext);
         jsonObject.addProperty("block", Registry.BLOCK.getId(copyStateFunction.block).toString());
         JsonArray jsonArray = new JsonArray();
         copyStateFunction.properties.forEach((property) -> {
            jsonArray.add(property.getName());
         });
         jsonObject.add("properties", jsonArray);
      }

      public CopyStateFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] lootConditions) {
         Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "block"));
         Block block = (Block)Registry.BLOCK.getOrEmpty(identifier).orElseThrow(() -> {
            return new IllegalArgumentException("Can't find block " + identifier);
         });
         StateManager<Block, BlockState> stateManager = block.getStateManager();
         Set<Property<?>> set = Sets.newHashSet();
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "properties", (JsonArray)null);
         if (jsonArray != null) {
            jsonArray.forEach((jsonElement) -> {
               set.add(stateManager.getProperty(JsonHelper.asString(jsonElement, "property")));
            });
         }

         return new CopyStateFunction(lootConditions, block, set);
      }
   }
}
