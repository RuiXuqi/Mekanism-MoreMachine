package com.jerry.mekmm.api.recipes.cache;

import com.jerry.mekmm.api.recipes.basic.BasicFluidChemicalToFluidRecipe;
import com.jerry.mekmm.api.recipes.basic.MMBasicChemicalChemicalToChemicalRecipe;
import com.jerry.mekmm.api.recipes.basic.MMBasicItemStackChemicalToItemStackRecipe;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.cache.CachedRecipe;
import mekanism.api.recipes.cache.CachedRecipeHelper;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.inputs.IInputHandler;
import mekanism.api.recipes.outputs.IOutputHandler;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.*;

@NothingNullByDefault
public class ReplicatorCachedRecipe<TYPE, RECIPE extends MekanismRecipe<?> & BiPredicate<TYPE, ChemicalStack>> extends CachedRecipe<RECIPE> {

    private final IInputHandler<TYPE> inputHandler;
    private final IInputHandler<ChemicalStack> secondaryInputHandler;
    private final IOutputHandler<TYPE> outputHandler;
    private final Predicate<TYPE> inputEmptyCheck;
    private final Predicate<ChemicalStack> secondaryInputEmptyCheck;
    private final Supplier<? extends InputIngredient<TYPE>> inputSupplier;
    private final Supplier<? extends InputIngredient<ChemicalStack>> secondaryInputSupplier;
    private final BiFunction<TYPE, ChemicalStack, TYPE> outputGetter;
    private final Predicate<TYPE> outputEmptyCheck;
    private final BiConsumer<TYPE, ChemicalStack> inputsSetter;
    private final Consumer<TYPE> outputSetter;

    //Note: Our inputs and outputs shouldn't be null in places they are actually used, but we mark them as nullable, so we don't have to initialize them
    @Nullable
    private TYPE input;
    @Nullable
    private ChemicalStack secondaryInput;
    @Nullable
    private TYPE output;

    /**
     * @param recipe           Recipe.
     * @param recheckAllErrors Returns {@code true} if processing should be continued even if an error is hit in order to gather all the errors. It is recommended to not
     *                         do this every tick or if there is no one viewing recipes.
     */
    private ReplicatorCachedRecipe(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<TYPE> inputHandler, IInputHandler<ChemicalStack> secondaryInputHandler,
                                   IOutputHandler<TYPE> outputHandler, Supplier<InputIngredient<TYPE>> inputSupplier, Supplier<InputIngredient<ChemicalStack>> secondaryInputSupplier,
                                   BiFunction<TYPE, ChemicalStack, TYPE> outputGetter, Predicate<TYPE> inputEmptyCheck, Predicate<ChemicalStack> secondaryInputEmptyCheck,
                                   Predicate<TYPE> outputEmptyCheck) {
        super(recipe, recheckAllErrors);
        this.inputHandler = Objects.requireNonNull(inputHandler, "Input handler cannot be null.");
        this.secondaryInputHandler = Objects.requireNonNull(secondaryInputHandler, "Secondary input handler cannot be null.");
        this.outputHandler = Objects.requireNonNull(outputHandler, "Output handler cannot be null.");
        this.inputSupplier = Objects.requireNonNull(inputSupplier, "Input ingredient supplier cannot be null.");
        this.secondaryInputSupplier = Objects.requireNonNull(secondaryInputSupplier, "Secondary input ingredient supplier cannot be null.");
        this.outputGetter = Objects.requireNonNull(outputGetter, "Output getter cannot be null.");
        this.inputEmptyCheck = Objects.requireNonNull(inputEmptyCheck, "Input empty check cannot be null.");
        this.secondaryInputEmptyCheck = Objects.requireNonNull(secondaryInputEmptyCheck, "Secondary input empty check cannot be null.");
        this.outputEmptyCheck = Objects.requireNonNull(outputEmptyCheck, "Output empty check cannot be null.");
        this.inputsSetter = (input, secondary) -> {
            this.input = input;
            this.secondaryInput = secondary;
        };
        this.outputSetter = output -> this.output = output;
    }

    public static <RECIPE extends MMBasicItemStackChemicalToItemStackRecipe> ReplicatorCachedRecipe<ItemStack, RECIPE>
    createItemReplicator(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ItemStack> itemInputHandler,
                         IInputHandler<@NotNull ChemicalStack> chemicalInputHandler, IOutputHandler<@NotNull ItemStack> outputHandler) {
        return new ReplicatorCachedRecipe<>(recipe, recheckAllErrors, itemInputHandler, chemicalInputHandler, outputHandler, recipe::getItemInput, recipe::getChemicalInput,
                recipe::getOutput, ConstantPredicates.ITEM_EMPTY, ConstantPredicates.CHEMICAL_EMPTY, ConstantPredicates.ITEM_EMPTY);
    }

    public static <RECIPE extends BasicFluidChemicalToFluidRecipe> ReplicatorCachedRecipe<FluidStack, RECIPE>
    createFluidReplicator(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull FluidStack> fluidInputHandler,
                          IInputHandler<@NotNull ChemicalStack> chemicalInputHandler, IOutputHandler<@NotNull FluidStack> outputHandler) {
        return new ReplicatorCachedRecipe<>(recipe, recheckAllErrors, fluidInputHandler, chemicalInputHandler, outputHandler, recipe::getFluidInput, recipe::getChemicalInput,
                recipe::getOutput, ConstantPredicates.FLUID_EMPTY, ConstantPredicates.CHEMICAL_EMPTY, ConstantPredicates.FLUID_EMPTY);
    }

    public static <RECIPE extends MMBasicChemicalChemicalToChemicalRecipe> ReplicatorCachedRecipe<ChemicalStack, RECIPE>
    createChemicalReplicator(RECIPE recipe, BooleanSupplier recheckAllErrors, IInputHandler<@NotNull ChemicalStack> firstInputHandler,
                          IInputHandler<@NotNull ChemicalStack> secondaryInputHandler, IOutputHandler<@NotNull ChemicalStack> outputHandler) {
        return new ReplicatorCachedRecipe<>(recipe, recheckAllErrors, firstInputHandler, secondaryInputHandler, outputHandler, recipe::getLeftInput, recipe::getRightInput,
                recipe::getOutput, ConstantPredicates.CHEMICAL_EMPTY, ConstantPredicates.CHEMICAL_EMPTY, ConstantPredicates.CHEMICAL_EMPTY);
    }

    @Override
    protected void calculateOperationsThisTick(OperationTracker tracker) {
        super.calculateOperationsThisTick(tracker);
        CachedRecipeHelper.twoInputCalculateOperationsThisTick(tracker, inputHandler, inputSupplier, secondaryInputHandler, secondaryInputSupplier, inputsSetter,
                outputHandler, outputGetter, outputSetter, inputEmptyCheck, secondaryInputEmptyCheck);
    }

    @Override
    public boolean isInputValid() {
        TYPE input = inputHandler.getInput();
        if (inputEmptyCheck.test(input)) {
            return false;
        }
        ChemicalStack secondaryInput = secondaryInputHandler.getInput();
        return !secondaryInputEmptyCheck.test(secondaryInput) && recipe.test(input, secondaryInput);
    }

    @Override
    protected void finishProcessing(int operations) {
        //Validate something didn't go horribly wrong
        if (input != null && secondaryInput != null && output != null && !inputEmptyCheck.test(input) && !secondaryInputEmptyCheck.test(secondaryInput) &&
                !outputEmptyCheck.test(output)) {
//            inputHandler.use(input, operations);
            secondaryInputHandler.use(secondaryInput, operations);
            outputHandler.handleOutput(output, operations);
        }
    }
}
