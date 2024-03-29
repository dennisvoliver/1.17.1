package net.minecraft.block;

import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class RedstoneOreBlock extends Block {
   public static final BooleanProperty LIT;

   public RedstoneOreBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)this.getDefaultState().with(LIT, false));
   }

   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
      light(state, world, pos);
      super.onBlockBreakStart(state, world, pos, player);
   }

   public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
      light(state, world, pos);
      super.onSteppedOn(world, pos, state, entity);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      if (world.isClient) {
         spawnParticles(world, pos);
      } else {
         light(state, world, pos);
      }

      ItemStack itemStack = player.getStackInHand(hand);
      return itemStack.getItem() instanceof BlockItem && (new ItemPlacementContext(player, hand, itemStack, hit)).canPlace() ? ActionResult.PASS : ActionResult.SUCCESS;
   }

   private static void light(BlockState state, World world, BlockPos pos) {
      spawnParticles(world, pos);
      if (!(Boolean)state.get(LIT)) {
         world.setBlockState(pos, (BlockState)state.with(LIT, true), Block.NOTIFY_ALL);
      }

   }

   public boolean hasRandomTicks(BlockState state) {
      return (Boolean)state.get(LIT);
   }

   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         world.setBlockState(pos, (BlockState)state.with(LIT, false), Block.NOTIFY_ALL);
      }

   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
      super.onStacksDropped(state, world, pos, stack);
      if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
         int i = 1 + world.random.nextInt(5);
         this.dropExperience(world, pos, i);
      }

   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Boolean)state.get(LIT)) {
         spawnParticles(world, pos);
      }

   }

   private static void spawnParticles(World world, BlockPos pos) {
      double d = 0.5625D;
      Random random = world.random;
      Direction[] var5 = Direction.values();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Direction direction = var5[var7];
         BlockPos blockPos = pos.offset(direction);
         if (!world.getBlockState(blockPos).isOpaqueFullCube(world, blockPos)) {
            Direction.Axis axis = direction.getAxis();
            double e = axis == Direction.Axis.X ? 0.5D + 0.5625D * (double)direction.getOffsetX() : (double)random.nextFloat();
            double f = axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double)direction.getOffsetY() : (double)random.nextFloat();
            double g = axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double)direction.getOffsetZ() : (double)random.nextFloat();
            world.addParticle(DustParticleEffect.DEFAULT, (double)pos.getX() + e, (double)pos.getY() + f, (double)pos.getZ() + g, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(LIT);
   }

   static {
      LIT = RedstoneTorchBlock.LIT;
   }
}
