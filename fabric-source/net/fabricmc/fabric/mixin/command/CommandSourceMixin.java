package net.fabricmc.fabric.mixin.command;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.command.CommandSource;

@Mixin(CommandSource.class)
public interface CommandSourceMixin {
	// Minecraft is hardcoded to only autofill identifiers with the "minecraft" namespace.
	// This allows non-vanilla identifiers to be autofilled
	@ModifyExpressionValue(
			method = "forEachMatching(Ljava/lang/Iterable;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Consumer;)V",
			at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", ordinal = 0)
	)
	private static boolean cancelNamespaceCheck(boolean original) {
		return true;
	}
}
