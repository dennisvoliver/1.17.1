package net.minecraft.structure.rule;

import com.mojang.datafixers.util.Function5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

public class AxisAlignedLinearPosRuleTest extends PosRuleTest {
   public static final Codec<AxisAlignedLinearPosRuleTest> CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter((axisAlignedLinearPosRuleTest) -> {
         return axisAlignedLinearPosRuleTest.minChance;
      }), Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter((axisAlignedLinearPosRuleTest) -> {
         return axisAlignedLinearPosRuleTest.maxChance;
      }), Codec.INT.fieldOf("min_dist").orElse(0).forGetter((axisAlignedLinearPosRuleTest) -> {
         return axisAlignedLinearPosRuleTest.minDistance;
      }), Codec.INT.fieldOf("max_dist").orElse(0).forGetter((axisAlignedLinearPosRuleTest) -> {
         return axisAlignedLinearPosRuleTest.maxDistance;
      }), Direction.Axis.CODEC.fieldOf("axis").orElse(Direction.Axis.Y).forGetter((axisAlignedLinearPosRuleTest) -> {
         return axisAlignedLinearPosRuleTest.axis;
      })).apply(instance, (Function5)(AxisAlignedLinearPosRuleTest::new));
   });
   private final float minChance;
   private final float maxChance;
   private final int minDistance;
   private final int maxDistance;
   private final Direction.Axis axis;

   public AxisAlignedLinearPosRuleTest(float minChance, float maxChance, int minDistance, int maxDistance, Direction.Axis axis) {
      if (minDistance >= maxDistance) {
         throw new IllegalArgumentException("Invalid range: [" + minDistance + "," + maxDistance + "]");
      } else {
         this.minChance = minChance;
         this.maxChance = maxChance;
         this.minDistance = minDistance;
         this.maxDistance = maxDistance;
         this.axis = axis;
      }
   }

   public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos pivot, Random random) {
      Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
      float f = (float)Math.abs((blockPos2.getX() - pivot.getX()) * direction.getOffsetX());
      float g = (float)Math.abs((blockPos2.getY() - pivot.getY()) * direction.getOffsetY());
      float h = (float)Math.abs((blockPos2.getZ() - pivot.getZ()) * direction.getOffsetZ());
      int i = (int)(f + g + h);
      float j = random.nextFloat();
      return (double)j <= MathHelper.clampedLerp((double)this.minChance, (double)this.maxChance, MathHelper.getLerpProgress((double)i, (double)this.minDistance, (double)this.maxDistance));
   }

   protected PosRuleTestType<?> getType() {
      return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS;
   }
}
