package com.tdark.mymod.tools;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

/**
 * A simple item handler that delegates all of its actual functionality to another item handler.
 */
public class DelegatingItemHandler
implements IItemHandlerModifiable {

    private final IItemHandlerModifiable delegate;

    public DelegatingItemHandler(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if(isItemValid(slot, stack)) {
            return delegate.insertItem(slot, stack, simulate);
        } else {
            return stack;
        }
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        delegate.setStackInSlot(slot, stack);
    }
}
