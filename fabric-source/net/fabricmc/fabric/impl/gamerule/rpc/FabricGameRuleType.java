package net.fabricmc.fabric.impl.gamerule.rpc;

import net.minecraft.util.StringIdentifiable;

/**
 * Extensions to {@link net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher.GameRuleType}.
 */
public enum FabricGameRuleType implements StringIdentifiable {
	DOUBLE("fabric:double"),
	ENUM("fabric:enum");

	private final String name;

	FabricGameRuleType(final String name) {
		this.name = name;
	}

	@Override
	public String asString() {
		return this.name;
	}
}
