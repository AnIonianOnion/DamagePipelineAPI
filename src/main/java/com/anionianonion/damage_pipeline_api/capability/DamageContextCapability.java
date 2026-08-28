package com.anionianonion.damage_pipeline_api.capability;

import com.anionianonion.damage_pipeline_api.DamageContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class DamageContextCapability {

    public static final Capability<DamageContext> INSTANCE =
            CapabilityManager.get(new CapabilityToken<>() {});
}
