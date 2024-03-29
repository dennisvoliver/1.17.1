package net.minecraft.block.entity;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantingTableBlockEntity extends BlockEntity implements Nameable {
   public int ticks;
   public float nextPageAngle;
   public float pageAngle;
   public float field_11969;
   public float field_11967;
   public float nextPageTurningSpeed;
   public float pageTurningSpeed;
   public float field_11964;
   public float field_11963;
   public float field_11962;
   private static final Random RANDOM = new Random();
   private Text customName;

   public EnchantingTableBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.ENCHANTING_TABLE, pos, state);
   }

   public NbtCompound writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.hasCustomName()) {
         nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

      return nbt;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("CustomName", 8)) {
         this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"));
      }

   }

   public static void tick(World world, BlockPos pos, BlockState state, EnchantingTableBlockEntity blockEntity) {
      blockEntity.pageTurningSpeed = blockEntity.nextPageTurningSpeed;
      blockEntity.field_11963 = blockEntity.field_11964;
      PlayerEntity playerEntity = world.getClosestPlayer((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, 3.0D, false);
      if (playerEntity != null) {
         double d = playerEntity.getX() - ((double)pos.getX() + 0.5D);
         double e = playerEntity.getZ() - ((double)pos.getZ() + 0.5D);
         blockEntity.field_11962 = (float)MathHelper.atan2(e, d);
         blockEntity.nextPageTurningSpeed += 0.1F;
         if (blockEntity.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
            float f = blockEntity.field_11969;

            do {
               blockEntity.field_11969 += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
            } while(f == blockEntity.field_11969);
         }
      } else {
         blockEntity.field_11962 += 0.02F;
         blockEntity.nextPageTurningSpeed -= 0.1F;
      }

      while(blockEntity.field_11964 >= 3.1415927F) {
         blockEntity.field_11964 -= 6.2831855F;
      }

      while(blockEntity.field_11964 < -3.1415927F) {
         blockEntity.field_11964 += 6.2831855F;
      }

      while(blockEntity.field_11962 >= 3.1415927F) {
         blockEntity.field_11962 -= 6.2831855F;
      }

      while(blockEntity.field_11962 < -3.1415927F) {
         blockEntity.field_11962 += 6.2831855F;
      }

      float g;
      for(g = blockEntity.field_11962 - blockEntity.field_11964; g >= 3.1415927F; g -= 6.2831855F) {
      }

      while(g < -3.1415927F) {
         g += 6.2831855F;
      }

      blockEntity.field_11964 += g * 0.4F;
      blockEntity.nextPageTurningSpeed = MathHelper.clamp(blockEntity.nextPageTurningSpeed, 0.0F, 1.0F);
      ++blockEntity.ticks;
      blockEntity.pageAngle = blockEntity.nextPageAngle;
      float h = (blockEntity.field_11969 - blockEntity.nextPageAngle) * 0.4F;
      float i = 0.2F;
      h = MathHelper.clamp(h, -0.2F, 0.2F);
      blockEntity.field_11967 += (h - blockEntity.field_11967) * 0.9F;
      blockEntity.nextPageAngle += blockEntity.field_11967;
   }

   public Text getName() {
      return (Text)(this.customName != null ? this.customName : new TranslatableText("container.enchant"));
   }

   public void setCustomName(@Nullable Text value) {
      this.customName = value;
   }

   @Nullable
   public Text getCustomName() {
      return this.customName;
   }
}
