package com.tdark.mymod.blocks.tileentities;

import com.tdark.mymod.blocks.ModBlocks;
import com.tdark.mymod.blocks.containers.ContainerPowerCharger;
import com.tdark.mymod.tools.CustomEnergyStorage;
import com.tdark.mymod.tools.DelegatingItemHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityPowerCharger extends TileEntity
implements ITickableTileEntity, INamedContainerProvider {

    private final LazyOptional<IItemHandlerModifiable> externalItemHandler;
    private final LazyOptional<ItemStackHandler> itemHandler;
    private final LazyOptional<CustomEnergyStorage> energyHandler;

    private LazyOptional<IEnergyStorage> itemEnergyCache;

    private static final int CHARGING_SPEED = 100;
    private static final int INPUT_SLOT = 0;
    private static final int CHARGING_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    public TileEntityPowerCharger() {
        super(ModBlocks.POWERCHARGER_TILE);

        ItemStackHandler internalItemHandler = createItemHandler();

        this.externalItemHandler = LazyOptional.of(() -> createExternalItemHandler(internalItemHandler));
        this.itemHandler = LazyOptional.of(() -> internalItemHandler);
        this.energyHandler = LazyOptional.of(this::createEnergyHandler);
    }

    private IItemHandlerModifiable createExternalItemHandler(IItemHandlerModifiable internalItemHandler) {
        return new DelegatingItemHandler(internalItemHandler) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot == INPUT_SLOT) {
                    return isItemStackChargeable(stack);
                } else {
                    return false;
                }
            }
        };
    }

    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
        };
    }

    private CustomEnergyStorage createEnergyHandler() {
        return new CustomEnergyStorage(100_000, 1000) {

            @Override
            public void onContentsChanged() {
                markDirty();
            }
        };
    }

    @Override
    public void tick() {
        if(world.isRemote) return;
        if(!hasEnergy()) return;

        attemptMoveItemStack(INPUT_SLOT, CHARGING_SLOT, 1);

        if(!isItemCharging()) return;

        chargeItem();
        if(isItemCharged()) {
            if(attemptMoveItemStack(CHARGING_SLOT, OUTPUT_SLOT, 1)) {
                itemEnergyCache = null; //Wipe the cache, we just output that item
            }
        }
    }

    private boolean hasEnergy() {
        return energyHandler
                .map(h -> h.getEnergyStored() > 0)
                .orElse(false);
    }

    /**
     * Attempts to move up to {@code amount} items from the stack in slot {@code fromSlot} to the slot {@code toSlot}
     * @param fromSlot the slot to move from
     * @param toSlot the slot to move to
     * @param amount the amount of items to move
     * @return {@code true} if the itemstack was fully moved, {@code false} if some items are still in {@code fromSlot}
     */
    private boolean attemptMoveItemStack(int fromSlot, int toSlot, int amount) {
        return itemHandler
                .map(h -> {
                    ItemStack stack = h.extractItem(fromSlot, amount, false);
                    ItemStack notInserted = h.insertItem(toSlot, stack, false);
                    h.insertItem(fromSlot, notInserted, false);
                    return notInserted.isEmpty();
                })
                .orElseThrow(() -> new IllegalStateException("Missing item handler in power charger TE"));
    }


    private LazyOptional<IEnergyStorage> getChargingStackEnergyCap() {
        if(itemEnergyCache == null) {
            itemEnergyCache = itemHandler
                    .map(h -> h.getStackInSlot(CHARGING_SLOT).getCapability(CapabilityEnergy.ENERGY))
                    .orElseThrow(() -> new IllegalStateException("Missing item handler in power charger TE"));
        }
        return itemEnergyCache;
    }

    private boolean isItemCharging() {
        return itemHandler
                .map(h -> ! h.getStackInSlot(CHARGING_SLOT).isEmpty())
                .orElseThrow(() -> new IllegalStateException("Missing item handler in power charger TE"));
    }

    private void chargeItem() {
        energyHandler.ifPresent(ownEnergy -> {
                int transferredEnergy = increaseItemEnergy(Math.min(CHARGING_SPEED, ownEnergy.getEnergyStored()));
                ownEnergy.extractEnergy(transferredEnergy, false);
        });
    }

    private int increaseItemEnergy(int energy) {
        return getChargingStackEnergyCap()
                .map(h -> h.receiveEnergy(energy, false))
                .orElseThrow(() -> new IllegalStateException("Charging item does not have an energy capability"));
    }

    private boolean isItemCharged() {
        return getChargingStackEnergyCap()
                .map(h -> h.getEnergyStored() == h.getMaxEnergyStored())
                .orElseThrow(() -> new IllegalStateException("Charging item does not have an energy capability"));
    }

    private boolean isItemStackChargeable(ItemStack stack) {
        return stack.getCapability(CapabilityEnergy.ENERGY)
                .map(IEnergyStorage::canReceive)
                .orElse(false);
    }

    @Override
    public void read(CompoundNBT compound) {
        energyHandler.ifPresent(h -> h.setEnergy(compound.getInt("Energy")));
        itemHandler.ifPresent(h -> h.deserializeNBT(compound.getCompound("Items")));
        super.read(compound);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        energyHandler.ifPresent(h -> compound.putInt("Energy", h.getEnergyStored()));
        itemHandler.map(ItemStackHandler::serializeNBT).ifPresent(nbt -> compound.put("Items", nbt));
        return super.write(compound);
    }

    // When the world loads from disk, the server needs to send the TileEntity information to the client
    // it uses getUpdatePacket(), getUpdateTag(), onDataPacket(), and handleUpdateTag() to do this:
    // getUpdatePacket() and onDataPacket() are used for one-at-a-time TileEntity updates
    // getUpdateTag() and handleUpdateTag() are used by vanilla to collate together into a single chunk update packet
    // Your container may still appear to work even if you forget to implement these methods, because when you open the
    // container using the GUI it takes the information from the server, but anything on the client
    // side that looks inside the tileEntity (for example: to change the rendering) won't see anything.
    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        int tileEntityType = -1;  // arbitrary number; only used for vanilla TileEntities.  You can use it, or not, as you want.
        return new SUpdateTileEntityPacket(this.pos, tileEntityType, nbtTagCompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        read(pkt.getNbtCompound());
    }

    /* Creates a tag containing all of the TileEntity information, used by vanilla to transmit from server to client
     */
    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT nbtTagCompound = new CompoundNBT();
        write(nbtTagCompound);
        return nbtTagCompound;
    }

    /* Populates this TileEntity with information from the tag, used by vanilla to transmit from server to client
     *  The vanilla default is suitable for this example but I've included an explicit definition anyway.
     */
    @Override
    public void handleUpdateTag(CompoundNBT tag)
    {
        this.read(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return externalItemHandler.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("Item Charger");
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerPowerCharger(i, this.world, this.pos, playerInventory);
    }
}
