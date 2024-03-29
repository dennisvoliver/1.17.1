package net.minecraft.block;

import java.util.Iterator;
import java.util.Random;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ChorusFlowerBlock extends Block {
   public static final int MAX_AGE = 5;
   public static final IntProperty AGE;
   private final ChorusPlantBlock plantBlock;

   protected ChorusFlowerBlock(ChorusPlantBlock plantBlock, AbstractBlock.Settings settings) {
      super(settings);
      this.plantBlock = plantBlock;
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AGE, 0));
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (!state.canPlaceAt(world, pos)) {
         world.breakBlock(pos, true);
      }

   }

   public boolean hasRandomTicks(BlockState state) {
      return (Integer)state.get(AGE) < 5;
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockPos blockPos = pos.up();
      if (world.isAir(blockPos) && blockPos.getY() < world.getTopY()) {
         int i = (Integer)state.get(AGE);
         if (i < 5) {
            boolean bl = false;
            boolean bl2 = false;
            BlockState blockState = world.getBlockState(pos.down());
            int l;
            if (blockState.isOf(Blocks.END_STONE)) {
               bl = true;
            } else if (blockState.isOf(this.plantBlock)) {
               l = 1;

               for(int k = 0; k < 4; ++k) {
                  BlockState blockState2 = world.getBlockState(pos.down(l + 1));
                  if (!blockState2.isOf(this.plantBlock)) {
                     if (blockState2.isOf(Blocks.END_STONE)) {
                        bl2 = true;
                     }
                     break;
                  }

                  ++l;
               }

               if (l < 2 || l <= random.nextInt(bl2 ? 5 : 4)) {
                  bl = true;
               }
            } else if (blockState.isAir()) {
               bl = true;
            }

            if (bl && isSurroundedByAir(world, blockPos, (Direction)null) && world.isAir(pos.up(2))) {
               world.setBlockState(pos, this.plantBlock.withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
               this.grow(world, blockPos, i);
            } else if (i < 4) {
               l = random.nextInt(4);
               if (bl2) {
                  ++l;
               }

               boolean bl3 = false;

               for(int m = 0; m < l; ++m) {
                  Direction direction = Direction.Type.HORIZONTAL.random(random);
                  BlockPos blockPos2 = pos.offset(direction);
                  if (world.isAir(blockPos2) && world.isAir(blockPos2.down()) && isSurroundedByAir(world, blockPos2, direction.getOpposite())) {
                     this.grow(world, blockPos2, i + 1);
                     bl3 = true;
                  }
               }

               if (bl3) {
                  world.setBlockState(pos, this.plantBlock.withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
               } else {
                  this.die(world, pos);
               }
            } else {
               this.die(world, pos);
            }

         }
      }
   }

   private void grow(World world, BlockPos pos, int age) {
      world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, age), Block.NOTIFY_LISTENERS);
      world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_GROWS, pos, 0);
   }

   private void die(World world, BlockPos pos) {
      world.setBlockState(pos, (BlockState)this.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
      world.syncWorldEvent(WorldEvents.CHORUS_FLOWER_DIES, pos, 0);
   }

   private static boolean isSurroundedByAir(WorldView world, BlockPos pos, @Nullable Direction exceptDirection) {
      Iterator var3 = Direction.Type.HORIZONTAL.iterator();

      Direction direction;
      do {
         if (!var3.hasNext()) {
            return true;
         }

         direction = (Direction)var3.next();
      } while(direction == exceptDirection || world.isAir(pos.offset(direction)));

      return false;
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (direction != Direction.UP && !state.canPlaceAt(world, pos)) {
         world.getBlockTickScheduler().schedule(pos, this, 1);
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
      BlockState blockState = world.getBlockState(pos.down());
      if (!blockState.isOf(this.plantBlock) && !blockState.isOf(Blocks.END_STONE)) {
         if (!blockState.isAir()) {
            return false;
         } else {
            boolean bl = false;
            Iterator var6 = Direction.Type.HORIZONTAL.iterator();

            while(var6.hasNext()) {
               Direction direction = (Direction)var6.next();
               BlockState blockState2 = world.getBlockState(pos.offset(direction));
               if (blockState2.isOf(this.plantBlock)) {
                  if (bl) {
                     return false;
                  }

                  bl = true;
               } else if (!blockState2.isAir()) {
                  return false;
               }
            }

            return bl;
         }
      } else {
         return true;
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AGE);
   }

   public static void generate(WorldAccess world, BlockPos pos, Random random, int size) {
      world.setBlockState(pos, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).withConnectionProperties(world, pos), Block.NOTIFY_LISTENERS);
      generate(world, pos, random, pos, size, 0);
   }

   private static void generate(WorldAccess world, BlockPos pos, Random random, BlockPos rootPos, int size, int layer) {
      ChorusPlantBlock chorusPlantBlock = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
      int i = random.nextInt(4) + 1;
      if (layer == 0) {
         ++i;
      }

      for(int j = 0; j < i; ++j) {
         BlockPos blockPos = pos.up(j + 1);
         if (!isSurroundedByAir(world, blockPos, (Direction)null)) {
            return;
         }

         world.setBlockState(blockPos, chorusPlantBlock.withConnectionProperties(world, blockPos), Block.NOTIFY_LISTENERS);
         world.setBlockState(blockPos.down(), chorusPlantBlock.withConnectionProperties(world, blockPos.down()), Block.NOTIFY_LISTENERS);
      }

      boolean bl = false;
      if (layer < 4) {
         int k = random.nextInt(4);
         if (layer == 0) {
            ++k;
         }

         for(int l = 0; l < k; ++l) {
            Direction direction = Direction.Type.HORIZONTAL.random(random);
            BlockPos blockPos2 = pos.up(i).offset(direction);
            if (Math.abs(blockPos2.getX() - rootPos.getX()) < size && Math.abs(blockPos2.getZ() - rootPos.getZ()) < size && world.isAir(blockPos2) && world.isAir(blockPos2.down()) && isSurroundedByAir(world, blockPos2, direction.getOpposite())) {
               bl = true;
               world.setBlockState(blockPos2, chorusPlantBlock.withConnectionProperties(world, blockPos2), Block.NOTIFY_LISTENERS);
               world.setBlockState(blockPos2.offset(direction.getOpposite()), chorusPlantBlock.withConnectionProperties(world, blockPos2.offset(direction.getOpposite())), Block.NOTIFY_LISTENERS);
               generate(world, blockPos2, random, rootPos, size, layer + 1);
            }
         }
      }

      if (!bl) {
         world.setBlockState(pos.up(i), (BlockState)Blocks.CHORUS_FLOWER.getDefaultState().with(AGE, 5), Block.NOTIFY_LISTENERS);
      }

   }

   public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
      BlockPos blockPos = hit.getBlockPos();
      if (!world.isClient && projectile.canModifyAt(world, blockPos) && projectile.getType().isIn(EntityTypeTags.IMPACT_PROJECTILES)) {
         world.breakBlock(blockPos, true, projectile);
      }

   }

   static {
      AGE = Properties.AGE_5;
   }
}
