package net.minecraft.world.gen.foliage;

import com.mojang.serialization.Codec;
import net.minecraft.util.registry.Registry;

public class FoliagePlacerType<P extends FoliagePlacer> {
   public static final FoliagePlacerType<BlobFoliagePlacer> BLOB_FOLIAGE_PLACER;
   public static final FoliagePlacerType<SpruceFoliagePlacer> SPRUCE_FOLIAGE_PLACER;
   public static final FoliagePlacerType<PineFoliagePlacer> PINE_FOLIAGE_PLACER;
   public static final FoliagePlacerType<AcaciaFoliagePlacer> ACACIA_FOLIAGE_PLACER;
   public static final FoliagePlacerType<BushFoliagePlacer> BUSH_FOLIAGE_PLACER;
   public static final FoliagePlacerType<LargeOakFoliagePlacer> FANCY_FOLIAGE_PLACER;
   public static final FoliagePlacerType<JungleFoliagePlacer> JUNGLE_FOLIAGE_PLACER;
   public static final FoliagePlacerType<MegaPineFoliagePlacer> MEGA_PINE_FOLIAGE_PLACER;
   public static final FoliagePlacerType<DarkOakFoliagePlacer> DARK_OAK_FOLIAGE_PLACER;
   public static final FoliagePlacerType<RandomSpreadFoliagePlacer> RANDOM_SPREAD_FOLIAGE_PLACER;
   private final Codec<P> codec;

   private static <P extends FoliagePlacer> FoliagePlacerType<P> register(String id, Codec<P> codec) {
      return (FoliagePlacerType)Registry.register(Registry.FOLIAGE_PLACER_TYPE, (String)id, new FoliagePlacerType(codec));
   }

   private FoliagePlacerType(Codec<P> codec) {
      this.codec = codec;
   }

   public Codec<P> getCodec() {
      return this.codec;
   }

   static {
      BLOB_FOLIAGE_PLACER = register("blob_foliage_placer", BlobFoliagePlacer.CODEC);
      SPRUCE_FOLIAGE_PLACER = register("spruce_foliage_placer", SpruceFoliagePlacer.CODEC);
      PINE_FOLIAGE_PLACER = register("pine_foliage_placer", PineFoliagePlacer.CODEC);
      ACACIA_FOLIAGE_PLACER = register("acacia_foliage_placer", AcaciaFoliagePlacer.CODEC);
      BUSH_FOLIAGE_PLACER = register("bush_foliage_placer", BushFoliagePlacer.CODEC);
      FANCY_FOLIAGE_PLACER = register("fancy_foliage_placer", LargeOakFoliagePlacer.CODEC);
      JUNGLE_FOLIAGE_PLACER = register("jungle_foliage_placer", JungleFoliagePlacer.CODEC);
      MEGA_PINE_FOLIAGE_PLACER = register("mega_pine_foliage_placer", MegaPineFoliagePlacer.CODEC);
      DARK_OAK_FOLIAGE_PLACER = register("dark_oak_foliage_placer", DarkOakFoliagePlacer.CODEC);
      RANDOM_SPREAD_FOLIAGE_PLACER = register("random_spread_foliage_placer", RandomSpreadFoliagePlacer.CODEC);
   }
}
