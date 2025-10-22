/*
 * External method calls:
 *   Lnet/minecraft/entity/ai/FuzzyPositions;guessBest(Ljava/util/function/Supplier;Ljava/util/function/ToDoubleFunction;)Lnet/minecraft/util/math/Vec3d;
 *   Lnet/minecraft/entity/ai/FuzzyPositions;guessBestPathTarget(Lnet/minecraft/entity/mob/PathAwareEntity;Ljava/util/function/Supplier;)Lnet/minecraft/util/math/Vec3d;
 *   Lnet/minecraft/entity/ai/FuzzyPositions;upWhile(Lnet/minecraft/util/math/BlockPos;ILjava/util/function/Predicate;)Lnet/minecraft/util/math/BlockPos;
 *   Lnet/minecraft/entity/ai/FuzzyPositions;towardTarget(Lnet/minecraft/entity/mob/PathAwareEntity;ILnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;
 *   Lnet/minecraft/entity/ai/FuzzyPositions;localFuzz(Lnet/minecraft/util/math/random/Random;IIIDDD)Lnet/minecraft/util/math/BlockPos;
 *   Lnet/minecraft/entity/ai/FuzzyPositions;localFuzz(Lnet/minecraft/util/math/random/Random;II)Lnet/minecraft/util/math/BlockPos;
 *
 * Internal private/static methods:
 *   Lnet/minecraft/entity/ai/FuzzyTargeting;find(Lnet/minecraft/entity/mob/PathAwareEntity;IILjava/util/function/ToDoubleFunction;)Lnet/minecraft/util/math/Vec3d;
 *   Lnet/minecraft/entity/ai/FuzzyTargeting;findValid(Lnet/minecraft/entity/mob/PathAwareEntity;IILnet/minecraft/util/math/Vec3d;Z)Lnet/minecraft/util/math/Vec3d;
 *   Lnet/minecraft/entity/ai/FuzzyTargeting;towardTarget(Lnet/minecraft/entity/mob/PathAwareEntity;IZLnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;
 *   Lnet/minecraft/entity/ai/FuzzyTargeting;validate(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;
 */
package net.minecraft.entity.ai;

import java.util.function.ToDoubleFunction;
import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FuzzyTargeting {
    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
        return FuzzyTargeting.find(entity, horizontalRange, verticalRange, entity::getPathfindingFavor);
    }

    @Nullable
    public static Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, ToDoubleFunction<BlockPos> scorer) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBest(() -> {
            BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange);
            BlockPos lv2 = FuzzyTargeting.towardTarget(entity, horizontalRange, bl, lv);
            if (lv2 == null) {
                return null;
            }
            return FuzzyTargeting.validate(entity, lv2);
        }, scorer);
    }

    @Nullable
    public static Vec3d findTo(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d end) {
        Vec3d lv = end.subtract(entity.getX(), entity.getY(), entity.getZ());
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyTargeting.findValid(entity, horizontalRange, verticalRange, lv, bl);
    }

    @Nullable
    public static Vec3d findFrom(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d start) {
        Vec3d lv = entity.getEntityPos().subtract(start);
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyTargeting.findValid(entity, horizontalRange, verticalRange, lv, bl);
    }

    @Nullable
    private static Vec3d findValid(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d direction, boolean posTargetInRange) {
        return FuzzyPositions.guessBestPathTarget(entity, () -> {
            BlockPos lv = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange, 0, arg2.x, arg2.z, 1.5707963705062866);
            if (lv == null) {
                return null;
            }
            BlockPos lv2 = FuzzyTargeting.towardTarget(entity, horizontalRange, posTargetInRange, lv);
            if (lv2 == null) {
                return null;
            }
            return FuzzyTargeting.validate(entity, lv2);
        });
    }

    @Nullable
    public static BlockPos validate(PathAwareEntity entity, BlockPos pos) {
        if (NavigationConditions.isWaterAt(entity, pos = FuzzyPositions.upWhile(pos, entity.getEntityWorld().getTopYInclusive(), currentPos -> NavigationConditions.isSolidAt(entity, currentPos))) || NavigationConditions.hasPathfindingPenalty(entity, pos)) {
            return null;
        }
        return pos;
    }

    @Nullable
    public static BlockPos towardTarget(PathAwareEntity entity, int horizontalRange, boolean posTargetInRange, BlockPos relativeInRangePos) {
        BlockPos lv = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), relativeInRangePos);
        if (NavigationConditions.isHeightInvalid(lv, entity) || NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, lv) || NavigationConditions.isInvalidPosition(entity.getNavigation(), lv)) {
            return null;
        }
        return lv;
    }
}

