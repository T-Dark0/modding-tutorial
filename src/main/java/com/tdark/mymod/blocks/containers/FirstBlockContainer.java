package com.tdark.mymod.blocks.containers;

import com.tdark.mymod.blocks.ModBlocks;
import com.tdark.mymod.tools.CustomEnergyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class FirstBlockContainer extends Container {

    private TileEntity tileEntity;
    private IItemHandler playerInventory;

    public FirstBlockContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory) {
        super(ModBlocks.FIRSTBLOCK_CONTAINER, windowId);
        tileEntity = world.getTileEntity(pos);

        this.playerInventory = new InvWrapper(playerInventory);
        layoutPlayerInventorySlots(10, 70);
        //In the texture for this gui. these are the coordinates of the
        //top left corner of the top left inventory slot

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
            addSlot(new SlotItemHandler(h, 0, 64, 24)); //add handler with index 0, on gui position (64,24)
        });

        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return getEnergy();
            }

            @Override
            public void set(int value) {
                tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> ((CustomEnergyStorage)h).setEnergy(value));
            }
        });
    }

    public int getEnergy() {
        return tileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerEntity) {
        return isWithinUsableDistance(IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()),
                playerEntity,
                ModBlocks.FIRSTBLOCK);
    }

    @Override
    //index is the slot we shift clicked on
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            if (index == 0) {
                if (!this.mergeItemStack(stack, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

            } else {
                if (stack.getItem() == Items.DIAMOND) {
                    if (!this.mergeItemStack(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 28) {
                    if (!this.mergeItemStack(stack, 28, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 37 && !this.mergeItemStack(stack, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemstack;
    }

    private int addSlotRange(IItemHandler handler, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) { //for each slot to add
            addSlot(new SlotItemHandler(handler, index, x, y)); //add the slot at coordinates (x,y) with slot index index
            x += dx; //move the x coordinate by dx, (aka slot width)
            index++; //update the slot index
        }
        return index;
    }

    private void addSlotBox(IItemHandler handler, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) { //for each row to add
            index = addSlotRange(handler, index, x, y, horAmount, dx); //add the row at coordinates (x,y) and update the index
            y += dy; //move the x coordinate by dy, (aka slot height)
        }
    }

    private void layoutPlayerInventorySlots(int leftCol, int topRow) {
        // Player inventory
        //Starting index is 9 because that's the top left inv slot
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        //starting index is 0 because that's the first hotbar slot
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }
}
