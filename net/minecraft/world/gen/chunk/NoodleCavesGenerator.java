package net.minecraft.world.gen.chunk;

import java.util.Random;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.NoiseHelper;
import net.minecraft.world.gen.SimpleRandom;

public class NoodleCavesGenerator {
   private static final int MAX_Y = 30;
   private static final double WEIGHT_NOISE_FACTOR = 1.5D;
   private static final double HORIZONTAL_WEIGHT_NOISE_SCALE = 2.6666666666666665D;
   private static final double VERTICAL_WEIGHT_NOISE_SCALE = 2.6666666666666665D;
   private final DoublePerlinNoiseSampler frequencyNoiseSampler;
   private final DoublePerlinNoiseSampler weightReducingNoiseSampler;
   private final DoublePerlinNoiseSampler firstWeightNoiseSampelr;
   private final DoublePerlinNoiseSampler secondWeightNoiseSampler;

   public NoodleCavesGenerator(long seed) {
      Random random = new Random(seed);
      this.frequencyNoiseSampler = DoublePerlinNoiseSampler.create(new SimpleRandom(random.nextLong()), -8, (double[])(1.0D));
      this.weightReducingNoiseSampler = DoublePerlinNoiseSampler.create(new SimpleRandom(random.nextLong()), -8, (double[])(1.0D));
      this.firstWeightNoiseSampelr = DoublePerlinNoiseSampler.create(new SimpleRandom(random.nextLong()), -7, (double[])(1.0D));
      this.secondWeightNoiseSampler = DoublePerlinNoiseSampler.create(new SimpleRandom(random.nextLong()), -7, (double[])(1.0D));
   }

   public void sampleFrequencyNoise(double[] buffer, int x, int z, int minY, int noiseSizeY) {
      this.sample(buffer, x, z, minY, noiseSizeY, this.frequencyNoiseSampler, 1.0D);
   }

   public void sampleWeightReducingNoise(double[] buffer, int x, int z, int minY, int noiseSizeY) {
      this.sample(buffer, x, z, minY, noiseSizeY, this.weightReducingNoiseSampler, 1.0D);
   }

   public void sampleFirstWeightNoise(double[] buffer, int x, int z, int minY, int noiseSizeY) {
      this.sample(buffer, x, z, minY, noiseSizeY, this.firstWeightNoiseSampelr, 2.6666666666666665D, 2.6666666666666665D);
   }

   public void sampleSecondWeightNoise(double[] buffer, int x, int z, int minY, int noiseSizeY) {
      this.sample(buffer, x, z, minY, noiseSizeY, this.secondWeightNoiseSampler, 2.6666666666666665D, 2.6666666666666665D);
   }

   public void sample(double[] buffer, int x, int z, int minY, int noiseSizeY, DoublePerlinNoiseSampler sampler, double scale) {
      this.sample(buffer, x, z, minY, noiseSizeY, sampler, scale, scale);
   }

   public void sample(double[] buffer, int x, int z, int minY, int noiseSizeY, DoublePerlinNoiseSampler sampler, double horizontalScale, double verticalScale) {
      int i = true;
      int j = true;

      for(int k = 0; k < noiseSizeY; ++k) {
         int l = k + minY;
         int m = x * 4;
         int n = l * 8;
         int o = z * 4;
         double e;
         if (n < 38) {
            e = NoiseHelper.lerpFromProgress(sampler, (double)m * horizontalScale, (double)n * verticalScale, (double)o * horizontalScale, -1.0D, 1.0D);
         } else {
            e = 1.0D;
         }

         buffer[k] = e;
      }

   }

   public double sampleWeight(double weight, int x, int y, int z, double frequencyNoise, double weightReducingNoise, double firstWeightNoise, double secondWeightNoise, int minY) {
      if (y <= 30 && y >= minY + 4) {
         if (weight < 0.0D) {
            return weight;
         } else if (frequencyNoise < 0.0D) {
            return weight;
         } else {
            double d = 0.05D;
            double e = 0.1D;
            double f = MathHelper.clampedLerpFromProgress(weightReducingNoise, -1.0D, 1.0D, 0.05D, 0.1D);
            double g = Math.abs(1.5D * firstWeightNoise) - f;
            double h = Math.abs(1.5D * secondWeightNoise) - f;
            double i = Math.max(g, h);
            return Math.min(weight, i);
         }
      } else {
         return weight;
      }
   }
}
