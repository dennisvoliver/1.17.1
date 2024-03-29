package net.minecraft.client.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class SleepingChatScreen extends ChatScreen {
   public SleepingChatScreen() {
      super("");
   }

   protected void init() {
      super.init();
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 40, 200, 20, new TranslatableText("multiplayer.stopSleeping"), (button) -> {
         this.stopSleeping();
      }));
   }

   public void onClose() {
      this.stopSleeping();
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
         this.stopSleeping();
      } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
         String string = this.chatField.getText().trim();
         if (!string.isEmpty()) {
            this.sendMessage(string);
         }

         this.chatField.setText("");
         this.client.inGameHud.getChatHud().resetScroll();
         return true;
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   private void stopSleeping() {
      ClientPlayNetworkHandler clientPlayNetworkHandler = this.client.player.networkHandler;
      clientPlayNetworkHandler.sendPacket(new ClientCommandC2SPacket(this.client.player, ClientCommandC2SPacket.Mode.STOP_SLEEPING));
   }
}
