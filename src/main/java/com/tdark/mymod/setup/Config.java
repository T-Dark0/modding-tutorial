package com.tdark.mymod.setup;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Path;

@Mod.EventBusSubscriber
public class Config {
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_POWER = "power";
    public static final String SUBCATEGORY_FIRSTBLOCK = "firstblock";
    public static final String CATEGORY_ITEMS = "items";
    public static final String SUBCATEGORY_TELEPORTSTAFF = "teleport_staff";

    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static ForgeConfigSpec.IntValue FIRSTBLOCK_MAXPOWER;
    public static ForgeConfigSpec.IntValue FIRSTBLOCK_GENERATE;
    public static ForgeConfigSpec.IntValue FIRSTBLOCK_POWEROUTPUT;
    public static ForgeConfigSpec.IntValue FIRSTBLOCK_FUELDURATION;
    public static ForgeConfigSpec.IntValue TELEPORTSTAFF_RANGE;

    static {
        COMMON_BUILDER.comment("General Settings").push(CATEGORY_GENERAL);
        COMMON_BUILDER.pop(); //The push and pop act like "category brackets

        COMMON_BUILDER.comment("Power Settings").push(CATEGORY_POWER);
            COMMON_BUILDER.comment("First Block settings").push(SUBCATEGORY_FIRSTBLOCK);
                FIRSTBLOCK_MAXPOWER = COMMON_BUILDER.comment("Maximum power that can be stored. Default is 100_000")
                        .defineInRange("maxPower", 100_000, 0, Integer.MAX_VALUE);
                FIRSTBLOCK_GENERATE = COMMON_BUILDER.comment("Power generated per diamond. Default is 1000")
                        .defineInRange("powerGen", 1000, 0, Integer.MAX_VALUE);
                FIRSTBLOCK_POWEROUTPUT = COMMON_BUILDER.comment("Maximum power output per tick. Default is 100")
                        .defineInRange("powerOutput", 100, 0, Integer.MAX_VALUE);
                FIRSTBLOCK_FUELDURATION = COMMON_BUILDER.comment("Time taken to burn a single diamond, in ticks. Default is 20")
                        .defineInRange("fuelDuration", 20, 0, Integer.MAX_VALUE);
            COMMON_BUILDER.pop();
        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Item Settings").push(CATEGORY_ITEMS);
            COMMON_BUILDER.comment("Teleport staff settings").push(SUBCATEGORY_TELEPORTSTAFF);
                TELEPORTSTAFF_RANGE = COMMON_BUILDER.comment("Maximum range of the teleport staff. Default is 32")
                        .defineInRange("maxRange", 32, 0, 128);
            COMMON_BUILDER.pop();
        COMMON_BUILDER.pop();


        COMMON_CONFIG = COMMON_BUILDER.build();
        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }
}
