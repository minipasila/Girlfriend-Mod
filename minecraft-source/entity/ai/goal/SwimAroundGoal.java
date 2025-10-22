/*
 * External method calls:
 *   Lnet/minecraft/entity/ai/brain/task/TargetUtil;find(Lnet/minecraft/entity/mob/PathAwareEntity;II)Lnet/minecraft/util/math/Vec3d;
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SwimAroundGoal
extends WanderAroundGoal {
    public SwimAroundGoal(PathAwareEntity arg, double d, int i) {
        super(arg, d, i);
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        return TargetUtil.find(this.mob, 10, 7);
    }
}

