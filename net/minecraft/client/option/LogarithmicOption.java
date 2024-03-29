package net.minecraft.client.option;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class LogarithmicOption extends DoubleOption {
   public LogarithmicOption(String string, double d, double e, float f, Function<GameOptions, Double> function, BiConsumer<GameOptions, Double> biConsumer, BiFunction<GameOptions, DoubleOption, Text> biFunction) {
      super(string, d, e, f, function, biConsumer, biFunction);
   }

   public double getRatio(double value) {
      return Math.log(value / this.min) / Math.log(this.max / this.min);
   }

   public double getValue(double ratio) {
      return this.min * Math.pow(2.718281828459045D, Math.log(this.max / this.min) * ratio);
   }
}
