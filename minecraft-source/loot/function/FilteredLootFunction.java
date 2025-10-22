/*
 * External method calls:
 *   Lnet/minecraft/predicate/item/ItemPredicate;test(Lnet/minecraft/item/ItemStack;)Z
 *   Lnet/minecraft/loot/function/LootFunction;apply(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 *   Lnet/minecraft/loot/function/ConditionalLootFunction;validate(Lnet/minecraft/loot/LootTableReporter;)V
 *   Lnet/minecraft/loot/LootTableReporter;makeChild(Lnet/minecraft/util/ErrorReporter$Context;)Lnet/minecraft/loot/LootTableReporter;
 *   Lnet/minecraft/loot/function/LootFunction;validate(Lnet/minecraft/loot/LootTableReporter;)V
 *
 * Internal private/static methods:
 *   Lnet/minecraft/loot/function/FilteredLootFunction;addConditionsField(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/Products$P1;
 */
package net.minecraft.loot.function;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.ErrorReporter;

public class FilteredLootFunction
extends ConditionalLootFunction {
    public static final MapCodec<FilteredLootFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> FilteredLootFunction.addConditionsField(instance).and(instance.group(((MapCodec)ItemPredicate.CODEC.fieldOf("item_filter")).forGetter(lootFunction -> lootFunction.itemFilter), ((MapCodec)LootFunctionTypes.CODEC.fieldOf("modifier")).forGetter(lootFunction -> lootFunction.modifier))).apply((Applicative<FilteredLootFunction, ?>)instance, FilteredLootFunction::new));
    private final ItemPredicate itemFilter;
    private final LootFunction modifier;

    private FilteredLootFunction(List<LootCondition> conditions, ItemPredicate itemFilter, LootFunction modifier) {
        super(conditions);
        this.itemFilter = itemFilter;
        this.modifier = modifier;
    }

    public LootFunctionType<FilteredLootFunction> getType() {
        return LootFunctionTypes.FILTERED;
    }

    @Override
    public ItemStack process(ItemStack stack, LootContext context) {
        if (this.itemFilter.test(stack)) {
            return (ItemStack)this.modifier.apply(stack, context);
        }
        return stack;
    }

    @Override
    public void validate(LootTableReporter reporter) {
        super.validate(reporter);
        this.modifier.validate(reporter.makeChild(new ErrorReporter.MapElementContext("modifier")));
    }
}

