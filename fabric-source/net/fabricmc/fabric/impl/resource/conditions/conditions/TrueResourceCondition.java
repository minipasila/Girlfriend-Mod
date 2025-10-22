package net.fabricmc.fabric.impl.resource.conditions.conditions;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;

public class TrueResourceCondition implements ResourceCondition {
	public static final MapCodec<TrueResourceCondition> CODEC = MapCodec.unit(TrueResourceCondition::new);

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.TRUE;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoGetter registryInfo) {
		return true;
	}
}
