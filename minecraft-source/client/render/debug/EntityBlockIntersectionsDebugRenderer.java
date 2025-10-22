/*
 * External method calls:
 *   Lnet/minecraft/world/debug/DebugDataStore;forEachBlockData(Lnet/minecraft/world/debug/DebugSubscriptionType;Ljava/util/function/BiConsumer;)V
 *   Lnet/minecraft/client/render/debug/DebugRenderer;drawBox(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/BlockPos;FFFFF)V
 */
package net.minecraft.client.render.debug;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;

@Environment(value=EnvType.CLIENT)
public class EntityBlockIntersectionsDebugRenderer
implements DebugRenderer.Renderer {
    private static final float EXPANSION = 0.02f;

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        store.forEachBlockData(DebugSubscriptionTypes.ENTITY_BLOCK_INTERSECTIONS, (pos, intersection) -> {
            float f = ColorHelper.getRedFloat(intersection.getColor());
            float g = ColorHelper.getGreenFloat(intersection.getColor());
            float h = ColorHelper.getBlueFloat(intersection.getColor());
            float i = ColorHelper.getAlphaFloat(intersection.getColor());
            DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.02f, f, g, h, i);
        });
    }
}

