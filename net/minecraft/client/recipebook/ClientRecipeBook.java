package net.minecraft.client.recipebook;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

@Environment(EnvType.CLIENT)
public class ClientRecipeBook extends RecipeBook {
   private static final Logger LOGGER = LogManager.getLogger();
   private Map<RecipeBookGroup, List<RecipeResultCollection>> resultsByGroup = ImmutableMap.of();
   private List<RecipeResultCollection> orderedResults = ImmutableList.of();

   public void reload(Iterable<Recipe<?>> recipes) {
      Map<RecipeBookGroup, List<List<Recipe<?>>>> map = toGroupedMap(recipes);
      Map<RecipeBookGroup, List<RecipeResultCollection>> map2 = Maps.newHashMap();
      Builder<RecipeResultCollection> builder = ImmutableList.builder();
      map.forEach((recipeBookGroup, list) -> {
         Stream var10002 = list.stream().map(RecipeResultCollection::new);
         Objects.requireNonNull(builder);
         map2.put(recipeBookGroup, (List)var10002.peek(builder::add).collect(ImmutableList.toImmutableList()));
      });
      RecipeBookGroup.SEARCH_MAP.forEach((recipeBookGroup, list) -> {
         map2.put(recipeBookGroup, (List)list.stream().flatMap((recipeBookGroupx) -> {
            return ((List)map2.getOrDefault(recipeBookGroupx, ImmutableList.of())).stream();
         }).collect(ImmutableList.toImmutableList()));
      });
      this.resultsByGroup = ImmutableMap.copyOf((Map)map2);
      this.orderedResults = builder.build();
   }

   private static Map<RecipeBookGroup, List<List<Recipe<?>>>> toGroupedMap(Iterable<Recipe<?>> recipes) {
      Map<RecipeBookGroup, List<List<Recipe<?>>>> map = Maps.newHashMap();
      Table<RecipeBookGroup, String, List<Recipe<?>>> table = HashBasedTable.create();
      Iterator var3 = recipes.iterator();

      while(var3.hasNext()) {
         Recipe<?> recipe = (Recipe)var3.next();
         if (!recipe.isIgnoredInRecipeBook() && !recipe.isEmpty()) {
            RecipeBookGroup recipeBookGroup = getGroupForRecipe(recipe);
            String string = recipe.getGroup();
            if (string.isEmpty()) {
               ((List)map.computeIfAbsent(recipeBookGroup, (group) -> {
                  return Lists.newArrayList();
               })).add(ImmutableList.of(recipe));
            } else {
               List<Recipe<?>> list = (List)table.get(recipeBookGroup, string);
               if (list == null) {
                  list = Lists.newArrayList();
                  table.put(recipeBookGroup, string, list);
                  ((List)map.computeIfAbsent(recipeBookGroup, (group) -> {
                     return Lists.newArrayList();
                  })).add(list);
               }

               ((List)list).add(recipe);
            }
         }
      }

      return map;
   }

   private static RecipeBookGroup getGroupForRecipe(Recipe<?> recipe) {
      RecipeType<?> recipeType = recipe.getType();
      if (recipeType == RecipeType.CRAFTING) {
         ItemStack itemStack = recipe.getOutput();
         ItemGroup itemGroup = itemStack.getItem().getGroup();
         if (itemGroup == ItemGroup.BUILDING_BLOCKS) {
            return RecipeBookGroup.CRAFTING_BUILDING_BLOCKS;
         } else if (itemGroup != ItemGroup.TOOLS && itemGroup != ItemGroup.COMBAT) {
            return itemGroup == ItemGroup.REDSTONE ? RecipeBookGroup.CRAFTING_REDSTONE : RecipeBookGroup.CRAFTING_MISC;
         } else {
            return RecipeBookGroup.CRAFTING_EQUIPMENT;
         }
      } else if (recipeType == RecipeType.SMELTING) {
         if (recipe.getOutput().getItem().isFood()) {
            return RecipeBookGroup.FURNACE_FOOD;
         } else {
            return recipe.getOutput().getItem() instanceof BlockItem ? RecipeBookGroup.FURNACE_BLOCKS : RecipeBookGroup.FURNACE_MISC;
         }
      } else if (recipeType == RecipeType.BLASTING) {
         return recipe.getOutput().getItem() instanceof BlockItem ? RecipeBookGroup.BLAST_FURNACE_BLOCKS : RecipeBookGroup.BLAST_FURNACE_MISC;
      } else if (recipeType == RecipeType.SMOKING) {
         return RecipeBookGroup.SMOKER_FOOD;
      } else if (recipeType == RecipeType.STONECUTTING) {
         return RecipeBookGroup.STONECUTTER;
      } else if (recipeType == RecipeType.CAMPFIRE_COOKING) {
         return RecipeBookGroup.CAMPFIRE;
      } else if (recipeType == RecipeType.SMITHING) {
         return RecipeBookGroup.SMITHING;
      } else {
         Logger var10000 = LOGGER;
         Supplier[] var10002 = new Supplier[]{() -> {
            return Registry.RECIPE_TYPE.getId(recipe.getType());
         }, null};
         Objects.requireNonNull(recipe);
         var10002[1] = recipe::getId;
         var10000.warn("Unknown recipe category: {}/{}", var10002);
         return RecipeBookGroup.UNKNOWN;
      }
   }

   public List<RecipeResultCollection> getOrderedResults() {
      return this.orderedResults;
   }

   public List<RecipeResultCollection> getResultsForGroup(RecipeBookGroup category) {
      return (List)this.resultsByGroup.getOrDefault(category, Collections.emptyList());
   }
}
