package net.minecraft.block.sapling;

import java.util.Random;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import org.jetbrains.annotations.Nullable;

public class JungleSaplingGenerator extends LargeTreeSaplingGenerator {
   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> getTreeFeature(Random random, boolean bees) {
      return ConfiguredFeatures.JUNGLE_TREE_NO_VINE;
   }

   @Nullable
   protected ConfiguredFeature<TreeFeatureConfig, ?> getLargeTreeFeature(Random random) {
      return ConfiguredFeatures.MEGA_JUNGLE_TREE;
   }
}
