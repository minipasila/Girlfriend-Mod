package net.fabricmc.fabric.mixin.gamerule;

import java.util.Objects;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.util.StringIdentifiable;

import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricTypedRule;

@Mixin(GameRuleRpcDispatcher.TypedRule.class)
public class GameRuleRpcDispatcherTypedRuleMixin implements FabricTypedRule {
	@Shadow
	@Final
	@Mutable
	public static MapCodec<GameRuleRpcDispatcher.TypedRule> CODEC;

	static {
		MapCodec<GameRuleRpcDispatcher.TypedRule> fabricTypedCodec = RecordCodecBuilder.mapCodec((instance) ->
				instance.group(
						Codec.STRING.fieldOf("key").forGetter(GameRuleRpcDispatcher.TypedRule::key),
						Codec.STRING.fieldOf("value").forGetter(GameRuleRpcDispatcher.TypedRule::value),
						StringIdentifiable.createCodec(FabricGameRuleType::values).fieldOf("type")
								.forGetter(typedRule -> ((FabricTypedRule) (Object) typedRule).getFabricType())
				).apply(instance, FabricTypedRule::create));

		CODEC = Codec.mapEither(fabricTypedCodec, CODEC).xmap(
				either -> either.map(Function.identity(), Function.identity()),
				typedRule -> ((FabricTypedRule) (Object) typedRule).getFabricType() == null ? Either.right(typedRule) : Either.left(typedRule));
	}

	@Nullable
	@Unique
	private FabricGameRuleType fabricGameRuleType = null;

	@Override
	public @Nullable FabricGameRuleType getFabricType() {
		return fabricGameRuleType;
	}

	@Override
	public void setFabricType(FabricGameRuleType type) {
		this.fabricGameRuleType = Objects.requireNonNull(type);
	}
}
