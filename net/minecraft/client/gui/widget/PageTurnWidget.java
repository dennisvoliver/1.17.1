package net.minecraft.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;

@Environment(EnvType.CLIENT)
public class PageTurnWidget extends ButtonWidget {
   private final boolean isNextPageButton;
   private final boolean playPageTurnSound;

   public PageTurnWidget(int x, int y, boolean isNextPageButton, ButtonWidget.PressAction action, boolean playPageTurnSound) {
      super(x, y, 23, 13, LiteralText.EMPTY, action);
      this.isNextPageButton = isNextPageButton;
      this.playPageTurnSound = playPageTurnSound;
   }

   public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShaderTexture(0, BookScreen.BOOK_TEXTURE);
      int i = 0;
      int j = 192;
      if (this.isHovered()) {
         i += 23;
      }

      if (!this.isNextPageButton) {
         j += 13;
      }

      this.drawTexture(matrices, this.x, this.y, i, j, 23, 13);
   }

   public void playDownSound(SoundManager soundManager) {
      if (this.playPageTurnSound) {
         soundManager.play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0F));
      }

   }
}
