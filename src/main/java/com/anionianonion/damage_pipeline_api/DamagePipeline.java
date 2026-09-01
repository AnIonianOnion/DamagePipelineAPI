package com.anionianonion.damage_pipeline_api;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.damage_pipeline_api.api.IPreHitDamageStep;
import com.anionianonion.damage_pipeline_api.api.IDamageStep;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
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
     * @param originAttackerStatContainer the StatContainer of the attacker's summoner/owner/etc. Pass in a new instance of the statContainer if not found.
     * @return false if the hit is counted as a miss, and true if it succeeded.
     */
    public static boolean didHitSucceed(StatContainer originAttackerStatContainer, StatContainer attackerStatContainer, StatContainer defenderStatContainer, DamageContext damageContext) {
        boolean hitSucceeded = true;

        if(damageContext.getSource().equals("self")) {
            for(var preHitDamageStep : preHitDamageSteps) {
                hitSucceeded = preHitDamageStep.apply(attackerStatContainer, defenderStatContainer, damageContext);
                if(!hitSucceeded) break;
            }
        }
        else if(originAttackerStatContainer != null) {
            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put(damageContext.getSource(), "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(attackerStatContainer, originAttackerStatContainer, tagsToReplaceToReplacementMap);

            for(var preHitDamageStep : preHitDamageSteps) {
                hitSucceeded = preHitDamageStep.apply(mergedStatContainer, defenderStatContainer, damageContext);
                if(!hitSucceeded) break;
            }
        }
        else {
            throw new IllegalStateException(String.format("Damage context's source is \"%s\" which isn't registered! You must let the authors of the mod know, and if you are the author, you must do DamagePipelineAPI.addValidDamageSourceTypeTag(\"%s\").", damageContext.getSource(), damageContext.getSource()));
        }

        return hitSucceeded;
    }

    /**
     * @param originAttackerStatContainer the StatContainer of the attacker's summoner/owner/etc. Pass in a new instance of the statContainer if not found.
     * @return damage to be dealt
     */
    public static float dealDamage(StatContainer originAttackerStatContainer, @NotNull StatContainer attackerStatContainer, @NotNull StatContainer defenderStatContainer, @NotNull DamageContext damageContext) {

        var damage = 0f;
        if(damageContext.getSource().equals("self")) {
            for(var damageStep : mitigationSteps) {
                damage = damageStep.apply(damage, attackerStatContainer, defenderStatContainer, damageContext);
            }
        }
        else if(originAttackerStatContainer != null) {
            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put(damageContext.getSource(), "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(attackerStatContainer, originAttackerStatContainer, tagsToReplaceToReplacementMap);

            for(var damageStep : mitigationSteps) {
                damage = damageStep.apply(damage, mergedStatContainer, defenderStatContainer, damageContext);
            }
        }
        else {
            throw new IllegalStateException(String.format("Damage context's source is \"%s\" which isn't registered! You must let the authors of the mod know, and if you are the author, you must do DamagePipelineAPI.addValidDamageSourceTypeTag(\"%s\").", damageContext.getSource(), damageContext.getSource()));
        }


        return damage;
    }
}
