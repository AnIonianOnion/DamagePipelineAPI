package com.anionianonion.damage_pipeline_api.api;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.damage_pipeline_api.DamageContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public interface IDamageStep {

    float apply(float initialDamage, LivingEntity attacker, LivingEntity defender, StatContainer attackerStatContainer, StatContainer defenderStatContainer, Entity directEntity, DamageContext damageContext);

}
