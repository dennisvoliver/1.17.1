package net.minecraft.data.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;

public class VariantsBlockStateSupplier implements BlockStateSupplier {
   private final Block block;
   private final List<BlockStateVariant> variants;
   private final Set<Property<?>> definedProperties = Sets.newHashSet();
   private final List<BlockStateVariantMap> variantMaps = Lists.newArrayList();

   private VariantsBlockStateSupplier(Block block, List<BlockStateVariant> variants) {
      this.block = block;
      this.variants = variants;
   }

   /**
    * Appends a block state variant map to this block state information.
    * 
    * <p>A block state variant map defines some of the variant settings based
    * on a defined set of properties in the block state, such as the model
    * of the block state is determined by a coordinated map of power and
    * machine type property, and the y rotation determined by a facing
    * property, etc.
    * 
    * @return this block state file
    * 
    * @param map the variant map to contribute property to variant settings
    * mappings to the block state file
    */
   public VariantsBlockStateSupplier coordinate(BlockStateVariantMap map) {
      map.getProperties().forEach((property) -> {
         if (this.block.getStateManager().getProperty(property.getName()) != property) {
            throw new IllegalStateException("Property " + property + " is not defined for block " + this.block);
         } else if (!this.definedProperties.add(property)) {
            throw new IllegalStateException("Values of property " + property + " already defined for block " + this.block);
         }
      });
      this.variantMaps.add(map);
      return this;
   }

   public JsonElement get() {
      Stream<Pair<PropertiesMap, List<BlockStateVariant>>> stream = Stream.of(Pair.of(PropertiesMap.empty(), this.variants));

      Map map;
      for(Iterator var2 = this.variantMaps.iterator(); var2.hasNext(); stream = stream.flatMap((pair) -> {
         return map.entrySet().stream().map((entry) -> {
            PropertiesMap propertiesMap = ((PropertiesMap)pair.getFirst()).copyOf((PropertiesMap)entry.getKey());
            List<BlockStateVariant> list = intersect((List)pair.getSecond(), (List)entry.getValue());
            return Pair.of(propertiesMap, list);
         });
      })) {
         BlockStateVariantMap blockStateVariantMap = (BlockStateVariantMap)var2.next();
         map = blockStateVariantMap.getVariants();
      }

      Map<String, JsonElement> map2 = new TreeMap();
      stream.forEach((pair) -> {
         map2.put(((PropertiesMap)pair.getFirst()).asString(), BlockStateVariant.toJson((List)pair.getSecond()));
      });
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("variants", (JsonElement)Util.make(new JsonObject(), (jsonObjectx) -> {
         Objects.requireNonNull(jsonObjectx);
         map2.forEach(jsonObjectx::add);
      }));
      return jsonObject;
   }

   private static List<BlockStateVariant> intersect(List<BlockStateVariant> left, List<BlockStateVariant> right) {
      Builder<BlockStateVariant> builder = ImmutableList.builder();
      left.forEach((blockStateVariant) -> {
         right.forEach((blockStateVariant2) -> {
            builder.add((Object)BlockStateVariant.union(blockStateVariant, blockStateVariant2));
         });
      });
      return builder.build();
   }

   public Block getBlock() {
      return this.block;
   }

   public static VariantsBlockStateSupplier create(Block block) {
      return new VariantsBlockStateSupplier(block, ImmutableList.of(BlockStateVariant.create()));
   }

   public static VariantsBlockStateSupplier create(Block block, BlockStateVariant variant) {
      return new VariantsBlockStateSupplier(block, ImmutableList.of(variant));
   }

   public static VariantsBlockStateSupplier create(Block block, BlockStateVariant... variants) {
      return new VariantsBlockStateSupplier(block, ImmutableList.copyOf((Object[])variants));
   }
}
