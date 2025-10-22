/*
 * External method calls:
 *   Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/util/Identifier;IIII)V
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/gui/widget/ToggleButtonWidget;appendDefaultNarrations(Lnet/minecraft/client/gui/screen/narration/NarrationMessageBuilder;)V
 */
package net.minecraft.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ToggleButtonWidget
extends ClickableWidget {
    @Nullable
    protected ButtonTextures textures;
    protected boolean toggled;

    public ToggleButtonWidget(int x, int y, int width, int height, boolean toggled) {
        super(x, y, width, height, ScreenTexts.EMPTY);
        this.toggled = toggled;
    }

    public void setTextures(ButtonTextures textures) {
        this.textures = textures;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean isToggled() {
        return this.toggled;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (this.textures == null) {
            return;
        }
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, this.textures.get(this.toggled, this.isSelected()), this.getX(), this.getY(), this.width, this.height);
    }
}

