package net.minecraft.world.gen.carver;

import com.mojang.datafixers.util.Function6;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.HeightProvider;

public class CarverConfig extends ProbabilityConfig {
   public static final MapCodec<CarverConfig> CONFIG_CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      return instance.group(Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter((carverConfig) -> {
         return carverConfig.probability;
      }), HeightProvider.CODEC.fieldOf("y").forGetter((carverConfig) -> {
         return carverConfig.y;
      }), FloatProvider.VALUE_CODEC.fieldOf("yScale").forGetter((carverConfig) -> {
         return carverConfig.yScale;
      }), YOffset.OFFSET_CODEC.fieldOf("lava_level").forGetter((carverConfig) -> {
         return carverConfig.lavaLevel;
      }), Codec.BOOL.fieldOf("aquifers_enabled").forGetter((carverConfig) -> {
         return carverConfig.aquifers;
      }), CarverDebugConfig.CODEC.optionalFieldOf("debug_settings", CarverDebugConfig.DEFAULT).forGetter((carverConfig) -> {
         return carverConfig.debugConfig;
      })).apply(instance, (Function6)(CarverConfig::new));
   });
   public final HeightProvider y;
   public final FloatProvider yScale;
   public final YOffset lavaLevel;
   public final boolean aquifers;
   public final CarverDebugConfig debugConfig;

   public CarverConfig(float probability, HeightProvider y, FloatProvider yScale, YOffset lavaLevel, boolean aquifers, CarverDebugConfig debugConfig) {
      super(probability);
      this.y = y;
      this.yScale = yScale;
      this.lavaLevel = lavaLevel;
      this.aquifers = aquifers;
      this.debugConfig = debugConfig;
   }
}
