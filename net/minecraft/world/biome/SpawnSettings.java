package net.minecraft.world.biome;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class SpawnSettings {
   public static final Logger LOGGER = LogManager.getLogger();
   private static final float field_30983 = 0.1F;
   public static final Pool<SpawnSettings.SpawnEntry> EMPTY_ENTRY_POOL = Pool.empty();
   public static final SpawnSettings INSTANCE = new SpawnSettings(0.1F, (Map)Stream.of(SpawnGroup.values()).collect(ImmutableMap.toImmutableMap((spawnGroup) -> {
      return spawnGroup;
   }, (spawnGroup) -> {
      return EMPTY_ENTRY_POOL;
   })), ImmutableMap.of(), false);
   public static final MapCodec<SpawnSettings> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
      RecordCodecBuilder var10001 = Codec.floatRange(0.0F, 0.9999999F).optionalFieldOf("creature_spawn_probability", 0.1F).forGetter((spawnSettings) -> {
         return spawnSettings.creatureSpawnProbability;
      });
      Codec var10002 = SpawnGroup.CODEC;
      Codec var10003 = Pool.createCodec(SpawnSettings.SpawnEntry.CODEC);
      Logger var10005 = LOGGER;
      Objects.requireNonNull(var10005);
      return instance.group(var10001, Codec.simpleMap(var10002, var10003.promotePartial(Util.addPrefix("Spawn data: ", var10005::error)), StringIdentifiable.toKeyable(SpawnGroup.values())).fieldOf("spawners").forGetter((spawnSettings) -> {
         return spawnSettings.spawners;
      }), Codec.simpleMap(Registry.ENTITY_TYPE, SpawnSettings.SpawnDensity.CODEC, Registry.ENTITY_TYPE).fieldOf("spawn_costs").forGetter((spawnSettings) -> {
         return spawnSettings.spawnCosts;
      }), Codec.BOOL.fieldOf("player_spawn_friendly").orElse(false).forGetter(SpawnSettings::isPlayerSpawnFriendly)).apply(instance, (Function4)(SpawnSettings::new));
   });
   private final float creatureSpawnProbability;
   private final Map<SpawnGroup, Pool<SpawnSettings.SpawnEntry>> spawners;
   private final Map<EntityType<?>, SpawnSettings.SpawnDensity> spawnCosts;
   private final boolean playerSpawnFriendly;

   SpawnSettings(float creatureSpawnProbability, Map<SpawnGroup, Pool<SpawnSettings.SpawnEntry>> spawners, Map<EntityType<?>, SpawnSettings.SpawnDensity> spawnCosts, boolean playerSpawnFriendly) {
      this.creatureSpawnProbability = creatureSpawnProbability;
      this.spawners = ImmutableMap.copyOf(spawners);
      this.spawnCosts = ImmutableMap.copyOf(spawnCosts);
      this.playerSpawnFriendly = playerSpawnFriendly;
   }

   public Pool<SpawnSettings.SpawnEntry> getSpawnEntries(SpawnGroup spawnGroup) {
      return (Pool)this.spawners.getOrDefault(spawnGroup, EMPTY_ENTRY_POOL);
   }

   @Nullable
   public SpawnSettings.SpawnDensity getSpawnDensity(EntityType<?> entityType) {
      return (SpawnSettings.SpawnDensity)this.spawnCosts.get(entityType);
   }

   public float getCreatureSpawnProbability() {
      return this.creatureSpawnProbability;
   }

   public boolean isPlayerSpawnFriendly() {
      return this.playerSpawnFriendly;
   }

   /**
    * Embodies the density limit information of a type of entity in entity
    * spawning logic. The density field is generated for all entities spawned
    * than a specific type of entity.
    */
   public static class SpawnDensity {
      public static final Codec<SpawnSettings.SpawnDensity> CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.DOUBLE.fieldOf("energy_budget").forGetter((spawnDensity) -> {
            return spawnDensity.gravityLimit;
         }), Codec.DOUBLE.fieldOf("charge").forGetter((spawnDensity) -> {
            return spawnDensity.mass;
         })).apply(instance, (BiFunction)(SpawnSettings.SpawnDensity::new));
      });
      private final double gravityLimit;
      private final double mass;

      SpawnDensity(double gravityLimit, double mass) {
         this.gravityLimit = gravityLimit;
         this.mass = mass;
      }

      /**
       * Represents the cap of gravity as in {@link
       * net.minecraft.util.math.GravityField#calculate(BlockPos, double)} for
       * entity spawning. If the cap is exceeded, the entity spawning attempt
       * will skip.
       */
      public double getGravityLimit() {
         return this.gravityLimit;
      }

      /**
       * Represents the mass of each entity spawned. Will affect gravity
       * calculation.
       */
      public double getMass() {
         return this.mass;
      }
   }

   public static class SpawnEntry extends Weighted.Absent {
      public static final Codec<SpawnSettings.SpawnEntry> CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Registry.ENTITY_TYPE.fieldOf("type").forGetter((spawnEntry) -> {
            return spawnEntry.type;
         }), Weight.CODEC.fieldOf("weight").forGetter(Weighted.Absent::getWeight), Codec.INT.fieldOf("minCount").forGetter((spawnEntry) -> {
            return spawnEntry.minGroupSize;
         }), Codec.INT.fieldOf("maxCount").forGetter((spawnEntry) -> {
            return spawnEntry.maxGroupSize;
         })).apply(instance, (Function4)(SpawnSettings.SpawnEntry::new));
      });
      public final EntityType<?> type;
      public final int minGroupSize;
      public final int maxGroupSize;

      public SpawnEntry(EntityType<?> type, int weight, int minGroupSize, int maxGroupSize) {
         this(type, Weight.of(weight), minGroupSize, maxGroupSize);
      }

      public SpawnEntry(EntityType<?> type, Weight weight, int minGroupSize, int maxGroupSize) {
         super(weight);
         this.type = type.getSpawnGroup() == SpawnGroup.MISC ? EntityType.PIG : type;
         this.minGroupSize = minGroupSize;
         this.maxGroupSize = maxGroupSize;
      }

      public String toString() {
         Identifier var10000 = EntityType.getId(this.type);
         return var10000 + "*(" + this.minGroupSize + "-" + this.maxGroupSize + "):" + this.getWeight();
      }
   }

   public static class Builder {
      private final Map<SpawnGroup, List<SpawnSettings.SpawnEntry>> spawners = (Map)Stream.of(SpawnGroup.values()).collect(ImmutableMap.toImmutableMap((spawnGroup) -> {
         return spawnGroup;
      }, (spawnGroup) -> {
         return Lists.newArrayList();
      }));
      private final Map<EntityType<?>, SpawnSettings.SpawnDensity> spawnCosts = Maps.newLinkedHashMap();
      private float creatureSpawnProbability = 0.1F;
      private boolean playerSpawnFriendly;

      public SpawnSettings.Builder spawn(SpawnGroup spawnGroup, SpawnSettings.SpawnEntry spawnEntry) {
         ((List)this.spawners.get(spawnGroup)).add(spawnEntry);
         return this;
      }

      public SpawnSettings.Builder spawnCost(EntityType<?> entityType, double mass, double gravityLimit) {
         this.spawnCosts.put(entityType, new SpawnSettings.SpawnDensity(gravityLimit, mass));
         return this;
      }

      public SpawnSettings.Builder creatureSpawnProbability(float probability) {
         this.creatureSpawnProbability = probability;
         return this;
      }

      public SpawnSettings.Builder playerSpawnFriendly() {
         this.playerSpawnFriendly = true;
         return this;
      }

      public SpawnSettings build() {
         return new SpawnSettings(this.creatureSpawnProbability, (Map)this.spawners.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (entry) -> {
            return Pool.of((List)entry.getValue());
         })), ImmutableMap.copyOf(this.spawnCosts), this.playerSpawnFriendly);
      }
   }
}
