package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.IronGolemEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.Vec3f;

@Environment(EnvType.CLIENT)
public class IronGolemFlowerFeatureRenderer extends FeatureRenderer<IronGolemEntity, IronGolemEntityModel<IronGolemEntity>> {
   public IronGolemFlowerFeatureRenderer(FeatureRendererContext<IronGolemEntity, IronGolemEntityModel<IronGolemEntity>> featureRendererContext) {
      super(featureRendererContext);
   }

   public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, IronGolemEntity ironGolemEntity, float f, float g, float h, float j, float k, float l) {
      if (ironGolemEntity.getLookingAtVillagerTicks() != 0) {
         matrixStack.push();
         ModelPart modelPart = ((IronGolemEntityModel)this.getContextModel()).getRightArm();
         modelPart.rotate(matrixStack);
         matrixStack.translate(-1.1875D, 1.0625D, -0.9375D);
         matrixStack.translate(0.5D, 0.5D, 0.5D);
         float m = 0.5F;
         matrixStack.scale(0.5F, 0.5F, 0.5F);
         matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-90.0F));
         matrixStack.translate(-0.5D, -0.5D, -0.5D);
         MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(Blocks.POPPY.getDefaultState(), matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV);
         matrixStack.pop();
      }
   }
}
