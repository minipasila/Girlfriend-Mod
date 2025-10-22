package net.fabricmc.fabric.impl.resource.conditions.conditions;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

public record AnyModsLoadedResourceCondition(List<String> modIds) implements ResourceCondition {
	public static final MapCodec<AnyModsLoadedResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.listOf().fieldOf("values").forGetter(AnyModsLoadedResourceCondition::modIds)
	).apply(instance, AnyModsLoadedResourceCondition::new));

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.ANY_MODS_LOADED;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoGetter registryInfo) {
		return ResourceConditionsImpl.modsLoaded(this.modIds(), false);
	}
}
