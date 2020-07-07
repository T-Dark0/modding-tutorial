package com.tdark.mymod.blocks.tileentities;

import com.tdark.mymod.blocks.ModBlocks;
import com.tdark.mymod.blocks.containers.FirstBlockContainer;
import com.tdark.mymod.setup.Config;
import com.tdark.mymod.tools.CustomEnergyStorage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FirstBlockTile extends TileEntity
implements ITickableTileEntity, INamedContainerProvider { //ITickable is another interface entirely

    private final LazyOptional<ItemStackHandler> itemHandler = LazyOptional.of(this::createItemHandler);
    private final LazyOptional<CustomEnergyStorage> energyHandler = LazyOptional.of(this::createEnergyHandler);

    private int counter;

    public FirstBlockTile() {
        super(ModBlocks.FIRSTBLOCK_TILE);
    }

    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(1) {

            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Items.DIAMOND;
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (isItemValid(slot, stack)) {
                    return super.insertItem(slot, stack, simulate);
                } else {
                    return stack;
                }
            }
        };
    }

    private CustomEnergyStorage createEnergyHandler()
    {
        return new CustomEnergyStorage(Config.FIRSTBLOCK_MAXPOWER.get(), 1000);
    }

    @Override
    public void tick() {
        if(world.isRemote) {
            return;
        }

        burnFuel();
        sendOutPower();
        setPoweredState();
    }

    private void burnFuel() {
        if(counter > 0) {
            counter--;
            if(counter <= 0) {
                energyHandler.ifPresent(e-> e.receiveEnergy(Config.FIRSTBLOCK_GENERATE.get()));
            }
            markDirty();
        }
        if(counter <= 0) {
            itemHandler.ifPresent(h -> {
                ItemStack stack = h.getStackInSlot(0);

                energyHandler.ifPresent(e -> {
                    if (stack.getItem() == Items.DIAMOND && !(e.getMaxEnergyStored() == e.getEnergyStored())) {
                        h.extractItem(0, 1, false);
                        counter = Config.FIRSTBLOCK_FUELDURATION.get();
                    }
                });
            });
        }
    }

    private void sendOutPower() {
        energyHandler.ifPresent(ownEnergy -> {
            int ownEnergyStored = ownEnergy.getEnergyStored();
            if(ownEnergyStored <= 0) return;

            for (Direction direction : Direction.values()) {
                TileEntity te = world.getTileEntity(pos.offset(direction));

                if (te != null) {
                    te.getCapability(CapabilityEnergy.ENERGY, direction).ifPresent(otherEnergy -> {
                        int transferredEnergy = otherEnergy.receiveEnergy(Math.min(ownEnergyStored, Config.FIRSTBLOCK_POWEROUTPUT.get()), false);
                        ownEnergy.extractEnergy(transferredEnergy);
                        markDirty();
                    });
                }
            }
        });
    }

    private void setPoweredState() {
        BlockState blockState = world.getBlockState(pos);
        if(blockState.get(BlockStateProperties.POWERED) != counter > 0) {
            world.setBlockState(pos, blockState.with(BlockStateProperties.POWERED, counter > 0));
        }
    }

    @Override
    public void read(CompoundNBT tag) {
        CompoundNBT invTag = tag.getCompound("inv");

        itemHandler.ifPresent(h -> h.deserializeNBT(invTag));
        energyHandler.ifPresent(h -> h.setEnergy(tag.getInt("energy")));
        super.read(tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        itemHandler.map(ItemStackHandler::serializeNBT).ifPresent(cnbt -> tag.put("inv", cnbt));
        energyHandler.ifPresent(h -> tag.putInt("energy", h.getEnergyStored()));
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        } else if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent("First Block Name");
        //TODO: use localised name
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new FirstBlockContainer(i, world, pos, playerInventory);
    }
}
