package net.fabricmc.fabric.impl.gamerule.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;

public final class BoundedIntRule extends GameRules.IntRule {
	private static final Logger LOGGER = LoggerFactory.getLogger(GameRuleRegistry.class);

	private final int minimumValue;
	private final int maximumValue;

	public BoundedIntRule(GameRules.Type<GameRules.IntRule> type, int initialValue, int minimumValue, int maximumValue) {
		super(type, initialValue);
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
	}

	@Override
	protected void deserialize(String value) {
		final int i = BoundedIntRule.parseInt(value);

		if (this.minimumValue > i || this.maximumValue < i) {
			LOGGER.warn("Failed to parse integer {}. Was out of bounds {} - {}", value, this.minimumValue, this.maximumValue);
			return;
		}

		this.set(i, null);
	}

	@Override
	public boolean validateAndSet(String input) {
		try {
			int value = Integer.parseInt(input);

			if (this.minimumValue > value || this.maximumValue < value) {
				return false;
			}

			this.set(value, null);
			return true;
		} catch (NumberFormatException var3) {
			return false;
		}
	}

	@Override
	protected GameRules.IntRule copy() {
		return new BoundedIntRule(this.type, this.get(), this.minimumValue, this.maximumValue);
	}

	private static int parseInt(String input) {
		if (!input.isEmpty()) {
			try {
				return Integer.parseInt(input);
			} catch (NumberFormatException var2) {
				LOGGER.warn("Failed to parse integer {}", input);
			}
		}

		return 0;
	}
}
