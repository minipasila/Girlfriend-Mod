package com.beckytidus.girlfriendmod.client.render;

import com.beckytidus.girlfriendmod.entity.GirlFriendEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class GirlFriendEntityRenderer extends BipedEntityRenderer<GirlFriendEntity, PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier TEXTURE = Identifier.of("girlfriend-mod", "entity/girlfriend");

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
        // Force set the texture in the render state
        state.skinTextures = new net.minecraft.entity.player.SkinTextures(
            new net.minecraft.util.AssetInfo.TextureAssetInfo(TEXTURE),
            null,
            null,
            net.minecraft.entity.player.PlayerSkinType.SLIM,
            false
        );
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }
}