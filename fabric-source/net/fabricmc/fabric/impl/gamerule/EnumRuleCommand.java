package net.fabricmc.fabric.impl.gamerule;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.mixin.gamerule.GameRuleCommandAccessor;

public final class EnumRuleCommand {
	public static <E extends Enum<E>> void register(LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder, GameRules.Key<EnumRule<E>> key, EnumRuleType<E> type) {
		literalArgumentBuilder.then(literal(key.getName()).executes(context -> {
			// We can use the vanilla query method
			return GameRuleCommandAccessor.invokeExecuteQuery(context.getSource(), key);
		}));

		// The LiteralRuleType handles the executeSet
		type.register(literalArgumentBuilder, key);
	}

	public static <E extends Enum<E>> int executeAndSetEnum(CommandContext<ServerCommandSource> context, E value, GameRules.Key<EnumRule<E>> key) throws CommandSyntaxException {
		// Mostly copied from vanilla, but tweaked so we can use literals
		ServerCommandSource serverCommandSource = context.getSource();
		EnumRule<E> rule = serverCommandSource.getServer().getGameRules().get(key);

		try {
			rule.set(value, serverCommandSource.getServer());
		} catch (IllegalArgumentException e) {
			throw new SimpleCommandExceptionType(Text.literal(e.getMessage())).create();
		}

		serverCommandSource.sendFeedback(() -> Text.translatable("commands.gamerule.set", key.getName(), rule.toString()), true);
		return rule.getCommandResult();
	}
}
