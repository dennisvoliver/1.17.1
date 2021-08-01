package net.minecraft.scoreboard;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.stat.StatType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ScoreboardCriterion {
   private static final Map<String, ScoreboardCriterion> field_33939 = Maps.newHashMap();
   /**
    * A map of all scoreboard criteria by their names.
    * Updated automatically in the constructor.
    */
   private static final Map<String, ScoreboardCriterion> CRITERIA = Maps.newHashMap();
   public static final ScoreboardCriterion DUMMY = create("dummy");
   public static final ScoreboardCriterion TRIGGER = create("trigger");
   public static final ScoreboardCriterion DEATH_COUNT = create("deathCount");
   public static final ScoreboardCriterion PLAYER_KILL_COUNT = create("playerKillCount");
   public static final ScoreboardCriterion TOTAL_KILL_COUNT = create("totalKillCount");
   public static final ScoreboardCriterion HEALTH;
   public static final ScoreboardCriterion FOOD;
   public static final ScoreboardCriterion AIR;
   public static final ScoreboardCriterion ARMOR;
   public static final ScoreboardCriterion XP;
   public static final ScoreboardCriterion LEVEL;
   public static final ScoreboardCriterion[] TEAM_KILLS;
   public static final ScoreboardCriterion[] KILLED_BY_TEAMS;
   private final String name;
   private final boolean readOnly;
   private final ScoreboardCriterion.RenderType defaultRenderType;

   private static ScoreboardCriterion create(String name, boolean readOnly, ScoreboardCriterion.RenderType defaultRenderType) {
      ScoreboardCriterion scoreboardCriterion = new ScoreboardCriterion(name, readOnly, defaultRenderType);
      field_33939.put(name, scoreboardCriterion);
      return scoreboardCriterion;
   }

   private static ScoreboardCriterion create(String name) {
      return create(name, false, ScoreboardCriterion.RenderType.INTEGER);
   }

   protected ScoreboardCriterion(String name) {
      this(name, false, ScoreboardCriterion.RenderType.INTEGER);
   }

   protected ScoreboardCriterion(String name, boolean readOnly, ScoreboardCriterion.RenderType defaultRenderType) {
      this.name = name;
      this.readOnly = readOnly;
      this.defaultRenderType = defaultRenderType;
      CRITERIA.put(name, this);
   }

   public static Set<String> method_37271() {
      return ImmutableSet.copyOf((Collection)field_33939.keySet());
   }

   public static Optional<ScoreboardCriterion> getOrCreateStatCriterion(String name) {
      ScoreboardCriterion scoreboardCriterion = (ScoreboardCriterion)CRITERIA.get(name);
      if (scoreboardCriterion != null) {
         return Optional.of(scoreboardCriterion);
      } else {
         int i = name.indexOf(58);
         return i < 0 ? Optional.empty() : Registry.STAT_TYPE.getOrEmpty(Identifier.splitOn(name.substring(0, i), '.')).flatMap((statType) -> {
            return getOrCreateStatCriterion(statType, Identifier.splitOn(name.substring(i + 1), '.'));
         });
      }
   }

   private static <T> Optional<ScoreboardCriterion> getOrCreateStatCriterion(StatType<T> statType, Identifier id) {
      Optional var10000 = statType.getRegistry().getOrEmpty(id);
      Objects.requireNonNull(statType);
      return var10000.map(statType::getOrCreateStat);
   }

   public String getName() {
      return this.name;
   }

   public boolean isReadOnly() {
      return this.readOnly;
   }

   public ScoreboardCriterion.RenderType getDefaultRenderType() {
      return this.defaultRenderType;
   }

   static {
      HEALTH = create("health", true, ScoreboardCriterion.RenderType.HEARTS);
      FOOD = create("food", true, ScoreboardCriterion.RenderType.INTEGER);
      AIR = create("air", true, ScoreboardCriterion.RenderType.INTEGER);
      ARMOR = create("armor", true, ScoreboardCriterion.RenderType.INTEGER);
      XP = create("xp", true, ScoreboardCriterion.RenderType.INTEGER);
      LEVEL = create("level", true, ScoreboardCriterion.RenderType.INTEGER);
      TEAM_KILLS = new ScoreboardCriterion[]{create("teamkill." + Formatting.BLACK.getName()), create("teamkill." + Formatting.DARK_BLUE.getName()), create("teamkill." + Formatting.DARK_GREEN.getName()), create("teamkill." + Formatting.DARK_AQUA.getName()), create("teamkill." + Formatting.DARK_RED.getName()), create("teamkill." + Formatting.DARK_PURPLE.getName()), create("teamkill." + Formatting.GOLD.getName()), create("teamkill." + Formatting.GRAY.getName()), create("teamkill." + Formatting.DARK_GRAY.getName()), create("teamkill." + Formatting.BLUE.getName()), create("teamkill." + Formatting.GREEN.getName()), create("teamkill." + Formatting.AQUA.getName()), create("teamkill." + Formatting.RED.getName()), create("teamkill." + Formatting.LIGHT_PURPLE.getName()), create("teamkill." + Formatting.YELLOW.getName()), create("teamkill." + Formatting.WHITE.getName())};
      KILLED_BY_TEAMS = new ScoreboardCriterion[]{create("killedByTeam." + Formatting.BLACK.getName()), create("killedByTeam." + Formatting.DARK_BLUE.getName()), create("killedByTeam." + Formatting.DARK_GREEN.getName()), create("killedByTeam." + Formatting.DARK_AQUA.getName()), create("killedByTeam." + Formatting.DARK_RED.getName()), create("killedByTeam." + Formatting.DARK_PURPLE.getName()), create("killedByTeam." + Formatting.GOLD.getName()), create("killedByTeam." + Formatting.GRAY.getName()), create("killedByTeam." + Formatting.DARK_GRAY.getName()), create("killedByTeam." + Formatting.BLUE.getName()), create("killedByTeam." + Formatting.GREEN.getName()), create("killedByTeam." + Formatting.AQUA.getName()), create("killedByTeam." + Formatting.RED.getName()), create("killedByTeam." + Formatting.LIGHT_PURPLE.getName()), create("killedByTeam." + Formatting.YELLOW.getName()), create("killedByTeam." + Formatting.WHITE.getName())};
   }

   public static enum RenderType {
      INTEGER("integer"),
      HEARTS("hearts");

      private final String name;
      private static final Map<String, ScoreboardCriterion.RenderType> CRITERION_TYPES;

      private RenderType(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public static ScoreboardCriterion.RenderType getType(String name) {
         return (ScoreboardCriterion.RenderType)CRITERION_TYPES.getOrDefault(name, INTEGER);
      }

      static {
         Builder<String, ScoreboardCriterion.RenderType> builder = ImmutableMap.builder();
         ScoreboardCriterion.RenderType[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            ScoreboardCriterion.RenderType renderType = var1[var3];
            builder.put(renderType.name, renderType);
         }

         CRITERION_TYPES = builder.build();
      }
   }
}
