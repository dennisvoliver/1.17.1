package net.minecraft.world.biome;

import com.mojang.datafixers.util.Function12;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.StringIdentifiable;

public class BiomeEffects {
   public static final Codec<BiomeEffects> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.INT.fieldOf("fog_color").forGetter((biomeEffects) -> {
         return biomeEffects.fogColor;
      }), Codec.INT.fieldOf("water_color").forGetter((biomeEffects) -> {
         return biomeEffects.waterColor;
      }), Codec.INT.fieldOf("water_fog_color").forGetter((biomeEffects) -> {
         return biomeEffects.waterFogColor;
      }), Codec.INT.fieldOf("sky_color").forGetter((biomeEffects) -> {
         return biomeEffects.skyColor;
      }), Codec.INT.optionalFieldOf("foliage_color").forGetter((biomeEffects) -> {
         return biomeEffects.foliageColor;
      }), Codec.INT.optionalFieldOf("grass_color").forGetter((biomeEffects) -> {
         return biomeEffects.grassColor;
      }), BiomeEffects.GrassColorModifier.CODEC.optionalFieldOf("grass_color_modifier", BiomeEffects.GrassColorModifier.NONE).forGetter((biomeEffects) -> {
         return biomeEffects.grassColorModifier;
      }), BiomeParticleConfig.CODEC.optionalFieldOf("particle").forGetter((biomeEffects) -> {
         return biomeEffects.particleConfig;
      }), SoundEvent.CODEC.optionalFieldOf("ambient_sound").forGetter((biomeEffects) -> {
         return biomeEffects.loopSound;
      }), BiomeMoodSound.CODEC.optionalFieldOf("mood_sound").forGetter((biomeEffects) -> {
         return biomeEffects.moodSound;
      }), BiomeAdditionsSound.CODEC.optionalFieldOf("additions_sound").forGetter((biomeEffects) -> {
         return biomeEffects.additionsSound;
      }), MusicSound.CODEC.optionalFieldOf("music").forGetter((biomeEffects) -> {
         return biomeEffects.music;
      })).apply(instance, (Function12)(BiomeEffects::new));
   });
   private final int fogColor;
   private final int waterColor;
   private final int waterFogColor;
   private final int skyColor;
   private final Optional<Integer> foliageColor;
   private final Optional<Integer> grassColor;
   private final BiomeEffects.GrassColorModifier grassColorModifier;
   private final Optional<BiomeParticleConfig> particleConfig;
   private final Optional<SoundEvent> loopSound;
   private final Optional<BiomeMoodSound> moodSound;
   private final Optional<BiomeAdditionsSound> additionsSound;
   private final Optional<MusicSound> music;

   BiomeEffects(int fogColor, int waterColor, int waterFogColor, int skyColor, Optional<Integer> foliageColor, Optional<Integer> grassColor, BiomeEffects.GrassColorModifier grassColorModifier, Optional<BiomeParticleConfig> particleConfig, Optional<SoundEvent> loopSound, Optional<BiomeMoodSound> moodSound, Optional<BiomeAdditionsSound> additionsSound, Optional<MusicSound> music) {
      this.fogColor = fogColor;
      this.waterColor = waterColor;
      this.waterFogColor = waterFogColor;
      this.skyColor = skyColor;
      this.foliageColor = foliageColor;
      this.grassColor = grassColor;
      this.grassColorModifier = grassColorModifier;
      this.particleConfig = particleConfig;
      this.loopSound = loopSound;
      this.moodSound = moodSound;
      this.additionsSound = additionsSound;
      this.music = music;
   }

   public int getFogColor() {
      return this.fogColor;
   }

   public int getWaterColor() {
      return this.waterColor;
   }

   public int getWaterFogColor() {
      return this.waterFogColor;
   }

   public int getSkyColor() {
      return this.skyColor;
   }

   public Optional<Integer> getFoliageColor() {
      return this.foliageColor;
   }

   public Optional<Integer> getGrassColor() {
      return this.grassColor;
   }

   public BiomeEffects.GrassColorModifier getGrassColorModifier() {
      return this.grassColorModifier;
   }

   public Optional<BiomeParticleConfig> getParticleConfig() {
      return this.particleConfig;
   }

   /**
    * Returns the loop sound.
    * 
    * <p>A loop sound is played continuously as an ambient sound whenever the
    * player is in the biome with this effect.
    */
   public Optional<SoundEvent> getLoopSound() {
      return this.loopSound;
   }

   /**
    * Returns the mood sound.
    * 
    * <p>A mood sound is played once every 6000 to 17999 ticks as an ambient
    * sound whenever the player is in the biome with this effect and near a
    * position that has 0 sky light and less than 7 combined light.
    * 
    * <p>Overworld biomes have the regular cave sound as their mood sound,
    * while three nether biomes in 20w10a have their dedicated mood sounds.
    */
   public Optional<BiomeMoodSound> getMoodSound() {
      return this.moodSound;
   }

   /**
    * Returns the additions sound.
    * 
    * <p>An additions sound is played at 1.1% chance every tick as an ambient
    * sound whenever the player is in the biome with this effect.
    */
   public Optional<BiomeAdditionsSound> getAdditionsSound() {
      return this.additionsSound;
   }

   public Optional<MusicSound> getMusic() {
      return this.music;
   }

   public static enum GrassColorModifier implements StringIdentifiable {
      NONE("none") {
         public int getModifiedGrassColor(double x, double z, int color) {
            return color;
         }
      },
      DARK_FOREST("dark_forest") {
         public int getModifiedGrassColor(double x, double z, int color) {
            return (color & 16711422) + 2634762 >> 1;
         }
      },
      SWAMP("swamp") {
         public int getModifiedGrassColor(double x, double z, int color) {
            double d = Biome.FOLIAGE_NOISE.sample(x * 0.0225D, z * 0.0225D, false);
            return d < -0.1D ? 5011004 : 6975545;
         }
      };

      private final String name;
      public static final Codec<BiomeEffects.GrassColorModifier> CODEC = StringIdentifiable.createCodec(BiomeEffects.GrassColorModifier::values, BiomeEffects.GrassColorModifier::byName);
      private static final Map<String, BiomeEffects.GrassColorModifier> BY_NAME = (Map)Arrays.stream(values()).collect(Collectors.toMap(BiomeEffects.GrassColorModifier::getName, (grassColorModifier) -> {
         return grassColorModifier;
      }));

      public abstract int getModifiedGrassColor(double x, double z, int color);

      GrassColorModifier(String string2) {
         this.name = string2;
      }

      public String getName() {
         return this.name;
      }

      public String asString() {
         return this.name;
      }

      public static BiomeEffects.GrassColorModifier byName(String name) {
         return (BiomeEffects.GrassColorModifier)BY_NAME.get(name);
      }
   }

   public static class Builder {
      private OptionalInt fogColor = OptionalInt.empty();
      private OptionalInt waterColor = OptionalInt.empty();
      private OptionalInt waterFogColor = OptionalInt.empty();
      private OptionalInt skyColor = OptionalInt.empty();
      private Optional<Integer> foliageColor = Optional.empty();
      private Optional<Integer> grassColor = Optional.empty();
      private BiomeEffects.GrassColorModifier grassColorModifier;
      private Optional<BiomeParticleConfig> particleConfig;
      private Optional<SoundEvent> loopSound;
      private Optional<BiomeMoodSound> moodSound;
      private Optional<BiomeAdditionsSound> additionsSound;
      private Optional<MusicSound> musicSound;

      public Builder() {
         this.grassColorModifier = BiomeEffects.GrassColorModifier.NONE;
         this.particleConfig = Optional.empty();
         this.loopSound = Optional.empty();
         this.moodSound = Optional.empty();
         this.additionsSound = Optional.empty();
         this.musicSound = Optional.empty();
      }

      public BiomeEffects.Builder fogColor(int fogColor) {
         this.fogColor = OptionalInt.of(fogColor);
         return this;
      }

      public BiomeEffects.Builder waterColor(int waterColor) {
         this.waterColor = OptionalInt.of(waterColor);
         return this;
      }

      public BiomeEffects.Builder waterFogColor(int waterFogColor) {
         this.waterFogColor = OptionalInt.of(waterFogColor);
         return this;
      }

      public BiomeEffects.Builder skyColor(int skyColor) {
         this.skyColor = OptionalInt.of(skyColor);
         return this;
      }

      public BiomeEffects.Builder foliageColor(int foliageColor) {
         this.foliageColor = Optional.of(foliageColor);
         return this;
      }

      public BiomeEffects.Builder grassColor(int grassColor) {
         this.grassColor = Optional.of(grassColor);
         return this;
      }

      public BiomeEffects.Builder grassColorModifier(BiomeEffects.GrassColorModifier grassColorModifier) {
         this.grassColorModifier = grassColorModifier;
         return this;
      }

      public BiomeEffects.Builder particleConfig(BiomeParticleConfig particleConfig) {
         this.particleConfig = Optional.of(particleConfig);
         return this;
      }

      public BiomeEffects.Builder loopSound(SoundEvent sound) {
         this.loopSound = Optional.of(sound);
         return this;
      }

      public BiomeEffects.Builder moodSound(BiomeMoodSound moodSound) {
         this.moodSound = Optional.of(moodSound);
         return this;
      }

      public BiomeEffects.Builder additionsSound(BiomeAdditionsSound additionsSound) {
         this.additionsSound = Optional.of(additionsSound);
         return this;
      }

      public BiomeEffects.Builder music(MusicSound music) {
         this.musicSound = Optional.of(music);
         return this;
      }

      public BiomeEffects build() {
         return new BiomeEffects(this.fogColor.orElseThrow(() -> {
            return new IllegalStateException("Missing 'fog' color.");
         }), this.waterColor.orElseThrow(() -> {
            return new IllegalStateException("Missing 'water' color.");
         }), this.waterFogColor.orElseThrow(() -> {
            return new IllegalStateException("Missing 'water fog' color.");
         }), this.skyColor.orElseThrow(() -> {
            return new IllegalStateException("Missing 'sky' color.");
         }), this.foliageColor, this.grassColor, this.grassColorModifier, this.particleConfig, this.loopSound, this.moodSound, this.additionsSound, this.musicSound);
      }
   }
}
