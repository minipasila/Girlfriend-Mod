package net.fabricmc.fabric.mixin.resource.loader;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.resource.DataPackSettings;
import net.minecraft.test.TestServer;

import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;

/**
 * @see ModResourcePackUtil#createTestServerSettings
 */
@Mixin(TestServer.class)
public class TestServerMixin {
	@Redirect(method = "create", at = @At(value = "NEW", target = "(Ljava/util/List;Ljava/util/List;)Lnet/minecraft/resource/DataPackSettings;"))
	private static DataPackSettings replaceDefaultDataPackSettings(List<String> enabled, List<String> disabled) {
		return ModResourcePackUtil.createTestServerSettings(enabled, disabled);
	}
}
