package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class GirlFriendEntityRenderer extends BipedEntityRenderer<GirlFriendEntity, PlayerEntityRenderState, PlayerEntityModel> {

    public GirlFriendEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), true), 0.5f);
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }

    @Override
    public void updateRenderState(GirlFriendEntity entity, PlayerEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);

        // Get texture from config or use default
        String texturePath = ModConfig.get().customTexturePath;

        // Parse the texture path (format: "namespace:path")
        String[] parts = texturePath.split(":", 2);
        String namespace = parts.length > 1 ? parts[0] : "girlfriend-mod";
        String path = parts.length > 1 ? parts[1] : parts[0];

        // Ensure it ends with .png
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }

        Identifier textureId = Identifier.of(namespace, path);

        state.skinTextures = new net.minecraft.entity.player.SkinTextures(
            new net.minecraft.util.AssetInfo.TextureAssetInfo(textureId),
            null,
            null,
            net.minecraft.entity.player.PlayerSkinType.SLIM,
            false
        );
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        String texturePath = ModConfig.get().customTexturePath;
        String[] parts = texturePath.split(":", 2);
        String namespace = parts.length > 1 ? parts[0] : "girlfriend-mod";
        String path = parts.length > 1 ? parts[1] : parts[0];
        if (!path.endsWith(".png")) {
            path = path + ".png";
        }
        return Identifier.of(namespace, path);
    }
}
