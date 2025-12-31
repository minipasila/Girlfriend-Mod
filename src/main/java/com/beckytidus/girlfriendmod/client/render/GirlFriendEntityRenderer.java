package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.config.ModConfig;
import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
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

        // Get the corrected texture identifier
        Identifier textureId = getCustomTextureId();

        // Assign directly to state.skinTextures
        // Note: For Player skins via AssetInfo, the game engine usually handles the "textures/" and ".png" 
        // automatically if using the specific Player rendering pipeline.
        state.skinTextures = new SkinTextures(
            new AssetInfo.TextureAssetInfo(textureId),
            null, // Cape
            null, // Elytra
            PlayerSkinType.SLIM, // Use SLIM (Alex) model
            false
        );
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return getCustomTextureId();
    }

    /**
     * Parses the config path and strips 'textures/' and '.png' to match 
     * the format expected by the Player Skin renderer.
     */
    private Identifier getCustomTextureId() {
        String texturePath = ModConfig.get().customTexturePath;

        // Fallback default if empty
        if (texturePath == null || texturePath.isEmpty()) {
            texturePath = "girlfriend-mod:textures/entity/girlfriend.png";
        }

        String[] parts = texturePath.split(":", 2);
        String namespace = parts.length > 1 ? parts[0] : "girlfriend-mod";
        String path = parts.length > 1 ? parts[1] : parts[0];

        // FIX: The Player rendering system (AssetInfo/SkinTextures) automatically adds 
        // "textures/" prefix and ".png" suffix. We must REMOVE them if the user included them.
        
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - 4);
        }
        
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        } else if (path.startsWith("/textures/")) {
             path = path.substring("/textures/".length());
        }

        return Identifier.of(namespace, path);
    }
}