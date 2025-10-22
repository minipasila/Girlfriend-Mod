/*
 * External method calls:
 *   Lnet/minecraft/component/type/WritableBookContentComponent;stream(Z)Ljava/util/stream/Stream;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget;builder()Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;textColor(I)Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;cursorColor(I)Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;textShadow(Z)Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;x(I)Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;y(I)Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/EditBoxWidget$Builder;build(Lnet/minecraft/client/font/TextRenderer;IILnet/minecraft/text/Text;)Lnet/minecraft/client/gui/widget/EditBoxWidget;
 *   Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;build()Lnet/minecraft/client/gui/widget/ButtonWidget;
 *   Lnet/minecraft/screen/ScreenTexts;joinSentences([Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V
 *   Lnet/minecraft/client/gui/widget/PageTurnWidget;onPress(Lnet/minecraft/client/input/AbstractInput;)V
 *   Lnet/minecraft/client/gui/screen/Screen;keyPressed(Lnet/minecraft/client/input/KeyInput;)Z
 *   Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V
 *   Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V
 *   Lnet/minecraft/client/gui/screen/Screen;renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V
 *   Lnet/minecraft/client/gui/DrawContext;drawTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIFFIIII)V
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/gui/screen/ingame/BookEditScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.BookSigningScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Hand;

@Environment(value=EnvType.CLIENT)
public class BookEditScreen
extends Screen {
    public static final int MAX_TEXT_WIDTH = 114;
    public static final int MAX_TEXT_HEIGHT = 126;
    public static final int WIDTH = 192;
    public static final int HEIGHT = 192;
    public static final int field_52805 = 256;
    public static final int field_52806 = 256;
    private static final Text TITLE_TEXT = Text.translatable("book.edit.title");
    private final PlayerEntity player;
    private final ItemStack stack;
    private final BookSigningScreen signingScreen;
    private int currentPage;
    private final List<String> pages = Lists.newArrayList();
    private PageTurnWidget nextPageButton;
    private PageTurnWidget previousPageButton;
    private final Hand hand;
    private Text pageIndicatorText = ScreenTexts.EMPTY;
    private EditBoxWidget editBox;

    public BookEditScreen(PlayerEntity player, ItemStack stack, Hand hand, WritableBookContentComponent writableBookContent) {
        super(TITLE_TEXT);
        this.player = player;
        this.stack = stack;
        this.hand = hand;
        writableBookContent.stream(MinecraftClient.getInstance().shouldFilterText()).forEach(this.pages::add);
        if (this.pages.isEmpty()) {
            this.pages.add("");
        }
        this.signingScreen = new BookSigningScreen(this, player, hand, this.pages);
    }

    private int countPages() {
        return this.pages.size();
    }

    @Override
    protected void init() {
        int i = (this.width - 192) / 2;
        int j = 2;
        int k = 8;
        this.editBox = EditBoxWidget.builder().hasOverlay(false).textColor(-16777216).cursorColor(-16777216).hasBackground(false).textShadow(false).x((this.width - 114) / 2 - 8).y(28).build(this.textRenderer, 122, 134, ScreenTexts.EMPTY);
        this.editBox.setMaxLength(1024);
        this.editBox.setMaxLines(126 / this.textRenderer.fontHeight);
        this.editBox.setChangeListener(page -> this.pages.set(this.currentPage, (String)page));
        this.addDrawableChild(this.editBox);
        this.updatePage();
        this.pageIndicatorText = this.getPageIndicatorText();
        this.previousPageButton = this.addDrawableChild(new PageTurnWidget(i + 43, 159, false, button -> this.openPreviousPage(), true));
        this.nextPageButton = this.addDrawableChild(new PageTurnWidget(i + 116, 159, true, button -> this.openNextPage(), true));
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("book.signButton"), button -> this.client.setScreen(this.signingScreen)).dimensions(this.width / 2 - 100, 196, 98, 20).build());
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            this.client.setScreen(null);
            this.finalizeBook();
        }).dimensions(this.width / 2 + 2, 196, 98, 20).build());
        this.updatePreviousPageButtonVisibility();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.editBox);
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), this.getPageIndicatorText());
    }

    private Text getPageIndicatorText() {
        return Text.translatable("book.pageIndicator", this.currentPage + 1, this.countPages());
    }

    private void openPreviousPage() {
        if (this.currentPage > 0) {
            --this.currentPage;
            this.updatePage();
        }
        this.updatePreviousPageButtonVisibility();
    }

    private void openNextPage() {
        if (this.currentPage < this.countPages() - 1) {
            ++this.currentPage;
        } else {
            this.appendNewPage();
            if (this.currentPage < this.countPages() - 1) {
                ++this.currentPage;
            }
        }
        this.updatePage();
        this.updatePreviousPageButtonVisibility();
    }

    private void updatePage() {
        this.editBox.setText(this.pages.get(this.currentPage), true);
        this.pageIndicatorText = this.getPageIndicatorText();
    }

    private void updatePreviousPageButtonVisibility() {
        this.previousPageButton.visible = this.currentPage > 0;
    }

    private void removeEmptyPages() {
        ListIterator<String> listIterator = this.pages.listIterator(this.pages.size());
        while (listIterator.hasPrevious() && listIterator.previous().isEmpty()) {
            listIterator.remove();
        }
    }

    private void finalizeBook() {
        this.removeEmptyPages();
        this.writeNbtData();
        int i = this.hand == Hand.MAIN_HAND ? this.player.getInventory().getSelectedSlot() : 40;
        this.client.getNetworkHandler().sendPacket(new BookUpdateC2SPacket(i, this.pages, Optional.empty()));
    }

    private void writeNbtData() {
        this.stack.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, new WritableBookContentComponent(this.pages.stream().map(RawFilteredPair::of).toList()));
    }

    private void appendNewPage() {
        if (this.countPages() >= 100) {
            return;
        }
        this.pages.add("");
    }

    @Override
    public boolean deferSubtitles() {
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        switch (input.key()) {
            case 266: {
                this.previousPageButton.onPress(input);
                return true;
            }
            case 267: {
                this.nextPageButton.onPress(input);
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        int k = (this.width - 192) / 2;
        int l = 2;
        int m = this.textRenderer.getWidth(this.pageIndicatorText);
        context.drawText(this.textRenderer, this.pageIndicatorText, k - m + 192 - 44, 18, Colors.BLACK, false);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BookScreen.BOOK_TEXTURE, (this.width - 192) / 2, 2, 0.0f, 0.0f, 192, 192, 256, 256);
    }
}

