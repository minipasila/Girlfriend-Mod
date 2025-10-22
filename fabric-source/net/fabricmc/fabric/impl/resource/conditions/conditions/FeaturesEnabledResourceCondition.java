package net.fabricmc.fabric.impl.resource.conditions.conditions;

import java.util.Collection;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

public record FeaturesEnabledResourceCondition(Collection<Identifier> features) implements ResourceCondition {
	public static final MapCodec<FeaturesEnabledResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.listOf().fieldOf("features").forGetter(condition -> List.copyOf(condition.features))
	).apply(instance, FeaturesEnabledResourceCondition::new));

	public FeaturesEnabledResourceCondition(Identifier... features) {
		this(List.of(features));
	}

	public FeaturesEnabledResourceCondition(FeatureFlag... flags) {
		this(FeatureFlags.FEATURE_MANAGER.toId(FeatureFlags.FEATURE_MANAGER.featureSetOf(flags)));
	}

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.FEATURES_ENABLED;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoGetter registryInfo) {
		return ResourceConditionsImpl.featuresEnabled(this.features());
	}
}
