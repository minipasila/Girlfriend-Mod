package net.fabricmc.fabric.impl.gamerule.rpc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;

public interface FabricTypedRule {
	@Nullable
	FabricGameRuleType getFabricType();

	void setFabricType(FabricGameRuleType type);

	static GameRuleRpcDispatcher.TypedRule create(String name, String value, FabricGameRuleType type) {
		GameRuleRpcDispatcher.TypedRule typedRule = new GameRuleRpcDispatcher.TypedRule(name, value, null);
		((FabricTypedRule) (Object) typedRule).setFabricType(type);
		return typedRule;
	}
}
