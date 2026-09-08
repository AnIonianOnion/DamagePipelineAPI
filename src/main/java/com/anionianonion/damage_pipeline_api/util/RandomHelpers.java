package com.anionianonion.damage_pipeline_api.util;

import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class RandomHelpers {

    public static boolean isMinion(Entity entity) {
        return entity instanceof LivingEntity && SummonManager.getOwner(entity) != null;
    }
}
