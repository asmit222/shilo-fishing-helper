package com.shilofishinghelper;

import lombok.Setter;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class ShiloFishingIdleOverlay extends Overlay {
    private final ShiloFishingConfig config;
    @Setter
    private boolean isIdle = false;

    @Inject
    public ShiloFishingIdleOverlay(ShiloFishingConfig config) {
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.showRedScreen() || !isIdle) {
            return null;
        }

        Dimension size = graphics.getDeviceConfiguration().getBounds().getSize();

        // Always use the user’s color but apply fixed opacity (e.g., 70/255)
        Color base = config.idleScreenColor();
        if (base == null) {
            base = Color.RED;
        }

        Color overlayColor = new Color(base.getRed(), base.getGreen(), base.getBlue(), 70);

        graphics.setColor(overlayColor);
        graphics.fillRect(0, 0, size.width, size.height);

        return null;
    }

}
