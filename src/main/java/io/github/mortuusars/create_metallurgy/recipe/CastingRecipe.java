package io.github.mortuusars.create_metallurgy.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

public class CastingRecipe extends ProcessingRecipe<RecipeWrapper> {

    public CastingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(MetallurgyRecipes.CASTING, params);
    }

    @Override
    public boolean matches(RecipeWrapper inv, @NotNull Level level) {
        return ingredients.get(0).test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    public FluidIngredient getRequiredFluid() {
        if (fluidIngredients.isEmpty())
            throw new IllegalStateException("Filling Recipe: " + id.toString() + " has no fluid ingredient!");
        return fluidIngredients.get(0);
    }
}
