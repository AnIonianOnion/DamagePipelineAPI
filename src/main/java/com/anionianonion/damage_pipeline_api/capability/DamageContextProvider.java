package com.anionianonion.damage_pipeline_api.capability;

import com.anionianonion.damage_pipeline_api.DamageContext;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DamageContextProvider implements ICapabilityProvider {

    private final DamageContext backend = new DamageContext();

    private final LazyOptional<DamageContext> optional =
            LazyOptional.of(() -> backend);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == DamageContextCapability.INSTANCE ? optional.cast() : LazyOptional.empty();
    }
}
