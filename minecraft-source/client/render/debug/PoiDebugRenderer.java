/*
 * External method calls:
 *   Lnet/minecraft/world/debug/DebugDataStore;forEachBlockData(Lnet/minecraft/world/debug/DebugSubscriptionType;Ljava/util/function/BiConsumer;)V
 *   Lnet/minecraft/client/render/debug/DebugRenderer;drawBox(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/BlockPos;FFFFF)V
 *   Lnet/minecraft/client/render/debug/DebugRenderer;drawFloatingText(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;IIF)V
 *   Lnet/minecraft/world/debug/data/PoiDebugData;poiType()Lnet/minecraft/registry/entry/RegistryEntry;
 *   Lnet/minecraft/world/debug/data/PoiDebugData;pos()Lnet/minecraft/util/math/BlockPos;
 *   Lnet/minecraft/world/debug/DebugDataStore;forEachEntityData(Lnet/minecraft/world/debug/DebugSubscriptionType;Ljava/util/function/BiConsumer;)V
 *   Lnet/minecraft/world/debug/data/BrainDebugData;potentialPoiContains(Lnet/minecraft/util/math/BlockPos;)Z
 *   Lnet/minecraft/world/debug/data/BrainDebugData;poiContains(Lnet/minecraft/util/math/BlockPos;)Z
 *   Lnet/minecraft/util/NameGenerator;name(Ljava/util/UUID;)Ljava/lang/String;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/client/render/debug/PoiDebugRenderer;drawTextOverPoi(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Ljava/lang/String;Lnet/minecraft/world/debug/data/PoiDebugData;II)V
 *   Lnet/minecraft/client/render/debug/PoiDebugRenderer;drawGhostPoi(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/BlockPos;Ljava/util/List;)V
 *   Lnet/minecraft/client/render/debug/PoiDebugRenderer;accentuatePoi(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/util/math/BlockPos;)V
 *   Lnet/minecraft/client/render/debug/PoiDebugRenderer;drawPoiInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/debug/data/PoiDebugData;Lnet/minecraft/world/debug/DebugDataStore;)V
 */
package net.minecraft.client.render.debug;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.BrainDebugRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.debug.DebugDataStore;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.data.PoiDebugData;

@Environment(value=EnvType.CLIENT)
public class PoiDebugRenderer
implements DebugRenderer.Renderer {
    private static final int field_62976 = 30;
    private static final float field_62977 = 0.02f;
    private static final int ORANGE_COLOR = -23296;
    private final BrainDebugRenderer brainDebugRenderer;

    public PoiDebugRenderer(BrainDebugRenderer brainDebugRenderer) {
        this.brainDebugRenderer = brainDebugRenderer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, double cameraX, double cameraY, double cameraZ, DebugDataStore store, Frustum frustum) {
        BlockPos lv = BlockPos.ofFloored(cameraX, cameraY, cameraZ);
        store.forEachBlockData(DebugSubscriptionTypes.POIS, (pos, poiInfo) -> {
            if (lv.isWithinDistance((Vec3i)pos, 30.0)) {
                PoiDebugRenderer.accentuatePoi(matrices, vertexConsumers, pos);
                this.drawPoiInfo(matrices, vertexConsumers, (PoiDebugData)poiInfo, store);
            }
        });
        this.brainDebugRenderer.getGhostPointsOfInterest(store).forEach((pos, pois) -> {
            if (store.getBlockData(DebugSubscriptionTypes.POIS, (BlockPos)pos) != null) {
                return;
            }
            if (lv.isWithinDistance((Vec3i)pos, 30.0)) {
                this.drawGhostPoi(matrices, vertexConsumers, (BlockPos)pos, (List<String>)pois);
            }
        });
    }

    private static void accentuatePoi(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos) {
        float f = 0.05f;
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
    }

    private void drawGhostPoi(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockPos pos, List<String> ticketHolders) {
        float f = 0.05f;
        DebugRenderer.drawBox(matrices, vertexConsumers, pos, 0.05f, 0.2f, 0.2f, 1.0f, 0.3f);
        DebugRenderer.drawFloatingText(matrices, vertexConsumers, ticketHolders.toString(), pos, 0, -256, 0.02f);
        DebugRenderer.drawFloatingText(matrices, vertexConsumers, "Ghost POI", pos, 1, -65536, 0.02f);
    }

    private void drawPoiInfo(MatrixStack matrices, VertexConsumerProvider vertexConsumers, PoiDebugData poiData, DebugDataStore store) {
        int i = 0;
        if (SharedConstants.BRAIN) {
            List<String> list = this.getTicketHolders(poiData, false, store);
            if (list.size() < 4) {
                PoiDebugRenderer.drawTextOverPoi(matrices, vertexConsumers, "Owners: " + String.valueOf(list), poiData, i, -256);
            } else {
                PoiDebugRenderer.drawTextOverPoi(matrices, vertexConsumers, list.size() + " ticket holders", poiData, i, -256);
            }
            ++i;
            List<String> list2 = this.getTicketHolders(poiData, true, store);
            if (list2.size() < 4) {
                PoiDebugRenderer.drawTextOverPoi(matrices, vertexConsumers, "Candidates: " + String.valueOf(list2), poiData, i, -23296);
            } else {
                PoiDebugRenderer.drawTextOverPoi(matrices, vertexConsumers, list2.size() + " potential owners", poiData, i, -23296);
            }
            ++i;
        }
        PoiDebugRenderer.drawTextOverPoi(matrices, vertexConsumers, "Free tickets: " + poiData.freeTicketCount(), poiData, i, -256);
        PoiDebugRenderer.drawTextOverPoi(matrices, vertexConsumers, poiData.poiType().getIdAsString(), poiData, ++i, -1);
    }

    private static void drawTextOverPoi(MatrixStack matrices, VertexConsumerProvider vertexConsumers, String ticketHolder, PoiDebugData poiData, int lineNumber, int color) {
        DebugRenderer.drawFloatingText(matrices, vertexConsumers, ticketHolder, poiData.pos(), lineNumber, color, 0.02f);
    }

    private List<String> getTicketHolders(PoiDebugData poiData, boolean potential, DebugDataStore store) {
        ArrayList<String> list = new ArrayList<String>();
        store.forEachEntityData(DebugSubscriptionTypes.BRAINS, (entity, grainData) -> {
            boolean bl2;
            boolean bl3 = bl2 = potential ? grainData.potentialPoiContains(poiData.pos()) : grainData.poiContains(poiData.pos());
            if (bl2) {
                list.add(NameGenerator.name(entity.getUuid()));
            }
        });
        return list;
    }
}

