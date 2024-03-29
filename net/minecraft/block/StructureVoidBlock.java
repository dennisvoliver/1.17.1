package net.minecraft.block;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class StructureVoidBlock extends Block {
   private static final double field_31257 = 5.0D;
   private static final VoxelShape SHAPE = Block.createCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);

   protected StructureVoidBlock(AbstractBlock.Settings settings) {
      super(settings);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.INVISIBLE;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return SHAPE;
   }

   public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
      return 1.0F;
   }

   public PistonBehavior getPistonBehavior(BlockState state) {
      return PistonBehavior.DESTROY;
   }
}
