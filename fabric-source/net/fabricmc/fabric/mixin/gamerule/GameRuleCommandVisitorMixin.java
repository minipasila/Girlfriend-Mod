package net.fabricmc.fabric.mixin.gamerule;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.impl.gamerule.EnumRuleCommand;
import net.fabricmc.fabric.impl.gamerule.EnumRuleType;

@Mixin(targets = "net/minecraft/server/command/GameRuleCommand$1")
public abstract class GameRuleCommandVisitorMixin {
	@Final
	@Shadow
	LiteralArgumentBuilder<ServerCommandSource> field_19419;

	@Inject(at = @At("HEAD"), method = "visit(Lnet/minecraft/world/GameRules$Key;Lnet/minecraft/world/GameRules$Type;)V", cancellable = true)
	private <T extends GameRules.Rule<T>> void onRegisterCommand(GameRules.Key<T> key, GameRules.Type<T> type, CallbackInfo ci) {
		// Check if our type is a EnumRuleType
		if (type instanceof EnumRuleType) {
			//noinspection rawtypes,unchecked
			EnumRuleCommand.register(this.field_19419, (GameRules.Key) key, (EnumRuleType) type);
			ci.cancel();
		}
	}
}
