package net.minecraft.world;

import net.minecraft.world.GameRules;

public interface GameRuleGetter {
    public <T extends GameRules.Rule<T>> T getRule(GameRules.Key<T> var1);
}

