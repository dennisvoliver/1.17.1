package net.minecraft.item;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BundleItem extends Item {
   private static final String ITEMS_KEY = "Items";
   public static final int MAX_STORAGE = 64;
   private static final int field_30859 = 4;
   private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);

   public BundleItem(Item.Settings settings) {
      super(settings);
   }

   public static float getAmountFilled(ItemStack stack) {
      return (float)getBundleOccupancy(stack) / 64.0F;
   }

   public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
      if (clickType != ClickType.RIGHT) {
         return false;
      } else {
         ItemStack itemStack = slot.getStack();
         if (itemStack.isEmpty()) {
            removeFirstStack(stack).ifPresent((removedStack) -> {
               addToBundle(stack, slot.insertStack(removedStack));
            });
         } else if (itemStack.getItem().canBeNested()) {
            int i = (64 - getBundleOccupancy(stack)) / getItemOccupancy(itemStack);
            addToBundle(stack, slot.takeStackRange(itemStack.getCount(), i, player));
         }

         return true;
      }
   }

   public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
      if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
         if (otherStack.isEmpty()) {
            Optional var10000 = removeFirstStack(stack);
            Objects.requireNonNull(cursorStackReference);
            var10000.ifPresent(cursorStackReference::set);
         } else {
            otherStack.decrement(addToBundle(stack, otherStack));
         }

         return true;
      } else {
         return false;
      }
   }

   public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
      ItemStack itemStack = user.getStackInHand(hand);
      if (dropAllBundledItems(itemStack, user)) {
         user.incrementStat(Stats.USED.getOrCreateStat(this));
         return TypedActionResult.success(itemStack, world.isClient());
      } else {
         return TypedActionResult.fail(itemStack);
      }
   }

   public boolean isItemBarVisible(ItemStack stack) {
      return getBundleOccupancy(stack) > 0;
   }

   public int getItemBarStep(ItemStack stack) {
      return Math.min(1 + 12 * getBundleOccupancy(stack) / 64, 13);
   }

   public int getItemBarColor(ItemStack stack) {
      return ITEM_BAR_COLOR;
   }

   private static int addToBundle(ItemStack bundle, ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem().canBeNested()) {
         NbtCompound nbtCompound = bundle.getOrCreateNbt();
         if (!nbtCompound.contains("Items")) {
            nbtCompound.put("Items", new NbtList());
         }

         int i = getBundleOccupancy(bundle);
         int j = getItemOccupancy(stack);
         int k = Math.min(stack.getCount(), (64 - i) / j);
         if (k == 0) {
            return 0;
         } else {
            NbtList nbtList = nbtCompound.getList("Items", 10);
            Optional<NbtCompound> optional = canMergeStack(stack, nbtList);
            if (optional.isPresent()) {
               NbtCompound nbtCompound2 = (NbtCompound)optional.get();
               ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
               itemStack.increment(k);
               itemStack.writeNbt(nbtCompound2);
               nbtList.remove(nbtCompound2);
               nbtList.add(0, (NbtElement)nbtCompound2);
            } else {
               ItemStack itemStack2 = stack.copy();
               itemStack2.setCount(k);
               NbtCompound nbtCompound3 = new NbtCompound();
               itemStack2.writeNbt(nbtCompound3);
               nbtList.add(0, (NbtElement)nbtCompound3);
            }

            return k;
         }
      } else {
         return 0;
      }
   }

   private static Optional<NbtCompound> canMergeStack(ItemStack stack, NbtList items) {
      if (stack.isOf(Items.BUNDLE)) {
         return Optional.empty();
      } else {
         Stream var10000 = items.stream();
         Objects.requireNonNull(NbtCompound.class);
         var10000 = var10000.filter(NbtCompound.class::isInstance);
         Objects.requireNonNull(NbtCompound.class);
         return var10000.map(NbtCompound.class::cast).filter((item) -> {
            return ItemStack.canCombine(ItemStack.fromNbt(item), stack);
         }).findFirst();
      }
   }

   private static int getItemOccupancy(ItemStack stack) {
      if (stack.isOf(Items.BUNDLE)) {
         return 4 + getBundleOccupancy(stack);
      } else {
         if ((stack.isOf(Items.BEEHIVE) || stack.isOf(Items.BEE_NEST)) && stack.hasNbt()) {
            NbtCompound nbtCompound = stack.getSubNbt("BlockEntityTag");
            if (nbtCompound != null && !nbtCompound.getList("Bees", 10).isEmpty()) {
               return 64;
            }
         }

         return 64 / stack.getMaxCount();
      }
   }

   private static int getBundleOccupancy(ItemStack stack) {
      return getBundledStacks(stack).mapToInt((itemStack) -> {
         return getItemOccupancy(itemStack) * itemStack.getCount();
      }).sum();
   }

   private static Optional<ItemStack> removeFirstStack(ItemStack stack) {
      NbtCompound nbtCompound = stack.getOrCreateNbt();
      if (!nbtCompound.contains("Items")) {
         return Optional.empty();
      } else {
         NbtList nbtList = nbtCompound.getList("Items", 10);
         if (nbtList.isEmpty()) {
            return Optional.empty();
         } else {
            int i = false;
            NbtCompound nbtCompound2 = nbtList.getCompound(0);
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
            nbtList.remove(0);
            if (nbtList.isEmpty()) {
               stack.removeSubNbt("Items");
            }

            return Optional.of(itemStack);
         }
      }
   }

   private static boolean dropAllBundledItems(ItemStack stack, PlayerEntity player) {
      NbtCompound nbtCompound = stack.getOrCreateNbt();
      if (!nbtCompound.contains("Items")) {
         return false;
      } else {
         if (player instanceof ServerPlayerEntity) {
            NbtList nbtList = nbtCompound.getList("Items", 10);

            for(int i = 0; i < nbtList.size(); ++i) {
               NbtCompound nbtCompound2 = nbtList.getCompound(i);
               ItemStack itemStack = ItemStack.fromNbt(nbtCompound2);
               player.dropItem(itemStack, true);
            }
         }

         stack.removeSubNbt("Items");
         return true;
      }
   }

   private static Stream<ItemStack> getBundledStacks(ItemStack stack) {
      NbtCompound nbtCompound = stack.getNbt();
      if (nbtCompound == null) {
         return Stream.empty();
      } else {
         NbtList nbtList = nbtCompound.getList("Items", 10);
         Stream var10000 = nbtList.stream();
         Objects.requireNonNull(NbtCompound.class);
         return var10000.map(NbtCompound.class::cast).map(ItemStack::fromNbt);
      }
   }

   public Optional<TooltipData> getTooltipData(ItemStack stack) {
      DefaultedList<ItemStack> defaultedList = DefaultedList.of();
      Stream var10000 = getBundledStacks(stack);
      Objects.requireNonNull(defaultedList);
      var10000.forEach(defaultedList::add);
      return Optional.of(new BundleTooltipData(defaultedList, getBundleOccupancy(stack)));
   }

   public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
      tooltip.add((new TranslatableText("item.minecraft.bundle.fullness", new Object[]{getBundleOccupancy(stack), 64})).formatted(Formatting.GRAY));
   }

   public void onItemEntityDestroyed(ItemEntity entity) {
      ItemUsage.spawnItemContents(entity, getBundledStacks(entity.getStack()));
   }
}
