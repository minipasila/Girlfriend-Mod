package net.minecraft.command;

import java.util.function.Predicate;

public interface PermissionLevelPredicate<T>
extends Predicate<T> {
    public int requiredLevel();
}

