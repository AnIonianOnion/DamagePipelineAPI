package com.anionianonion.damage_pipeline_api.api;

import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;
import com.anionianonion.damage_pipeline_api.DamageContext;
import com.anionianonion.damage_pipeline_api.DamagePipeline;
import com.anionianonion.damage_pipeline_api.ValidSources;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DamagePipelineAPI {

    public List<IPreHitDamageStep> getPreHitDamageSteps() { return DamagePipeline.getPreHitDamageSteps(); }

    public List<IDamageStep> getDamageSteps() { return DamagePipeline.getMitigationSteps(); }

    public void addPreHitDamageStep(IPreHitDamageStep preHitDamageStep) {
        DamagePipeline.addPreHitDamageStep(preHitDamageStep);
    }
    public void addDamageStep(IDamageStep damageStep) {
        DamagePipeline.addDamageStep(damageStep);
    }

    /**
     * if the item's class is found within this mod's registered weapon classes and tags, then we add the corresponding tag into the damage context.
     */
    public static void determineAndAddWeaponDamageTagToContext(Item itemInHand, DamageContext damageContext) {
        AdvancedARPGAttributesAPI aaaAPI = new AdvancedARPGAttributesAPI();

        var itemClass = itemInHand.getClass();
        List<Class<?>> parentClasses = new ArrayList<>();

        Class<?> currentClass = itemClass;
        do {
            parentClasses.add(currentClass);
            currentClass = currentClass.getSuperclass();
        }
        while (currentClass != Object.class);

        for(Class<?> superclass : parentClasses) {
            if(aaaAPI.getClassesOfWeaponItemsToTag().containsKey(superclass))
                damageContext.addTag(aaaAPI.getClassesOfWeaponItemsToTag().get(superclass));
        }

    }

    public Set<String> getValidDamageSourceTypeTags() {
        return ValidSources.getValidDamageSourceTypeTags();
    }

    public void addValidDamageSourceTypeTag(String newTag) {
        ValidSources.addValidDamageSourceTypeTag(newTag);
    }
}
