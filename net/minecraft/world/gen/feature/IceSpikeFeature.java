package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class IceSpikeFeature extends Feature<DefaultFeatureConfig> {
   public IceSpikeFeature(Codec<DefaultFeatureConfig> codec) {
      super(codec);
   }

   public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
      BlockPos blockPos = context.getOrigin();
      Random random = context.getRandom();

      StructureWorldAccess structureWorldAccess;
      for(structureWorldAccess = context.getWorld(); structureWorldAccess.isAir(blockPos) && blockPos.getY() > structureWorldAccess.getBottomY() + 2; blockPos = blockPos.down()) {
      }

      if (!structureWorldAccess.getBlockState(blockPos).isOf(Blocks.SNOW_BLOCK)) {
         return false;
      } else {
         blockPos = blockPos.up(random.nextInt(4));
         int i = random.nextInt(4) + 7;
         int j = i / 4 + random.nextInt(2);
         if (j > 1 && random.nextInt(60) == 0) {
            blockPos = blockPos.up(10 + random.nextInt(30));
         }

         int k;
         int l;
         for(k = 0; k < i; ++k) {
            float f = (1.0F - (float)k / (float)i) * (float)j;
            l = MathHelper.ceil(f);

            for(int m = -l; m <= l; ++m) {
               float g = (float)MathHelper.abs(m) - 0.25F;

               for(int n = -l; n <= l; ++n) {
                  float h = (float)MathHelper.abs(n) - 0.25F;
                  if ((m == 0 && n == 0 || !(g * g + h * h > f * f)) && (m != -l && m != l && n != -l && n != l || !(random.nextFloat() > 0.75F))) {
                     BlockState blockState = structureWorldAccess.getBlockState(blockPos.add(m, k, n));
                     if (blockState.isAir() || isSoil(blockState) || blockState.isOf(Blocks.SNOW_BLOCK) || blockState.isOf(Blocks.ICE)) {
                        this.setBlockState(structureWorldAccess, blockPos.add(m, k, n), Blocks.PACKED_ICE.getDefaultState());
                     }

                     if (k != 0 && l > 1) {
                        blockState = structureWorldAccess.getBlockState(blockPos.add(m, -k, n));
                        if (blockState.isAir() || isSoil(blockState) || blockState.isOf(Blocks.SNOW_BLOCK) || blockState.isOf(Blocks.ICE)) {
                           this.setBlockState(structureWorldAccess, blockPos.add(m, -k, n), Blocks.PACKED_ICE.getDefaultState());
                        }
                     }
                  }
               }
            }
         }

         k = j - 1;
         if (k < 0) {
            k = 0;
         } else if (k > 1) {
            k = 1;
         }

         for(int p = -k; p <= k; ++p) {
            for(l = -k; l <= k; ++l) {
               BlockPos blockPos2 = blockPos.add(p, -1, l);
               int r = 50;
               if (Math.abs(p) == 1 && Math.abs(l) == 1) {
                  r = random.nextInt(5);
               }

               while(blockPos2.getY() > 50) {
                  BlockState blockState2 = structureWorldAccess.getBlockState(blockPos2);
                  if (!blockState2.isAir() && !isSoil(blockState2) && !blockState2.isOf(Blocks.SNOW_BLOCK) && !blockState2.isOf(Blocks.ICE) && !blockState2.isOf(Blocks.PACKED_ICE)) {
                     break;
                  }

                  this.setBlockState(structureWorldAccess, blockPos2, Blocks.PACKED_ICE.getDefaultState());
                  blockPos2 = blockPos2.down();
                  --r;
                  if (r <= 0) {
                     blockPos2 = blockPos2.down(random.nextInt(5) + 1);
                     r = random.nextInt(5);
                  }
               }
            }
         }

         return true;
      }
   }
}
