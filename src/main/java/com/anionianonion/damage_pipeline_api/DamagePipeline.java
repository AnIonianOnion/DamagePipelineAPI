package com.anionianonion.damage_pipeline_api;

import com.anionianonion.advanced_arpg_attributes_api.StatContainer;
import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.advanced_arpg_attributes_api.capability.StatContainerCapability;
import com.anionianonion.damage_pipeline_api.api.IPreHitDamageStep;
import com.anionianonion.damage_pipeline_api.api.IDamageStep;
import com.anionianonion.damage_pipeline_api.capability.DamageContextCapability;
import com.anionianonion.damage_pipeline_api.util.RandomHelpers;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
    @Deprecated
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

            //make sure to reset and use minion's attributes as the "self" instead of the minion's minion attributes
            //todo: fix damageContext.setSource("self") not working;
            /*
            I think the reason why damageContext.setSource("self") isn't working for getting the attributes calculated, is that
            if we change the damageContext to "self" here, it will stay "self" and "self" will be used when dealDamage is called, so it will only use the attacker's attributes
            instead of the summoner
             */

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
    @Deprecated
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

            //merged summoner's minion bonus stats onto minion's stats
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(attackerStatContainer, originAttackerStatContainer, tagsToReplaceToReplacementMap);

            //no need to set DamageContext#setSource a second time, since we've set it in didHitSucceed which should be called in LivingAttackEvent.
            //actually, we might need to set it if didHitSucceed isn't called for any reason
            //todo: fix damageContext.setSource("self") not working;

            for(var damageStep : mitigationSteps) {
                damage = damageStep.apply(damage, mergedStatContainer, defenderStatContainer, damageContext);
            }
        }
        //(damage context source != "self" && origin attacker stat container == null) -> damage context source isn't registered?
        else {
            throw new IllegalStateException(String.format("Damage context's source is \"%s\", " +
                    "which isn't \"self\" but the stat container of the attacker's summoner/owner is also null. " +
                    "None of which are bad on its own, but when taken together is an illegal state. " +
                    "You must let the authors of the mod know, and if you are the author, " +
                    "you must do DamagePipelineAPI.addValidDamageSourceTypeTag(\"%s\").", damageContext.getSource(), damageContext.getSource()));
        }


        return damage;
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
            tagsToReplaceToReplacementMap.put(damageContext.getSource(), "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(minionStatContainer, livingAttackerOrCasterStatContainer, tagsToReplaceToReplacementMap);

            for(var preHitStep : preHitDamageSteps) {
                didHitSucceed = preHitStep.apply(mergedStatContainer, livingDefenderStatContainer, damageContext);
                if(!didHitSucceed) break;
            }
        }
        else if(RandomHelpers.isMinion(livingAttackerOrCaster)) {
            var summoner = SummonManager.getOwner(livingAttackerOrCaster);
            if(!(summoner instanceof LivingEntity livingSummoner)) return true;

            var summonerStatContainer = livingSummoner.getCapability(StatContainerCapability.INSTANCE).resolve().orElse(null);

            HashMap<String, String> tagsToReplaceToReplacementMap = new HashMap<>();
            tagsToReplaceToReplacementMap.put(damageContext.getSource(), "self");
            var mergedStatContainer = AdvancedARPGAttributesAPI.getNewStatContainerByRemappingBtoA(livingAttackerOrCasterStatContainer, summonerStatContainer, tagsToReplaceToReplacementMap);

            for(var preHitStep : preHitDamageSteps) {
                didHitSucceed = preHitStep.apply(mergedStatContainer, livingDefenderStatContainer, damageContext);
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
                didHitSucceed = preHitStep.apply(livingAttackerOrCasterStatContainer, livingDefenderStatContainer, damageContext);
                if(!didHitSucceed) break;
            }
        }

        return didHitSucceed;
    }
}
