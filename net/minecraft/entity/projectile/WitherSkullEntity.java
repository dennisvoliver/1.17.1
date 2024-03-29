package net.minecraft.entity.projectile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class WitherSkullEntity extends ExplosiveProjectileEntity {
   private static final TrackedData<Boolean> CHARGED;

   public WitherSkullEntity(EntityType<? extends WitherSkullEntity> entityType, World world) {
      super(entityType, world);
   }

   public WitherSkullEntity(World world, LivingEntity owner, double directionX, double directionY, double directionZ) {
      super(EntityType.WITHER_SKULL, owner, directionX, directionY, directionZ, world);
   }

   protected float getDrag() {
      return this.isCharged() ? 0.73F : super.getDrag();
   }

   public boolean isOnFire() {
      return false;
   }

   public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
      return this.isCharged() && WitherEntity.canDestroy(blockState) ? Math.min(0.8F, max) : max;
   }

   protected void onEntityHit(EntityHitResult entityHitResult) {
      super.onEntityHit(entityHitResult);
      if (!this.world.isClient) {
         Entity entity = entityHitResult.getEntity();
         Entity entity2 = this.getOwner();
         boolean bl2;
         if (entity2 instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity2;
            bl2 = entity.damage(DamageSource.witherSkull(this, livingEntity), 8.0F);
            if (bl2) {
               if (entity.isAlive()) {
                  this.applyDamageEffects(livingEntity, entity);
               } else {
                  livingEntity.heal(5.0F);
               }
            }
         } else {
            bl2 = entity.damage(DamageSource.MAGIC, 5.0F);
         }

         if (bl2 && entity instanceof LivingEntity) {
            int i = 0;
            if (this.world.getDifficulty() == Difficulty.NORMAL) {
               i = 10;
            } else if (this.world.getDifficulty() == Difficulty.HARD) {
               i = 40;
            }

            if (i > 0) {
               ((LivingEntity)entity).addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 20 * i, 1), this.getEffectCause());
            }
         }

      }
   }

   protected void onCollision(HitResult hitResult) {
      super.onCollision(hitResult);
      if (!this.world.isClient) {
         Explosion.DestructionType destructionType = this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING) ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.NONE;
         this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, destructionType);
         this.discard();
      }

   }

   public boolean collides() {
      return false;
   }

   public boolean damage(DamageSource source, float amount) {
      return false;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(CHARGED, false);
   }

   public boolean isCharged() {
      return (Boolean)this.dataTracker.get(CHARGED);
   }

   public void setCharged(boolean charged) {
      this.dataTracker.set(CHARGED, charged);
   }

   protected boolean isBurning() {
      return false;
   }

   static {
      CHARGED = DataTracker.registerData(WitherSkullEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }
}
