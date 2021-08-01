package net.minecraft.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DecorationItem extends Item {
   private final EntityType<? extends AbstractDecorationEntity> entityType;

   public DecorationItem(EntityType<? extends AbstractDecorationEntity> type, Item.Settings settings) {
      super(settings);
      this.entityType = type;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      BlockPos blockPos = context.getBlockPos();
      Direction direction = context.getSide();
      BlockPos blockPos2 = blockPos.offset(direction);
      PlayerEntity playerEntity = context.getPlayer();
      ItemStack itemStack = context.getStack();
      if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
         return ActionResult.FAIL;
      } else {
         World world = context.getWorld();
         Object abstractDecorationEntity4;
         if (this.entityType == EntityType.PAINTING) {
            abstractDecorationEntity4 = new PaintingEntity(world, blockPos2, direction);
         } else if (this.entityType == EntityType.ITEM_FRAME) {
            abstractDecorationEntity4 = new ItemFrameEntity(world, blockPos2, direction);
         } else {
            if (this.entityType != EntityType.GLOW_ITEM_FRAME) {
               return ActionResult.success(world.isClient);
            }

            abstractDecorationEntity4 = new GlowItemFrameEntity(world, blockPos2, direction);
         }

         NbtCompound nbtCompound = itemStack.getNbt();
         if (nbtCompound != null) {
            EntityType.loadFromEntityNbt(world, playerEntity, (Entity)abstractDecorationEntity4, nbtCompound);
         }

         if (((AbstractDecorationEntity)abstractDecorationEntity4).canStayAttached()) {
            if (!world.isClient) {
               ((AbstractDecorationEntity)abstractDecorationEntity4).onPlace();
               world.emitGameEvent(playerEntity, GameEvent.ENTITY_PLACE, blockPos);
               world.spawnEntity((Entity)abstractDecorationEntity4);
            }

            itemStack.decrement(1);
            return ActionResult.success(world.isClient);
         } else {
            return ActionResult.CONSUME;
         }
      }
   }

   protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
      return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
   }
}
