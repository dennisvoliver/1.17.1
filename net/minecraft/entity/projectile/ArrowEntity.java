package net.minecraft.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ArrowEntity extends PersistentProjectileEntity {
   private static final int field_30660 = 600;
   private static final int field_30658 = -1;
   private static final TrackedData<Integer> COLOR;
   private static final byte field_30659 = 0;
   private Potion potion;
   private final Set<StatusEffectInstance> effects;
   private boolean colorSet;

   public ArrowEntity(EntityType<? extends ArrowEntity> entityType, World world) {
      super(entityType, world);
      this.potion = Potions.EMPTY;
      this.effects = Sets.newHashSet();
   }

   public ArrowEntity(World world, double x, double y, double z) {
      super(EntityType.ARROW, x, y, z, world);
      this.potion = Potions.EMPTY;
      this.effects = Sets.newHashSet();
   }

   public ArrowEntity(World world, LivingEntity owner) {
      super(EntityType.ARROW, owner, world);
      this.potion = Potions.EMPTY;
      this.effects = Sets.newHashSet();
   }

   public void initFromStack(ItemStack stack) {
      if (stack.isOf(Items.TIPPED_ARROW)) {
         this.potion = PotionUtil.getPotion(stack);
         Collection<StatusEffectInstance> collection = PotionUtil.getCustomPotionEffects(stack);
         if (!collection.isEmpty()) {
            Iterator var3 = collection.iterator();

            while(var3.hasNext()) {
               StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var3.next();
               this.effects.add(new StatusEffectInstance(statusEffectInstance));
            }
         }

         int i = getCustomPotionColor(stack);
         if (i == -1) {
            this.initColor();
         } else {
            this.setColor(i);
         }
      } else if (stack.isOf(Items.ARROW)) {
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.dataTracker.set(COLOR, -1);
      }

   }

   public static int getCustomPotionColor(ItemStack stack) {
      NbtCompound nbtCompound = stack.getNbt();
      return nbtCompound != null && nbtCompound.contains("CustomPotionColor", 99) ? nbtCompound.getInt("CustomPotionColor") : -1;
   }

   private void initColor() {
      this.colorSet = false;
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.dataTracker.set(COLOR, -1);
      } else {
         this.dataTracker.set(COLOR, PotionUtil.getColor((Collection)PotionUtil.getPotionEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(StatusEffectInstance effect) {
      this.effects.add(effect);
      this.getDataTracker().set(COLOR, PotionUtil.getColor((Collection)PotionUtil.getPotionEffects(this.potion, this.effects)));
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(COLOR, -1);
   }

   public void tick() {
      super.tick();
      if (this.world.isClient) {
         if (this.inGround) {
            if (this.inGroundTime % 5 == 0) {
               this.spawnParticles(1);
            }
         } else {
            this.spawnParticles(2);
         }
      } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
         this.world.sendEntityStatus(this, (byte)0);
         this.potion = Potions.EMPTY;
         this.effects.clear();
         this.dataTracker.set(COLOR, -1);
      }

   }

   private void spawnParticles(int amount) {
      int i = this.getColor();
      if (i != -1 && amount > 0) {
         double d = (double)(i >> 16 & 255) / 255.0D;
         double e = (double)(i >> 8 & 255) / 255.0D;
         double f = (double)(i >> 0 & 255) / 255.0D;

         for(int j = 0; j < amount; ++j) {
            this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5D), this.getRandomBodyY(), this.getParticleZ(0.5D), d, e, f);
         }

      }
   }

   public int getColor() {
      return (Integer)this.dataTracker.get(COLOR);
   }

   private void setColor(int color) {
      this.colorSet = true;
      this.dataTracker.set(COLOR, color);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      if (this.potion != Potions.EMPTY) {
         nbt.putString("Potion", Registry.POTION.getId(this.potion).toString());
      }

      if (this.colorSet) {
         nbt.putInt("Color", this.getColor());
      }

      if (!this.effects.isEmpty()) {
         NbtList nbtList = new NbtList();
         Iterator var3 = this.effects.iterator();

         while(var3.hasNext()) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var3.next();
            nbtList.add(statusEffectInstance.writeNbt(new NbtCompound()));
         }

         nbt.put("CustomPotionEffects", nbtList);
      }

   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      if (nbt.contains("Potion", 8)) {
         this.potion = PotionUtil.getPotion(nbt);
      }

      Iterator var2 = PotionUtil.getCustomPotionEffects(nbt).iterator();

      while(var2.hasNext()) {
         StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var2.next();
         this.addEffect(statusEffectInstance);
      }

      if (nbt.contains("Color", 99)) {
         this.setColor(nbt.getInt("Color"));
      } else {
         this.initColor();
      }

   }

   protected void onHit(LivingEntity target) {
      super.onHit(target);
      Entity entity = this.getEffectCause();
      Iterator var3 = this.potion.getEffects().iterator();

      StatusEffectInstance statusEffectInstance2;
      while(var3.hasNext()) {
         statusEffectInstance2 = (StatusEffectInstance)var3.next();
         target.addStatusEffect(new StatusEffectInstance(statusEffectInstance2.getEffectType(), Math.max(statusEffectInstance2.getDuration() / 8, 1), statusEffectInstance2.getAmplifier(), statusEffectInstance2.isAmbient(), statusEffectInstance2.shouldShowParticles()), entity);
      }

      if (!this.effects.isEmpty()) {
         var3 = this.effects.iterator();

         while(var3.hasNext()) {
            statusEffectInstance2 = (StatusEffectInstance)var3.next();
            target.addStatusEffect(statusEffectInstance2, entity);
         }
      }

   }

   protected ItemStack asItemStack() {
      if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
         return new ItemStack(Items.ARROW);
      } else {
         ItemStack itemStack = new ItemStack(Items.TIPPED_ARROW);
         PotionUtil.setPotion(itemStack, this.potion);
         PotionUtil.setCustomPotionEffects(itemStack, this.effects);
         if (this.colorSet) {
            itemStack.getOrCreateNbt().putInt("CustomPotionColor", this.getColor());
         }

         return itemStack;
      }
   }

   public void handleStatus(byte status) {
      if (status == 0) {
         int i = this.getColor();
         if (i != -1) {
            double d = (double)(i >> 16 & 255) / 255.0D;
            double e = (double)(i >> 8 & 255) / 255.0D;
            double f = (double)(i >> 0 & 255) / 255.0D;

            for(int j = 0; j < 20; ++j) {
               this.world.addParticle(ParticleTypes.ENTITY_EFFECT, this.getParticleX(0.5D), this.getRandomBodyY(), this.getParticleZ(0.5D), d, e, f);
            }
         }
      } else {
         super.handleStatus(status);
      }

   }

   static {
      COLOR = DataTracker.registerData(ArrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
   }
}
