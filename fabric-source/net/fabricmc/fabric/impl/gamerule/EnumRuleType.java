package net.fabricmc.fabric.impl.gamerule;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;

public final class EnumRuleType<E extends Enum<E>> extends GameRules.Type<EnumRule<E>> {
	private final E[] supportedValues;

	public EnumRuleType(Function<GameRules.Type<EnumRule<E>>, EnumRule<E>> ruleFactory, BiConsumer<MinecraftServer, EnumRule<E>> changeCallback, E[] supportedValues, GameRules.Acceptor<EnumRule<E>> acceptor, Class<EnumRule<E>> enumRuleClass) {
		super(null, ruleFactory, changeCallback, acceptor, enumRuleClass, FeatureSet.empty());
		this.supportedValues = supportedValues;
	}

	public void register(LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder, GameRules.Key<EnumRule<E>> key) {
		LiteralCommandNode<ServerCommandSource> ruleNode = literal(key.getName()).build();

		for (E supportedValue : this.supportedValues) {
			ruleNode.addChild(literal(supportedValue.toString()).executes(context -> EnumRuleCommand.executeAndSetEnum(context, supportedValue, key)).build());
		}

		literalArgumentBuilder.then(ruleNode);
	}

	@Override
	@Deprecated
	public RequiredArgumentBuilder<ServerCommandSource, ?> argument(String name) {
		return super.argument(name);
	}
}
