package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnerBlock extends BlockWithEntity {
   protected SpawnerBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new MobSpawnerBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return checkType(type, BlockEntityType.MOB_SPAWNER, world.isClient ? MobSpawnerBlockEntity::clientTick : MobSpawnerBlockEntity::serverTick);
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
      super.onStacksDropped(state, world, pos, stack);
      int i = 15 + world.random.nextInt(15) + world.random.nextInt(15);
      this.dropExperience(world, pos, i);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }
}
