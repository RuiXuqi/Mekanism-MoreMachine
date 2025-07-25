package com.jerry.mekaf.common.upgrade;

import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.chemical.ChemicalInventorySlot;
import mekanism.common.tile.component.ITileComponent;
import mekanism.common.tile.interfaces.IRedstoneControl;
import net.minecraft.core.HolderLookup;

import java.util.Collections;
import java.util.List;

public class ChemicalChemicalToChemicalUpgradeData extends ChemicalToChemicalUpgradeData {

    public final ChemicalInventorySlot chemicalSlot;
    public final IChemicalTank inputTank;
    public final long[] usedSoFar;

    public ChemicalChemicalToChemicalUpgradeData(HolderLookup.Provider provider, boolean redstone, IRedstoneControl.RedstoneControl controlType,
                                                 IEnergyContainer energyContainer, int operatingTicks, EnergyInventorySlot energySlot,
                                                 ChemicalInventorySlot chemicalSlot, IChemicalTank inputSlot, IChemicalTank inputTank, IChemicalTank outputTank,
                                                 List<ITileComponent> components) {
        this(provider, redstone, controlType, energyContainer, new int[]{operatingTicks}, null, energySlot, chemicalSlot, Collections.singletonList(inputSlot), inputTank, Collections.singletonList(outputTank), false, components);
    }

    public ChemicalChemicalToChemicalUpgradeData(HolderLookup.Provider provider, boolean redstone, IRedstoneControl.RedstoneControl controlType,
                                                 IEnergyContainer energyContainer, int operatingTicks, long usedSoFar, EnergyInventorySlot energySlot,
                                                 ChemicalInventorySlot chemicalSlot, IChemicalTank inputSlot, IChemicalTank inputTank, IChemicalTank outputTank,
                                                 List<ITileComponent> components) {
        this(provider, redstone, controlType, energyContainer, new int[]{operatingTicks}, new long[]{usedSoFar}, energySlot, chemicalSlot, Collections.singletonList(inputSlot), inputTank, Collections.singletonList(outputTank), false, components);
    }

    public ChemicalChemicalToChemicalUpgradeData(HolderLookup.Provider provider, boolean redstone, IRedstoneControl.RedstoneControl controlType,
                                                 IEnergyContainer energyContainer, int[] progress, long[] usedSoFar, EnergyInventorySlot energySlot,
                                                 ChemicalInventorySlot chemicalSlot, List<IChemicalTank> inputSlots, IChemicalTank inputTank,
                                                 List<IChemicalTank> outputTanks, boolean sorting, List<ITileComponent> components) {
        super(provider, redstone, controlType, energyContainer, progress, energySlot, inputSlots, outputTanks, sorting, components);
        this.chemicalSlot = chemicalSlot;
        this.inputTank = inputTank;
        this.usedSoFar = usedSoFar;
    }
}
