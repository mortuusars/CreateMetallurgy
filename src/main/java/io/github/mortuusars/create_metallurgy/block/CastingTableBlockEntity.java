package io.github.mortuusars.create_metallurgy.block;

import com.google.common.base.Preconditions;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import io.github.mortuusars.create_metallurgy.component.CastingTableFluidHandler;
import io.github.mortuusars.create_metallurgy.recipe.CastingRecipe;
import io.github.mortuusars.create_metallurgy.recipe.MetallurgyRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CastingTableBlockEntity extends SmartBlockEntity {
    private static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));

    ItemStack moldStack;
    SmartInventory outputInventory;
    CastingTableFluidHandler fluidHandler;
    int totalSolidifyingTicks;
    int solidifyingTicks;

    int fillingTicks;

    FluidStack fluidStack;
    ItemStack resultItemStack;

    protected LazyOptional<IItemHandler> itemCapability;
    protected LazyOptional<IFluidHandler> fluidCapability;

    @Nullable
    private CastingRecipe cachedRecipe;

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

        totalSolidifyingTicks = tag.getInt("TotalSolidifyingTicks");
        solidifyingTicks = tag.getInt("SolidifyingTicks");
        fillingTicks = tag.getInt("FillingTicks");
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);

        compound.put("Mold", moldStack.save(new CompoundTag()));
        compound.put("OutputItems", outputInventory.serializeNBT());

        compound.put("Result", resultItemStack.save(new CompoundTag()));
        compound.put("Fluid", fluidStack.writeToNBT(new CompoundTag()));

        if (totalSolidifyingTicks > -1)
            compound.putInt("TotalSolidifyingTicks", totalSolidifyingTicks);
        if (solidifyingTicks > -1)
            compound.putInt("SolidifyingTicks", solidifyingTicks);
        if (fillingTicks > -1)
            compound.putInt("FillingTicks", fillingTicks);
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
        return level != null && !moldStack.isEmpty() && solidifyingTicks < 0 && outputInventory.getStackInSlot(0).isEmpty();
    }

    protected @Nullable CastingRecipe getMatchingRecipe(FluidStack fluid) {
        if (level == null)
            return null;

        RECIPE_WRAPPER.setItem(0, moldStack);

        if (cachedRecipe != null) {
            if (recipeMatches(cachedRecipe, moldStack, fluid))
                return cachedRecipe;
            else
                cachedRecipe = null;
        }

        List<Recipe<RecipeWrapper>> castingRecipes = level.getRecipeManager()
                .getRecipesFor(MetallurgyRecipes.CASTING.getType(), RECIPE_WRAPPER, level);

        for (Recipe<RecipeWrapper> recipe : castingRecipes) {
            CastingRecipe castingRecipe = ((CastingRecipe) recipe);
            if (recipeMatches(castingRecipe, moldStack, fluid)) {
                cachedRecipe = castingRecipe;
                return castingRecipe;
            }
        }

        return null;
    }

    private boolean recipeMatches(CastingRecipe castingRecipe, ItemStack mold, FluidStack fluid) {
        return level != null && castingRecipe.matches(RECIPE_WRAPPER, level) && castingRecipe.getRequiredFluid().test(fluid);
    }

    public int tryFill(FluidStack fluidStack, boolean simulate) {
        if (!canFillWithFluid(fluidStack))
            return 0;

        @Nullable CastingRecipe recipe = getMatchingRecipe(fluidStack);

        if (recipe == null)
            return 0;

        if (!simulate) {
            this.fluidStack = fluidStack;
            this.solidifyingTicks = recipe.getProcessingDuration();
            this.totalSolidifyingTicks = solidifyingTicks;

            this.resultItemStack = recipe.getResultItem().copy();
            notifyUpdate();
        }
        else {
            this.fluidStack = fluidStack;
            this.fillingTicks = 18;
            notifyUpdate();
        }

        return recipe.getRequiredFluid().getRequiredAmount();
    }

    @Override
    public void tick() {
        super.tick();

        if (fillingTicks > 0) {
            fillingTicks--;
            return;
        }

        if (solidifyingTicks)

        if (moldStack.isEmpty() || this.fluidStack.isEmpty() || !outputInventory.isEmpty())
            return;

        if (solidifyingTicks > 0) {
            solidifyingTicks--;

            if (solidifyingTicks <= 0) {
                outputInventory.setItem(0, getResultItemStack());

                if (level != null) {
                    level.playSound(null, getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f,
                            level.getRandom().nextFloat() * 0.3f + 0.9f);
                }

                this.fluidStack = FluidStack.EMPTY;
                this.totalSolidifyingTicks = Integer.MIN_VALUE;
                this.solidifyingTicks = Integer.MIN_VALUE;
                this.resultItemStack = ItemStack.EMPTY;
                notifyUpdate();
            }
        }
    }

    public ItemStack getResultItemStack() {
        return resultItemStack;
    }
}
