package net.minecraft.item;

import java.util.Collection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class DebugStickItem extends Item {
   public DebugStickItem(Item.Settings settings) {
      super(settings);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      if (!world.isClient) {
         this.use(miner, state, world, pos, false, miner.getStackInHand(Hand.MAIN_HAND));
      }

      return false;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      PlayerEntity playerEntity = context.getPlayer();
      World world = context.getWorld();
      if (!world.isClient && playerEntity != null) {
         BlockPos blockPos = context.getBlockPos();
         if (!this.use(playerEntity, world.getBlockState(blockPos), world, blockPos, true, context.getStack())) {
            return ActionResult.FAIL;
         }
      }

      return ActionResult.success(world.isClient);
   }

   private boolean use(PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos, boolean update, ItemStack stack) {
      if (!player.isCreativeLevelTwoOp()) {
         return false;
      } else {
         Block block = state.getBlock();
         StateManager<Block, BlockState> stateManager = block.getStateManager();
         Collection<Property<?>> collection = stateManager.getProperties();
         String string = Registry.BLOCK.getId(block).toString();
         if (collection.isEmpty()) {
            sendMessage(player, new TranslatableText(this.getTranslationKey() + ".empty", new Object[]{string}));
            return false;
         } else {
            NbtCompound nbtCompound = stack.getOrCreateSubNbt("DebugProperty");
            String string2 = nbtCompound.getString(string);
            Property<?> property = stateManager.getProperty(string2);
            if (update) {
               if (property == null) {
                  property = (Property)collection.iterator().next();
               }

               BlockState blockState = cycle(state, property, player.shouldCancelInteraction());
               world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
               sendMessage(player, new TranslatableText(this.getTranslationKey() + ".update", new Object[]{property.getName(), getValueString(blockState, property)}));
            } else {
               property = (Property)cycle((Iterable)collection, (Object)property, player.shouldCancelInteraction());
               String string3 = property.getName();
               nbtCompound.putString(string, string3);
               sendMessage(player, new TranslatableText(this.getTranslationKey() + ".select", new Object[]{string3, getValueString(state, property)}));
            }

            return true;
         }
      }
   }

   private static <T extends Comparable<T>> BlockState cycle(BlockState state, Property<T> property, boolean inverse) {
      return (BlockState)state.with(property, (Comparable)cycle((Iterable)property.getValues(), (Object)state.get(property), inverse));
   }

   private static <T> T cycle(Iterable<T> elements, @Nullable T current, boolean inverse) {
      return inverse ? Util.previous(elements, current) : Util.next(elements, current);
   }

   private static void sendMessage(PlayerEntity player, Text message) {
      ((ServerPlayerEntity)player).sendMessage(message, MessageType.GAME_INFO, Util.NIL_UUID);
   }

   private static <T extends Comparable<T>> String getValueString(BlockState state, Property<T> property) {
      return property.name(state.get(property));
   }
}
