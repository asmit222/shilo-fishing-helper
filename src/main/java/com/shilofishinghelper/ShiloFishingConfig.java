package com.shilofishinghelper;

import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("shilofishing")
public interface ShiloFishingConfig extends Config {
    @ConfigItem(
            keyName = "showPath",
            name = "Show Fishing Path",
            description = "Toggles the cyan path overlay"
    )
    default boolean showPath() {
        return true;
    }

    // 🎨 Path color
    @Alpha
    @ConfigItem(
            keyName = "pathColor",
            name = "Path color",
            description = "Color used to draw the path tiles"
    )
    default Color pathColor() {
        // Default cyan
        return new Color(0, 255, 255, 200);
    }

    @ConfigItem(
            keyName = "showInventoryCount",
            name = "Show Free Inventory Count",
            description = "Displays your remaining free inventory spaces next to your character"
    )
    default boolean showInventoryCount() {
        return true;
    }

    @ConfigItem(
            keyName = "showDepositPath",
            name = "Show Path to Deposit Box",
            description = "Show the walking path to the deposit box when inventory is full"
    )
    default boolean showDepositPath() {
        return true;
    }


    @ConfigItem(
            keyName = "showRedScreen",
            name = "Show Color Overlay When Idle",
            description = "Turns the screen red when player is idle / not fishing"
    )
    default boolean showRedScreen() {
        return true;
    }


    @ConfigItem(
            keyName = "idleScreenColor",
            name = "Idle Screen Color",
            description = "Choose the base color for the screen overlay when idle (opacity is fixed)"
    )
    default Color idleScreenColor() {
        return Color.RED;   // base color only, we’ll add the opacity later
    }
}
