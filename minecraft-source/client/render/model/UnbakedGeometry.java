/*
 * External method calls:
 *   Lnet/minecraft/client/render/model/json/ModelElement;faces()Ljava/util/Map;
 *   Lnet/minecraft/client/render/model/BakedGeometry$Builder;build()Lnet/minecraft/client/render/model/BakedGeometry;
 *   Lnet/minecraft/client/render/model/json/ModelElement;from()Lorg/joml/Vector3fc;
 *   Lnet/minecraft/client/render/model/json/ModelElement;to()Lorg/joml/Vector3fc;
 *   Lnet/minecraft/client/render/model/json/ModelElement;rotation()Lnet/minecraft/client/render/model/json/ModelRotation;
 *   Lnet/minecraft/client/render/model/BakedQuadFactory;bake(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;Lnet/minecraft/client/render/model/json/ModelElementFace;Lnet/minecraft/client/texture/Sprite;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/client/render/model/json/ModelRotation;ZI)Lnet/minecraft/client/render/model/BakedQuad;
 *   Lnet/minecraft/client/render/model/json/ModelElementFace;textureId()Ljava/lang/String;
 *   Lnet/minecraft/client/render/model/json/ModelElementFace;cullFace()Lnet/minecraft/util/math/Direction;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/render/model/UnbakedGeometry;bakeGeometry(Ljava/util/List;Lnet/minecraft/client/render/model/ModelTextures;Lnet/minecraft/client/render/model/ErrorCollectingSpriteGetter;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/client/render/model/SimpleModel;)Lnet/minecraft/client/render/model/BakedGeometry;
 *   Lnet/minecraft/client/render/model/UnbakedGeometry;bakeQuad(Lnet/minecraft/client/render/model/json/ModelElement;Lnet/minecraft/client/render/model/json/ModelElementFace;Lnet/minecraft/client/texture/Sprite;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedQuad;
 */
package net.minecraft.client.render.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

@Environment(value=EnvType.CLIENT)
public record UnbakedGeometry(List<ModelElement> elements) implements Geometry
{
    @Override
    public BakedGeometry bake(ModelTextures arg, Baker arg2, ModelBakeSettings arg3, SimpleModel arg4) {
        return UnbakedGeometry.bakeGeometry(this.elements, arg, arg2.getSpriteGetter(), arg3, arg4);
    }

    public static BakedGeometry bakeGeometry(List<ModelElement> elements, ModelTextures textures, ErrorCollectingSpriteGetter arg2, ModelBakeSettings settings, SimpleModel model) {
        BakedGeometry.Builder lv = new BakedGeometry.Builder();
        for (ModelElement lv2 : elements) {
            lv2.faces().forEach((arg7, arg8) -> {
                Sprite lv = arg2.get(textures, arg8.textureId(), model);
                if (arg8.cullFace() == null) {
                    lv.add(UnbakedGeometry.bakeQuad(lv2, arg8, lv, arg7, settings));
                } else {
                    lv.add(Direction.transform(settings.getRotation().getMatrix(), arg8.cullFace()), UnbakedGeometry.bakeQuad(lv2, arg8, lv, arg7, settings));
                }
            });
        }
        return lv.build();
    }

    private static BakedQuad bakeQuad(ModelElement element, ModelElementFace face, Sprite sprite, Direction facing, ModelBakeSettings settings) {
        return BakedQuadFactory.bake(element.from(), element.to(), face, sprite, facing, settings, element.rotation(), element.shade(), element.lightEmission());
    }
}

