package net.minecraft.client.gui.widget;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public abstract class ElementListWidget<E extends ElementListWidget.Entry<E>> extends EntryListWidget<E> {
   private boolean widgetFocused;

   public ElementListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m) {
      super(minecraftClient, i, j, k, l, m);
   }

   public boolean changeFocus(boolean lookForwards) {
      this.widgetFocused = super.changeFocus(lookForwards);
      if (this.widgetFocused) {
         this.ensureVisible((ElementListWidget.Entry)this.getFocused());
      }

      return this.widgetFocused;
   }

   public Selectable.SelectionType getType() {
      return this.widgetFocused ? Selectable.SelectionType.FOCUSED : super.getType();
   }

   protected boolean isSelectedEntry(int index) {
      return false;
   }

   public void appendNarrations(NarrationMessageBuilder builder) {
      E entry = (ElementListWidget.Entry)this.getHoveredEntry();
      if (entry != null) {
         entry.appendNarrations(builder.nextMessage());
         this.appendNarrations(builder, entry);
      } else {
         E entry2 = (ElementListWidget.Entry)this.getFocused();
         if (entry2 != null) {
            entry2.appendNarrations(builder.nextMessage());
            this.appendNarrations(builder, entry2);
         }
      }

      builder.put(NarrationPart.USAGE, (Text)(new TranslatableText("narration.component_list.usage")));
   }

   @Environment(EnvType.CLIENT)
   public abstract static class Entry<E extends ElementListWidget.Entry<E>> extends EntryListWidget.Entry<E> implements ParentElement {
      @Nullable
      private Element focused;
      @Nullable
      private Selectable focusedSelectable;
      private boolean dragging;

      public boolean isDragging() {
         return this.dragging;
      }

      public void setDragging(boolean dragging) {
         this.dragging = dragging;
      }

      public void setFocused(@Nullable Element focused) {
         this.focused = focused;
      }

      @Nullable
      public Element getFocused() {
         return this.focused;
      }

      public abstract List<? extends Selectable> selectableChildren();

      void appendNarrations(NarrationMessageBuilder builder) {
         List<? extends Selectable> list = this.selectableChildren();
         Screen.SelectedElementNarrationData selectedElementNarrationData = Screen.findSelectedElementData(list, this.focusedSelectable);
         if (selectedElementNarrationData != null) {
            if (selectedElementNarrationData.selectType.isFocused()) {
               this.focusedSelectable = selectedElementNarrationData.selectable;
            }

            if (list.size() > 1) {
               builder.put(NarrationPart.POSITION, (Text)(new TranslatableText("narrator.position.object_list", new Object[]{selectedElementNarrationData.index + 1, list.size()})));
               if (selectedElementNarrationData.selectType == Selectable.SelectionType.FOCUSED) {
                  builder.put(NarrationPart.USAGE, (Text)(new TranslatableText("narration.component_list.usage")));
               }
            }

            selectedElementNarrationData.selectable.appendNarrations(builder.nextMessage());
         }

      }
   }
}
