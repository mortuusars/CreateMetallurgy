package io.github.mortuusars.create_metallurgy.block;

import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class SpoutCastingBehaviour extends BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CastingTableBlockEntity castingTableBlockEntity))
            return 0;

        @Nullable IFluidHandler handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);

        if (handler == null)
            return 0;
        if (handler.getTanks() != 1)
            return 0;
        if (!handler.isFluidValid(0, availableFluid))
            return 0;
        if (!castingTableBlockEntity.canFillWithFluid(availableFluid))
            return 0;

        FluidStack containedFluid = handler.getFluidInTank(0);
        if (!(containedFluid.isEmpty() || containedFluid.isFluidEqual(availableFluid)))
            return 0;

        // Do not fill if it would only partially fill the table (unless > 1000mb)
        int amount = availableFluid.getAmount();
        if (amount < 1000
                && handler.fill(FluidHelper.copyStackWithAmount(availableFluid, amount + 1), IFluidHandler.FluidAction.SIMULATE) > amount)
            return 0;

        // Return amount filled into the table/basin
        return handler.fill(availableFluid, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }
}
