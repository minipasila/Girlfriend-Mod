/*
 * External method calls:
 *   Lnet/minecraft/component/type/EquippableComponent;assetId()Ljava/util/Optional;
 *   Lnet/minecraft/client/render/entity/equipment/EquipmentRenderer;render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/util/Identifier;II)V
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/render/entity/feature/SaddleFeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V
 */
package net.minecraft.client.render.entity.feature;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class SaddleFeatureRenderer<S extends LivingEntityRenderState, RM extends EntityModel<? super S>, EM extends EntityModel<? super S>>
extends FeatureRenderer<S, RM> {
    private final EquipmentRenderer equipmentRenderer;
    private final EquipmentModel.LayerType layerType;
    private final Function<S, ItemStack> saddleStackGetter;
    private final EM adultModel;
    private final EM babyModel;
    private final int field_61938;

    public SaddleFeatureRenderer(FeatureRendererContext<S, RM> context, EquipmentRenderer equipmentRenderer, EquipmentModel.LayerType layerType, Function<S, ItemStack> saddleStackGetter, EM adultModel, EM babyModel, int i) {
        super(context);
        this.equipmentRenderer = equipmentRenderer;
        this.layerType = layerType;
        this.saddleStackGetter = saddleStackGetter;
        this.adultModel = adultModel;
        this.babyModel = babyModel;
        this.field_61938 = i;
    }

    public SaddleFeatureRenderer(FeatureRendererContext<S, RM> context, EquipmentRenderer equipmentRenderer, EquipmentModel.LayerType arg3, Function<S, ItemStack> function, EM arg4, EM arg5) {
        this(context, equipmentRenderer, arg3, function, arg4, arg5, 0);
    }

    @Override
    public void render(MatrixStack arg, OrderedRenderCommandQueue arg2, int i, S arg3, float f, float g) {
        ItemStack lv = this.saddleStackGetter.apply(arg3);
        EquippableComponent lv2 = lv.get(DataComponentTypes.EQUIPPABLE);
        if (lv2 == null || lv2.assetId().isEmpty()) {
            return;
        }
        EM lv3 = ((LivingEntityRenderState)arg3).baby ? this.babyModel : this.adultModel;
        this.equipmentRenderer.render(this.layerType, lv2.assetId().get(), lv3, arg3, lv, arg, arg2, i, (Identifier)null, ((LivingEntityRenderState)arg3).outlineColor, this.field_61938);
    }
}

