package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BlockFallingDustParticle extends SpriteBillboardParticle {
   private final float field_3809;
   private final SpriteProvider spriteProvider;

   BlockFallingDustParticle(ClientWorld clientWorld, double d, double e, double f, float g, float h, float i, SpriteProvider spriteProvider) {
      super(clientWorld, d, e, f);
      this.spriteProvider = spriteProvider;
      this.colorRed = g;
      this.colorGreen = h;
      this.colorBlue = i;
      float j = 0.9F;
      this.scale *= 0.67499995F;
      int k = (int)(32.0D / (Math.random() * 0.8D + 0.2D));
      this.maxAge = (int)Math.max((float)k * 0.9F, 1.0F);
      this.setSpriteForAge(spriteProvider);
      this.field_3809 = ((float)Math.random() - 0.5F) * 0.1F;
      this.angle = (float)Math.random() * 6.2831855F;
   }

   public ParticleTextureSheet getType() {
      return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
   }

   public float getSize(float tickDelta) {
      return this.scale * MathHelper.clamp(((float)this.age + tickDelta) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.prevPosX = this.x;
      this.prevPosY = this.y;
      this.prevPosZ = this.z;
      if (this.age++ >= this.maxAge) {
         this.markDead();
      } else {
         this.setSpriteForAge(this.spriteProvider);
         this.prevAngle = this.angle;
         this.angle += 3.1415927F * this.field_3809 * 2.0F;
         if (this.onGround) {
            this.prevAngle = this.angle = 0.0F;
         }

         this.move(this.velocityX, this.velocityY, this.velocityZ);
         this.velocityY -= 0.003000000026077032D;
         this.velocityY = Math.max(this.velocityY, -0.14000000059604645D);
      }
   }

   @Environment(EnvType.CLIENT)
   public static class Factory implements ParticleFactory<BlockStateParticleEffect> {
      private final SpriteProvider spriteProvider;

      public Factory(SpriteProvider spriteProvider) {
         this.spriteProvider = spriteProvider;
      }

      @Nullable
      public Particle createParticle(BlockStateParticleEffect blockStateParticleEffect, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
         BlockState blockState = blockStateParticleEffect.getBlockState();
         if (!blockState.isAir() && blockState.getRenderType() == BlockRenderType.INVISIBLE) {
            return null;
         } else {
            BlockPos blockPos = new BlockPos(d, e, f);
            int j = MinecraftClient.getInstance().getBlockColors().getParticleColor(blockState, clientWorld, blockPos);
            if (blockState.getBlock() instanceof FallingBlock) {
               j = ((FallingBlock)blockState.getBlock()).getColor(blockState, clientWorld, blockPos);
            }

            float k = (float)(j >> 16 & 255) / 255.0F;
            float l = (float)(j >> 8 & 255) / 255.0F;
            float m = (float)(j & 255) / 255.0F;
            return new BlockFallingDustParticle(clientWorld, d, e, f, k, l, m, this.spriteProvider);
         }
      }
   }
}
