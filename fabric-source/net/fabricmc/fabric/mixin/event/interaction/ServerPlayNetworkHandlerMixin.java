package net.fabricmc.fabric.mixin.event.interaction;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PickItemFromBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PickItemFromEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private ServerPlayerEntity player;

	@Shadow
	private void onPickItem(ItemStack stack) {
		throw new AssertionError();
	}

	@WrapOperation(method = "onPickItemFromBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getPickStack(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)Lnet/minecraft/item/ItemStack;"))
	public ItemStack onPickItemFromBlock(BlockState state, WorldView world, BlockPos pos, boolean includeData, Operation<ItemStack> operation, @Local(argsOnly = true) PickItemFromBlockC2SPacket packet) {
		ItemStack stack = PlayerPickItemEvents.BLOCK.invoker().onPickItemFromBlock(player, pos, state, packet.includeData());

		if (stack == null) {
			return operation.call(state, world, pos, includeData);
		} else if (!stack.isEmpty()) {
			this.onPickItem(stack);
		}

		// Prevent vanilla data-inclusion behavior
		return ItemStack.EMPTY;
	}

	@WrapOperation(method = "onPickItemFromEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPickBlockStack()Lnet/minecraft/item/ItemStack;"))
	public ItemStack onPickItemFromEntity(Entity entity, Operation<ItemStack> operation, @Local(argsOnly = true) PickItemFromEntityC2SPacket packet) {
		ItemStack stack = PlayerPickItemEvents.ENTITY.invoker().onPickItemFromEntity(player, entity, packet.includeData());

		if (stack == null) {
			return operation.call(entity);
		} else if (!stack.isEmpty()) {
			this.onPickItem(stack);
		}

		// Prevent vanilla data-inclusion behavior
		return ItemStack.EMPTY;
	}
}
