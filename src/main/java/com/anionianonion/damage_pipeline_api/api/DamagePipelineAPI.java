package com.anionianonion.damage_pipeline_api.api;

import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.damage_pipeline_api.DamageContext;
import com.anionianonion.damage_pipeline_api.DamagePipeline;
import com.anionianonion.damage_pipeline_api.ValidSourcesRegistry;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DamagePipelineAPI {

    public static List<IPreHitDamageStep> getPreHitDamageSteps() { return DamagePipeline.getPreHitDamageSteps(); }

    public static List<IDamageStep> getDamageSteps() { return DamagePipeline.getMitigationSteps(); }

    public static void addPreHitDamageStep(IPreHitDamageStep preHitDamageStep) {
        DamagePipeline.addPreHitDamageStep(preHitDamageStep);
    }
    public static void addDamageStep(IDamageStep damageStep) {
        DamagePipeline.addDamageStep(damageStep);
    }

    /**
     * if the item's class is found within this mod's registered melee weapon classes-to-tags mapper, then we add the corresponding tag into the damage context.
     */
    public static void determineAndAddMeleeWeaponDamageTagToContext(Item itemInHand, DamageContext damageContext) {

        var classesAndParentClasses = getItemClassesAndParentClasses(itemInHand);
        for(Class<?> type : classesAndParentClasses) {
            if(AdvancedARPGAttributesAPI.getClassesOfMeleeWeaponItemsToTag().containsKey(type)) {
                damageContext.addTag(AdvancedARPGAttributesAPI.getClassesOfMeleeWeaponItemsToTag().get(type));
                break;
            }
        }
    }

    /**
     * if the item's class is found within this mod's registered ranged weapon classes-to-tags mapper, then we add the corresponding tag into the damage context.
     */
    public static void determineAndAddRangedWeaponDamageTagToContext(Item itemInHand, DamageContext damageContext) {

        var classesAndParentClasses = getItemClassesAndParentClasses(itemInHand);
        for(Class<?> type : classesAndParentClasses) {
            if(AdvancedARPGAttributesAPI.getClassesOfRangedWeaponItemsToTag().containsKey(type)) {
                damageContext.addTag(AdvancedARPGAttributesAPI.getClassesOfRangedWeaponItemsToTag().get(type));
                break;
            }
        }
    }

    private static List<Class<?>> getItemClassesAndParentClasses(Item itemInHand) {
        var itemClass = itemInHand.getClass();
        List<Class<?>> classesAndParentClasses = new ArrayList<>();

        Class<?> currentClass = itemClass;
        do {
            classesAndParentClasses.add(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        while (currentClass != Object.class);

        return classesAndParentClasses;
    }

    public static Set<String> getValidDamageSourceTypeTags() {
        return ValidSourcesRegistry.get();
    }

    public static void addValidDamageSourceTypeTag(String newTag) {
        ValidSourcesRegistry.addValidDamageSourceTypeTag(newTag);
    }
}
