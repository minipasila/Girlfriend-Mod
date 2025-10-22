package net.fabricmc.fabric.api.gamerule.v1.rule;

/**
 * A type of game rule which can validate an input.
 * This can be used to enforce syntax or clamp values.
 */
public interface ValidateableRule {
	/**
	 * Validates if a rule can accept the input.
	 * If valid, the input will be set as the rule's value.
	 *
	 * @param value the value to validate
	 * @return true if the value was accepted.
	 */
	boolean validate(String value);
}
