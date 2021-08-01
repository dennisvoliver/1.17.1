package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class TintedGlassBlock extends AbstractGlassBlock {
   public TintedGlassBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
      return false;
   }

   public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
      return world.getMaxLightLevel();
   }
}
