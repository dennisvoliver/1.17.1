package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RandomFeatureConfig implements FeatureConfig {
   public static final Codec<RandomFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.apply2(RandomFeatureConfig::new, RandomFeatureEntry.CODEC.listOf().fieldOf("features").forGetter((randomFeatureConfig) -> {
         return randomFeatureConfig.features;
      }), ConfiguredFeature.REGISTRY_CODEC.fieldOf("default").forGetter((randomFeatureConfig) -> {
         return randomFeatureConfig.defaultFeature;
      }));
   });
   public final List<RandomFeatureEntry> features;
   public final Supplier<ConfiguredFeature<?, ?>> defaultFeature;

   public RandomFeatureConfig(List<RandomFeatureEntry> features, ConfiguredFeature<?, ?> defaultFeature) {
      this(features, () -> {
         return defaultFeature;
      });
   }

   private RandomFeatureConfig(List<RandomFeatureEntry> features, Supplier<ConfiguredFeature<?, ?>> defaultFeature) {
      this.features = features;
      this.defaultFeature = defaultFeature;
   }

   public Stream<ConfiguredFeature<?, ?>> getDecoratedFeatures() {
      return Stream.concat(this.features.stream().flatMap((randomFeatureEntry) -> {
         return ((ConfiguredFeature)randomFeatureEntry.feature.get()).getDecoratedFeatures();
      }), ((ConfiguredFeature)this.defaultFeature.get()).getDecoratedFeatures());
   }
}
