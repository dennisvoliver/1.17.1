package net.minecraft.world.gen.foliage;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeatureConfig;

public class JungleFoliagePlacer extends FoliagePlacer {
   public static final Codec<JungleFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) -> {
      return fillFoliagePlacerFields(instance).and((App)Codec.intRange(0, 16).fieldOf("height").forGetter((placer) -> {
         return placer.height;
      })).apply(instance, (Function3)(JungleFoliagePlacer::new));
   });
   protected final int height;

   public JungleFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
      super(radius, offset);
      this.height = height;
   }

   protected FoliagePlacerType<?> getType() {
      return FoliagePlacerType.JUNGLE_FOLIAGE_PLACER;
   }

   protected void generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, TreeFeatureConfig config, int trunkHeight, FoliagePlacer.TreeNode treeNode, int foliageHeight, int radius, int offset) {
      int i = treeNode.isGiantTrunk() ? foliageHeight : 1 + random.nextInt(2);

      for(int j = offset; j >= offset - i; --j) {
         int k = radius + treeNode.getFoliageRadius() + 1 - j;
         this.generateSquare(world, replacer, random, config, treeNode.getCenter(), k, j, treeNode.isGiantTrunk());
      }

   }

   public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
      return this.height;
   }

   protected boolean isInvalidForLeaves(Random random, int dx, int y, int dz, int radius, boolean giantTrunk) {
      if (dx + dz >= 7) {
         return true;
      } else {
         return dx * dx + dz * dz > radius * radius;
      }
   }
}
