package com.shilofishinghelper;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@PluginDescriptor(
        name = "Shilo Fishing Helper",
        description = "Shows a cyan path to the nearest fishing spot and turns the screen red when you stop fishing until you click again",
        tags = {"fishing", "shilo", "path", "idle"}
)
public class ShiloFishingPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ShiloFishingIdleOverlay idleOverlay;
    @Inject
    private ShiloFishingPathOverlay pathOverlay;
    @Inject
    private ShiloFishingInventoryOverlay inventoryOverlay;


    private boolean isIdle = false;
    private boolean wasFishing = false;
    private int lastAnimation = -1;
    private Object lastInteracting = null;
    private long lastClickTime = 0;
    private static final int CLICK_DELAY_MS = 600;
    private boolean userClickedSinceLastFishing = false;

    @Provides
    ShiloFishingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ShiloFishingConfig.class);
    }


    @Override
    protected void startUp() {
        overlayManager.add(idleOverlay);
        overlayManager.add(pathOverlay);
        overlayManager.add(inventoryOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(idleOverlay);
        overlayManager.remove(pathOverlay);
        overlayManager.remove(inventoryOverlay);
        idleOverlay.setIdle(false);
        pathOverlay.setTargetFishingSpot(null);
        isIdle = false;
        wasFishing = false;
    }

    // 🖱️ Detects actual user clicks — clears red
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked e) {
        lastClickTime = System.currentTimeMillis();
        userClickedSinceLastFishing = true; // mark user intent

        if (isIdle) {
            isIdle = false;
            idleOverlay.setIdle(false);
        }
    }

    private boolean isInShiloVillage() {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return false;
        }

        int x = player.getWorldLocation().getX();
        int y = player.getWorldLocation().getY();

        // ✅ Shilo Village coordinate boundaries
        // Adjusted to include the entire fishing area and bank area
        return x >= 2810 && x <= 2880 && y >= 2940 && y <= 3000;
    }


    @Subscribe
    public void onGameTick(GameTick tick) {
        Player player = client.getLocalPlayer();
        if (player == null) {
            return;
        }

        boolean inShilo = isInShiloVillage();

        // 🔴 If not in Shilo Village, disable overlays and logic
        if (!inShilo) {
            if (pathOverlay != null) {
                overlayManager.remove(pathOverlay);
            }
            if (idleOverlay != null) {
                overlayManager.remove(idleOverlay);
            }
            if (inventoryOverlay != null) {
                overlayManager.remove(inventoryOverlay);
            }

            idleOverlay.setIdle(false);
            pathOverlay.setTargetFishingSpot(null);
            return;
        }

        // 🟢 If in Shilo, ensure overlays are added
        if (pathOverlay != null && !overlayManager.anyMatch(o -> o == pathOverlay)) {
            overlayManager.add(pathOverlay);
        }
        if (idleOverlay != null && !overlayManager.anyMatch(o -> o == idleOverlay)) {
            overlayManager.add(idleOverlay);
        }
        if (inventoryOverlay != null && !overlayManager.anyMatch(o -> o == inventoryOverlay)) {
            overlayManager.add(inventoryOverlay);
        }

        // 🎣 Find nearest fishing spot and update path overlay
        NPC bestSpot = null;
        List<WorldPoint> bestPath = null;
        WorldPoint playerLoc = client.getLocalPlayer().getWorldLocation();

// First: look for any spot the player is already adjacent to
        for (NPC npc : client.getNpcs()) {
            if (npc.getName() == null || !npc.getName().toLowerCase().contains("fishing"))
                continue;

            if (isCardinallyAdjacent(playerLoc, npc.getWorldLocation())) {
                // ✅ Immediately pick this — you’re right next to it
                bestSpot = npc;
                bestPath = Collections.emptyList(); // no path needed
                break;
            }
        }

// Otherwise, fall back to “shortest walking path”
        if (bestSpot == null) {
            for (NPC npc : client.getNpcs()) {
                if (npc.getName() == null || !npc.getName().toLowerCase().contains("fishing"))
                    continue;

                List<WorldPoint> path = pathOverlay.findWalkablePath(playerLoc, npc.getWorldLocation());
                if (path.isEmpty())
                    continue;

                if (bestPath == null || path.size() < bestPath.size()) {
                    bestPath = path;
                    bestSpot = npc;
                }
            }
        }

        pathOverlay.setTargetFishingSpot(bestSpot);


        int anim = player.getAnimation();
        Object interacting = player.getInteracting();

        // 🎣 Actively fishing (animation running + interacting with fishing spot)
        boolean currentlyFishing = interacting instanceof NPC &&
                ((NPC) interacting).getName() != null &&
                ((NPC) interacting).getName().toLowerCase().contains("fishing");

        if (currentlyFishing && anim != -1) {
            // reset click flag while fishing
            userClickedSinceLastFishing = false;

            if (isIdle) {
                isIdle = false;
                idleOverlay.setIdle(false);
            }

            wasFishing = true;
            lastInteracting = interacting;
            lastAnimation = anim;
            return;
        }

        // 🔴 If we were fishing but now stopped (no animation or interaction)
        if (wasFishing && (anim == -1 || interacting == null)) {
            // Only show red if user did NOT click since fishing
            if (!userClickedSinceLastFishing) {
                if (!isIdle) {
                    isIdle = true;
                    idleOverlay.setIdle(true);
                }
            }

            // Reset after processing
            wasFishing = false;
            userClickedSinceLastFishing = false;
            return;
        }


    }

    private boolean isCardinallyAdjacent(WorldPoint a, WorldPoint b) {
        if (a.getPlane() != b.getPlane()) return false;
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return (dx + dy) == 1; // one tile away N/S/E/W
    }


}
