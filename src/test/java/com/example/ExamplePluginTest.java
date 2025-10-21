package com.example;

import com.shilofishinghelper.ShiloFishingPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExamplePluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(ShiloFishingPlugin.class);
        RuneLite.main(args);
    }
}