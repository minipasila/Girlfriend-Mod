package net.fabricmc.fabric.mixin.gamerule;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.impl.gamerule.RuleKeyExtensions;

@Mixin(GameRules.Key.class)
public abstract class GameRulesKeyMixin implements RuleKeyExtensions {
	@Unique
	@Nullable
	private CustomGameRuleCategory customCategory;

	@Override
	public CustomGameRuleCategory fabric_getCustomCategory() {
		return this.customCategory;
	}

	@Override
	public void fabric_setCustomCategory(CustomGameRuleCategory customCategory) {
		this.customCategory = customCategory;
	}
}
