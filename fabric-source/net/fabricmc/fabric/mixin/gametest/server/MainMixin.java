package net.fabricmc.fabric.mixin.gametest.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;

@Mixin(Main.class)
public class MainMixin {
	@ModifyExpressionValue(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;isEulaAgreedTo()Z"))
	private static boolean isEulaAgreedTo(boolean isEulaAgreedTo) {
		return FabricGameTestRunner.ENABLED || isEulaAgreedTo;
	}

	// Inject after resourcePackManager is stored
	@Inject(method = "main", cancellable = true, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/resource/VanillaDataPackProvider;createManager(Lnet/minecraft/world/level/storage/LevelStorage$Session;)Lnet/minecraft/resource/ResourcePackManager;"))
	private static void main(String[] args, CallbackInfo info, @Local LevelStorage.Session session, @Local ResourcePackManager resourcePackManager) {
		if (FabricGameTestRunner.ENABLED) {
			FabricGameTestRunner.runHeadlessServer(session, resourcePackManager);
			info.cancel();  // Do not progress in starting the normal dedicated server
		}
	}

	// Exit with a non-zero exit code when the server fails to start.
	// Otherwise gradlew test will succeed without errors, although no tests have been run.
	@Inject(method = "main", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Throwable;)V", shift = At.Shift.AFTER))
	private static void exitOnError(CallbackInfo info) {
		if (FabricGameTestRunner.ENABLED) {
			System.exit(-1);
		}
	}
}
