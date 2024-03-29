package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class JukeboxBlock extends BlockWithEntity {
   public static final BooleanProperty HAS_RECORD;

   protected JukeboxBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HAS_RECORD, false));
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      super.onPlaced(world, pos, state, placer, itemStack);
      NbtCompound nbtCompound = itemStack.getOrCreateNbt();
      if (nbtCompound.contains("BlockEntityTag")) {
         NbtCompound nbtCompound2 = nbtCompound.getCompound("BlockEntityTag");
         if (nbtCompound2.contains("RecordItem")) {
            world.setBlockState(pos, (BlockState)state.with(HAS_RECORD, true), Block.NOTIFY_LISTENERS);
         }
      }

   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if ((Boolean)state.get(HAS_RECORD)) {
         this.removeRecord(world, pos);
         state = (BlockState)state.with(HAS_RECORD, false);
         world.setBlockState(pos, state, Block.NOTIFY_LISTENERS);
         return ActionResult.success(world.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public void setRecord(WorldAccess world, BlockPos pos, BlockState state, ItemStack stack) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof JukeboxBlockEntity) {
         ((JukeboxBlockEntity)blockEntity).setRecord(stack.copy());
         world.setBlockState(pos, (BlockState)state.with(HAS_RECORD, true), Block.NOTIFY_LISTENERS);
      }
   }

   private void removeRecord(World world, BlockPos pos) {
      if (!world.isClient) {
         BlockEntity blockEntity = world.getBlockEntity(pos);
         if (blockEntity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
            ItemStack itemStack = jukeboxBlockEntity.getRecord();
            if (!itemStack.isEmpty()) {
               world.syncWorldEvent(WorldEvents.MUSIC_DISC_PLAYED, pos, 0);
               jukeboxBlockEntity.clear();
               float f = 0.7F;
               double d = (double)(world.random.nextFloat() * 0.7F) + 0.15000000596046448D;
               double e = (double)(world.random.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
               double g = (double)(world.random.nextFloat() * 0.7F) + 0.15000000596046448D;
               ItemStack itemStack2 = itemStack.copy();
               ItemEntity itemEntity = new ItemEntity(world, (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + g, itemStack2);
               itemEntity.setToDefaultPickupDelay();
               world.spawnEntity(itemEntity);
            }
         }
      }
   }

   public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
      if (!state.isOf(newState.getBlock())) {
         this.removeRecord(world, pos);
         super.onStateReplaced(state, world, pos, newState, moved);
      }
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new JukeboxBlockEntity(pos, state);
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      BlockEntity blockEntity = world.getBlockEntity(pos);
      if (blockEntity instanceof JukeboxBlockEntity) {
         Item item = ((JukeboxBlockEntity)blockEntity).getRecord().getItem();
         if (item instanceof MusicDiscItem) {
            return ((MusicDiscItem)item).getComparatorOutput();
         }
      }

      return 0;
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(HAS_RECORD);
   }

   static {
      HAS_RECORD = Properties.HAS_RECORD;
   }
}
