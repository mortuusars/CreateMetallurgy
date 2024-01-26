package io.github.mortuusars.create_metallurgy.component;

import io.github.mortuusars.create_metallurgy.block.CastingTableBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;

public class CastingTableFluidHandler extends VoidFluidHandler {

    private final CastingTableBlockEntity castingTableBlockEntity;

    public CastingTableFluidHandler(CastingTableBlockEntity castingTableBlockEntity) {
        this.castingTableBlockEntity = castingTableBlockEntity;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return castingTableBlockEntity.tryFill(resource, action.simulate());
    }
}
