package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class TrappedChestBlock extends ChestBlock {
   public TrappedChestBlock(AbstractBlock.Settings settings) {
      super(settings, () -> {
         return BlockEntityType.TRAPPED_CHEST;
      });
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new TrappedChestBlockEntity(pos, state);
   }

   protected Stat<Identifier> getOpenStat() {
      return Stats.CUSTOM.getOrCreateStat(Stats.TRIGGER_TRAPPED_CHEST);
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return MathHelper.clamp((int)ChestBlockEntity.getPlayersLookingInChestCount(world, pos), (int)0, (int)15);
   }

   public int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return direction == Direction.UP ? state.getWeakRedstonePower(world, pos, direction) : 0;
   }
}
