package net.minecraft.block;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class InfestedBlock extends Block {
   private final Block regularBlock;
   private static final Map<Block, Block> REGULAR_TO_INFESTED_BLOCK = Maps.newIdentityHashMap();
   private static final Map<BlockState, BlockState> REGULAR_TO_INFESTED_STATE = Maps.newIdentityHashMap();
   private static final Map<BlockState, BlockState> INFESTED_TO_REGULAR_STATE = Maps.newIdentityHashMap();

   /**
    * Creates an infested block
    * 
    * @param regularBlock the block this infested block should mimic
    * @param settings block settings
    */
   public InfestedBlock(Block regularBlock, AbstractBlock.Settings settings) {
      super(settings.hardness(regularBlock.getHardness() / 2.0F).resistance(0.75F));
      this.regularBlock = regularBlock;
      REGULAR_TO_INFESTED_BLOCK.put(regularBlock, this);
   }

   public Block getRegularBlock() {
      return this.regularBlock;
   }

   public static boolean isInfestable(BlockState block) {
      return REGULAR_TO_INFESTED_BLOCK.containsKey(block.getBlock());
   }

   private void spawnSilverfish(ServerWorld world, BlockPos pos) {
      SilverfishEntity silverfishEntity = (SilverfishEntity)EntityType.SILVERFISH.create(world);
      silverfishEntity.refreshPositionAndAngles((double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, 0.0F, 0.0F);
      world.spawnEntity(silverfishEntity);
      silverfishEntity.playSpawnEffects();
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
      super.onStacksDropped(state, world, pos, stack);
      if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0) {
         this.spawnSilverfish(world, pos);
      }

   }

   public void onDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
      if (world instanceof ServerWorld) {
         this.spawnSilverfish((ServerWorld)world, pos);
      }

   }

   public static BlockState fromRegularState(BlockState regularState) {
      return copyProperties(REGULAR_TO_INFESTED_STATE, regularState, () -> {
         return ((Block)REGULAR_TO_INFESTED_BLOCK.get(regularState.getBlock())).getDefaultState();
      });
   }

   public BlockState toRegularState(BlockState infestedState) {
      return copyProperties(INFESTED_TO_REGULAR_STATE, infestedState, () -> {
         return this.getRegularBlock().getDefaultState();
      });
   }

   private static BlockState copyProperties(Map<BlockState, BlockState> stateMap, BlockState fromState, Supplier<BlockState> toStateSupplier) {
      return (BlockState)stateMap.computeIfAbsent(fromState, (infestedState) -> {
         BlockState blockState = (BlockState)toStateSupplier.get();

         Property property;
         for(Iterator var3 = infestedState.getProperties().iterator(); var3.hasNext(); blockState = blockState.contains(property) ? (BlockState)blockState.with(property, infestedState.get(property)) : blockState) {
            property = (Property)var3.next();
         }

         return blockState;
      });
   }
}
