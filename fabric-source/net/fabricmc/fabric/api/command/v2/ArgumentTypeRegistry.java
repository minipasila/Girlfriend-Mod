package net.fabricmc.fabric.api.command.v2;

import com.mojang.brigadier.arguments.ArgumentType;

import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.mixin.command.ArgumentTypesAccessor;

public final class ArgumentTypeRegistry {
	/**
	 * Register a new argument type.
	 *
	 * @param id the identifier of the argument type
	 * @param clazz the class of the argument type
	 * @param serializer the serializer for the argument type
	 * @param <A> the argument type
	 * @param <T> the argument type properties
	 */
	public static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> void registerArgumentType(
			Identifier id, Class<? extends A> clazz, ArgumentSerializer<A, T> serializer) {
		ArgumentTypesAccessor.fabric_getClassMap().put(clazz, serializer);
		Registry.register(Registries.COMMAND_ARGUMENT_TYPE, id, serializer);
	}

	private ArgumentTypeRegistry() {
	}
}
