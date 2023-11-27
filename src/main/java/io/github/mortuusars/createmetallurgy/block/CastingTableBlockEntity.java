package io.github.mortuusars.createmetallurgy.block;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import io.github.mortuusars.createmetallurgy.component.CastingTableFluidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class CastingTableBlockEntity extends SmartBlockEntity {

    ItemStack moldStack;
    SmartInventory outputInventory;
    CastingTableFluidHandler fluidHandler;
    int solidifyingTicks;

    FluidStack fluidStack;
    ItemStack resultItemStack;

    protected LazyOptional<IItemHandler> itemCapability;
    protected LazyOptional<IFluidHandler> fluidCapability;

    public CastingTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        moldStack = ItemStack.EMPTY;
        fluidStack = FluidStack.EMPTY;
        resultItemStack = ItemStack.EMPTY;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        outputInventory = new SmartInventory(1, this, 1, false)
                .forbidInsertion()
                .whenContentsChanged(slot -> notifyUpdate());

        fluidHandler = new CastingTableFluidHandler(this);

        itemCapability = LazyOptional.of(() -> outputInventory);
        fluidCapability = LazyOptional.of(() -> fluidHandler);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        itemCapability.invalidate();
        fluidCapability.invalidate();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER)
            return itemCapability.cast();
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return fluidCapability.cast();
        return super.getCapability(cap, side);
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);

        moldStack = ItemStack.of(tag.getCompound("Mold"));
        outputInventory.deserializeNBT(tag.getCompound("OutputItems"));

        resultItemStack = ItemStack.of(tag.getCompound("Result"));
        fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));

        solidifyingTicks = tag.getInt("SolidifyingTicks");
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);

        compound.put("Mold", moldStack.save(new CompoundTag()));
        compound.put("OutputItems", outputInventory.serializeNBT());

        compound.put("Result", resultItemStack.save(new CompoundTag()));
        compound.put("Fluid", fluidStack.writeToNBT(new CompoundTag()));

        if (solidifyingTicks > -1)
            compound.putInt("SolidifyingTicks", solidifyingTicks);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) {
            ItemHelper.dropContents(level, worldPosition, outputInventory);
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, moldStack);
        }
    }

    public boolean canFillWithFluid(FluidStack fluid) {
        if (moldStack.isEmpty() || !fluidStack.isEmpty() || !outputInventory.getStackInSlot(0).isEmpty())
            return false;

        if (fluid.getFluid().getFluidType().equals(AllFluids.CHOCOLATE.getType()))
            return true;
        return false;
    }

    public int getRequiredFluidAmount() {
        return 200;
    }

    public void fill(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
        this.solidifyingTicks = 40;

        this.resultItemStack = new ItemStack(AllItems.BAR_OF_CHOCOLATE.get());
        notifyUpdate();
    }

    @Override
    public void tick() {
        super.tick();

        if (moldStack.isEmpty() || this.fluidStack.isEmpty() || solidifyingTicks == Integer.MIN_VALUE || !outputInventory.isEmpty())
            return;

        solidifyingTicks--;

        if (solidifyingTicks <= 0) {
            outputInventory.setItem(0, getResultItemStack());

            level.playSound(null, getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f,
                    level.getRandom().nextFloat() * 0.3f + 0.9f);

            this.fluidStack = FluidStack.EMPTY;
            this.solidifyingTicks = Integer.MIN_VALUE;
            this.resultItemStack = ItemStack.EMPTY;
        }
    }

    public ItemStack getResultItemStack() {
        return resultItemStack;
    }
}
