package net.minecraft.client.render.entity;

import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity> {
   private static final float field_32924 = 0.15F;
   private static final int MAX_COUNT_FOR_4_ITEMS_RENDERED = 48;
   private static final int MAX_COUNT_FOR_3_ITEMS_RENDERED = 32;
   private static final int MAX_COUNT_FOR_2_ITEMS_RENDERED = 16;
   private static final int MAX_COUNT_FOR_1_ITEM_RENDERED = 1;
   private static final float field_32929 = 0.0F;
   private static final float field_32930 = 0.0F;
   private static final float field_32931 = 0.09375F;
   private final ItemRenderer itemRenderer;
   private final Random random = new Random();

   public ItemEntityRenderer(EntityRendererFactory.Context context) {
      super(context);
      this.itemRenderer = context.getItemRenderer();
      this.shadowRadius = 0.15F;
      this.shadowOpacity = 0.75F;
   }

   private int getRenderedAmount(ItemStack stack) {
      int i = 1;
      if (stack.getCount() > 48) {
         i = 5;
      } else if (stack.getCount() > 32) {
         i = 4;
      } else if (stack.getCount() > 16) {
         i = 3;
      } else if (stack.getCount() > 1) {
         i = 2;
      }

      return i;
   }

   public void render(ItemEntity itemEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
      matrixStack.push();
      ItemStack itemStack = itemEntity.getStack();
      int j = itemStack.isEmpty() ? 187 : Item.getRawId(itemStack.getItem()) + itemStack.getDamage();
      this.random.setSeed((long)j);
      BakedModel bakedModel = this.itemRenderer.getHeldItemModel(itemStack, itemEntity.world, (LivingEntity)null, itemEntity.getId());
      boolean bl = bakedModel.hasDepth();
      int k = this.getRenderedAmount(itemStack);
      float h = 0.25F;
      float l = MathHelper.sin(((float)itemEntity.getItemAge() + g) / 10.0F + itemEntity.uniqueOffset) * 0.1F + 0.1F;
      float m = bakedModel.getTransformation().getTransformation(ModelTransformation.Mode.GROUND).scale.getY();
      matrixStack.translate(0.0D, (double)(l + 0.25F * m), 0.0D);
      float n = itemEntity.getRotation(g);
      matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(n));
      float o = bakedModel.getTransformation().ground.scale.getX();
      float p = bakedModel.getTransformation().ground.scale.getY();
      float q = bakedModel.getTransformation().ground.scale.getZ();
      float v;
      float w;
      if (!bl) {
         float r = -0.0F * (float)(k - 1) * 0.5F * o;
         v = -0.0F * (float)(k - 1) * 0.5F * p;
         w = -0.09375F * (float)(k - 1) * 0.5F * q;
         matrixStack.translate((double)r, (double)v, (double)w);
      }

      for(int u = 0; u < k; ++u) {
         matrixStack.push();
         if (u > 0) {
            if (bl) {
               v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               w = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float x = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               matrixStack.translate((double)v, (double)w, (double)x);
            } else {
               v = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               w = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               matrixStack.translate((double)v, (double)w, 0.0D);
            }
         }

         this.itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel);
         matrixStack.pop();
         if (!bl) {
            matrixStack.translate((double)(0.0F * o), (double)(0.0F * p), (double)(0.09375F * q));
         }
      }

      matrixStack.pop();
      super.render(itemEntity, f, g, matrixStack, vertexConsumerProvider, i);
   }

   public Identifier getTexture(ItemEntity itemEntity) {
      return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
   }
}
