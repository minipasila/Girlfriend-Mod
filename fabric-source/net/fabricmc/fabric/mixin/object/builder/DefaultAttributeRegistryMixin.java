package net.fabricmc.fabric.mixin.object.builder;

import java.util.IdentityHashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;

@Mixin(DefaultAttributeRegistry.class)
public abstract class DefaultAttributeRegistryMixin {
	@Shadow
	@Final
	@Mutable
	private static Map<EntityType<? extends LivingEntity>, DefaultAttributeContainer> DEFAULT_ATTRIBUTE_REGISTRY;

	@Inject(method = "<clinit>*", at = @At("TAIL"))
	private static void injectAttributes(CallbackInfo ci) {
		DEFAULT_ATTRIBUTE_REGISTRY = new IdentityHashMap<>(DEFAULT_ATTRIBUTE_REGISTRY);
	}
}
