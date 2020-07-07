package com.tdark.mymod;

import com.tdark.mymod.blocks.*;
import com.tdark.mymod.blocks.containers.ContainerPowerCharger;
import com.tdark.mymod.blocks.containers.FirstBlockContainer;
import com.tdark.mymod.blocks.tileentities.FirstBlockTile;
import com.tdark.mymod.blocks.tileentities.TileEntityPowerCharger;
import com.tdark.mymod.entities.WeirdEntity;
import com.tdark.mymod.items.FirstItem;
import com.tdark.mymod.items.ItemTeleportStaff;
import com.tdark.mymod.items.WeirdEntityEggItem;
import com.tdark.mymod.setup.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
    Order of setup: The constructor runs first, followed by all the registry events, followed by the setup event
 */

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mymod")
public class MyMod
{
    public static final String MODID = "mymod";

    //Depending on the side, the DistExecutor runs either of its arguments, creating an appropriate proxy
    //As for the "double lambda", it ensures that clientProxy isn't loaded unless we are, in fact, on the client.
    // I suppose a single lambda would try to load a class (and its imports) too soon
    //Ignore the lint: class loading is subtly different with method references
    @SuppressWarnings("Convert2MethodRef")
    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    public static ModSetup setup = new ModSetup();

    private static final Logger LOGGER = LogManager.getLogger();

    public MyMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        //Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("MyMod-client.toml"));
        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("MyMod-common.toml"));

        // Register ourselves for server and other game events we are interested in
        //MinecraftForge.EVENT_BUS.register(this); //This will be needed when this class needs game events. Currently
        //only the inner class does, and it's auto registered due to the annotation
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        setup.init();
        proxy.init();
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            IForgeRegistry<Block> registry = event.getRegistry();

            registry.register(new FirstBlock());
            registry.register(new FancyBlock());
            registry.register(new BlockPowerCharger());
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            Item.Properties properties = new Item.Properties()
                    .group(setup.itemGroup);

            IForgeRegistry<Item> registry = event.getRegistry();

            //blockItems
            registry.register(new BlockItem(ModBlocks.FIRSTBLOCK, properties).setRegistryName("firstblock"));
            registry.register(new BlockItem(ModBlocks.FANCYBLOCK, properties).setRegistryName("fancyblock"));
            registry.register(new BlockItem(ModBlocks.POWERCHARGER, properties).setRegistryName("block_power_charger"));

            //Items
            registry.register(new FirstItem());
            registry.register(new WeirdEntityEggItem());
            registry.register(new ItemTeleportStaff());
        }

        @SubscribeEvent
        public static void OnTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {

            IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();

            registry.register(TileEntityType.Builder.create(FirstBlockTile::new, ModBlocks.FIRSTBLOCK)
                    .build(null)
                    .setRegistryName("firstblock")
            );
            //Create a new TET. The supplier is the constructor for FirstBlockTile, and we associate it with FIRSTBLOCK.
            //The same TE can be associated with multiple blocks.

            registry.register(TileEntityType.Builder.create(FancyBlockTile::new, ModBlocks.FANCYBLOCK)
                    .build(null)
                    .setRegistryName("fancyblock")
            );

            registry.register(TileEntityType.Builder.create(TileEntityPowerCharger::new, ModBlocks.POWERCHARGER)
                    .build(null)
                    .setRegistryName("block_power_charger")
            );
        }

        @SubscribeEvent
        public static void OnContainerRegistry(final RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().register(
                IForgeContainerType.create((windowId, inv, data) -> {
                    BlockPos pos = data.readBlockPos();
                    return new FirstBlockContainer(windowId, proxy.getClientWorld(), pos, inv);
                }).setRegistryName("firstblock")
            );

            event.getRegistry().register(
                    IForgeContainerType.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        return new ContainerPowerCharger(windowId, proxy.getClientWorld(), pos, inv);
                    }).setRegistryName("block_power_charger")
            );
        }

        @SubscribeEvent
        public static void OnEntityRegistry(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().register(EntityType.Builder.create(WeirdEntity::new, EntityClassification.CREATURE)
                    .size(1, 1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("weirdentity").setRegistryName(MyMod.MODID, "weirdentity")
            );
        }

        @SubscribeEvent
        public static void onTextureStitch(TextureStitchEvent.Pre event) {
            if (!event.getMap().getTextureLocation().equals((AtlasTexture.LOCATION_BLOCKS_TEXTURE))) {
                return;
            }
            event.addSprite(new ResourceLocation(MyMod.MODID, "block/fancyblock"));
        }

        @SubscribeEvent
        public static void onModelBake(ModelBakeEvent event) {
            event.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(MyMod.MODID, "fancyblock"), ""),
                    new FancyBlockBakedModel());
            event.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(MyMod.MODID, "fancyblock"), "inventory"),
                    new FancyBlockBakedModel());
        }
    }
}
