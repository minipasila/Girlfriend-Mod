/*
 * External method calls:
 *   Lnet/minecraft/client/render/command/RenderCommandQueue;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/RenderLayer;IIILnet/minecraft/client/texture/Sprite;ILnet/minecraft/client/render/command/ModelCommandRenderer$CrumblingOverlayCommand;)V
 *   Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/render/entity/feature/BreezeWindFeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/BreezeEntityRenderState;FF)V
 */
package net.minecraft.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BreezeEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.state.BreezeEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BreezeWindFeatureRenderer
extends FeatureRenderer<BreezeEntityRenderState, BreezeEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/breeze/breeze_wind.png");
    private final BreezeEntityModel model;

    public BreezeWindFeatureRenderer(FeatureRendererContext<BreezeEntityRenderState, BreezeEntityModel> arg, LoadedEntityModels arg2) {
        super(arg);
        this.model = new BreezeEntityModel(arg2.getModelPart(EntityModelLayers.BREEZE_WIND));
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, BreezeEntityRenderState arg3, float f, float g) {
        RenderLayer lv = RenderLayer.getBreezeWind(TEXTURE, this.getXOffset(arg3.age) % 1.0f, 0.0f);
        arg2.getBatchingQueue(1).submitModel(this.model, arg3, arg, lv, i, OverlayTexture.DEFAULT_UV, -1, null, arg3.outlineColor, null);
    }

    private float getXOffset(float tickProgress) {
        return tickProgress * 0.02f;
    }
}

