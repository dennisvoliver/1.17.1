package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class GameMenuScreen extends Screen {
   private static final String SNAPSHOT_FEEDBACK_URL = "https://aka.ms/snapshotfeedback?ref=game";
   private static final String JAVA_FEEDBACK_URL = "https://aka.ms/javafeedback?ref=game";
   private static final String SNAPSHOT_BUGS_URL = "https://aka.ms/snapshotbugs?ref=game";
   private final boolean showMenu;

   public GameMenuScreen(boolean showMenu) {
      super(showMenu ? new TranslatableText("menu.game") : new TranslatableText("menu.paused"));
      this.showMenu = showMenu;
   }

   protected void init() {
      if (this.showMenu) {
         this.initWidgets();
      }

   }

   private void initWidgets() {
      int i = true;
      int j = true;
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslatableText("menu.returnToGame"), (button) -> {
         this.client.setScreen((Screen)null);
         this.client.mouse.lockCursor();
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslatableText("gui.advancements"), (button) -> {
         this.client.setScreen(new AdvancementsScreen(this.client.player.networkHandler.getAdvancementHandler()));
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslatableText("gui.stats"), (button) -> {
         this.client.setScreen(new StatsScreen(this, this.client.player.getStatHandler()));
      }));
      String string = SharedConstants.getGameVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslatableText("menu.sendFeedback"), (button) -> {
         this.client.setScreen(new ConfirmChatLinkScreen((confirmed) -> {
            if (confirmed) {
               Util.getOperatingSystem().open(string);
            }

            this.client.setScreen(this);
         }, string, true));
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TranslatableText("menu.reportBugs"), (button) -> {
         this.client.setScreen(new ConfirmChatLinkScreen((confirmed) -> {
            if (confirmed) {
               Util.getOperatingSystem().open("https://aka.ms/snapshotbugs?ref=game");
            }

            this.client.setScreen(this);
         }, "https://aka.ms/snapshotbugs?ref=game", true));
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslatableText("menu.options"), (button) -> {
         this.client.setScreen(new OptionsScreen(this, this.client.options));
      }));
      ButtonWidget buttonWidget = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslatableText("menu.shareToLan"), (button) -> {
         this.client.setScreen(new OpenToLanScreen(this));
      }));
      buttonWidget.active = this.client.isIntegratedServerRunning() && !this.client.getServer().isRemote();
      Text text = this.client.isInSingleplayer() ? new TranslatableText("menu.returnToMenu") : new TranslatableText("menu.disconnect");
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, text, (button) -> {
         boolean bl = this.client.isInSingleplayer();
         boolean bl2 = this.client.isConnectedToRealms();
         button.active = false;
         this.client.world.disconnect();
         if (bl) {
            this.client.disconnect(new SaveLevelScreen(new TranslatableText("menu.savingLevel")));
         } else {
            this.client.disconnect();
         }

         TitleScreen titleScreen = new TitleScreen();
         if (bl) {
            this.client.setScreen(titleScreen);
         } else if (bl2) {
            this.client.setScreen(new RealmsMainScreen(titleScreen));
         } else {
            this.client.setScreen(new MultiplayerScreen(titleScreen));
         }

      }));
   }

   public void tick() {
      super.tick();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.showMenu) {
         this.renderBackground(matrices);
         drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
      } else {
         drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 10, 16777215);
      }

      super.render(matrices, mouseX, mouseY, delta);
   }
}
