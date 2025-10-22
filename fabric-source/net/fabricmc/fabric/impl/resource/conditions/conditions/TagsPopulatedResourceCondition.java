package net.fabricmc.fabric.impl.resource.conditions.conditions;

import java.util.Arrays;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.impl.resource.conditions.DefaultResourceConditionTypes;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

public record TagsPopulatedResourceCondition(Identifier registry, List<Identifier> tags) implements ResourceCondition {
	// Cannot use registry-bound codec because they fail parsing if nonexistent,
	// and resource conditions themselves should not fail to parse on condition failure
	public static final MapCodec<TagsPopulatedResourceCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("registry").orElse(RegistryKeys.ITEM.getValue()).forGetter(TagsPopulatedResourceCondition::registry),
			Identifier.CODEC.listOf().fieldOf("values").forGetter(TagsPopulatedResourceCondition::tags)
	).apply(instance, TagsPopulatedResourceCondition::new));

	@SafeVarargs
	public <T> TagsPopulatedResourceCondition(Identifier registry, TagKey<T>... tags) {
		this(registry, Arrays.stream(tags).map(TagKey::id).toList());
	}

	@SafeVarargs
	public <T> TagsPopulatedResourceCondition(TagKey<T>... tags) {
		this(tags[0].registryRef().getValue(), Arrays.stream(tags).map(TagKey::id).toList());
	}

	@Override
	public ResourceConditionType<?> getType() {
		return DefaultResourceConditionTypes.TAGS_POPULATED;
	}

	@Override
	public boolean test(@Nullable RegistryOps.RegistryInfoGetter registryInfo) {
		return ResourceConditionsImpl.tagsPopulated(registryInfo, this.registry(), this.tags());
	}
}
