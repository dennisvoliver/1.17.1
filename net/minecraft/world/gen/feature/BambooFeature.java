package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class BambooFeature extends Feature<ProbabilityConfig> {
   private static final BlockState BAMBOO;
   private static final BlockState BAMBOO_TOP_1;
   private static final BlockState BAMBOO_TOP_2;
   private static final BlockState BAMBOO_TOP_3;

   public BambooFeature(Codec<ProbabilityConfig> codec) {
      super(codec);
   }

   public boolean generate(FeatureContext<ProbabilityConfig> context) {
      int i = 0;
      BlockPos blockPos = context.getOrigin();
      StructureWorldAccess structureWorldAccess = context.getWorld();
      Random random = context.getRandom();
      ProbabilityConfig probabilityConfig = (ProbabilityConfig)context.getConfig();
      BlockPos.Mutable mutable = blockPos.mutableCopy();
      BlockPos.Mutable mutable2 = blockPos.mutableCopy();
      if (structureWorldAccess.isAir(mutable)) {
         if (Blocks.BAMBOO.getDefaultState().canPlaceAt(structureWorldAccess, mutable)) {
            int j = random.nextInt(12) + 5;
            int k;
            if (random.nextFloat() < probabilityConfig.probability) {
               k = random.nextInt(4) + 1;

               for(int l = blockPos.getX() - k; l <= blockPos.getX() + k; ++l) {
                  for(int m = blockPos.getZ() - k; m <= blockPos.getZ() + k; ++m) {
                     int n = l - blockPos.getX();
                     int o = m - blockPos.getZ();
                     if (n * n + o * o <= k * k) {
                        mutable2.set(l, structureWorldAccess.getTopY(Heightmap.Type.WORLD_SURFACE, l, m) - 1, m);
                        if (isSoil(structureWorldAccess.getBlockState(mutable2))) {
                           structureWorldAccess.setBlockState(mutable2, Blocks.PODZOL.getDefaultState(), Block.NOTIFY_LISTENERS);
                        }
                     }
                  }
               }
            }

            for(k = 0; k < j && structureWorldAccess.isAir(mutable); ++k) {
               structureWorldAccess.setBlockState(mutable, BAMBOO, Block.NOTIFY_LISTENERS);
               mutable.move(Direction.UP, 1);
            }

            if (mutable.getY() - blockPos.getY() >= 3) {
               structureWorldAccess.setBlockState(mutable, BAMBOO_TOP_1, Block.NOTIFY_LISTENERS);
               structureWorldAccess.setBlockState(mutable.move(Direction.DOWN, 1), BAMBOO_TOP_2, Block.NOTIFY_LISTENERS);
               structureWorldAccess.setBlockState(mutable.move(Direction.DOWN, 1), BAMBOO_TOP_3, Block.NOTIFY_LISTENERS);
            }
         }

         ++i;
      }

      return i > 0;
   }

   static {
      BAMBOO = (BlockState)((BlockState)((BlockState)Blocks.BAMBOO.getDefaultState().with(BambooBlock.AGE, 1)).with(BambooBlock.LEAVES, BambooLeaves.NONE)).with(BambooBlock.STAGE, 0);
      BAMBOO_TOP_1 = (BlockState)((BlockState)BAMBOO.with(BambooBlock.LEAVES, BambooLeaves.LARGE)).with(BambooBlock.STAGE, 1);
      BAMBOO_TOP_2 = (BlockState)BAMBOO.with(BambooBlock.LEAVES, BambooLeaves.LARGE);
      BAMBOO_TOP_3 = (BlockState)BAMBOO.with(BambooBlock.LEAVES, BambooLeaves.SMALL);
   }
}
