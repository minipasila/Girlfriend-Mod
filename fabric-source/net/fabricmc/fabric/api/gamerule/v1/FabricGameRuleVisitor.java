package net.fabricmc.fabric.api.gamerule.v1;

import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;

/**
 * An extended game rule visitor which supports Fabric's own rule types.
 *
 * <p>Game rule visitors are typically used iterating all game rules.
 * In vanilla, the visitor is used to register game rule commands and populate the {@code Edit Game Rules} screen.
 *
 * <p>Rule types specified by this interface are not exhaustive.
 * New entries may be added in the future.
 */
public interface FabricGameRuleVisitor extends GameRules.Visitor {
	/**
	 * Visit a double rule.
	 *
	 * <p>Note {@link #visit(GameRules.Key, GameRules.Type)} will be called before this method is visited.
	 *
	 * @param key the rule key
	 * @param type the rule type
	 */
	default void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
	}

	/**
	 * Visit an enum rule.
	 *
	 * <p>Note {@link #visit(GameRules.Key, GameRules.Type)} will be called before this method is visited.
	 *
	 * @param key the rule key
	 * @param type the rule type
	 */
	default <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
	}
}
