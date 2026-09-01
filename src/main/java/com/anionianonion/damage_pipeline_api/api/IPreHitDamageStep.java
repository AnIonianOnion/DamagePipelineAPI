package com.anionianonion.damage_pipeline_api.api;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.damage_pipeline_api.DamageContext;
import net.minecraft.world.entity.LivingEntity;

public interface IPreHitDamageStep {

    /**
     * This condition is iteratively evaluated by DamagePipeline#.
     * @return false if the hit is counted as a miss, and true if it succeeded.
     */
    boolean apply(StatContainer attackerStatContainer, StatContainer defenderStatContainer, DamageContext damageContext);

}
