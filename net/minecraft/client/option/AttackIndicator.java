package net.minecraft.client.option;

import java.util.Arrays;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public enum AttackIndicator {
   OFF(0, "options.off"),
   CROSSHAIR(1, "options.attack.crosshair"),
   HOTBAR(2, "options.attack.hotbar");

   private static final AttackIndicator[] VALUES = (AttackIndicator[])Arrays.stream(values()).sorted(Comparator.comparingInt(AttackIndicator::getId)).toArray((i) -> {
      return new AttackIndicator[i];
   });
   private final int id;
   private final String translationKey;

   private AttackIndicator(int id, String translationKey) {
      this.id = id;
      this.translationKey = translationKey;
   }

   public int getId() {
      return this.id;
   }

   public String getTranslationKey() {
      return this.translationKey;
   }

   public static AttackIndicator byId(int id) {
      return VALUES[MathHelper.floorMod(id, VALUES.length)];
   }
}
