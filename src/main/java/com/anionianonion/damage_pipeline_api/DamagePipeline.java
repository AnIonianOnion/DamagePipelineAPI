package com.anionianonion.damage_pipeline_api;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.damage_pipeline_api.api.IPreHitDamageStep;
import com.anionianonion.damage_pipeline_api.api.IDamageStep;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;


public class DamagePipeline {

    private static final List<IPreHitDamageStep> preHitDamageSteps = new ArrayList<>();
    private static final List<IDamageStep> mitigationSteps = new ArrayList<>();

    public static List<IPreHitDamageStep> getPreHitDamageSteps() {
        return preHitDamageSteps;
    }
    public static List<IDamageStep> getMitigationSteps() {
        return mitigationSteps;
    }

    /**
     * Should be used to add a step in determining whether a hit succeeds or fails.
     */
    public static void addPreHitDamageStep(IPreHitDamageStep preHitDamageStep) {
        preHitDamageSteps.add(preHitDamageStep);
    }
    /**
     * Should be used to add a damage step for when damage is already confirmed.
     */
    public static void addDamageStep(IDamageStep damageStep) {
        mitigationSteps.add(damageStep);
    }

    /**
     * Should be used within your event handler class that handles damage, within your LivingAttackEvent.
     * @return false if the hit is counted as a miss, and true if it succeeded.
     */
    public static boolean didHitSucceed(LivingEntity attacker, LivingEntity defender, DamageContext damageContext) {
        boolean hitSucceeded = true;
        for(var prehitDamageStep : preHitDamageSteps) {
            hitSucceeded = prehitDamageStep.apply(attacker, defender, damageContext);
            if(!hitSucceeded) break;
        }
        return hitSucceeded;
    }

    /**
     * @param attacker
     * @param defender
     * @param attackerStatContainer can get from attacker.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null), but it's here for accessibility
     * @param defenderStatContainer can get from defender.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null), but it's here for accessibility
     * @param directEntity the direct entity that triggered the attack
     * @param damageContext
     * @return damage to be dealt
     */
    public static float dealDamage(LivingEntity attacker, LivingEntity defender,
                                   StatContainer attackerStatContainer, StatContainer defenderStatContainer,
                                   Entity directEntity,
                                   DamageContext damageContext) {

        var damage = 0f;
        for(var damageStep : mitigationSteps) {
            damage = damageStep.apply(damage, attacker, defender, attackerStatContainer, defenderStatContainer, directEntity, damageContext);
        }

        return damage;
    }
}
