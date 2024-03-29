package net.minecraft.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A recipe manager allows easier use of recipes, such as finding matches and
 * remainders. It is also integrated with a recipe loader, which loads recipes
 * from data packs' JSON files.
 */
public class RecipeManager extends JsonDataLoader {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private static final Logger LOGGER = LogManager.getLogger();
   private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes = ImmutableMap.of();
   /**
    * This isn't quite indicating an errored state; its value is only set to
    * {@code false} and is never {@code true}, and isn't used anywhere.
    */
   private boolean errored;

   public RecipeManager() {
      super(GSON, "recipes");
   }

   protected void apply(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler) {
      this.errored = false;
      Map<RecipeType<?>, Builder<Identifier, Recipe<?>>> map2 = Maps.newHashMap();
      Iterator var5 = map.entrySet().iterator();

      while(var5.hasNext()) {
         Entry<Identifier, JsonElement> entry = (Entry)var5.next();
         Identifier identifier = (Identifier)entry.getKey();

         try {
            Recipe<?> recipe = deserialize(identifier, JsonHelper.asObject((JsonElement)entry.getValue(), "top element"));
            ((Builder)map2.computeIfAbsent(recipe.getType(), (recipeType) -> {
               return ImmutableMap.builder();
            })).put(identifier, recipe);
         } catch (IllegalArgumentException | JsonParseException var9) {
            LOGGER.error((String)"Parsing error loading recipe {}", (Object)identifier, (Object)var9);
         }
      }

      this.recipes = (Map)map2.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (entryx) -> {
         return ((Builder)entryx.getValue()).build();
      }));
      LOGGER.info((String)"Loaded {} recipes", (Object)map2.size());
   }

   /**
    * {@return the {@link #errored} field} This is unused in vanilla and will only
    * return {@code false} without mods.
    */
   public boolean isErrored() {
      return this.errored;
   }

   /**
    * {@return a recipe of the given {@code type} that match the given
    * {@code inventory} and {@code world}}
    * 
    * <p>If there are multiple matching recipes, the result is arbitrary,
    * but this method will return the same result unless the recipes in this
    * manager are updated.
    * 
    * @param type the desired recipe type
    * @param inventory the input inventory
    * @param world the input world
    */
   public <C extends Inventory, T extends Recipe<C>> Optional<T> getFirstMatch(RecipeType<T> type, C inventory, World world) {
      return this.getAllOfType(type).values().stream().flatMap((recipe) -> {
         return Util.stream(type.match(recipe, world, inventory));
      }).findFirst();
   }

   /**
    * Creates a list of all recipes of the given {@code type}. The list has an
    * arbitrary order.
    * 
    * <p>This list does not update with this manager. Modifications to
    * the returned list do not affect this manager.
    * 
    * @return the created list of recipes of the given {@code type}
    * 
    * @param type the desired recipe type
    */
   public <C extends Inventory, T extends Recipe<C>> List<T> listAllOfType(RecipeType<T> type) {
      return (List)this.getAllOfType(type).values().stream().map((recipe) -> {
         return recipe;
      }).collect(Collectors.toList());
   }

   /**
    * Creates a list of all recipes of the given {@code type} that match the
    * given {@code inventory} and {@code world}. The list is ordered by the
    * translation key of the output item stack of each recipe.
    * 
    * <p>This list does not update with this manager. Modifications to
    * the returned list do not affect this manager.
    * 
    * @return the created list of matching recipes
    * 
    * @param type the desired recipe type
    * @param inventory the input inventory
    * @param world the input world
    */
   public <C extends Inventory, T extends Recipe<C>> List<T> getAllMatches(RecipeType<T> type, C inventory, World world) {
      return (List)this.getAllOfType(type).values().stream().flatMap((recipe) -> {
         return Util.stream(type.match(recipe, world, inventory));
      }).sorted(Comparator.comparing((recipe) -> {
         return recipe.getOutput().getTranslationKey();
      })).collect(Collectors.toList());
   }

   private <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> type) {
      return (Map)this.recipes.getOrDefault(type, Collections.emptyMap());
   }

   /**
    * {@return the remainder of a recipe of the given {@code type} that match
    * the given {@code inventory} and {@code world}, or a shallow copy of the
    * {@code inventory}}
    * 
    * <p>This retrieves the {@linkplain Recipe#getRemainder(Inventory)
    * remainders} of {@link #getFirstMatch(RecipeType, Inventory, World)
    * getFirstMatch(type, inventory, world)} if the match exists.
    * 
    * @see Recipe#getRemainder(Inventory)
    * 
    * @param type the desired recipe type
    * @param inventory the input inventory
    * @param world the input world
    */
   public <C extends Inventory, T extends Recipe<C>> DefaultedList<ItemStack> getRemainingStacks(RecipeType<T> type, C inventory, World world) {
      Optional<T> optional = this.getFirstMatch(type, inventory, world);
      if (optional.isPresent()) {
         return ((Recipe)optional.get()).getRemainder(inventory);
      } else {
         DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

         for(int i = 0; i < defaultedList.size(); ++i) {
            defaultedList.set(i, inventory.getStack(i));
         }

         return defaultedList;
      }
   }

   /**
    * {@return a recipe with the given {@code id}, or empty if there is no such recipe}
    * 
    * @param id the ID of the desired recipe
    */
   public Optional<? extends Recipe<?>> get(Identifier id) {
      return this.recipes.values().stream().map((map) -> {
         return (Recipe)map.get(id);
      }).filter(Objects::nonNull).findFirst();
   }

   /**
    * {@return all recipes in this manager}
    * 
    * <p>The returned set does not update with the manager. Modifications to the
    * returned set does not affect this manager.
    */
   public Collection<Recipe<?>> values() {
      return (Collection)this.recipes.values().stream().flatMap((map) -> {
         return map.values().stream();
      }).collect(Collectors.toSet());
   }

   /**
    * {@return a stream of IDs of recipes in this manager}
    * 
    * <p>The returned stream does not update after {@link #setRecipes(Iterable)}
    * call.
    * 
    * @apiNote This is used by the command sources to suggest recipe IDs for command
    * arguments.
    */
   public Stream<Identifier> keys() {
      return this.recipes.values().stream().flatMap((map) -> {
         return map.keySet().stream();
      });
   }

   /**
    * Reads a recipe from a JSON object.
    * 
    * @implNote Even though a recipe's {@linkplain Recipe#getSerializer() serializer}
    * is stored in a {@code type} field in the JSON format and referred so in this
    * method, its registry has key {@code minecraft:root/minecraft:recipe_serializer}
    * and is thus named.
    * 
    * @throws com.google.gson.JsonParseException if the recipe JSON is invalid
    * @return the read recipe
    * @see RecipeSerializer#read
    * 
    * @param id the recipe's ID
    * @param json the recipe JSON
    */
   public static Recipe<?> deserialize(Identifier id, JsonObject json) {
      String string = JsonHelper.getString(json, "type");
      return ((RecipeSerializer)Registry.RECIPE_SERIALIZER.getOrEmpty(new Identifier(string)).orElseThrow(() -> {
         return new JsonSyntaxException("Invalid or unsupported recipe type '" + string + "'");
      })).read(id, json);
   }

   /**
    * Sets the recipes for this recipe manager. Used by the client to set the server
    * side recipes.
    * 
    * @param recipes the recipes to set
    */
   public void setRecipes(Iterable<Recipe<?>> recipes) {
      this.errored = false;
      Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map = Maps.newHashMap();
      recipes.forEach((recipe) -> {
         Map<Identifier, Recipe<?>> map2 = (Map)map.computeIfAbsent(recipe.getType(), (t) -> {
            return Maps.newHashMap();
         });
         Recipe<?> recipe2 = (Recipe)map2.put(recipe.getId(), recipe);
         if (recipe2 != null) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
         }
      });
      this.recipes = ImmutableMap.copyOf((Map)map);
   }
}
