package net.fabricmc.fabric.impl.gamerule;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;

public interface RuleKeyExtensions {
	@Nullable
	CustomGameRuleCategory fabric_getCustomCategory();

	void fabric_setCustomCategory(CustomGameRuleCategory customCategory);
}
