package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.NetworkUtils;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.GameMode;

@Environment(EnvType.CLIENT)
public class OpenToLanScreen extends Screen {
   private static final Text ALLOW_COMMANDS_TEXT = new TranslatableText("selectWorld.allowCommands");
   private static final Text GAME_MODE_TEXT = new TranslatableText("selectWorld.gameMode");
   private static final Text OTHER_PLAYERS_TEXT = new TranslatableText("lanServer.otherPlayers");
   private final Screen parent;
   private GameMode gameMode;
   private boolean allowCommands;

   public OpenToLanScreen(Screen parent) {
      super(new TranslatableText("lanServer.title"));
      this.gameMode = GameMode.SURVIVAL;
      this.parent = parent;
   }

   protected void init() {
      this.addDrawableChild(CyclingButtonWidget.builder(GameMode::getSimpleTranslatableName).values((Object[])(GameMode.SURVIVAL, GameMode.SPECTATOR, GameMode.CREATIVE, GameMode.ADVENTURE)).initially(this.gameMode).build(this.width / 2 - 155, 100, 150, 20, GAME_MODE_TEXT, (button, gameMode) -> {
         this.gameMode = gameMode;
      }));
      this.addDrawableChild(CyclingButtonWidget.onOffBuilder(this.allowCommands).build(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_TEXT, (button, allowCommands) -> {
         this.allowCommands = allowCommands;
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableText("lanServer.start"), (button) -> {
         this.client.setScreen((Screen)null);
         int i = NetworkUtils.findLocalPort();
         TranslatableText text2;
         if (this.client.getServer().openToLan(this.gameMode, this.allowCommands, i)) {
            text2 = new TranslatableText("commands.publish.started", new Object[]{i});
         } else {
            text2 = new TranslatableText("commands.publish.failed");
         }

         this.client.inGameHud.getChatHud().addMessage(text2);
         this.client.updateWindowTitle();
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height - 28, 150, 20, ScreenTexts.CANCEL, (button) -> {
         this.client.setScreen(this.parent);
      }));
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 50, 16777215);
      drawCenteredText(matrices, this.textRenderer, OTHER_PLAYERS_TEXT, this.width / 2, 82, 16777215);
      super.render(matrices, mouseX, mouseY, delta);
   }
}
