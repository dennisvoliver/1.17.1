package net.minecraft.client.gui.screen.multiplayer;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class MultiplayerWarningScreen extends Screen {
   private final Screen parent;
   private static final Text HEADER;
   private static final Text MESSAGE;
   private static final Text CHECK_MESSAGE;
   private static final Text PROCEED_TEXT;
   private CheckboxWidget checkbox;
   private MultilineText lines;

   public MultiplayerWarningScreen(Screen parent) {
      super(NarratorManager.EMPTY);
      this.lines = MultilineText.EMPTY;
      this.parent = parent;
   }

   protected void init() {
      super.init();
      this.lines = MultilineText.create(this.textRenderer, MESSAGE, this.width - 50);
      int var10000 = this.lines.count() + 1;
      Objects.requireNonNull(this.textRenderer);
      int i = var10000 * 9 * 2;
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, 100 + i, 150, 20, ScreenTexts.PROCEED, (button) -> {
         if (this.checkbox.isChecked()) {
            this.client.options.skipMultiplayerWarning = true;
            this.client.options.write();
         }

         this.client.setScreen(new MultiplayerScreen(this.parent));
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 155 + 160, 100 + i, 150, 20, ScreenTexts.BACK, (button) -> {
         this.client.setScreen(this.parent);
      }));
      this.checkbox = new CheckboxWidget(this.width / 2 - 155 + 80, 76 + i, 150, 20, CHECK_MESSAGE, false);
      this.addDrawableChild(this.checkbox);
   }

   public Text getNarratedTitle() {
      return PROCEED_TEXT;
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackgroundTexture(0);
      drawTextWithShadow(matrices, this.textRenderer, HEADER, 25, 30, 16777215);
      MultilineText var10000 = this.lines;
      Objects.requireNonNull(this.textRenderer);
      var10000.drawWithShadow(matrices, 25, 70, 9 * 2, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }

   static {
      HEADER = (new TranslatableText("multiplayerWarning.header")).formatted(Formatting.BOLD);
      MESSAGE = new TranslatableText("multiplayerWarning.message");
      CHECK_MESSAGE = new TranslatableText("multiplayerWarning.check");
      PROCEED_TEXT = HEADER.shallowCopy().append("\n").append(MESSAGE);
   }
}
