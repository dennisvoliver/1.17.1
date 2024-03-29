package net.minecraft.client.render.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

@Environment(EnvType.CLIENT)
public class FluidRenderer {
   private static final float field_32781 = 0.8888889F;
   private final Sprite[] lavaSprites = new Sprite[2];
   private final Sprite[] waterSprites = new Sprite[2];
   private Sprite waterOverlaySprite;

   protected void onResourceReload() {
      this.lavaSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.LAVA.getDefaultState()).getParticleSprite();
      this.lavaSprites[1] = ModelLoader.LAVA_FLOW.getSprite();
      this.waterSprites[0] = MinecraftClient.getInstance().getBakedModelManager().getBlockModels().getModel(Blocks.WATER.getDefaultState()).getParticleSprite();
      this.waterSprites[1] = ModelLoader.WATER_FLOW.getSprite();
      this.waterOverlaySprite = ModelLoader.WATER_OVERLAY.getSprite();
   }

   private static boolean isSameFluid(BlockView world, BlockPos pos, Direction side, FluidState state) {
      BlockPos blockPos = pos.offset(side);
      FluidState fluidState = world.getFluidState(blockPos);
      return fluidState.getFluid().matchesType(state.getFluid());
   }

   private static boolean isSideCovered(BlockView world, Direction direction, float f, BlockPos pos, BlockState state) {
      if (state.isOpaque()) {
         VoxelShape voxelShape = VoxelShapes.cuboid(0.0D, 0.0D, 0.0D, 1.0D, (double)f, 1.0D);
         VoxelShape voxelShape2 = state.getCullingShape(world, pos);
         return VoxelShapes.isSideCovered(voxelShape, voxelShape2, direction);
      } else {
         return false;
      }
   }

   private static boolean isSideCovered(BlockView world, BlockPos pos, Direction direction, float maxDeviation) {
      BlockPos blockPos = pos.offset(direction);
      BlockState blockState = world.getBlockState(blockPos);
      return isSideCovered(world, direction, maxDeviation, blockPos, blockState);
   }

   private static boolean isOppositeSideCovered(BlockView world, BlockPos pos, BlockState state, Direction direction) {
      return isSideCovered(world, direction.getOpposite(), 1.0F, pos, state);
   }

   public static boolean method_29708(BlockRenderView world, BlockPos pos, FluidState state, BlockState blockState, Direction direction) {
      return !isOppositeSideCovered(world, pos, blockState, direction) && !isSameFluid(world, pos, direction, state);
   }

   public boolean render(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer, FluidState state) {
      boolean bl = state.isIn(FluidTags.LAVA);
      Sprite[] sprites = bl ? this.lavaSprites : this.waterSprites;
      BlockState blockState = world.getBlockState(pos);
      int i = bl ? 16777215 : BiomeColors.getWaterColor(world, pos);
      float f = (float)(i >> 16 & 255) / 255.0F;
      float g = (float)(i >> 8 & 255) / 255.0F;
      float h = (float)(i & 255) / 255.0F;
      boolean bl2 = !isSameFluid(world, pos, Direction.UP, state);
      boolean bl3 = method_29708(world, pos, state, blockState, Direction.DOWN) && !isSideCovered(world, pos, Direction.DOWN, 0.8888889F);
      boolean bl4 = method_29708(world, pos, state, blockState, Direction.NORTH);
      boolean bl5 = method_29708(world, pos, state, blockState, Direction.SOUTH);
      boolean bl6 = method_29708(world, pos, state, blockState, Direction.WEST);
      boolean bl7 = method_29708(world, pos, state, blockState, Direction.EAST);
      if (!bl2 && !bl3 && !bl7 && !bl6 && !bl4 && !bl5) {
         return false;
      } else {
         boolean bl8 = false;
         float j = world.getBrightness(Direction.DOWN, true);
         float k = world.getBrightness(Direction.UP, true);
         float l = world.getBrightness(Direction.NORTH, true);
         float m = world.getBrightness(Direction.WEST, true);
         float n = this.getNorthWestCornerFluidHeight(world, pos, state.getFluid());
         float o = this.getNorthWestCornerFluidHeight(world, pos.south(), state.getFluid());
         float p = this.getNorthWestCornerFluidHeight(world, pos.east().south(), state.getFluid());
         float q = this.getNorthWestCornerFluidHeight(world, pos.east(), state.getFluid());
         double d = (double)(pos.getX() & 15);
         double e = (double)(pos.getY() & 15);
         double r = (double)(pos.getZ() & 15);
         float s = 0.001F;
         float t = bl3 ? 0.001F : 0.0F;
         float ag;
         float ai;
         float ca;
         float cb;
         float aj;
         float al;
         float an;
         float cg;
         float ch;
         if (bl2 && !isSideCovered(world, pos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
            bl8 = true;
            n -= 0.001F;
            o -= 0.001F;
            p -= 0.001F;
            q -= 0.001F;
            Vec3d vec3d = state.getVelocity(world, pos);
            float ah;
            Sprite sprite2;
            float ap;
            float aq;
            float ar;
            float as;
            if (vec3d.x == 0.0D && vec3d.z == 0.0D) {
               sprite2 = sprites[0];
               ag = sprite2.getFrameU(0.0D);
               ah = sprite2.getFrameV(0.0D);
               ai = ag;
               aj = sprite2.getFrameV(16.0D);
               ca = sprite2.getFrameU(16.0D);
               al = aj;
               cb = ca;
               an = ah;
            } else {
               sprite2 = sprites[1];
               ap = (float)MathHelper.atan2(vec3d.z, vec3d.x) - 1.5707964F;
               aq = MathHelper.sin(ap) * 0.25F;
               ar = MathHelper.cos(ap) * 0.25F;
               as = 8.0F;
               ag = sprite2.getFrameU((double)(8.0F + (-ar - aq) * 16.0F));
               ah = sprite2.getFrameV((double)(8.0F + (-ar + aq) * 16.0F));
               ai = sprite2.getFrameU((double)(8.0F + (-ar + aq) * 16.0F));
               aj = sprite2.getFrameV((double)(8.0F + (ar + aq) * 16.0F));
               ca = sprite2.getFrameU((double)(8.0F + (ar + aq) * 16.0F));
               al = sprite2.getFrameV((double)(8.0F + (ar - aq) * 16.0F));
               cb = sprite2.getFrameU((double)(8.0F + (ar - aq) * 16.0F));
               an = sprite2.getFrameV((double)(8.0F + (-ar - aq) * 16.0F));
            }

            float ao = (ag + ai + ca + cb) / 4.0F;
            ap = (ah + aj + al + an) / 4.0F;
            aq = (float)sprites[0].getWidth() / (sprites[0].getMaxU() - sprites[0].getMinU());
            ar = (float)sprites[0].getHeight() / (sprites[0].getMaxV() - sprites[0].getMinV());
            as = 4.0F / Math.max(ar, aq);
            ag = MathHelper.lerp(as, ag, ao);
            ai = MathHelper.lerp(as, ai, ao);
            ca = MathHelper.lerp(as, ca, ao);
            cb = MathHelper.lerp(as, cb, ao);
            ah = MathHelper.lerp(as, ah, ap);
            aj = MathHelper.lerp(as, aj, ap);
            al = MathHelper.lerp(as, al, ap);
            an = MathHelper.lerp(as, an, ap);
            int at = this.getLight(world, pos);
            float au = k * f;
            cg = k * g;
            ch = k * h;
            this.vertex(vertexConsumer, d + 0.0D, e + (double)n, r + 0.0D, au, cg, ch, ag, ah, at);
            this.vertex(vertexConsumer, d + 0.0D, e + (double)o, r + 1.0D, au, cg, ch, ai, aj, at);
            this.vertex(vertexConsumer, d + 1.0D, e + (double)p, r + 1.0D, au, cg, ch, ca, al, at);
            this.vertex(vertexConsumer, d + 1.0D, e + (double)q, r + 0.0D, au, cg, ch, cb, an, at);
            if (state.method_15756(world, pos.up())) {
               this.vertex(vertexConsumer, d + 0.0D, e + (double)n, r + 0.0D, au, cg, ch, ag, ah, at);
               this.vertex(vertexConsumer, d + 1.0D, e + (double)q, r + 0.0D, au, cg, ch, cb, an, at);
               this.vertex(vertexConsumer, d + 1.0D, e + (double)p, r + 1.0D, au, cg, ch, ca, al, at);
               this.vertex(vertexConsumer, d + 0.0D, e + (double)o, r + 1.0D, au, cg, ch, ai, aj, at);
            }
         }

         if (bl3) {
            ag = sprites[0].getMinU();
            ai = sprites[0].getMaxU();
            ca = sprites[0].getMinV();
            cb = sprites[0].getMaxV();
            int bb = this.getLight(world, pos.down());
            aj = j * f;
            al = j * g;
            an = j * h;
            this.vertex(vertexConsumer, d, e + (double)t, r + 1.0D, aj, al, an, ag, cb, bb);
            this.vertex(vertexConsumer, d, e + (double)t, r, aj, al, an, ag, ca, bb);
            this.vertex(vertexConsumer, d + 1.0D, e + (double)t, r, aj, al, an, ai, ca, bb);
            this.vertex(vertexConsumer, d + 1.0D, e + (double)t, r + 1.0D, aj, al, an, ai, cb, bb);
            bl8 = true;
         }

         int bf = this.getLight(world, pos);

         for(int bg = 0; bg < 4; ++bg) {
            double cc;
            double ce;
            double cd;
            double cf;
            Direction direction3;
            boolean bl12;
            if (bg == 0) {
               ca = n;
               cb = q;
               cc = d;
               cd = d + 1.0D;
               ce = r + 0.0010000000474974513D;
               cf = r + 0.0010000000474974513D;
               direction3 = Direction.NORTH;
               bl12 = bl4;
            } else if (bg == 1) {
               ca = p;
               cb = o;
               cc = d + 1.0D;
               cd = d;
               ce = r + 1.0D - 0.0010000000474974513D;
               cf = r + 1.0D - 0.0010000000474974513D;
               direction3 = Direction.SOUTH;
               bl12 = bl5;
            } else if (bg == 2) {
               ca = o;
               cb = n;
               cc = d + 0.0010000000474974513D;
               cd = d + 0.0010000000474974513D;
               ce = r + 1.0D;
               cf = r;
               direction3 = Direction.WEST;
               bl12 = bl6;
            } else {
               ca = q;
               cb = p;
               cc = d + 1.0D - 0.0010000000474974513D;
               cd = d + 1.0D - 0.0010000000474974513D;
               ce = r;
               cf = r + 1.0D;
               direction3 = Direction.EAST;
               bl12 = bl7;
            }

            if (bl12 && !isSideCovered(world, pos, direction3, Math.max(ca, cb))) {
               bl8 = true;
               BlockPos blockPos = pos.offset(direction3);
               Sprite sprite3 = sprites[1];
               if (!bl) {
                  Block block = world.getBlockState(blockPos).getBlock();
                  if (block instanceof TransparentBlock || block instanceof LeavesBlock) {
                     sprite3 = this.waterOverlaySprite;
                  }
               }

               cg = sprite3.getFrameU(0.0D);
               ch = sprite3.getFrameU(8.0D);
               float ci = sprite3.getFrameV((double)((1.0F - ca) * 16.0F * 0.5F));
               float cj = sprite3.getFrameV((double)((1.0F - cb) * 16.0F * 0.5F));
               float ck = sprite3.getFrameV(8.0D);
               float cl = bg < 2 ? l : m;
               float cm = k * cl * f;
               float cn = k * cl * g;
               float co = k * cl * h;
               this.vertex(vertexConsumer, cc, e + (double)ca, ce, cm, cn, co, cg, ci, bf);
               this.vertex(vertexConsumer, cd, e + (double)cb, cf, cm, cn, co, ch, cj, bf);
               this.vertex(vertexConsumer, cd, e + (double)t, cf, cm, cn, co, ch, ck, bf);
               this.vertex(vertexConsumer, cc, e + (double)t, ce, cm, cn, co, cg, ck, bf);
               if (sprite3 != this.waterOverlaySprite) {
                  this.vertex(vertexConsumer, cc, e + (double)t, ce, cm, cn, co, cg, ck, bf);
                  this.vertex(vertexConsumer, cd, e + (double)t, cf, cm, cn, co, ch, ck, bf);
                  this.vertex(vertexConsumer, cd, e + (double)cb, cf, cm, cn, co, ch, cj, bf);
                  this.vertex(vertexConsumer, cc, e + (double)ca, ce, cm, cn, co, cg, ci, bf);
               }
            }
         }

         return bl8;
      }
   }

   private void vertex(VertexConsumer vertexConsumer, double x, double y, double z, float red, float green, float blue, float u, float v, int light) {
      vertexConsumer.vertex(x, y, z).color(red, green, blue, 1.0F).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F).next();
   }

   private int getLight(BlockRenderView world, BlockPos pos) {
      int i = WorldRenderer.getLightmapCoordinates(world, pos);
      int j = WorldRenderer.getLightmapCoordinates(world, pos.up());
      int k = i & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      int l = j & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      int m = i >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      int n = j >> 16 & (LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE | 15);
      return (k > l ? k : l) | (m > n ? m : n) << 16;
   }

   private float getNorthWestCornerFluidHeight(BlockView world, BlockPos pos, Fluid fluid) {
      int i = 0;
      float f = 0.0F;

      for(int j = 0; j < 4; ++j) {
         BlockPos blockPos = pos.add(-(j & 1), 0, -(j >> 1 & 1));
         if (world.getFluidState(blockPos.up()).getFluid().matchesType(fluid)) {
            return 1.0F;
         }

         FluidState fluidState = world.getFluidState(blockPos);
         if (fluidState.getFluid().matchesType(fluid)) {
            float g = fluidState.getHeight(world, blockPos);
            if (g >= 0.8F) {
               f += g * 10.0F;
               i += 10;
            } else {
               f += g;
               ++i;
            }
         } else if (!world.getBlockState(blockPos).getMaterial().isSolid()) {
            ++i;
         }
      }

      return f / (float)i;
   }
}
