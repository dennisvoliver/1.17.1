package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Random;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.block.enums.SculkSensorPhase;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class SculkSensorBlock extends BlockWithEntity implements Waterloggable {
   public static final int field_31239 = 40;
   public static final int field_31240 = 1;
   public static final Object2IntMap<GameEvent> FREQUENCIES = Object2IntMaps.unmodifiable((Object2IntMap)Util.make(new Object2IntOpenHashMap(), (map) -> {
      map.put(GameEvent.STEP, 1);
      map.put(GameEvent.FLAP, 2);
      map.put(GameEvent.SWIM, 3);
      map.put(GameEvent.ELYTRA_FREE_FALL, 4);
      map.put(GameEvent.HIT_GROUND, 5);
      map.put(GameEvent.SPLASH, 6);
      map.put(GameEvent.WOLF_SHAKING, 6);
      map.put(GameEvent.MINECART_MOVING, 6);
      map.put(GameEvent.RING_BELL, 6);
      map.put(GameEvent.BLOCK_CHANGE, 6);
      map.put(GameEvent.PROJECTILE_SHOOT, 7);
      map.put(GameEvent.DRINKING_FINISH, 7);
      map.put(GameEvent.PRIME_FUSE, 7);
      map.put(GameEvent.PROJECTILE_LAND, 8);
      map.put(GameEvent.EAT, 8);
      map.put(GameEvent.MOB_INTERACT, 8);
      map.put(GameEvent.ENTITY_DAMAGED, 8);
      map.put(GameEvent.EQUIP, 9);
      map.put(GameEvent.SHEAR, 9);
      map.put(GameEvent.RAVAGER_ROAR, 9);
      map.put(GameEvent.BLOCK_CLOSE, 10);
      map.put(GameEvent.BLOCK_UNSWITCH, 10);
      map.put(GameEvent.BLOCK_UNPRESS, 10);
      map.put(GameEvent.BLOCK_DETACH, 10);
      map.put(GameEvent.DISPENSE_FAIL, 10);
      map.put(GameEvent.BLOCK_OPEN, 11);
      map.put(GameEvent.BLOCK_SWITCH, 11);
      map.put(GameEvent.BLOCK_PRESS, 11);
      map.put(GameEvent.BLOCK_ATTACH, 11);
      map.put(GameEvent.ENTITY_PLACE, 12);
      map.put(GameEvent.BLOCK_PLACE, 12);
      map.put(GameEvent.FLUID_PLACE, 12);
      map.put(GameEvent.ENTITY_KILLED, 13);
      map.put(GameEvent.BLOCK_DESTROY, 13);
      map.put(GameEvent.FLUID_PICKUP, 13);
      map.put(GameEvent.FISHING_ROD_REEL_IN, 14);
      map.put(GameEvent.CONTAINER_CLOSE, 14);
      map.put(GameEvent.PISTON_CONTRACT, 14);
      map.put(GameEvent.SHULKER_CLOSE, 14);
      map.put(GameEvent.PISTON_EXTEND, 15);
      map.put(GameEvent.CONTAINER_OPEN, 15);
      map.put(GameEvent.FISHING_ROD_CAST, 15);
      map.put(GameEvent.EXPLODE, 15);
      map.put(GameEvent.LIGHTNING_STRIKE, 15);
      map.put(GameEvent.SHULKER_OPEN, 15);
   }));
   public static final EnumProperty<SculkSensorPhase> SCULK_SENSOR_PHASE;
   public static final IntProperty POWER;
   public static final BooleanProperty WATERLOGGED;
   protected static final VoxelShape OUTLINE_SHAPE;
   private final int range;

   public SculkSensorBlock(AbstractBlock.Settings settings, int range) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE)).with(POWER, 0)).with(WATERLOGGED, false));
      this.range = range;
   }

   public int getRange() {
      return this.range;
   }

   @Nullable
   public BlockState getPlacementState(ItemPlacementContext ctx) {
      BlockPos blockPos = ctx.getBlockPos();
      FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
      return (BlockState)this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
   }

   public FluidState getFluidState(BlockState state) {
      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if (getPhase(state) != SculkSensorPhase.ACTIVE) {
         if (getPhase(state) == SculkSensorPhase.COOLDOWN) {
            world.setBlockState(pos, (BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.INACTIVE), Block.NOTIFY_ALL);
         }

      } else {
         setCooldown(world, pos, state);
      }
   }

   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
      if (!world.isClient() && !state.isOf(oldState.getBlock())) {
         if ((Integer)state.get(POWER) > 0 && !world.getBlockTickScheduler().isScheduled(pos, this)) {
            world.setBlockState(pos, (BlockState)state.with(POWER, 0), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
         }

         world.getBlockTickScheduler().schedule(new BlockPos(pos), state.getBlock(), 1);
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         if (getPhase(state) == SculkSensorPhase.ACTIVE) {
            updateNeighbors(world, pos);
         }

         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if ((Boolean)state.get(WATERLOGGED)) {
         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   private static void updateNeighbors(World world, BlockPos pos) {
      world.updateNeighborsAlways(pos, Blocks.SCULK_SENSOR);
      world.updateNeighborsAlways(pos.offset(Direction.UP.getOpposite()), Blocks.SCULK_SENSOR);
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new SculkSensorBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> GameEventListener getGameEventListener(World world, T blockEntity) {
      return blockEntity instanceof SculkSensorBlockEntity ? ((SculkSensorBlockEntity)blockEntity).getEventListener() : null;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
      return !world.isClient ? checkType(type, BlockEntityType.SCULK_SENSOR, (worldx, pos, statex, blockEntity) -> {
         blockEntity.getEventListener().tick(worldx);
      }) : null;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return OUTLINE_SHAPE;
   }

   public boolean emitsRedstonePower(BlockState state) {
      return true;
   }

   public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
      return (Integer)state.get(POWER);
   }

   public static SculkSensorPhase getPhase(BlockState state) {
      return (SculkSensorPhase)state.get(SCULK_SENSOR_PHASE);
   }

   public static boolean isInactive(BlockState state) {
      return getPhase(state) == SculkSensorPhase.INACTIVE;
   }

   public static void setCooldown(World world, BlockPos pos, BlockState state) {
      world.setBlockState(pos, (BlockState)((BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.COOLDOWN)).with(POWER, 0), Block.NOTIFY_ALL);
      world.getBlockTickScheduler().schedule(new BlockPos(pos), state.getBlock(), 1);
      if (!(Boolean)state.get(WATERLOGGED)) {
         world.playSound((PlayerEntity)null, pos, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING_STOP, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
      }

      updateNeighbors(world, pos);
   }

   public static void setActive(World world, BlockPos pos, BlockState state, int power) {
      world.setBlockState(pos, (BlockState)((BlockState)state.with(SCULK_SENSOR_PHASE, SculkSensorPhase.ACTIVE)).with(POWER, power), Block.NOTIFY_ALL);
      world.getBlockTickScheduler().schedule(new BlockPos(pos), state.getBlock(), 40);
      updateNeighbors(world, pos);
      if (!(Boolean)state.get(WATERLOGGED)) {
         world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (getPhase(state) == SculkSensorPhase.ACTIVE) {
         Direction direction = Direction.random(random);
         if (direction != Direction.UP && direction != Direction.DOWN) {
            double d = (double)pos.getX() + 0.5D + (direction.getOffsetX() == 0 ? 0.5D - random.nextDouble() : (double)direction.getOffsetX() * 0.6D);
            double e = (double)pos.getY() + 0.25D;
            double f = (double)pos.getZ() + 0.5D + (direction.getOffsetZ() == 0 ? 0.5D - random.nextDouble() : (double)direction.getOffsetZ() * 0.6D);
            double g = (double)random.nextFloat() * 0.04D;
            world.addParticle(DustColorTransitionParticleEffect.DEFAULT, d, e, f, 0.0D, g, 0.0D);
         }
      }
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(SCULK_SENSOR_PHASE, POWER, WATERLOGGED);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof SculkSensorBlockEntity) {
         SculkSensorBlockEntity sculkSensorBlockEntity = (SculkSensorBlockEntity)blockEntity;
         return getPhase(state) == SculkSensorPhase.ACTIVE ? sculkSensorBlockEntity.getLastVibrationFrequency() : 0;
      } else {
         return 0;
      }
   }

   public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
      return false;
   }

   public boolean hasSidedTransparency(BlockState state) {
      return true;
   }

   static {
      SCULK_SENSOR_PHASE = Properties.SCULK_SENSOR_PHASE;
      POWER = Properties.POWER;
      WATERLOGGED = Properties.WATERLOGGED;
      OUTLINE_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   }
}
