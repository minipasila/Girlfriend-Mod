package net.fabricmc.fabric.impl.resource.conditions.conditions;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryOps;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

public record AndResourceCondition(List<ResourceCondition> conditions) implements ResourceCondition {
	public static final MapCodec<AndResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ResourceCondition.CODEC.listOf().fieldOf("values").forGetter(AndResourceCondition::conditions)
	).apply(instance, AndResourceCondition::new));

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.AND;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoGetter registryInfo) {
		return ResourceConditionsImpl.conditionsMet(this.conditions(), registryInfo, true);
	}
}
