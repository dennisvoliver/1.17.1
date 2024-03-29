package net.minecraft.client.gui.screen.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SoundSliderWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.TranslatableText;

@Environment(EnvType.CLIENT)
public class SoundOptionsScreen extends GameOptionsScreen {
   public SoundOptionsScreen(Screen parent, GameOptions options) {
      super(parent, options, new TranslatableText("options.sounds.title"));
   }

   protected void init() {
      int i = 0;
      this.addDrawableChild(new SoundSliderWidget(this.client, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), SoundCategory.MASTER, 310));
      int i = i + 2;
      SoundCategory[] var2 = SoundCategory.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         SoundCategory soundCategory = var2[var4];
         if (soundCategory != SoundCategory.MASTER) {
            this.addDrawableChild(new SoundSliderWidget(this.client, this.width / 2 - 155 + i % 2 * 160, this.height / 6 - 12 + 24 * (i >> 1), soundCategory, 150));
            ++i;
         }
      }

      int var10003 = this.width / 2 - 75;
      int var10004 = this.height / 6 - 12;
      ++i;
      this.addDrawableChild(Option.SUBTITLES.createButton(this.gameOptions, var10003, var10004 + 24 * (i >> 1), 150));
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, (button) -> {
         this.client.setScreen(this.parent);
      }));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
