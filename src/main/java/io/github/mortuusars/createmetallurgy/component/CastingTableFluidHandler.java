package io.github.mortuusars.createmetallurgy.component;

import com.simibubi.create.foundation.fluid.FluidHelper;
import io.github.mortuusars.createmetallurgy.block.CastingTableBlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.VoidFluidHandler;

public class CastingTableFluidHandler extends VoidFluidHandler {

    private final CastingTableBlockEntity castingTableBlockEntity;

    public CastingTableFluidHandler(CastingTableBlockEntity castingTableBlockEntity) {
        this.castingTableBlockEntity = castingTableBlockEntity;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        int requiredAmount = castingTableBlockEntity.getRequiredFluidAmount();

        if (resource.getAmount() < requiredAmount)
            return 0;

        if (action.execute())
            castingTableBlockEntity.fill(FluidHelper.copyStackWithAmount(resource, requiredAmount));

        return requiredAmount;
    }
}
