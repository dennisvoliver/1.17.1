package net.minecraft.block.sapling;

import java.util.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

public class DarkOakSaplingGenerator extends LargeTreeSaplingGenerator {
   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random random, boolean bees) {
      return null;
   }

   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> getLargeTreeFeature(Random random) {
      return ConfiguredFeatures.DARK_OAK;
   }
}
