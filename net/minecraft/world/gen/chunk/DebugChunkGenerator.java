package net.minecraft.world.gen.chunk;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.dynamic.RegistryLookupCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;

public class DebugChunkGenerator extends ChunkGenerator {
   public static final Codec<DebugChunkGenerator> CODEC;
   private static final int field_31467 = 2;
   private static final List<BlockState> BLOCK_STATES;
   private static final int X_SIDE_LENGTH;
   private static final int Z_SIDE_LENGTH;
   protected static final BlockState AIR;
   protected static final BlockState BARRIER;
   public static final int field_31465 = 70;
   public static final int field_31466 = 60;
   private final Registry<Biome> biomeRegistry;

   public DebugChunkGenerator(Registry<Biome> biomeRegistry) {
      super(new FixedBiomeSource((Biome)biomeRegistry.getOrThrow(BiomeKeys.PLAINS)), new StructuresConfig(false));
      this.biomeRegistry = biomeRegistry;
   }

   public Registry<Biome> getBiomeRegistry() {
      return this.biomeRegistry;
   }

   protected Codec<? extends ChunkGenerator> getCodec() {
      return CODEC;
   }

   public ChunkGenerator withSeed(long seed) {
      return this;
   }

   public void buildSurface(ChunkRegion region, Chunk chunk) {
   }

   public void carve(long seed, BiomeAccess access, Chunk chunk, GenerationStep.Carver carver) {
   }

   public void generateFeatures(ChunkRegion region, StructureAccessor accessor) {
      BlockPos.Mutable mutable = new BlockPos.Mutable();
      ChunkPos chunkPos = region.getCenterPos();

      for(int i = 0; i < 16; ++i) {
         for(int j = 0; j < 16; ++j) {
            int k = ChunkSectionPos.getOffsetPos(chunkPos.x, i);
            int l = ChunkSectionPos.getOffsetPos(chunkPos.z, j);
            region.setBlockState(mutable.set(k, 60, l), BARRIER, Block.NOTIFY_LISTENERS);
            BlockState blockState = getBlockState(k, l);
            if (blockState != null) {
               region.setBlockState(mutable.set(k, 70, l), blockState, Block.NOTIFY_LISTENERS);
            }
         }
      }

   }

   public CompletableFuture<Chunk> populateNoise(Executor executor, StructureAccessor accessor, Chunk chunk) {
      return CompletableFuture.completedFuture(chunk);
   }

   public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world) {
      return 0;
   }

   public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world) {
      return new VerticalBlockSample(0, new BlockState[0]);
   }

   public static BlockState getBlockState(int x, int z) {
      BlockState blockState = AIR;
      if (x > 0 && z > 0 && x % 2 != 0 && z % 2 != 0) {
         x /= 2;
         z /= 2;
         if (x <= X_SIDE_LENGTH && z <= Z_SIDE_LENGTH) {
            int i = MathHelper.abs(x * X_SIDE_LENGTH + z);
            if (i < BLOCK_STATES.size()) {
               blockState = (BlockState)BLOCK_STATES.get(i);
            }
         }
      }

      return blockState;
   }

   static {
      CODEC = RegistryLookupCodec.of(Registry.BIOME_KEY).xmap(DebugChunkGenerator::new, DebugChunkGenerator::getBiomeRegistry).stable().codec();
      BLOCK_STATES = (List)StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap((block) -> {
         return block.getStateManager().getStates().stream();
      }).collect(Collectors.toList());
      X_SIDE_LENGTH = MathHelper.ceil(MathHelper.sqrt((float)BLOCK_STATES.size()));
      Z_SIDE_LENGTH = MathHelper.ceil((float)BLOCK_STATES.size() / (float)X_SIDE_LENGTH);
      AIR = Blocks.AIR.getDefaultState();
      BARRIER = Blocks.BARRIER.getDefaultState();
   }
}
