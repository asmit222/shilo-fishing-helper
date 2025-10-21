package com.shilofishinghelper;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class ShiloFishingInventoryOverlay extends Overlay {
    private final Client client;
    private final ShiloFishingConfig config;

    @Inject
    public ShiloFishingInventoryOverlay(Client client, ShiloFishingConfig config) {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showInventoryCount()) {
            return null;
        }

        Player player = client.getLocalPlayer();
        if (player == null) {
            return null;
        }

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            return null;
        }

        int totalSlots = 28;
        int filledSlots = (int) java.util.Arrays.stream(inventory.getItems())
                .filter(item -> item != null && item.getId() != -1)
                .count();
        int freeSlots = totalSlots - filledSlots;

        LocalPoint lp = player.getLocalLocation();
        if (lp == null) {
            return null;
        }

        // Position the overlay slightly to the right of the player
        Point canvasPoint = Perspective.localToCanvas(client, lp, client.getPlane(), player.getLogicalHeight() / 2);
        if (canvasPoint == null) {
            return null;
        }

        int offsetX = 35; // move right of player
        int offsetY = -10; // small vertical offset

        int x = canvasPoint.getX() + offsetX;
        int y = canvasPoint.getY() + offsetY;

        // Text (just the number)
        Color textColor = freeSlots > 5 ? Color.WHITE : (freeSlots > 0 ? Color.YELLOW : Color.RED);
        graphics.setFont(new Font("Arial", Font.BOLD, 16));
        graphics.setColor(Color.BLACK);
        graphics.drawString(String.valueOf(freeSlots), x + 2, y + 1);
        graphics.setColor(textColor);
        graphics.drawString(String.valueOf(freeSlots), x + 1, y);

        return null;
    }
}
