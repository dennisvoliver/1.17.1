package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class StatsScreen extends Screen implements StatsListener {
   private static final Text DOWNLOADING_STATS_TEXT = new TranslatableText("multiplayer.downloadingStats");
   protected final Screen parent;
   private StatsScreen.GeneralStatsListWidget generalStats;
   StatsScreen.ItemStatsListWidget itemStats;
   private StatsScreen.EntityStatsListWidget mobStats;
   final StatHandler statHandler;
   @Nullable
   private AlwaysSelectedEntryListWidget<?> selectedList;
   private boolean downloadingStats = true;
   private static final int field_32281 = 128;
   private static final int field_32282 = 18;
   private static final int field_32283 = 20;
   private static final int field_32284 = 1;
   private static final int field_32285 = 1;
   private static final int field_32274 = 2;
   private static final int field_32275 = 2;
   private static final int field_32276 = 40;
   private static final int field_32277 = 5;
   private static final int field_32278 = 0;
   private static final int field_32279 = -1;
   private static final int field_32280 = 1;

   public StatsScreen(Screen parent, StatHandler statHandler) {
      super(new TranslatableText("gui.stats"));
      this.parent = parent;
      this.statHandler = statHandler;
   }

   protected void init() {
      this.downloadingStats = true;
      this.client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
   }

   public void createLists() {
      this.generalStats = new StatsScreen.GeneralStatsListWidget(this.client);
      this.itemStats = new StatsScreen.ItemStatsListWidget(this.client);
      this.mobStats = new StatsScreen.EntityStatsListWidget(this.client);
   }

   public void createButtons() {
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 120, this.height - 52, 80, 20, new TranslatableText("stat.generalButton"), (button) -> {
         this.selectStatList(this.generalStats);
      }));
      ButtonWidget buttonWidget = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 - 40, this.height - 52, 80, 20, new TranslatableText("stat.itemsButton"), (button) -> {
         this.selectStatList(this.itemStats);
      }));
      ButtonWidget buttonWidget2 = (ButtonWidget)this.addDrawableChild(new ButtonWidget(this.width / 2 + 40, this.height - 52, 80, 20, new TranslatableText("stat.mobsButton"), (button) -> {
         this.selectStatList(this.mobStats);
      }));
      this.addDrawableChild(new ButtonWidget(this.width / 2 - 100, this.height - 28, 200, 20, ScreenTexts.DONE, (button) -> {
         this.client.setScreen(this.parent);
      }));
      if (this.itemStats.children().isEmpty()) {
         buttonWidget.active = false;
      }

      if (this.mobStats.children().isEmpty()) {
         buttonWidget2.active = false;
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.downloadingStats) {
         this.renderBackground(matrices);
         drawCenteredText(matrices, this.textRenderer, DOWNLOADING_STATS_TEXT, this.width / 2, this.height / 2, 16777215);
         TextRenderer var10001 = this.textRenderer;
         String var10002 = PROGRESS_BAR_STAGES[(int)(Util.getMeasuringTimeMs() / 150L % (long)PROGRESS_BAR_STAGES.length)];
         int var10003 = this.width / 2;
         int var10004 = this.height / 2;
         Objects.requireNonNull(this.textRenderer);
         drawCenteredText(matrices, var10001, var10002, var10003, var10004 + 9 * 2, 16777215);
      } else {
         this.getSelectedStatList().render(matrices, mouseX, mouseY, delta);
         drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
         super.render(matrices, mouseX, mouseY, delta);
      }

   }

   public void onStatsReady() {
      if (this.downloadingStats) {
         this.createLists();
         this.createButtons();
         this.selectStatList(this.generalStats);
         this.downloadingStats = false;
      }

   }

   public boolean isPauseScreen() {
      return !this.downloadingStats;
   }

   @Nullable
   public AlwaysSelectedEntryListWidget<?> getSelectedStatList() {
      return this.selectedList;
   }

   public void selectStatList(@Nullable AlwaysSelectedEntryListWidget<?> list) {
      if (this.selectedList != null) {
         this.remove(this.selectedList);
      }

      if (list != null) {
         this.addSelectableChild(list);
         this.selectedList = list;
      }

   }

   static String getStatTranslationKey(Stat<Identifier> stat) {
      String var10000 = ((Identifier)stat.getValue()).toString();
      return "stat." + var10000.replace(':', '.');
   }

   int getColumnX(int index) {
      return 115 + 40 * index;
   }

   void renderStatItem(MatrixStack matrices, int x, int y, Item item) {
      this.renderIcon(matrices, x + 1, y + 1, 0, 0);
      this.itemRenderer.renderGuiItemIcon(item.getDefaultStack(), x + 2, y + 2);
   }

   void renderIcon(MatrixStack matrices, int x, int y, int u, int v) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, STATS_ICON_TEXTURE);
      drawTexture(matrices, x, y, this.getZOffset(), (float)u, (float)v, 18, 18, 128, 128);
   }

   @Environment(EnvType.CLIENT)
   class GeneralStatsListWidget extends AlwaysSelectedEntryListWidget<StatsScreen.GeneralStatsListWidget.Entry> {
      public GeneralStatsListWidget(MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
         ObjectArrayList<Stat<Identifier>> objectArrayList = new ObjectArrayList(Stats.CUSTOM.iterator());
         objectArrayList.sort(Comparator.comparing((statx) -> {
            return I18n.translate(StatsScreen.getStatTranslationKey(statx));
         }));
         ObjectListIterator var4 = objectArrayList.iterator();

         while(var4.hasNext()) {
            Stat<Identifier> stat = (Stat)var4.next();
            this.addEntry(new StatsScreen.GeneralStatsListWidget.Entry(stat));
         }

      }

      protected void renderBackground(MatrixStack matrices) {
         StatsScreen.this.renderBackground(matrices);
      }

      @Environment(EnvType.CLIENT)
      private class Entry extends AlwaysSelectedEntryListWidget.Entry<StatsScreen.GeneralStatsListWidget.Entry> {
         private final Stat<Identifier> stat;
         private final Text displayName;

         Entry(Stat<Identifier> stat) {
            this.stat = stat;
            this.displayName = new TranslatableText(StatsScreen.getStatTranslationKey(stat));
         }

         private String getFormatted() {
            return this.stat.format(StatsScreen.this.statHandler.getStat(this.stat));
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            DrawableHelper.drawTextWithShadow(matrices, StatsScreen.this.textRenderer, this.displayName, x + 2, y + 1, index % 2 == 0 ? 16777215 : 9474192);
            String string = this.getFormatted();
            DrawableHelper.drawStringWithShadow(matrices, StatsScreen.this.textRenderer, string, x + 2 + 213 - StatsScreen.this.textRenderer.getWidth(string), y + 1, index % 2 == 0 ? 16777215 : 9474192);
         }

         public Text getNarration() {
            return new TranslatableText("narrator.select", new Object[]{(new LiteralText("")).append(this.displayName).append(" ").append(this.getFormatted())});
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private class ItemStatsListWidget extends AlwaysSelectedEntryListWidget<StatsScreen.ItemStatsListWidget.Entry> {
      protected final List<StatType<Block>> blockStatTypes = Lists.newArrayList();
      protected final List<StatType<Item>> itemStatTypes;
      private final int[] HEADER_ICON_SPRITE_INDICES = new int[]{3, 4, 1, 2, 5, 6};
      protected int selectedHeaderColumn = -1;
      protected final Comparator<StatsScreen.ItemStatsListWidget.Entry> comparator = new StatsScreen.ItemStatsListWidget.ItemComparator();
      @Nullable
      protected StatType<?> selectedStatType;
      protected int listOrder;

      public ItemStatsListWidget(MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
         this.blockStatTypes.add(Stats.MINED);
         this.itemStatTypes = Lists.newArrayList((Object[])(Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED));
         this.setRenderHeader(true, 20);
         Set<Item> set = Sets.newIdentityHashSet();
         Iterator var4 = Registry.ITEM.iterator();

         Item item2;
         boolean bl2;
         Iterator var7;
         StatType statType2;
         while(var4.hasNext()) {
            item2 = (Item)var4.next();
            bl2 = false;
            var7 = this.itemStatTypes.iterator();

            while(var7.hasNext()) {
               statType2 = (StatType)var7.next();
               if (statType2.hasStat(item2) && StatsScreen.this.statHandler.getStat(statType2.getOrCreateStat(item2)) > 0) {
                  bl2 = true;
               }
            }

            if (bl2) {
               set.add(item2);
            }
         }

         var4 = Registry.BLOCK.iterator();

         while(var4.hasNext()) {
            Block block = (Block)var4.next();
            bl2 = false;
            var7 = this.blockStatTypes.iterator();

            while(var7.hasNext()) {
               statType2 = (StatType)var7.next();
               if (statType2.hasStat(block) && StatsScreen.this.statHandler.getStat(statType2.getOrCreateStat(block)) > 0) {
                  bl2 = true;
               }
            }

            if (bl2) {
               set.add(block.asItem());
            }
         }

         set.remove(Items.AIR);
         var4 = set.iterator();

         while(var4.hasNext()) {
            item2 = (Item)var4.next();
            this.addEntry(new StatsScreen.ItemStatsListWidget.Entry(item2));
         }

      }

      protected void renderHeader(MatrixStack matrices, int x, int y, Tessellator tessellator) {
         if (!this.client.mouse.wasLeftButtonClicked()) {
            this.selectedHeaderColumn = -1;
         }

         int l;
         for(l = 0; l < this.HEADER_ICON_SPRITE_INDICES.length; ++l) {
            StatsScreen.this.renderIcon(matrices, x + StatsScreen.this.getColumnX(l) - 18, y + 1, 0, this.selectedHeaderColumn == l ? 0 : 18);
         }

         int m;
         if (this.selectedStatType != null) {
            l = StatsScreen.this.getColumnX(this.getHeaderIndex(this.selectedStatType)) - 36;
            m = this.listOrder == 1 ? 2 : 1;
            StatsScreen.this.renderIcon(matrices, x + l, y + 1, 18 * m, 0);
         }

         for(l = 0; l < this.HEADER_ICON_SPRITE_INDICES.length; ++l) {
            m = this.selectedHeaderColumn == l ? 1 : 0;
            StatsScreen.this.renderIcon(matrices, x + StatsScreen.this.getColumnX(l) - 18 + m, y + 1 + m, 18 * this.HEADER_ICON_SPRITE_INDICES[l], 18);
         }

      }

      public int getRowWidth() {
         return 375;
      }

      protected int getScrollbarPositionX() {
         return this.width / 2 + 140;
      }

      protected void renderBackground(MatrixStack matrices) {
         StatsScreen.this.renderBackground(matrices);
      }

      protected void clickedHeader(int x, int y) {
         this.selectedHeaderColumn = -1;

         for(int i = 0; i < this.HEADER_ICON_SPRITE_INDICES.length; ++i) {
            int j = x - StatsScreen.this.getColumnX(i);
            if (j >= -36 && j <= 0) {
               this.selectedHeaderColumn = i;
               break;
            }
         }

         if (this.selectedHeaderColumn >= 0) {
            this.selectStatType(this.getStatType(this.selectedHeaderColumn));
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

      }

      private StatType<?> getStatType(int headerColumn) {
         return headerColumn < this.blockStatTypes.size() ? (StatType)this.blockStatTypes.get(headerColumn) : (StatType)this.itemStatTypes.get(headerColumn - this.blockStatTypes.size());
      }

      private int getHeaderIndex(StatType<?> statType) {
         int i = this.blockStatTypes.indexOf(statType);
         if (i >= 0) {
            return i;
         } else {
            int j = this.itemStatTypes.indexOf(statType);
            return j >= 0 ? j + this.blockStatTypes.size() : -1;
         }
      }

      protected void renderDecorations(MatrixStack matrices, int mouseX, int mouseY) {
         if (mouseY >= this.top && mouseY <= this.bottom) {
            StatsScreen.ItemStatsListWidget.Entry entry = (StatsScreen.ItemStatsListWidget.Entry)this.getHoveredEntry();
            int i = (this.width - this.getRowWidth()) / 2;
            if (entry != null) {
               if (mouseX < i + 40 || mouseX > i + 40 + 20) {
                  return;
               }

               Item item = entry.getItem();
               this.render(matrices, this.getText(item), mouseX, mouseY);
            } else {
               Text text = null;
               int j = mouseX - i;

               for(int k = 0; k < this.HEADER_ICON_SPRITE_INDICES.length; ++k) {
                  int l = StatsScreen.this.getColumnX(k);
                  if (j >= l - 18 && j <= l) {
                     text = this.getStatType(k).getName();
                     break;
                  }
               }

               this.render(matrices, text, mouseX, mouseY);
            }

         }
      }

      protected void render(MatrixStack matrices, @Nullable Text text, int mouseX, int mouseY) {
         if (text != null) {
            int i = mouseX + 12;
            int j = mouseY - 12;
            int k = StatsScreen.this.textRenderer.getWidth((StringVisitable)text);
            this.fillGradient(matrices, i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
            matrices.push();
            matrices.translate(0.0D, 0.0D, 400.0D);
            StatsScreen.this.textRenderer.drawWithShadow(matrices, (Text)text, (float)i, (float)j, -1);
            matrices.pop();
         }
      }

      protected Text getText(Item item) {
         return item.getName();
      }

      protected void selectStatType(StatType<?> statType) {
         if (statType != this.selectedStatType) {
            this.selectedStatType = statType;
            this.listOrder = -1;
         } else if (this.listOrder == -1) {
            this.listOrder = 1;
         } else {
            this.selectedStatType = null;
            this.listOrder = 0;
         }

         this.children().sort(this.comparator);
      }

      @Environment(EnvType.CLIENT)
      class ItemComparator implements Comparator<StatsScreen.ItemStatsListWidget.Entry> {
         public int compare(StatsScreen.ItemStatsListWidget.Entry entry, StatsScreen.ItemStatsListWidget.Entry entry2) {
            Item item = entry.getItem();
            Item item2 = entry2.getItem();
            int k;
            int n;
            if (ItemStatsListWidget.this.selectedStatType == null) {
               k = 0;
               n = 0;
            } else {
               StatType statType;
               if (ItemStatsListWidget.this.blockStatTypes.contains(ItemStatsListWidget.this.selectedStatType)) {
                  statType = ItemStatsListWidget.this.selectedStatType;
                  k = item instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item).getBlock()) : -1;
                  n = item2 instanceof BlockItem ? StatsScreen.this.statHandler.getStat(statType, ((BlockItem)item2).getBlock()) : -1;
               } else {
                  statType = ItemStatsListWidget.this.selectedStatType;
                  k = StatsScreen.this.statHandler.getStat(statType, item);
                  n = StatsScreen.this.statHandler.getStat(statType, item2);
               }
            }

            return k == n ? ItemStatsListWidget.this.listOrder * Integer.compare(Item.getRawId(item), Item.getRawId(item2)) : ItemStatsListWidget.this.listOrder * Integer.compare(k, n);
         }
      }

      @Environment(EnvType.CLIENT)
      private class Entry extends AlwaysSelectedEntryListWidget.Entry<StatsScreen.ItemStatsListWidget.Entry> {
         private final Item item;

         Entry(Item item) {
            this.item = item;
         }

         public Item getItem() {
            return this.item;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            StatsScreen.this.renderStatItem(matrices, x + 40, y, this.item);

            int i;
            for(i = 0; i < StatsScreen.this.itemStats.blockStatTypes.size(); ++i) {
               Stat stat2;
               if (this.item instanceof BlockItem) {
                  stat2 = ((StatType)StatsScreen.this.itemStats.blockStatTypes.get(i)).getOrCreateStat(((BlockItem)this.item).getBlock());
               } else {
                  stat2 = null;
               }

               this.render(matrices, stat2, x + StatsScreen.this.getColumnX(i), y, index % 2 == 0);
            }

            for(i = 0; i < StatsScreen.this.itemStats.itemStatTypes.size(); ++i) {
               this.render(matrices, ((StatType)StatsScreen.this.itemStats.itemStatTypes.get(i)).getOrCreateStat(this.item), x + StatsScreen.this.getColumnX(i + StatsScreen.this.itemStats.blockStatTypes.size()), y, index % 2 == 0);
            }

         }

         protected void render(MatrixStack matrices, @Nullable Stat<?> stat, int x, int y, boolean white) {
            String string = stat == null ? "-" : stat.format(StatsScreen.this.statHandler.getStat(stat));
            DrawableHelper.drawStringWithShadow(matrices, StatsScreen.this.textRenderer, string, x - StatsScreen.this.textRenderer.getWidth(string), y + 5, white ? 16777215 : 9474192);
         }

         public Text getNarration() {
            return new TranslatableText("narrator.select", new Object[]{this.item.getName()});
         }
      }
   }

   @Environment(EnvType.CLIENT)
   class EntityStatsListWidget extends AlwaysSelectedEntryListWidget<StatsScreen.EntityStatsListWidget.Entry> {
      public EntityStatsListWidget(MinecraftClient client) {
         int var10002 = StatsScreen.this.width;
         int var10003 = StatsScreen.this.height;
         int var10005 = StatsScreen.this.height - 64;
         Objects.requireNonNull(StatsScreen.this.textRenderer);
         super(client, var10002, var10003, 32, var10005, 9 * 4);
         Iterator var3 = Registry.ENTITY_TYPE.iterator();

         while(true) {
            EntityType entityType;
            do {
               if (!var3.hasNext()) {
                  return;
               }

               entityType = (EntityType)var3.next();
            } while(StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType)) <= 0 && StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType)) <= 0);

            this.addEntry(new StatsScreen.EntityStatsListWidget.Entry(entityType));
         }
      }

      protected void renderBackground(MatrixStack matrices) {
         StatsScreen.this.renderBackground(matrices);
      }

      @Environment(EnvType.CLIENT)
      private class Entry extends AlwaysSelectedEntryListWidget.Entry<StatsScreen.EntityStatsListWidget.Entry> {
         private final Text entityTypeName;
         private final Text killedText;
         private final boolean killedAny;
         private final Text killedByText;
         private final boolean killedByAny;

         public Entry(EntityType<?> entityType) {
            this.entityTypeName = entityType.getName();
            int i = StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType));
            if (i == 0) {
               this.killedText = new TranslatableText("stat_type.minecraft.killed.none", new Object[]{this.entityTypeName});
               this.killedAny = false;
            } else {
               this.killedText = new TranslatableText("stat_type.minecraft.killed", new Object[]{i, this.entityTypeName});
               this.killedAny = true;
            }

            int j = StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType));
            if (j == 0) {
               this.killedByText = new TranslatableText("stat_type.minecraft.killed_by.none", new Object[]{this.entityTypeName});
               this.killedByAny = false;
            } else {
               this.killedByText = new TranslatableText("stat_type.minecraft.killed_by", new Object[]{this.entityTypeName, j});
               this.killedByAny = true;
            }

         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            DrawableHelper.drawTextWithShadow(matrices, StatsScreen.this.textRenderer, this.entityTypeName, x + 2, y + 1, 16777215);
            TextRenderer var10001 = StatsScreen.this.textRenderer;
            Text var10002 = this.killedText;
            int var10003 = x + 2 + 10;
            int var10004 = y + 1;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            DrawableHelper.drawTextWithShadow(matrices, var10001, var10002, var10003, var10004 + 9, this.killedAny ? 9474192 : 6316128);
            var10001 = StatsScreen.this.textRenderer;
            var10002 = this.killedByText;
            var10003 = x + 2 + 10;
            var10004 = y + 1;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            DrawableHelper.drawTextWithShadow(matrices, var10001, var10002, var10003, var10004 + 9 * 2, this.killedByAny ? 9474192 : 6316128);
         }

         public Text getNarration() {
            return new TranslatableText("narrator.select", new Object[]{ScreenTexts.joinSentences(this.killedText, this.killedByText)});
         }
      }
   }
}
