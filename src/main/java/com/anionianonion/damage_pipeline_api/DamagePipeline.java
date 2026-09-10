package com.anionianonion.damage_pipeline_api;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.advanced_arpg_attributes_api.capability.StatContainerCapability;
import com.anionianonion.damage_pipeline_api.api.DamagePipelineAPI;
import com.anionianonion.damage_pipeline_api.api.IPreHitDamageStep;
import com.anionianonion.damage_pipeline_api.api.IDamageStep;
import com.anionianonion.damage_pipeline_api.capability.DamageContextCapability;
import com.anionianonion.damage_pipeline_api.util.RandomHelpers;
import com.anionianonion.elementals_api.api.ElementalsAPI;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

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

    //decided to use LivingAttackEvent from now on, because every variable we used in the deprecated method can be derived from it.
    public static boolean didHitSucceed(LivingAttackEvent e) {

        //todo: have damage from environmental still go through this pipeline, which requires that we account for Entity as well, and not just LivingEntity

        Entity attacker = e.getSource().getEntity();
        Entity directAttacker = e.getSource().getDirectEntity();
        LivingEntity livingDefender = e.getEntity();

        if(!(attacker instanceof LivingEntity livingAttackerOrCaster)) return true;

        DamageContext damageContext = livingAttackerOrCaster.getCapability(DamageContextCapability.INSTANCE).resolve().orElse(null);
        StatContainer livingAttackerOrCasterStatContainer = livingAttackerOrCaster.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
        StatContainer livingDefenderStatContainer = livingDefender.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);

        if(damageContext == null || livingAttackerOrCasterStatContainer == null || livingDefenderStatContainer == null) return true;

        boolean didHitSucceed = true;
        //based on https://github.com/AnIonianOnion/EADCFISSv2/commit/55224bf1d12d366b88ae75535465b1ff4e75c38f 's Helper class.
        //the conditional statements here are the same there

        //previously, this first if branch and the immediately below else if branch will handle the mergedStatContainer for us
        // whenever originAttackerStatContainer != null, which is by definition, from the way I had used it in the commited helper method's line 40 and line 48,
        //whenever minion damage is involved.
        if(RandomHelpers.isMinion(directAttacker)) {
            var minionStatContainer = directAttacker.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
            if(minionStatContainer == null) return true;

            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put("minion", "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(minionStatContainer, livingAttackerOrCasterStatContainer, tagsToReplaceToReplacementMap);

            var livingMinion = (LivingEntity) directAttacker;
            for(var preHitStep : preHitDamageSteps) {
                didHitSucceed = preHitStep.apply(mergedStatContainer, livingDefenderStatContainer, livingMinion, livingDefender, damageContext);
                if(!didHitSucceed) break;
            }
        }
        else if(RandomHelpers.isMinion(livingAttackerOrCaster)) {
            var summoner = SummonManager.getOwner(livingAttackerOrCaster);
            if(!(summoner instanceof LivingEntity livingSummoner)) return true;

            var summonerStatContainer = livingSummoner.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
            //var summonerDamageContext = livingSummoner.getCapability(DamageContextCapability.INSTANCE).resolve().orElse(null);

            if(summonerStatContainer == null //|| summonerDamageContext == null
            ) return true;

            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put("minion", "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(livingAttackerOrCasterStatContainer, summonerStatContainer, tagsToReplaceToReplacementMap);
            //DamagePipelineAPI.copyDamageContextFromAToB(damageContext, summonerDamageContext);

            for(var preHitStep : preHitDamageSteps) {
                didHitSucceed = preHitStep.apply(mergedStatContainer, livingDefenderStatContainer, //summonerDamageContext);
                        livingAttackerOrCaster, livingDefender,
                        damageContext);
                if(!didHitSucceed) break;
            }
        }
        //should be the same condition as the deprecated version of this method: we assume the damage is dealt by the "self": player or living entity by default,
        // unless it's any of the above conditions. Only this time, we are not using the damage tags to check the condition.
        else {
            //based on:
            /*
            if(damageContext.getSource().equals("self")) {
            for(var preHitDamageStep : preHitDamageSteps) {
                hitSucceeded = preHitDamageStep.apply(attackerStatContainer, defenderStatContainer, damageContext);
                if(!hitSucceeded) break;
            }
        }
             */
            for(var preHitStep : preHitDamageSteps) {
                didHitSucceed = preHitStep.apply(livingAttackerOrCasterStatContainer, livingDefenderStatContainer, livingAttackerOrCaster, livingDefender, damageContext);
                if(!didHitSucceed) break;
            }
        }

        return didHitSucceed;
    }

    //based on the method above. But whereas the default unmodified return value is true above (because it's a boolean, and that is what happens when damage is dealt),
    //this time the default return value is the damage amount it was initially.
    public static float dealDamage(LivingDamageEvent e) {
        Entity attacker = e.getSource().getEntity();
        Entity directAttacker = e.getSource().getDirectEntity();
        LivingEntity livingDefender = e.getEntity();

        var initialDamage = e.getAmount();

        if(!(attacker instanceof LivingEntity livingAttacker)) return initialDamage;

        DamageContext damageContext = livingAttacker.getCapability(DamageContextCapability.INSTANCE).resolve().orElse(null);
        StatContainer livingAttackerStatContainer = livingAttacker.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
        StatContainer livingDefenderStatContainer = livingDefender.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);

        if(damageContext == null || livingAttackerStatContainer == null || livingDefenderStatContainer == null) return initialDamage;

        float totalDamage = 0;

        //minion melee
        if(RandomHelpers.isMinion(directAttacker)) {
            var minionStatContainer = directAttacker.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
            if(minionStatContainer == null) return initialDamage;

            DamagePipelineAPIMod.LOGGER.info("direct attacker is livingattacker? " + String.valueOf(directAttacker == livingAttacker));

            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put("minion", "self");

            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(minionStatContainer, livingAttackerStatContainer, tagsToReplaceToReplacementMap);

            var livingMinion = (LivingEntity) directAttacker;

            for(var element : ElementalsAPI.getAllElementNames()) {
                damageContext.setElement(element);
                float elementDamage = 0;
                for(var damageStep : mitigationSteps) {
                    elementDamage = damageStep.apply(elementDamage, mergedStatContainer, livingDefenderStatContainer, livingMinion, livingDefender, damageContext);
                }
                totalDamage += elementDamage;
            }
        }
        //minion ranged
        else if (RandomHelpers.isMinion(livingAttacker)) {
            var summoner = SummonManager.getOwner(livingAttacker);
            if(!(summoner instanceof LivingEntity livingSummoner)) return initialDamage;

            var summonerStatContainer = livingSummoner.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);
            //var summonerDamageContext = livingSummoner.getCapability(DamageContextCapability.INSTANCE).resolve().orElse(null);

            if(summonerStatContainer == null //|| summonerDamageContext == null
            ) return initialDamage;

            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put("minion", "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(livingAttackerStatContainer, summonerStatContainer, tagsToReplaceToReplacementMap);
            //DamagePipelineAPI.copyDamageContextFromAToB(damageContext, summonerDamageContext);

            for(var element : ElementalsAPI.getAllElementNames()) {
                damageContext.setElement(element);
                float elementDamage = 0;
                for(var damageStep : mitigationSteps) {
                    elementDamage = damageStep.apply(elementDamage, mergedStatContainer, livingDefenderStatContainer, //summonerDamageContext);
                            livingAttacker, livingDefender,
                            damageContext);
                }
                totalDamage += elementDamage;
            }
        }
        else {
            for(var element : ElementalsAPI.getAllElementNames()) {
                damageContext.setElement(element);
                float elementDamage = 0;
                for(var damageStep : mitigationSteps) {
                    elementDamage = damageStep.apply(elementDamage, livingAttackerStatContainer, livingDefenderStatContainer, livingAttacker, livingDefender, damageContext);
                }
                totalDamage += elementDamage;
            }
        }
        return totalDamage;
    }
}
