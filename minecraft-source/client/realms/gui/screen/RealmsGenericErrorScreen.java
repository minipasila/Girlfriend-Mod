/*
 * External method calls:
 *   Lnet/minecraft/text/Text;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;
 *   Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;build()Lnet/minecraft/client/gui/widget/ButtonWidget;
 *   Lnet/minecraft/client/font/MultilineText;create(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;I)Lnet/minecraft/client/font/MultilineText;
 *   Lnet/minecraft/text/Text;empty()Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/text/MutableText;append(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/text/MutableText;append(Ljava/lang/String;)Lnet/minecraft/text/MutableText;
 *   Lnet/minecraft/client/realms/gui/screen/RealmsScreen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V
 *   Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V
 *   Lnet/minecraft/client/font/MultilineText;draw(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/MultilineText$Alignment;IIIZI)I
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/realms/gui/screen/RealmsGenericErrorScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;
 */
package net.minecraft.client.realms.gui.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.realms.RealmsError;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

@Environment(value=EnvType.CLIENT)
public class RealmsGenericErrorScreen
extends RealmsScreen {
    private final Screen parent;
    private final ErrorMessages errorMessages;
    private MultilineText description = MultilineText.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        this.errorMessages = RealmsGenericErrorScreen.getErrorMessages(realmsServiceException);
    }

    public RealmsGenericErrorScreen(Text description, Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        this.errorMessages = RealmsGenericErrorScreen.getErrorMessages(description);
    }

    public RealmsGenericErrorScreen(Text title, Text description, Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        this.errorMessages = RealmsGenericErrorScreen.getErrorMessages(title, description);
    }

    private static ErrorMessages getErrorMessages(RealmsServiceException exception) {
        RealmsError lv = exception.error;
        return RealmsGenericErrorScreen.getErrorMessages(Text.translatable("mco.errorMessage.realmsService.realmsError", lv.getErrorCode()), lv.getText());
    }

    private static ErrorMessages getErrorMessages(Text description) {
        return RealmsGenericErrorScreen.getErrorMessages(Text.translatable("mco.errorMessage.generic"), description);
    }

    private static ErrorMessages getErrorMessages(Text title, Text description) {
        return new ErrorMessages(title, description);
    }

    @Override
    public void init() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.OK, button -> this.close()).dimensions(this.width / 2 - 100, this.height - 52, 200, 20).build());
        this.description = MultilineText.create(this.textRenderer, this.errorMessages.detail, this.width * 3 / 4);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public Text getNarratedTitle() {
        return Text.empty().append(this.errorMessages.title).append(": ").append(this.errorMessages.detail);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.errorMessages.title, this.width / 2, 80, Colors.WHITE);
        this.description.draw(context, MultilineText.Alignment.CENTER, this.width / 2, 100, this.client.textRenderer.fontHeight, true, -2142128);
    }

    @Environment(value=EnvType.CLIENT)
    record ErrorMessages(Text title, Text detail) {
    }
}

