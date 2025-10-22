package net.fabricmc.fabric.mixin.content.registry;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;

import net.fabricmc.fabric.impl.content.registry.OxidizableBlocksRegistryImpl;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockAbstractBlockStateMixin extends State<Block, BlockState> implements OxidizableBlocksRegistryImpl.RandomTickCacheRefresher {
	@Shadow
	private boolean ticksRandomly;

	private AbstractBlockAbstractBlockStateMixin(Block owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> propertyMap, MapCodec<BlockState> codec) {
		super(owner, propertyMap, codec);
	}

	@Override
	public void fabric_api$refreshRandomTickCache() {
		this.ticksRandomly = ((AbstractBlockAccessor) this.owner).callHasRandomTicks(this.asBlockState());
	}

	@Shadow
	protected BlockState asBlockState() {
		return null;
	}
}
