package net.fabricmc.fabric.mixin.object.builder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.PersistentStateManager;

@Mixin(PersistentStateManager.class)
class PersistentStateManagerMixin {
	/**
	 * Handle mods passing a null DataFixTypes to a PersistentState.Type.
	 */
	@WrapOperation(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/datafixer/DataFixTypes;update(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/NbtCompound;II)Lnet/minecraft/nbt/NbtCompound;"))
	private NbtCompound handleNullDataFixType(DataFixTypes dataFixTypes, DataFixer dataFixer, NbtCompound nbt, int oldVersion, int newVersion, Operation<NbtCompound> original) {
		if (dataFixTypes == null) {
			return nbt;
		}

		return original.call(dataFixTypes, dataFixer, nbt, oldVersion, newVersion);
	}
}
