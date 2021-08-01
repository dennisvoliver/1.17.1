package net.minecraft.screen;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class SmithingScreenHandler extends ForgingScreenHandler {
   private final World world;
   @Nullable
   private SmithingRecipe currentRecipe;
   private final List<SmithingRecipe> recipes;

   public SmithingScreenHandler(int syncId, PlayerInventory playerInventory) {
      this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
   }

   public SmithingScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
      super(ScreenHandlerType.SMITHING, syncId, playerInventory, context);
      this.world = playerInventory.player.world;
      this.recipes = this.world.getRecipeManager().listAllOfType(RecipeType.SMITHING);
   }

   protected boolean canUse(BlockState state) {
      return state.isOf(Blocks.SMITHING_TABLE);
   }

   protected boolean canTakeOutput(PlayerEntity player, boolean present) {
      return this.currentRecipe != null && this.currentRecipe.matches(this.input, this.world);
   }

   protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
      stack.onCraft(player.world, player, stack.getCount());
      this.output.unlockLastRecipe(player);
      this.decrementStack(0);
      this.decrementStack(1);
      this.context.run((world, pos) -> {
         world.syncWorldEvent(WorldEvents.SMITHING_TABLE_USED, pos, 0);
      });
   }

   private void decrementStack(int slot) {
      ItemStack itemStack = this.input.getStack(slot);
      itemStack.decrement(1);
      this.input.setStack(slot, itemStack);
   }

   public void updateResult() {
      List<SmithingRecipe> list = this.world.getRecipeManager().getAllMatches(RecipeType.SMITHING, this.input, this.world);
      if (list.isEmpty()) {
         this.output.setStack(0, ItemStack.EMPTY);
      } else {
         this.currentRecipe = (SmithingRecipe)list.get(0);
         ItemStack itemStack = this.currentRecipe.craft(this.input);
         this.output.setLastRecipe(this.currentRecipe);
         this.output.setStack(0, itemStack);
      }

   }

   protected boolean isUsableAsAddition(ItemStack stack) {
      return this.recipes.stream().anyMatch((recipe) -> {
         return recipe.testAddition(stack);
      });
   }

   public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
      return slot.inventory != this.output && super.canInsertIntoSlot(stack, slot);
   }
}
