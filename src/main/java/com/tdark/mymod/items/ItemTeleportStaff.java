package com.tdark.mymod.items;

import com.tdark.mymod.MyMod;
import com.tdark.mymod.setup.Config;
import com.tdark.mymod.tools.CustomEnergyStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemTeleportStaff extends Item {

    private static final Vec3d RAYCAST_LENGTH;

    static {
        int raycastRange = Config.TELEPORTSTAFF_RANGE.get();
        RAYCAST_LENGTH = new Vec3d(raycastRange, raycastRange, raycastRange);
    }

    public ItemTeleportStaff() {
        super(new Item.Properties()
            .maxStackSize(1)
            .group(MyMod.setup.itemGroup)
        );
        setRegistryName("teleport_staff");
    }

    private BlockRayTraceResult rayTraceBlock(PlayerEntity player) {
        Vec3d startVec = player.getEyePosition(1.0f);
        Vec3d endVec = startVec.add(RAYCAST_LENGTH.mul(player.getLookVec()));

        return player.world.rayTraceBlocks(
            new RayTraceContext(
                startVec,
                endVec,
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                player
            )
        );
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {

        ItemStack thisStaff = player.getHeldItem(hand);

        if(world.isRemote) return ActionResult.resultSuccess(thisStaff);

        System.out.println("Right click");
        LazyOptional<CustomEnergyStorage> thisEnergy = thisStaff.getCapability(CapabilityEnergy.ENERGY).cast();

        if(!canPayTeleport(thisEnergy)) return ActionResult.resultSuccess(thisStaff);

        thisEnergy.ifPresent(h -> System.out.println("On use energy: " + h.getEnergyStored()));

        BlockRayTraceResult hitBlock = rayTraceBlock(player);
        BlockPos pos = hitBlock.getPos();
        Direction dir = hitBlock.getFace();

        BlockPos teleportPos = pos.offset(dir);

        BlockPos headPos = teleportPos.offset(Direction.UP);
        BlockPos floorPos = teleportPos.offset(Direction.DOWN);

        if(!world.isAirBlock(headPos)) { //Are we moving to a space where our head would be in a block?
            if(!world.isAirBlock(floorPos)) { //Is the space we are trying to move to 1 block tall?
                return ActionResult.resultSuccess(thisStaff);
            }
            teleportPos = floorPos;
        }

        payTeleport(thisEnergy);
        player.setPositionAndUpdate(teleportPos.getX() + 0.5, teleportPos.getY(), teleportPos.getZ() + 0.5);
        return ActionResult.resultSuccess(thisStaff);
    }

    private void payTeleport(LazyOptional<CustomEnergyStorage> thisEnergy) {
        thisEnergy.ifPresent(h -> h.extractEnergy(100));
    }



    private boolean canPayTeleport(LazyOptional<CustomEnergyStorage> thisEnergy) {
        return thisEnergy
                .map(h -> h.getEnergyStored() > 100)
                .orElse(false);
    }

    @Nullable
    @Override
    //Must return the ENTIRE NBT TAG of the item, including any extra data added in this method
    public CompoundNBT getShareTag(ItemStack stack) {
        CompoundNBT standardTag = stack.getOrCreateTag();
        int energy = stack.getCapability(CapabilityEnergy.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElseThrow(() -> new IllegalStateException("Teleport staff had no energy capability"));

        CompoundNBT shareTag = new CompoundNBT();
        shareTag.putInt("Energy", energy);
        standardTag.put(MyMod.MODID, shareTag);
        return standardTag;
    }

    @Override
    public void readShareTag(ItemStack stack, @Nullable CompoundNBT nbt) {
        int energy = nbt.getCompound(MyMod.MODID).getInt("Energy");

        stack.getCapability(CapabilityEnergy.ENERGY)
                .ifPresent(h -> ((CustomEnergyStorage)h).setEnergy(energy));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        int energy = stack.getCapability(CapabilityEnergy.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElseThrow(() -> new IllegalStateException("Clientside staff had no energy capability"));

        tooltip.add(new StringTextComponent("Energy: " + energy));
    }



    @Nullable
    @Override
    //The stack of this item that is being created/loaded, and its capability NBT data
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new EnergyCapProvider();
    }

    //This is what the return type of initCapabilities needs to be. Can be used to do fancy things such as change capabilities dynamically
    //which is handy for stuff like upgrades that add a tank, for example
    private static class EnergyCapProvider
    implements ICapabilitySerializable<CompoundNBT> {

        private final LazyOptional<CustomEnergyStorage> energyHandler;

        public EnergyCapProvider() {
            energyHandler = LazyOptional.of(() -> new CustomEnergyStorage(10_000, 100, 10_000,0));
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if(cap == CapabilityEnergy.ENERGY) {
                return energyHandler.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundNBT serializeNBT() {
            int energy = energyHandler
                    .map(EnergyStorage::getEnergyStored)
                    .orElseThrow(() -> new IllegalStateException("Teleport staff energy cap provider has no energy cap"));

            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Energy", energy);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            int energy = nbt.getInt("Energy");
            energyHandler.ifPresent(h -> h.setEnergy(energy));
        }
    }
}
