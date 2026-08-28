package com.anionianonion.damage_pipeline_api.capability;

import com.anionianonion.damage_pipeline_api.DamagePipelineAPIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DamagePipelineAPIMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachLivingEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();

        if (entity instanceof LivingEntity) {
            event.addCapability(ResourceLocation.fromNamespaceAndPath(DamagePipelineAPIMod.MOD_ID, "damage_context"), new DamageContextProvider());
        }
    }
}
