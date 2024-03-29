package net.minecraft.sound;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class BiomeMoodSound {
   public static final Codec<BiomeMoodSound> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(SoundEvent.CODEC.fieldOf("sound").forGetter((biomeMoodSound) -> {
         return biomeMoodSound.sound;
      }), Codec.INT.fieldOf("tick_delay").forGetter((biomeMoodSound) -> {
         return biomeMoodSound.cultivationTicks;
      }), Codec.INT.fieldOf("block_search_extent").forGetter((biomeMoodSound) -> {
         return biomeMoodSound.spawnRange;
      }), Codec.DOUBLE.fieldOf("offset").forGetter((biomeMoodSound) -> {
         return biomeMoodSound.extraDistance;
      })).apply(instance, (Function4)(BiomeMoodSound::new));
   });
   public static final BiomeMoodSound CAVE;
   private final SoundEvent sound;
   private final int cultivationTicks;
   private final int spawnRange;
   private final double extraDistance;

   public BiomeMoodSound(SoundEvent sound, int cultivationTicks, int spawnRange, double extraDistance) {
      this.sound = sound;
      this.cultivationTicks = cultivationTicks;
      this.spawnRange = spawnRange;
      this.extraDistance = extraDistance;
   }

   public SoundEvent getSound() {
      return this.sound;
   }

   /**
    * Returns the ticks it takes for entering the mood environment (a totally
    * dark cave) to playing the mood sound, or the inverse of the per-tick.
    */
   public int getCultivationTicks() {
      return this.cultivationTicks;
   }

   /**
    * Returns the chebyshev distance from which the mood sound can play to
    * the player.
    */
   public int getSpawnRange() {
      return this.spawnRange;
   }

   /**
    * Returns the extra distance of the sound from the player when the sound
    * plays from the mood position.
    * 
    * <p>The sound is actually played at a position along the line on the
    * three-dimensional vector from the player to the chosen mood position that
    * is this distance to the mood position and this distance farther from the
    * player.
    */
   public double getExtraDistance() {
      return this.extraDistance;
   }

   static {
      CAVE = new BiomeMoodSound(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0D);
   }
}
