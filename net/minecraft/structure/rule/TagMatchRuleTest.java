package net.minecraft.structure.rule;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.registry.Registry;

public class TagMatchRuleTest extends RuleTest {
   public static final Codec<TagMatchRuleTest> CODEC = Tag.codec(() -> {
      return ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.BLOCK_KEY);
   }).fieldOf("tag").xmap(TagMatchRuleTest::new, (tagMatchRuleTest) -> {
      return tagMatchRuleTest.tag;
   }).codec();
   private final Tag<Block> tag;

   public TagMatchRuleTest(Tag<Block> tag) {
      this.tag = tag;
   }

   public boolean test(BlockState state, Random random) {
      return state.isIn(this.tag);
   }

   protected RuleTestType<?> getType() {
      return RuleTestType.TAG_MATCH;
   }
}
