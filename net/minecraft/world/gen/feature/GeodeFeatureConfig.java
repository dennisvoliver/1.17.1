package net.minecraft.world.gen.feature;

import com.mojang.datafixers.util.Function13;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class GeodeFeatureConfig implements FeatureConfig {
   public static final Codec<Double> RANGE = Codec.doubleRange(0.0D, 1.0D);
   public static final Codec<GeodeFeatureConfig> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(GeodeLayerConfig.CODEC.fieldOf("blocks").forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.layerConfig;
      }), GeodeLayerThicknessConfig.CODEC.fieldOf("layers").forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.layerThicknessConfig;
      }), GeodeCrackConfig.CODEC.fieldOf("crack").forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.crackConfig;
      }), RANGE.fieldOf("use_potential_placements_chance").orElse(0.35D).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.usePotentialPlacementsChance;
      }), RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0D).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.useAlternateLayer0Chance;
      }), Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.placementsRequireLayer0Alternate;
      }), IntProvider.createValidatingCodec(1, 20).fieldOf("outer_wall_distance").orElse(UniformIntProvider.create(4, 5)).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.outerWallDistance;
      }), IntProvider.createValidatingCodec(1, 20).fieldOf("distribution_points").orElse(UniformIntProvider.create(3, 4)).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.distributionPoints;
      }), IntProvider.createValidatingCodec(0, 10).fieldOf("point_offset").orElse(UniformIntProvider.create(1, 2)).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.pointOffset;
      }), Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.minGenOffset;
      }), Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.maxGenOffset;
      }), RANGE.fieldOf("noise_multiplier").orElse(0.05D).forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.noiseMultiplier;
      }), Codec.INT.fieldOf("invalid_blocks_threshold").forGetter((geodeFeatureConfig) -> {
         return geodeFeatureConfig.invalidBlocksThreshold;
      })).apply(instance, (Function13)(GeodeFeatureConfig::new));
   });
   public final GeodeLayerConfig layerConfig;
   public final GeodeLayerThicknessConfig layerThicknessConfig;
   public final GeodeCrackConfig crackConfig;
   public final double usePotentialPlacementsChance;
   public final double useAlternateLayer0Chance;
   public final boolean placementsRequireLayer0Alternate;
   public final IntProvider outerWallDistance;
   public final IntProvider distributionPoints;
   public final IntProvider pointOffset;
   public final int minGenOffset;
   public final int maxGenOffset;
   public final double noiseMultiplier;
   public final int invalidBlocksThreshold;

   public GeodeFeatureConfig(GeodeLayerConfig layerConfig, GeodeLayerThicknessConfig layerThicknessConfig, GeodeCrackConfig crackConfig, double usePotentialPlacementsChance, double useAlternateLayer0Chance, boolean placementsRequireLayer0Alternate, IntProvider outerWallDistance, IntProvider distributionPoints, IntProvider pointOffset, int maxDistributionPoints, int minPointOffset, double noiseMultiplier, int maxGenOffset) {
      this.layerConfig = layerConfig;
      this.layerThicknessConfig = layerThicknessConfig;
      this.crackConfig = crackConfig;
      this.usePotentialPlacementsChance = usePotentialPlacementsChance;
      this.useAlternateLayer0Chance = useAlternateLayer0Chance;
      this.placementsRequireLayer0Alternate = placementsRequireLayer0Alternate;
      this.outerWallDistance = outerWallDistance;
      this.distributionPoints = distributionPoints;
      this.pointOffset = pointOffset;
      this.minGenOffset = maxDistributionPoints;
      this.maxGenOffset = minPointOffset;
      this.noiseMultiplier = noiseMultiplier;
      this.invalidBlocksThreshold = maxGenOffset;
   }
}
