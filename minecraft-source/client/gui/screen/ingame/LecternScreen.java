/*
 * External method calls:
 *   Lnet/minecraft/screen/LecternScreenHandler;addListener(Lnet/minecraft/screen/ScreenHandlerListener;)V
 *   Lnet/minecraft/screen/LecternScreenHandler;removeListener(Lnet/minecraft/screen/ScreenHandlerListener;)V
 *   Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;build()Lnet/minecraft/client/gui/widget/ButtonWidget;
 *   Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickButton(II)V
 *   Lnet/minecraft/client/gui/screen/ingame/BookScreen$Contents;create(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/gui/screen/ingame/BookScreen$Contents;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/gui/screen/ingame/LecternScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;
 *   Lnet/minecraft/client/gui/screen/ingame/LecternScreen;sendButtonPressPacket(I)V
 */
package net.minecraft.client.gui.screen.ingame;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class LecternScreen
extends BookScreen
implements ScreenHandlerProvider<LecternScreenHandler> {
    private final LecternScreenHandler handler;
    private final ScreenHandlerListener listener = new ScreenHandlerListener(){

        @Override
        public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
            LecternScreen.this.updatePageProvider();
        }

        @Override
        public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
            if (property == 0) {
                LecternScreen.this.updatePage();
            }
        }
    };

    public LecternScreen(LecternScreenHandler handler, PlayerInventory inventory, Text title) {
        this.handler = handler;
    }

    @Override
    public LecternScreenHandler getScreenHandler() {
        return this.handler;
    }

    @Override
    protected void init() {
        super.init();
        this.handler.addListener(this.listener);
    }

    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }

    @Override
    public void removed() {
        super.removed();
        this.handler.removeListener(this.listener);
    }

    @Override
    protected void addCloseButton() {
        if (this.client.player.canModifyBlocks()) {
            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, 196, 98, 20).build());
            this.addDrawableChild(ButtonWidget.builder(Text.translatable("lectern.take_book"), button -> this.sendButtonPressPacket(3)).dimensions(this.width / 2 + 2, 196, 98, 20).build());
        } else {
            super.addCloseButton();
        }
    }

    @Override
    protected void goToPreviousPage() {
        this.sendButtonPressPacket(1);
    }

    @Override
    protected void goToNextPage() {
        this.sendButtonPressPacket(2);
    }

    @Override
    protected boolean jumpToPage(int page) {
        if (page != this.handler.getPage()) {
            this.sendButtonPressPacket(100 + page);
            return true;
        }
        return false;
    }

    private void sendButtonPressPacket(int id) {
        this.client.interactionManager.clickButton(this.handler.syncId, id);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    void updatePageProvider() {
        ItemStack lv = this.handler.getBookItem();
        this.setPageProvider(Objects.requireNonNullElse(BookScreen.Contents.create(lv), BookScreen.EMPTY_PROVIDER));
    }

    void updatePage() {
        this.setPage(this.handler.getPage());
    }

    @Override
    protected void closeScreen() {
        this.client.player.closeHandledScreen();
    }

    @Override
    public /* synthetic */ ScreenHandler getScreenHandler() {
        return this.getScreenHandler();
    }
}

