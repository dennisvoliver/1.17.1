package net.minecraft.client.render.entity.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class HorseEntityModel<T extends HorseBaseEntity> extends AnimalModel<T> {
   private static final float EATING_GRASS_ANIMATION_HEAD_BASE_PITCH = 2.1816616F;
   private static final float ANGRY_ANIMATION_FRONT_LEG_PITCH_MULTIPLIER = 1.0471976F;
   private static final float ANGRY_ANIMATION_BODY_PITCH_MULTIPLIER = 0.7853982F;
   private static final float HEAD_TAIL_BASE_PITCH = 0.5235988F;
   private static final float ANGRY_ANIMATION_HIND_LEG_PITCH_MULTIPLIER = 0.2617994F;
   /**
    * The key of the model part containing the head model parts, whose value is {@value}.
    */
   protected static final String HEAD_PARTS = "head_parts";
   /**
    * The key of the left hind baby leg model part, whose value is {@value}.
    */
   private static final String LEFT_HIND_BABY_LEG = "left_hind_baby_leg";
   /**
    * The key of the right hind baby leg model part, whose value is {@value}.
    */
   private static final String RIGHT_HIND_BABY_LEG = "right_hind_baby_leg";
   /**
    * The key of the left front baby leg model part, whose value is {@value}.
    */
   private static final String LEFT_FRONT_BABY_LEG = "left_front_baby_leg";
   /**
    * The key of the right front baby leg model part, whose value is {@value}.
    */
   private static final String RIGHT_FRONT_BABY_LEG = "right_front_baby_leg";
   /**
    * The key of the saddle mane model part, whose value is {@value}.
    */
   private static final String SADDLE = "saddle";
   /**
    * The key of the left saddle mouth model part, whose value is {@value}.
    */
   private static final String LEFT_SADDLE_MOUTH = "left_saddle_mouth";
   /**
    * The key of the left saddle line model part, whose value is {@value}.
    */
   private static final String LEFT_SADDLE_LINE = "left_saddle_line";
   /**
    * The key of the right saddle mouth model part, whose value is {@value}.
    */
   private static final String RIGHT_SADDLE_MOUTH = "right_saddle_mouth";
   /**
    * The key of the right saddle line model part, whose value is {@value}.
    */
   private static final String RIGHT_SADDLE_LINE = "right_saddle_line";
   /**
    * The key of the head saddle model part, whose value is {@value}.
    */
   private static final String HEAD_SADDLE = "head_saddle";
   /**
    * The key of the mouth saddle wrap mane model part, whose value is {@value}.
    */
   private static final String MOUTH_SADDLE_WRAP = "mouth_saddle_wrap";
   protected final ModelPart body;
   protected final ModelPart head;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightHindBabyLeg;
   private final ModelPart leftHindBabyLeg;
   private final ModelPart rightFrontBabyLeg;
   private final ModelPart leftFrontBabyLeg;
   private final ModelPart tail;
   private final ModelPart[] saddle;
   private final ModelPart[] straps;

   public HorseEntityModel(ModelPart root) {
      super(true, 16.2F, 1.36F, 2.7272F, 2.0F, 20.0F);
      this.body = root.getChild(EntityModelPartNames.BODY);
      this.head = root.getChild("head_parts");
      this.rightHindLeg = root.getChild(EntityModelPartNames.RIGHT_HIND_LEG);
      this.leftHindLeg = root.getChild(EntityModelPartNames.LEFT_HIND_LEG);
      this.rightFrontLeg = root.getChild(EntityModelPartNames.RIGHT_FRONT_LEG);
      this.leftFrontLeg = root.getChild(EntityModelPartNames.LEFT_FRONT_LEG);
      this.rightHindBabyLeg = root.getChild("right_hind_baby_leg");
      this.leftHindBabyLeg = root.getChild("left_hind_baby_leg");
      this.rightFrontBabyLeg = root.getChild("right_front_baby_leg");
      this.leftFrontBabyLeg = root.getChild("left_front_baby_leg");
      this.tail = this.body.getChild(EntityModelPartNames.TAIL);
      ModelPart modelPart = this.body.getChild("saddle");
      ModelPart modelPart2 = this.head.getChild("left_saddle_mouth");
      ModelPart modelPart3 = this.head.getChild("right_saddle_mouth");
      ModelPart modelPart4 = this.head.getChild("left_saddle_line");
      ModelPart modelPart5 = this.head.getChild("right_saddle_line");
      ModelPart modelPart6 = this.head.getChild("head_saddle");
      ModelPart modelPart7 = this.head.getChild("mouth_saddle_wrap");
      this.saddle = new ModelPart[]{modelPart, modelPart2, modelPart3, modelPart6, modelPart7};
      this.straps = new ModelPart[]{modelPart4, modelPart5};
   }

   public static ModelData getModelData(Dilation dilation) {
      ModelData modelData = new ModelData();
      ModelPartData modelPartData = modelData.getRoot();
      ModelPartData modelPartData2 = modelPartData.addChild(EntityModelPartNames.BODY, ModelPartBuilder.create().uv(0, 32).cuboid(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new Dilation(0.05F)), ModelTransform.pivot(0.0F, 11.0F, 5.0F));
      ModelPartData modelPartData3 = modelPartData.addChild("head_parts", ModelPartBuilder.create().uv(0, 35).cuboid(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F), ModelTransform.of(0.0F, 4.0F, -12.0F, 0.5235988F, 0.0F, 0.0F));
      ModelPartData modelPartData4 = modelPartData3.addChild(EntityModelPartNames.HEAD, ModelPartBuilder.create().uv(0, 13).cuboid(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F, dilation), ModelTransform.NONE);
      modelPartData3.addChild(EntityModelPartNames.MANE, ModelPartBuilder.create().uv(56, 36).cuboid(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F, dilation), ModelTransform.NONE);
      modelPartData3.addChild("upper_mouth", ModelPartBuilder.create().uv(0, 25).cuboid(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F, dilation), ModelTransform.NONE);
      modelPartData.addChild(EntityModelPartNames.LEFT_HIND_LEG, ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, dilation), ModelTransform.pivot(4.0F, 14.0F, 7.0F));
      modelPartData.addChild(EntityModelPartNames.RIGHT_HIND_LEG, ModelPartBuilder.create().uv(48, 21).cuboid(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, dilation), ModelTransform.pivot(-4.0F, 14.0F, 7.0F));
      modelPartData.addChild(EntityModelPartNames.LEFT_FRONT_LEG, ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, dilation), ModelTransform.pivot(4.0F, 14.0F, -12.0F));
      modelPartData.addChild(EntityModelPartNames.RIGHT_FRONT_LEG, ModelPartBuilder.create().uv(48, 21).cuboid(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, dilation), ModelTransform.pivot(-4.0F, 14.0F, -12.0F));
      Dilation dilation2 = dilation.add(0.0F, 5.5F, 0.0F);
      modelPartData.addChild("left_hind_baby_leg", ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, dilation2), ModelTransform.pivot(4.0F, 14.0F, 7.0F));
      modelPartData.addChild("right_hind_baby_leg", ModelPartBuilder.create().uv(48, 21).cuboid(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F, dilation2), ModelTransform.pivot(-4.0F, 14.0F, 7.0F));
      modelPartData.addChild("left_front_baby_leg", ModelPartBuilder.create().uv(48, 21).mirrored().cuboid(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, dilation2), ModelTransform.pivot(4.0F, 14.0F, -12.0F));
      modelPartData.addChild("right_front_baby_leg", ModelPartBuilder.create().uv(48, 21).cuboid(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F, dilation2), ModelTransform.pivot(-4.0F, 14.0F, -12.0F));
      modelPartData2.addChild(EntityModelPartNames.TAIL, ModelPartBuilder.create().uv(42, 36).cuboid(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F, dilation), ModelTransform.of(0.0F, -5.0F, 2.0F, 0.5235988F, 0.0F, 0.0F));
      modelPartData2.addChild("saddle", ModelPartBuilder.create().uv(26, 0).cuboid(-5.0F, -8.0F, -9.0F, 10.0F, 9.0F, 9.0F, new Dilation(0.5F)), ModelTransform.NONE);
      modelPartData3.addChild("left_saddle_mouth", ModelPartBuilder.create().uv(29, 5).cuboid(2.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, dilation), ModelTransform.NONE);
      modelPartData3.addChild("right_saddle_mouth", ModelPartBuilder.create().uv(29, 5).cuboid(-3.0F, -9.0F, -6.0F, 1.0F, 2.0F, 2.0F, dilation), ModelTransform.NONE);
      modelPartData3.addChild("left_saddle_line", ModelPartBuilder.create().uv(32, 2).cuboid(3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F, dilation), ModelTransform.rotation(-0.5235988F, 0.0F, 0.0F));
      modelPartData3.addChild("right_saddle_line", ModelPartBuilder.create().uv(32, 2).cuboid(-3.1F, -6.0F, -8.0F, 0.0F, 3.0F, 16.0F, dilation), ModelTransform.rotation(-0.5235988F, 0.0F, 0.0F));
      modelPartData3.addChild("head_saddle", ModelPartBuilder.create().uv(1, 1).cuboid(-3.0F, -11.0F, -1.9F, 6.0F, 5.0F, 6.0F, new Dilation(0.2F)), ModelTransform.NONE);
      modelPartData3.addChild("mouth_saddle_wrap", ModelPartBuilder.create().uv(19, 0).cuboid(-2.0F, -11.0F, -4.0F, 4.0F, 5.0F, 2.0F, new Dilation(0.2F)), ModelTransform.NONE);
      modelPartData4.addChild(EntityModelPartNames.LEFT_EAR, ModelPartBuilder.create().uv(19, 16).cuboid(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new Dilation(-0.001F)), ModelTransform.NONE);
      modelPartData4.addChild(EntityModelPartNames.RIGHT_EAR, ModelPartBuilder.create().uv(19, 16).cuboid(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F, new Dilation(-0.001F)), ModelTransform.NONE);
      return modelData;
   }

   public void setAngles(T horseBaseEntity, float f, float g, float h, float i, float j) {
      boolean bl = horseBaseEntity.isSaddled();
      boolean bl2 = horseBaseEntity.hasPassengers();
      ModelPart[] var9 = this.saddle;
      int var10 = var9.length;

      int var11;
      ModelPart modelPart2;
      for(var11 = 0; var11 < var10; ++var11) {
         modelPart2 = var9[var11];
         modelPart2.visible = bl;
      }

      var9 = this.straps;
      var10 = var9.length;

      for(var11 = 0; var11 < var10; ++var11) {
         modelPart2 = var9[var11];
         modelPart2.visible = bl2 && bl;
      }

      this.body.pivotY = 11.0F;
   }

   public Iterable<ModelPart> getHeadParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> getBodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightHindBabyLeg, this.leftHindBabyLeg, this.rightFrontBabyLeg, this.leftFrontBabyLeg);
   }

   public void animateModel(T horseBaseEntity, float f, float g, float h) {
      super.animateModel(horseBaseEntity, f, g, h);
      float i = MathHelper.lerpAngle(horseBaseEntity.prevBodyYaw, horseBaseEntity.bodyYaw, h);
      float j = MathHelper.lerpAngle(horseBaseEntity.prevHeadYaw, horseBaseEntity.headYaw, h);
      float k = MathHelper.lerp(h, horseBaseEntity.prevPitch, horseBaseEntity.getPitch());
      float l = j - i;
      float m = k * 0.017453292F;
      if (l > 20.0F) {
         l = 20.0F;
      }

      if (l < -20.0F) {
         l = -20.0F;
      }

      if (g > 0.2F) {
         m += MathHelper.cos(f * 0.4F) * 0.15F * g;
      }

      float n = horseBaseEntity.getEatingGrassAnimationProgress(h);
      float o = horseBaseEntity.getAngryAnimationProgress(h);
      float p = 1.0F - o;
      float q = horseBaseEntity.getEatingAnimationProgress(h);
      boolean bl = horseBaseEntity.tailWagTicks != 0;
      float r = (float)horseBaseEntity.age + h;
      this.head.pivotY = 4.0F;
      this.head.pivotZ = -12.0F;
      this.body.pitch = 0.0F;
      this.head.pitch = 0.5235988F + m;
      this.head.yaw = l * 0.017453292F;
      float s = horseBaseEntity.isTouchingWater() ? 0.2F : 1.0F;
      float t = MathHelper.cos(s * f * 0.6662F + 3.1415927F);
      float u = t * 0.8F * g;
      float v = (1.0F - Math.max(o, n)) * (0.5235988F + m + q * MathHelper.sin(r) * 0.05F);
      this.head.pitch = o * (0.2617994F + m) + n * (2.1816616F + MathHelper.sin(r) * 0.05F) + v;
      this.head.yaw = o * l * 0.017453292F + (1.0F - Math.max(o, n)) * this.head.yaw;
      this.head.pivotY = o * -4.0F + n * 11.0F + (1.0F - Math.max(o, n)) * this.head.pivotY;
      this.head.pivotZ = o * -4.0F + n * -12.0F + (1.0F - Math.max(o, n)) * this.head.pivotZ;
      this.body.pitch = o * -0.7853982F + p * this.body.pitch;
      float w = 0.2617994F * o;
      float x = MathHelper.cos(r * 0.6F + 3.1415927F);
      this.leftFrontLeg.pivotY = 2.0F * o + 14.0F * p;
      this.leftFrontLeg.pivotZ = -6.0F * o - 10.0F * p;
      this.rightFrontLeg.pivotY = this.leftFrontLeg.pivotY;
      this.rightFrontLeg.pivotZ = this.leftFrontLeg.pivotZ;
      float y = (-1.0471976F + x) * o + u * p;
      float z = (-1.0471976F - x) * o - u * p;
      this.leftHindLeg.pitch = w - t * 0.5F * g * p;
      this.rightHindLeg.pitch = w + t * 0.5F * g * p;
      this.leftFrontLeg.pitch = y;
      this.rightFrontLeg.pitch = z;
      this.tail.pitch = 0.5235988F + g * 0.75F;
      this.tail.pivotY = -5.0F + g;
      this.tail.pivotZ = 2.0F + g * 2.0F;
      if (bl) {
         this.tail.yaw = MathHelper.cos(r * 0.7F);
      } else {
         this.tail.yaw = 0.0F;
      }

      this.rightHindBabyLeg.pivotY = this.rightHindLeg.pivotY;
      this.rightHindBabyLeg.pivotZ = this.rightHindLeg.pivotZ;
      this.rightHindBabyLeg.pitch = this.rightHindLeg.pitch;
      this.leftHindBabyLeg.pivotY = this.leftHindLeg.pivotY;
      this.leftHindBabyLeg.pivotZ = this.leftHindLeg.pivotZ;
      this.leftHindBabyLeg.pitch = this.leftHindLeg.pitch;
      this.rightFrontBabyLeg.pivotY = this.rightFrontLeg.pivotY;
      this.rightFrontBabyLeg.pivotZ = this.rightFrontLeg.pivotZ;
      this.rightFrontBabyLeg.pitch = this.rightFrontLeg.pitch;
      this.leftFrontBabyLeg.pivotY = this.leftFrontLeg.pivotY;
      this.leftFrontBabyLeg.pivotZ = this.leftFrontLeg.pivotZ;
      this.leftFrontBabyLeg.pitch = this.leftFrontLeg.pitch;
      boolean bl2 = horseBaseEntity.isBaby();
      this.rightHindLeg.visible = !bl2;
      this.leftHindLeg.visible = !bl2;
      this.rightFrontLeg.visible = !bl2;
      this.leftFrontLeg.visible = !bl2;
      this.rightHindBabyLeg.visible = bl2;
      this.leftHindBabyLeg.visible = bl2;
      this.rightFrontBabyLeg.visible = bl2;
      this.leftFrontBabyLeg.visible = bl2;
      this.body.pivotY = bl2 ? 10.8F : 0.0F;
   }
}
