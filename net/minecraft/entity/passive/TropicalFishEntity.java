package net.minecraft.entity.passive;

import java.util.Locale;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TropicalFishEntity extends SchoolingFishEntity {
   public static final String BUCKET_VARIANT_TAG_KEY = "BucketVariantTag";
   private static final TrackedData<Integer> VARIANT;
   public static final int field_30380 = 0;
   public static final int field_30383 = 1;
   private static final int field_30379 = 2;
   private static final Identifier[] SHAPE_IDS;
   private static final Identifier[] SMALL_FISH_VARIETY_IDS;
   private static final Identifier[] LARGE_FISH_VARIETY_IDS;
   private static final int field_30381 = 6;
   private static final int field_30382 = 15;
   public static final int[] COMMON_VARIANTS;
   private boolean commonSpawn = true;

   private static int toVariant(TropicalFishEntity.Variety variety, DyeColor baseColor, DyeColor patternColor) {
      return variety.getShape() & 255 | (variety.getPattern() & 255) << 8 | (baseColor.getId() & 255) << 16 | (patternColor.getId() & 255) << 24;
   }

   public TropicalFishEntity(EntityType<? extends TropicalFishEntity> entityType, World world) {
      super(entityType, world);
   }

   public static String getToolTipForVariant(int variant) {
      return "entity.minecraft.tropical_fish.predefined." + variant;
   }

   public static DyeColor getBaseDyeColor(int variant) {
      return DyeColor.byId(getBaseDyeColorIndex(variant));
   }

   public static DyeColor getPatternDyeColor(int variant) {
      return DyeColor.byId(getPatternDyeColorIndex(variant));
   }

   public static String getTranslationKey(int variant) {
      int i = getShape(variant);
      int j = getPattern(variant);
      String var10000 = TropicalFishEntity.Variety.getTranslateKey(i, j);
      return "entity.minecraft.tropical_fish.type." + var10000;
   }

   protected void initDataTracker() {
      super.initDataTracker();
      this.dataTracker.startTracking(VARIANT, 0);
   }

   public void writeCustomDataToNbt(NbtCompound nbt) {
      super.writeCustomDataToNbt(nbt);
      nbt.putInt("Variant", this.getVariant());
   }

   public void readCustomDataFromNbt(NbtCompound nbt) {
      super.readCustomDataFromNbt(nbt);
      this.setVariant(nbt.getInt("Variant"));
   }

   public void setVariant(int variant) {
      this.dataTracker.set(VARIANT, variant);
   }

   public boolean spawnsTooManyForEachTry(int count) {
      return !this.commonSpawn;
   }

   public int getVariant() {
      return (Integer)this.dataTracker.get(VARIANT);
   }

   public void copyDataToStack(ItemStack stack) {
      super.copyDataToStack(stack);
      NbtCompound nbtCompound = stack.getOrCreateNbt();
      nbtCompound.putInt("BucketVariantTag", this.getVariant());
   }

   public ItemStack getBucketItem() {
      return new ItemStack(Items.TROPICAL_FISH_BUCKET);
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_TROPICAL_FISH_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_TROPICAL_FISH_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return SoundEvents.ENTITY_TROPICAL_FISH_HURT;
   }

   protected SoundEvent getFlopSound() {
      return SoundEvents.ENTITY_TROPICAL_FISH_FLOP;
   }

   private static int getBaseDyeColorIndex(int variant) {
      return (variant & 16711680) >> 16;
   }

   public float[] getBaseColorComponents() {
      return DyeColor.byId(getBaseDyeColorIndex(this.getVariant())).getColorComponents();
   }

   private static int getPatternDyeColorIndex(int variant) {
      return (variant & -16777216) >> 24;
   }

   public float[] getPatternColorComponents() {
      return DyeColor.byId(getPatternDyeColorIndex(this.getVariant())).getColorComponents();
   }

   public static int getShape(int variant) {
      return Math.min(variant & 255, 1);
   }

   public int getShape() {
      return getShape(this.getVariant());
   }

   private static int getPattern(int variant) {
      return Math.min((variant & '\uff00') >> 8, 5);
   }

   public Identifier getVarietyId() {
      return getShape(this.getVariant()) == 0 ? SMALL_FISH_VARIETY_IDS[getPattern(this.getVariant())] : LARGE_FISH_VARIETY_IDS[getPattern(this.getVariant())];
   }

   public Identifier getShapeId() {
      return SHAPE_IDS[getShape(this.getVariant())];
   }

   @Nullable
   public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
      EntityData entityData = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
      if (spawnReason == SpawnReason.BUCKET && entityNbt != null && entityNbt.contains("BucketVariantTag", 3)) {
         this.setVariant(entityNbt.getInt("BucketVariantTag"));
         return (EntityData)entityData;
      } else {
         int n;
         int o;
         int p;
         int q;
         if (entityData instanceof TropicalFishEntity.TropicalFishData) {
            TropicalFishEntity.TropicalFishData tropicalFishData = (TropicalFishEntity.TropicalFishData)entityData;
            n = tropicalFishData.shape;
            o = tropicalFishData.pattern;
            p = tropicalFishData.baseColor;
            q = tropicalFishData.patternColor;
         } else if ((double)this.random.nextFloat() < 0.9D) {
            int m = Util.getRandom(COMMON_VARIANTS, this.random);
            n = m & 255;
            o = (m & '\uff00') >> 8;
            p = (m & 16711680) >> 16;
            q = (m & -16777216) >> 24;
            entityData = new TropicalFishEntity.TropicalFishData(this, n, o, p, q);
         } else {
            this.commonSpawn = false;
            n = this.random.nextInt(2);
            o = this.random.nextInt(6);
            p = this.random.nextInt(15);
            q = this.random.nextInt(15);
         }

         this.setVariant(n | o << 8 | p << 16 | q << 24);
         return (EntityData)entityData;
      }
   }

   static {
      VARIANT = DataTracker.registerData(TropicalFishEntity.class, TrackedDataHandlerRegistry.INTEGER);
      SHAPE_IDS = new Identifier[]{new Identifier("textures/entity/fish/tropical_a.png"), new Identifier("textures/entity/fish/tropical_b.png")};
      SMALL_FISH_VARIETY_IDS = new Identifier[]{new Identifier("textures/entity/fish/tropical_a_pattern_1.png"), new Identifier("textures/entity/fish/tropical_a_pattern_2.png"), new Identifier("textures/entity/fish/tropical_a_pattern_3.png"), new Identifier("textures/entity/fish/tropical_a_pattern_4.png"), new Identifier("textures/entity/fish/tropical_a_pattern_5.png"), new Identifier("textures/entity/fish/tropical_a_pattern_6.png")};
      LARGE_FISH_VARIETY_IDS = new Identifier[]{new Identifier("textures/entity/fish/tropical_b_pattern_1.png"), new Identifier("textures/entity/fish/tropical_b_pattern_2.png"), new Identifier("textures/entity/fish/tropical_b_pattern_3.png"), new Identifier("textures/entity/fish/tropical_b_pattern_4.png"), new Identifier("textures/entity/fish/tropical_b_pattern_5.png"), new Identifier("textures/entity/fish/tropical_b_pattern_6.png")};
      COMMON_VARIANTS = new int[]{toVariant(TropicalFishEntity.Variety.STRIPEY, DyeColor.ORANGE, DyeColor.GRAY), toVariant(TropicalFishEntity.Variety.FLOPPER, DyeColor.GRAY, DyeColor.GRAY), toVariant(TropicalFishEntity.Variety.FLOPPER, DyeColor.GRAY, DyeColor.BLUE), toVariant(TropicalFishEntity.Variety.CLAYFISH, DyeColor.WHITE, DyeColor.GRAY), toVariant(TropicalFishEntity.Variety.SUNSTREAK, DyeColor.BLUE, DyeColor.GRAY), toVariant(TropicalFishEntity.Variety.KOB, DyeColor.ORANGE, DyeColor.WHITE), toVariant(TropicalFishEntity.Variety.SPOTTY, DyeColor.PINK, DyeColor.LIGHT_BLUE), toVariant(TropicalFishEntity.Variety.BLOCKFISH, DyeColor.PURPLE, DyeColor.YELLOW), toVariant(TropicalFishEntity.Variety.CLAYFISH, DyeColor.WHITE, DyeColor.RED), toVariant(TropicalFishEntity.Variety.SPOTTY, DyeColor.WHITE, DyeColor.YELLOW), toVariant(TropicalFishEntity.Variety.GLITTER, DyeColor.WHITE, DyeColor.GRAY), toVariant(TropicalFishEntity.Variety.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE), toVariant(TropicalFishEntity.Variety.DASHER, DyeColor.CYAN, DyeColor.PINK), toVariant(TropicalFishEntity.Variety.BRINELY, DyeColor.LIME, DyeColor.LIGHT_BLUE), toVariant(TropicalFishEntity.Variety.BETTY, DyeColor.RED, DyeColor.WHITE), toVariant(TropicalFishEntity.Variety.SNOOPER, DyeColor.GRAY, DyeColor.RED), toVariant(TropicalFishEntity.Variety.BLOCKFISH, DyeColor.RED, DyeColor.WHITE), toVariant(TropicalFishEntity.Variety.FLOPPER, DyeColor.WHITE, DyeColor.YELLOW), toVariant(TropicalFishEntity.Variety.KOB, DyeColor.RED, DyeColor.WHITE), toVariant(TropicalFishEntity.Variety.SUNSTREAK, DyeColor.GRAY, DyeColor.WHITE), toVariant(TropicalFishEntity.Variety.DASHER, DyeColor.CYAN, DyeColor.YELLOW), toVariant(TropicalFishEntity.Variety.FLOPPER, DyeColor.YELLOW, DyeColor.YELLOW)};
   }

   static enum Variety {
      KOB(0, 0),
      SUNSTREAK(0, 1),
      SNOOPER(0, 2),
      DASHER(0, 3),
      BRINELY(0, 4),
      SPOTTY(0, 5),
      FLOPPER(1, 0),
      STRIPEY(1, 1),
      GLITTER(1, 2),
      BLOCKFISH(1, 3),
      BETTY(1, 4),
      CLAYFISH(1, 5);

      private final int shape;
      private final int pattern;
      private static final TropicalFishEntity.Variety[] VALUES = values();

      private Variety(int shape, int pattern) {
         this.shape = shape;
         this.pattern = pattern;
      }

      public int getShape() {
         return this.shape;
      }

      public int getPattern() {
         return this.pattern;
      }

      public static String getTranslateKey(int shape, int pattern) {
         return VALUES[pattern + 6 * shape].getTranslationKey();
      }

      public String getTranslationKey() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }

   static class TropicalFishData extends SchoolingFishEntity.FishData {
      final int shape;
      final int pattern;
      final int baseColor;
      final int patternColor;

      TropicalFishData(TropicalFishEntity tropicalFishEntity, int i, int j, int k, int l) {
         super(tropicalFishEntity);
         this.shape = i;
         this.pattern = j;
         this.baseColor = k;
         this.patternColor = l;
      }
   }
}
