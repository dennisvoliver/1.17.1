package net.minecraft.loot.provider.number;

import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import net.minecraft.util.JsonSerializing;
import net.minecraft.util.registry.Registry;

public class LootNumberProviderTypes {
   public static final LootNumberProviderType CONSTANT = register("constant", new ConstantLootNumberProvider.Serializer());
   public static final LootNumberProviderType UNIFORM = register("uniform", new UniformLootNumberProvider.Serializer());
   public static final LootNumberProviderType BINOMIAL = register("binomial", new BinomialLootNumberProvider.Serializer());
   public static final LootNumberProviderType SCORE = register("score", new ScoreLootNumberProvider.Serializer());

   private static LootNumberProviderType register(String id, JsonSerializer<? extends LootNumberProvider> jsonSerializer) {
      return (LootNumberProviderType)Registry.register(Registry.LOOT_NUMBER_PROVIDER_TYPE, (Identifier)(new Identifier(id)), new LootNumberProviderType(jsonSerializer));
   }

   public static Object createGsonSerializer() {
      return JsonSerializing.createSerializerBuilder(Registry.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", LootNumberProvider::getType).elementSerializer(CONSTANT, new ConstantLootNumberProvider.CustomSerializer()).defaultType(UNIFORM).build();
   }
}
