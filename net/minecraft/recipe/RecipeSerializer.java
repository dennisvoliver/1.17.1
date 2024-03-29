package net.minecraft.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * The recipe serializer controls the serialization and deserialization of
 * recipe content. The caller should handle the serialization of recipes' IDs.
 * 
 * <p>Even though they are referred to by the {@code type} field in recipe
 * JSON format, they are stored in a registry with key
 * {@code minecraft:root/minecraft:recipe_serializer}, and is hence named.
 * 
 * <p>If a recipe's serializer exists only on the server but not on the
 * client, the client will disconnect upon receiving the recipe; if a
 * recipe JSON intended for an absent recipe serializer is read, it is
 * skipped.
 */
public interface RecipeSerializer<T extends Recipe<?>> {
   RecipeSerializer<ShapedRecipe> SHAPED = register("crafting_shaped", new ShapedRecipe.Serializer());
   RecipeSerializer<ShapelessRecipe> SHAPELESS = register("crafting_shapeless", new ShapelessRecipe.Serializer());
   SpecialRecipeSerializer<ArmorDyeRecipe> ARMOR_DYE = (SpecialRecipeSerializer)register("crafting_special_armordye", new SpecialRecipeSerializer(ArmorDyeRecipe::new));
   SpecialRecipeSerializer<BookCloningRecipe> BOOK_CLONING = (SpecialRecipeSerializer)register("crafting_special_bookcloning", new SpecialRecipeSerializer(BookCloningRecipe::new));
   SpecialRecipeSerializer<MapCloningRecipe> MAP_CLONING = (SpecialRecipeSerializer)register("crafting_special_mapcloning", new SpecialRecipeSerializer(MapCloningRecipe::new));
   SpecialRecipeSerializer<MapExtendingRecipe> MAP_EXTENDING = (SpecialRecipeSerializer)register("crafting_special_mapextending", new SpecialRecipeSerializer(MapExtendingRecipe::new));
   SpecialRecipeSerializer<FireworkRocketRecipe> FIREWORK_ROCKET = (SpecialRecipeSerializer)register("crafting_special_firework_rocket", new SpecialRecipeSerializer(FireworkRocketRecipe::new));
   SpecialRecipeSerializer<FireworkStarRecipe> FIREWORK_STAR = (SpecialRecipeSerializer)register("crafting_special_firework_star", new SpecialRecipeSerializer(FireworkStarRecipe::new));
   SpecialRecipeSerializer<FireworkStarFadeRecipe> FIREWORK_STAR_FADE = (SpecialRecipeSerializer)register("crafting_special_firework_star_fade", new SpecialRecipeSerializer(FireworkStarFadeRecipe::new));
   SpecialRecipeSerializer<TippedArrowRecipe> TIPPED_ARROW = (SpecialRecipeSerializer)register("crafting_special_tippedarrow", new SpecialRecipeSerializer(TippedArrowRecipe::new));
   SpecialRecipeSerializer<BannerDuplicateRecipe> BANNER_DUPLICATE = (SpecialRecipeSerializer)register("crafting_special_bannerduplicate", new SpecialRecipeSerializer(BannerDuplicateRecipe::new));
   SpecialRecipeSerializer<ShieldDecorationRecipe> SHIELD_DECORATION = (SpecialRecipeSerializer)register("crafting_special_shielddecoration", new SpecialRecipeSerializer(ShieldDecorationRecipe::new));
   SpecialRecipeSerializer<ShulkerBoxColoringRecipe> SHULKER_BOX = (SpecialRecipeSerializer)register("crafting_special_shulkerboxcoloring", new SpecialRecipeSerializer(ShulkerBoxColoringRecipe::new));
   SpecialRecipeSerializer<SuspiciousStewRecipe> SUSPICIOUS_STEW = (SpecialRecipeSerializer)register("crafting_special_suspiciousstew", new SpecialRecipeSerializer(SuspiciousStewRecipe::new));
   SpecialRecipeSerializer<RepairItemRecipe> REPAIR_ITEM = (SpecialRecipeSerializer)register("crafting_special_repairitem", new SpecialRecipeSerializer(RepairItemRecipe::new));
   CookingRecipeSerializer<SmeltingRecipe> SMELTING = (CookingRecipeSerializer)register("smelting", new CookingRecipeSerializer(SmeltingRecipe::new, 200));
   CookingRecipeSerializer<BlastingRecipe> BLASTING = (CookingRecipeSerializer)register("blasting", new CookingRecipeSerializer(BlastingRecipe::new, 100));
   CookingRecipeSerializer<SmokingRecipe> SMOKING = (CookingRecipeSerializer)register("smoking", new CookingRecipeSerializer(SmokingRecipe::new, 100));
   CookingRecipeSerializer<CampfireCookingRecipe> CAMPFIRE_COOKING = (CookingRecipeSerializer)register("campfire_cooking", new CookingRecipeSerializer(CampfireCookingRecipe::new, 100));
   RecipeSerializer<StonecuttingRecipe> STONECUTTING = register("stonecutting", new CuttingRecipe.Serializer(StonecuttingRecipe::new));
   RecipeSerializer<SmithingRecipe> SMITHING = register("smithing", new SmithingRecipe.Serializer());

   /**
    * Reads a recipe from a JSON object.
    * 
    * @implNote If this throws any exception besides {@link com.google.gson.JsonParseException}
    * and {@link IllegalArgumentException}, it will terminate and affect loading
    * of all recipes from data packs beyond the current recipe.
    * 
    * @throws com.google.gson.JsonParseException if the recipe JSON is incorrect
    * @return the read recipe
    * 
    * @param id the recipe's ID
    * @param json the recipe JSON
    */
   T read(Identifier id, JsonObject json);

   /**
    * Reads a recipe from a packet byte buf, usually on the client.
    * 
    * <p>This can throw whatever exception the packet byte buf throws. This may be
    * called in the netty event loop than the client game engine thread.
    * 
    * @return the read recipe
    * 
    * @param id the recipe's ID
    * @param buf the recipe buf
    */
   T read(Identifier id, PacketByteBuf buf);

   /**
    * Writes a recipe to a packet byte buf, usually on the server.
    * 
    * <p>The recipe's ID is already written into the buf when this is called.
    * 
    * <p>This can throw whatever exception the packet byte buf throws. This may be
    * called in the netty event loop than the server game engine thread.
    * 
    * @param buf the recipe buf
    * @param recipe the recipe
    */
   void write(PacketByteBuf buf, T recipe);

   static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
      return (RecipeSerializer)Registry.register(Registry.RECIPE_SERIALIZER, (String)id, serializer);
   }
}
