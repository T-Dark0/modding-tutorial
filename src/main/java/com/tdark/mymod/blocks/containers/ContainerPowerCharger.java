package com.tdark.mymod.blocks.containers;

import com.tdark.mymod.blocks.ModBlocks;
import com.tdark.mymod.tools.CustomEnergyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
Mind the existence of container slot indices.

When addSlot()-ing, I should use _inventory_ indices, which are unique per inventory. When opening a chest, the chest gets
[0,26] and the player gets [0,35].

In any situation in which I access a slot without specifying an inventory, I need to use a _container_ index. These are
automatically assigned in the range [0, inf), and every time a slot is added it gets the next index.
Therefore, adding player slots _before_ chest slots means they get the range [0,35] and the chest gets the range [36, 62]
 */
public class ContainerPowerCharger extends Container {

    private final TileEntity tileEntity;
    private final IItemHandler playerInventory;

    private LazyOptional<CustomEnergyStorage> TEEnergyCache = null;

    //These are all container slot indices.
    private static final int PLAYER_INVENTORY_START = 0;
    private static final int PLAYER_INVENTORY_END = 35;
    private static final int TE_INVENTORY_START = 36;
    private static final int TE_INVENTORY_END = 38;

    private static final Logger LOGGER = LogManager.getLogger();

    public ContainerPowerCharger(int windowId, World world, BlockPos pos, PlayerInventory playerInventory) {
        super(ModBlocks.POWERCHARGER_CONTAINER, windowId);
        this.tileEntity = world.getTileEntity(pos);
        this.playerInventory = new InvWrapper(playerInventory);

        layoutPlayerInventorySlots(10, 70); //Player gets the container range [0,35]

        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> { //and I get the container range [36,38]
            addSlot(new SlotItemHandler(h, 0, 33, 24));
            addSlot(new SlotItemHandler(h, 1, 82, 24));
            addSlot(new SlotItemHandler(h, 2, 131, 24));
        });

        trackInt(new IntReferenceHolder() {
            @Override
            public int get() {
                return getEnergy();
            }

            @Override
            public void set(int value) {
                setEnergy(value);
            }
        });
    }

    private LazyOptional<CustomEnergyStorage> getTEEnergyCap() {
        if(TEEnergyCache == null) {

            TEEnergyCache = tileEntity.getCapability(CapabilityEnergy.ENERGY).cast();
        }
        return TEEnergyCache;
    }

    private void setEnergy(int value) {
        getTEEnergyCap().ifPresent(h -> h.setEnergy(value));
    }

    public int getEnergy() {
        return getTEEnergyCap()
                .map(EnergyStorage::getEnergyStored)
                .orElse(0);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerEntity) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(), tileEntity.getPos()),
                playerEntity,
                ModBlocks.POWERCHARGER);
    }

    @Override
    //returns all the items that couldn't be moved
    //This method goes by container indices. They are the unique per container, and automatically assigned depending on
    //the order in which I added slots
    public ItemStack transferStackInSlot(PlayerEntity playerEntity, int sourceSlotindex) {
        Slot sourceSlot = getSlot(sourceSlotindex);
        if(!sourceSlot.getHasStack()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getStack();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if(isPlayerSlot(sourceSlotindex)) {
            if (!mergeItemStack(sourceStack, TE_INVENTORY_START,TE_INVENTORY_END + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if(isTESlot(sourceSlotindex)) {
                if(!mergeItemStack(sourceStack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END + 1, false)) {
                    return ItemStack.EMPTY;
                }
        } else {
            LOGGER.warn("Invalid slot index: " + sourceSlotindex);
            return ItemStack.EMPTY;
        }

        if(sourceStack.isEmpty()) { //Was fully moved
            sourceSlot.putStack(ItemStack.EMPTY); //Internally calls onSlotChanged
        } else {
            sourceSlot.onSlotChanged();
        }

        sourceSlot.onTake(playerEntity, sourceStack);
        return copyOfSourceStack;
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
        // Starting index is 9 because that's the top left inv slot
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        // Starting index is 0 because that's the first hotbar slot
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    private boolean isPlayerSlot(int index) {
        return index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END + 1;
    }

    private boolean isTESlot(int index) {
        return index >= TE_INVENTORY_START && index < TE_INVENTORY_END + 1;
    }
}
