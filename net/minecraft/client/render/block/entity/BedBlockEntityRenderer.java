package net.minecraft.client.render.block.entity;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DoubleBlockProperties;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.BedPart;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

@Environment(EnvType.CLIENT)
public class BedBlockEntityRenderer implements BlockEntityRenderer<BedBlockEntity> {
   private final ModelPart bedHead;
   private final ModelPart bedFoot;

   public BedBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
      this.bedHead = ctx.getLayerModelPart(EntityModelLayers.BED_HEAD);
      this.bedFoot = ctx.getLayerModelPart(EntityModelLayers.BED_FOOT);
   }

   public static TexturedModelData getHeadTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), ModelTransform.NONE);
      modelPartData.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(50, 6).cuboid(0.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 1.5707964F));
      modelPartData.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(50, 18).cuboid(-16.0F, 6.0F, 0.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 3.1415927F));
      return TexturedModelData.of(modelData, 64, 64);
   }

   public static TexturedModelData getFootTexturedModelData() {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      modelPartData.addChild("main", ModelPartBuilder.create().uv(0, 22).cuboid(0.0F, 0.0F, 0.0F, 16.0F, 16.0F, 6.0F), ModelTransform.NONE);
      modelPartData.addChild(EntityModelPartNames.LEFT_LEG, ModelPartBuilder.create().uv(50, 0).cuboid(0.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 0.0F));
      modelPartData.addChild(EntityModelPartNames.RIGHT_LEG, ModelPartBuilder.create().uv(50, 12).cuboid(-16.0F, 6.0F, -16.0F, 3.0F, 3.0F, 3.0F), ModelTransform.rotation(1.5707964F, 0.0F, 4.712389F));
      return TexturedModelData.of(modelData, 64, 64);
   }

   public void render(BedBlockEntity bedBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
      SpriteIdentifier spriteIdentifier = TexturedRenderLayers.BED_TEXTURES[bedBlockEntity.getColor().getId()];
      World world = bedBlockEntity.getWorld();
      if (world != null) {
         BlockState blockState = bedBlockEntity.getCachedState();
         DoubleBlockProperties.PropertySource<? extends BedBlockEntity> propertySource = DoubleBlockProperties.toPropertySource(BlockEntityType.BED, BedBlock::getBedPart, BedBlock::getOppositePartDirection, ChestBlock.FACING, blockState, world, bedBlockEntity.getPos(), (worldx, pos) -> {
            return false;
         });
         int k = ((Int2IntFunction)propertySource.apply(new LightmapCoordinatesRetriever())).get(i);
         this.renderPart(matrixStack, vertexConsumerProvider, blockState.get(BedBlock.PART) == BedPart.HEAD ? this.bedHead : this.bedFoot, (Direction)blockState.get(BedBlock.FACING), spriteIdentifier, k, j, false);
      } else {
         this.renderPart(matrixStack, vertexConsumerProvider, this.bedHead, Direction.SOUTH, spriteIdentifier, i, j, false);
         this.renderPart(matrixStack, vertexConsumerProvider, this.bedFoot, Direction.SOUTH, spriteIdentifier, i, j, true);
      }

   }

   private void renderPart(MatrixStack matrix, VertexConsumerProvider vertexConsumers, ModelPart part, Direction direction, SpriteIdentifier sprite, int light, int overlay, boolean isFoot) {
      matrix.push();
      matrix.translate(0.0D, 0.5625D, isFoot ? -1.0D : 0.0D);
      matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90.0F));
      matrix.translate(0.5D, 0.5D, 0.5D);
      matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180.0F + direction.asRotation()));
      matrix.translate(-0.5D, -0.5D, -0.5D);
      VertexConsumer vertexConsumer = sprite.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
      part.render(matrix, vertexConsumer, light, overlay);
      matrix.pop();
   }
}
