package com.anionianonion.damage_pipeline_api;

import net.minecraft.resources.ResourceLocation;
import com.anionianonion.advanced_arpg_attributes_api.api.AdvancedARPGAttributesAPI;

import java.util.Set;

public class AdvancedARPGAttributeHook {

    public static Set<ResourceLocation> getFilteredAttributes(DamageContext damageContext) {
        AdvancedARPGAttributesAPI api = new AdvancedARPGAttributesAPI();

        return api.getFilteredAttributes(damageContext.getTags());
    }
}
