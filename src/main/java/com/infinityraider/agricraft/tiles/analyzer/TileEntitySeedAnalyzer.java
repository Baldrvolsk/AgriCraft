package com.infinityraider.agricraft.tiles.analyzer;

import com.agricraft.agricore.core.AgriCore;
import com.infinityraider.agricraft.api.v1.AgriApi;
import com.infinityraider.agricraft.api.v1.misc.IAgriDisplayable;
import com.infinityraider.agricraft.api.v1.seed.AgriSeed;
import com.infinityraider.agricraft.init.AgriItems;
import com.infinityraider.agricraft.items.ItemJournal;
import com.infinityraider.agricraft.reference.AgriNBT;
import com.infinityraider.agricraft.utility.StackHelper;
import com.infinityraider.infinitylib.block.tile.TileEntityRotatableBase;
import java.util.Optional;
import java.util.function.Consumer;

import com.infinityraider.infinitylib.utility.inventory.IInventoryItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class TileEntitySeedAnalyzer extends TileEntityRotatableBase implements ISidedInventory, ITickable, IAgriDisplayable, IInventoryItemHandler {

    public static final int SPECIMEN_SLOT_ID = 36;
    public static final int JOURNAL_SLOT_ID = 37;

    private static final int[] SLOTS = new int[]{
        SPECIMEN_SLOT_ID,
        JOURNAL_SLOT_ID
    };

    /**
     * The SEED that the SEED analyzer contains.
     *
     * Defaults to null, for empty.
     */
    private ItemStack specimen = null;

    /**
     * The journal that the SEED analyzer contains.
     *
     * Defaults to null, for empty.
     */
    private ItemStack journal = null;

    /**
     * The current progress of the SEED analyzer.
     */
    private int progress = 0;

    @Override
    protected void writeRotatableTileNBT(NBTTagCompound tag) {
        if (this.specimen != null && this.specimen.getItem() != null) {
            NBTTagCompound seedTag = new NBTTagCompound();
            this.specimen.writeToNBT(seedTag);
            tag.setTag(AgriNBT.SEED, seedTag);
        }
        if (this.journal != null && this.journal.getItem() != null) {
            NBTTagCompound journalTag = new NBTTagCompound();
            this.journal.writeToNBT(journalTag);
            tag.setTag(AgriItems.getInstance().JOURNAL.getUnlocalizedName(), journalTag);
        }
        tag.setInteger("progress", this.progress);
    }

    @Override
    protected void readRotatableTileNBT(NBTTagCompound tag) {
        if (tag.hasKey(AgriNBT.SEED)) {
            this.specimen = new ItemStack(tag.getCompoundTag(AgriNBT.SEED));
        } else {
            //Not certain this is required... Unsure if networking thing?
            this.specimen = null;
        }
        if (tag.hasKey(AgriItems.getInstance().JOURNAL.getUnlocalizedName())) {
            this.journal = new ItemStack(tag.getCompoundTag(AgriItems.getInstance().JOURNAL.getUnlocalizedName()));
        } else {
            this.journal = null;
        }
        this.progress = tag.getInteger("progress");
    }

    /**
     * Determines if the SEED analyzer contains a SEED or trowel in its analyze
     * slot. A null check on {@link #getSpecimen()} should return the same.
     *
     * @return if a SEED or trowel is present.
     */
    public final boolean hasSpecimen() {
        return this.hasSeed();
    }

    /**
     * Retrieves the item in the analyzer's analyze slot. (Does not remove). May
     * be either a SEED or a trowel.
     *
     * @return the item in the analyze slot.
     */
    public final ItemStack getSpecimen() {
        return this.specimen;
    }

    /**
     * Determines if the analyzer has a <em>valid</em> SEED in its analyze slot.
     *
     * @return if the analyze slot contains a <em>valid</em> SEED.
     */
    public final boolean hasSeed() {
        return AgriApi.getSeedRegistry().hasAdapter(specimen);
    }

    public final void setProgress(int value) {
        this.progress = value;
    }

    public final int getProgress() {
        return this.progress;
    }

    /**
     * Calculates the number of ticks it takes to analyze the SEED.
     *
     * @return ticks to analyze SEED.
     */
    public final int maxProgress() {
        return 100;
    }

    /**
     * Determines if a stack is valid for analyzation.
     *
     * @param stack the stack to check.
     * @return if the stack is valid.
     */
    public static boolean isValid(ItemStack stack) {
        return AgriApi.getSeedRegistry().hasAdapter(stack);
    }

    /**
     * Determines if a contained specimen has already been ANALYZED.
     *
     * @return if the specimen has been ANALYZED.
     */
    public final boolean isSpecimenAnalyzed() {
        if (this.specimen != null) {
            Optional<AgriSeed> seed = AgriApi.getSeedRegistry().valueOf(specimen);
            return seed.isPresent() && seed.get().getStat().isAnalyzed();
        }
        return false;
    }

    /**
     * Called every tick.
     *
     * Used to update the progress counter.
     */
    @Override
    public void update() {
        boolean change = false;
        if (this.isAnalyzing()) {
            //increment progress counter
            this.progress = progress < this.maxProgress() ? progress + 1 : this.maxProgress();
            //if progress is complete analyze the SEED
            if (progress == this.maxProgress() && !this.getWorld().isRemote) {
                this.analyze();
                change = true;
            }
        }
        if (change) {
            this.markForUpdate();
            this.getWorld().addBlockEvent(this.getPos(), this.getWorld().getBlockState(getPos()).getBlock(), 0, 0);
            this.getWorld().notifyNeighborsOfStateChange(getPos(), this.getBlockType(), true);
        }
    }

    /**
     * Analyzes the current SEED.
     *
     * Marked for cleanup.
     */
    public void analyze() {
        //analyze the SEED
        final Optional<AgriSeed> wrapper = AgriApi.getSeedRegistry().valueOf(specimen);
        if (wrapper.isPresent()) {
            AgriSeed seed = wrapper.get();
            seed = seed.withStat(seed.getStat().withAnalyzed(true));
            seed.getStat().writeToNBT(StackHelper.getTag(specimen));
            if (this.hasJournal()) {
                ((ItemJournal) journal.getItem()).addEntry(journal, seed.getPlant());
            }
        }
    }

    /**
     * Checks if the analyzer is analyzing.
     *
     * @return if the analyzer is analyzing.
     */
    public final boolean isAnalyzing() {
        return this.specimen != null && !this.isSpecimenAnalyzed() && progress < maxProgress();
    }

    /**
     * checks if there is a journal in the analyzer.
     *
     * @return if the analyzer contains a journal.
     */
    public final boolean hasJournal() {
        return (this.journal != null && this.journal.getItem() != null);
    }

    /**
     * Retrieves the journal from the analyzer. (Does not remove).
     *
     * @return the journal from the analyzer.
     */
    public final ItemStack getJournal() {
        return this.journal;
    }

    /**
     * Returns the scaled progress percentage. Rounds the progress up.
     *
     * @param scale ???
     * @return the scaled progress percentage.
     */
    public final int getProgressScaled(int scale) {
        return Math.round(((float) this.progress * scale) / ((float) this.maxProgress()));
    }

    //Inventory methods
    //-----------------
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }

    //check if item can be inserted
    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing direction) {
        switch (slot) {
            case SPECIMEN_SLOT_ID:
                return isValid(stack);
            case JOURNAL_SLOT_ID:
                return this.journal == null && this.isItemValidForSlot(slot, stack);
            default:
                return false;
        }
    }

    //check if an item can be extracted
    @Override
    public boolean canExtractItem(int slot, ItemStack itemStackIn, EnumFacing direction) {
        if (slot == SPECIMEN_SLOT_ID && this.specimen != null && this.specimen.hasTagCompound()) {
            return this.isSpecimenAnalyzed();
        }
        return false;
    }

    //returns the INVENTORY SIZE
    @Override
    public int getSizeInventory() {
        return 2;
    }

    //returns the stack in the slot
    @Override
    public ItemStack getStackInSlot(int slot) {
        switch (slot) {
            case SPECIMEN_SLOT_ID:
                return this.specimen;
            case JOURNAL_SLOT_ID:
                return this.journal;
            default:
                return null;
        }
    }

    //decreases the stack in a slot by an amount and returns that amount as an itemstack
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack output = null;
        switch (slot) {
            case SPECIMEN_SLOT_ID:
                if (this.specimen != null) {
                    if (amount < this.specimen.getCount()) {
                        output = this.specimen.splitStack(amount);
                    } else {
                        output = this.specimen.copy();
                        this.specimen = null;
                        this.progress = 0;
                        this.markForUpdate();
                    }
                }
                break;
            case JOURNAL_SLOT_ID:
                if (this.journal != null) {
                    output = this.journal.copy();
                    this.journal = null;
                    this.markForUpdate();
                }
                break;
        }
        return output;
    }

    //gets item stack in the slot when closing the INVENTORY
    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack result;
        switch (slot) {
            case SPECIMEN_SLOT_ID:
                result = this.specimen;
                this.specimen = null;
                this.progress = 0;
                break;
            case JOURNAL_SLOT_ID:
                result = this.journal;
                this.journal = null;
                break;
            default:
                return null;
        }
        this.markForUpdate();
        return result;
    }

    /**
     * Sets the items in a slot to this stack.
     * Also makes sure the journal is updated if the specimen is already analyzed.
     * @param slot  Use either of the two constants to refer to the slots.
     * @param stack It is recommended to first call isItemValidForSlot, as this method does not.
     */
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        // Step 1: Update the appropriate slot.
        switch (slot) {
            case SPECIMEN_SLOT_ID:
                this.specimen = stack;
                if (stack != null && stack.getCount() > getInventoryStackLimit()) {
                    stack.setCount(getInventoryStackLimit());
                }
                this.progress = isSpecimenAnalyzed() ? maxProgress() : 0;
                break;
            case JOURNAL_SLOT_ID:
                this.journal = stack;
                break;
        }

        // Step 2: If both an analyzed plant and a journal are present, then make sure there's an entry recorded.
        final Optional<AgriSeed> seed = AgriApi.getSeedRegistry().valueOf(specimen);
        if (seed.isPresent() && this.hasJournal() && seed.get().getStat().isAnalyzed()) {
            // The add method tests if the journal already has the plant before adding it.
            ((ItemJournal) journal.getItem()).addEntry(journal, seed.get().getPlant());
        }

        // Step 3: Finish up.
        this.markForUpdate();
    }

    //returns the maximum stacksize
    @Override
    public final int getInventoryStackLimit() {
        return 64;
    }

    //if this is usable by a player
    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.getWorld().getTileEntity(pos) == this
                && player.getDistanceSq(pos.add(0.5, 0.5, 0.5)) <= 64.0;
    }

    /**
     * Opens the INVENTORY. (Empty method).
     *
     * @param player
     */
    @Override
    public void openInventory(EntityPlayer player) {
    }

    /**
     * Closes the INVENTORY. (Empty method).
     *
     * @param player
     */
    @Override
    public void closeInventory(EntityPlayer player) {
    }

    /**
     * Checks if a stack is valid for a slot.
     *
     * @param slot
     * @param stack
     * @return if the item is valid.
     */
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        switch (slot) {
            case SPECIMEN_SLOT_ID:
                return TileEntitySeedAnalyzer.isValid(stack);
            case JOURNAL_SLOT_ID:
                return StackHelper.isValid(stack, ItemJournal.class);
            default:
                return false;
        }
    }

    @Override
    public String getName() {
        return "container.agricraft:seedAnalyzer";
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString("container.agricraft:seedAnalyzer");
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.specimen = null;
        this.journal = null;
    }

    @Override
    public void addDisplayInfo(Consumer<String> information) {
        information.accept(AgriCore.getTranslator().translate("agricraft_tooltip.analyzer") + ": " + (this.hasSpecimen() ? specimen.getDisplayName() : AgriCore.getTranslator().translate("agricraft_tooltip.none")));
    }
}
