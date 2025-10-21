package com.shilofishinghelper;

import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

@Singleton
public class ShiloFishingPathOverlay extends Overlay {
    private final Client client;
    private final ShiloFishingConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Setter
    private NPC targetFishingSpot;

    private static final int DEPOSIT_BOX_ID = 10529;
    private static final WorldPoint DEPOSIT_BOX_POINT = new WorldPoint(2852, 2952, 0);

    @Inject
    public ShiloFishingPathOverlay(Client client, ShiloFishingConfig config, ModelOutlineRenderer modelOutlineRenderer) {
        this.client = client;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Player player = client.getLocalPlayer();
        if (player == null || !config.showPath()) {
            return null;
        }

        // --- INVENTORY CHECK ---
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        boolean inventoryFull = false;
        if (inventory != null) {
            int freeSlots = 28 - (int) Arrays.stream(inventory.getItems())
                    .filter(Objects::nonNull)
                    .filter(item -> item.getId() != -1)
                    .count();
            inventoryFull = freeSlots <= 0;
        }

        // --- DETERMINE TARGET ---
        WorldPoint start = player.getWorldLocation();
        WorldPoint end;
        Color pathColor;

        if (inventoryFull && config.showDepositPath()) {
            end = DEPOSIT_BOX_POINT;
            pathColor = new Color(255, 215, 0, 140); // gold with alpha
            highlightDepositBox(true);
        } else {
            highlightDepositBox(false);
            if (targetFishingSpot == null) {
                return null;
            }
            end = targetFishingSpot.getWorldLocation();
            pathColor = config.pathColor(); // user’s chosen color
        }

        if (start == null || end == null || start.equals(end)) {
            return null;
        }

        List<WorldPoint> path = findWalkablePath(start, end);
        if (path.isEmpty()) {
            return null;
        }

        drawPath(graphics, path, pathColor);
        return null;
    }

    private void drawPath(Graphics2D graphics, List<WorldPoint> path, Color color) {
        for (WorldPoint wp : path) {
            LocalPoint lp = LocalPoint.fromWorld(client, wp);
            if (lp == null)
                continue;

            Polygon tilePoly = Perspective.getCanvasTilePoly(client, lp);
            if (tilePoly == null)
                continue;

            graphics.setColor(color);
            graphics.fillPolygon(tilePoly);

            Color outline = new Color(
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    Math.min(255, color.getAlpha() + 80)
            );
            graphics.setColor(outline);
            graphics.setStroke(new BasicStroke(2));
            graphics.drawPolygon(tilePoly);
        }
    }

    private void highlightDepositBox(boolean highlight) {
        if (!highlight)
            return;

        Scene scene = client.getScene();
        if (scene == null)
            return;

        for (Tile[][] planeTiles : scene.getTiles()) {
            if (planeTiles == null)
                continue;

            for (Tile[] row : planeTiles) {
                if (row == null)
                    continue;

                for (Tile tile : row) {
                    if (tile == null)
                        continue;

                    for (GameObject obj : tile.getGameObjects()) {
                        if (obj != null && obj.getId() == DEPOSIT_BOX_ID) {
                            modelOutlineRenderer.drawOutline(
                                    obj,
                                    3,
                                    new Color(255, 215, 0, 200),
                                    3
                            );
                        }
                    }
                }
            }
        }
    }

    // --- PATHFINDING ---

    List<WorldPoint> findWalkablePath(WorldPoint start, WorldPoint end) {
        CollisionData[] collisionData = client.getCollisionMaps();
        if (collisionData == null) {
            return Collections.emptyList();
        }

        int baseX = client.getTopLevelWorldView().getBaseX();
        int baseY = client.getTopLevelWorldView().getBaseY();

        Queue<WorldPoint> frontier = new LinkedList<>();
        Map<WorldPoint, WorldPoint> cameFrom = new HashMap<>();
        Set<WorldPoint> visited = new HashSet<>();

        frontier.add(start);
        visited.add(start);
        cameFrom.put(start, null);

        int maxDistance = 80;

        while (!frontier.isEmpty()) {
            WorldPoint current = frontier.poll();

            if (isCardinallyAdjacent(current, end)) {
                return reconstructPath(cameFrom, start, current);
            }

            if (current.distanceTo(start) > maxDistance)
                continue;

            for (WorldPoint neighbor : getWalkableNeighbors(current, collisionData, baseX, baseY)) {
                if (!visited.add(neighbor))
                    continue;

                cameFrom.put(neighbor, current);
                frontier.add(neighbor);
            }
        }

        return Collections.emptyList();
    }

    private boolean isCardinallyAdjacent(WorldPoint a, WorldPoint b) {
        if (a.getPlane() != b.getPlane()) return false;
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return (dx + dy) == 1;
    }

    private List<WorldPoint> getWalkableNeighbors(WorldPoint wp, CollisionData[] collisionData, int baseX, int baseY) {
        List<WorldPoint> result = new ArrayList<>();
        int plane = wp.getPlane();
        if (plane < 0 || plane >= collisionData.length)
            return result;

        int[][] flags = collisionData[plane].getFlags();

        int localX = wp.getX() - baseX;
        int localY = wp.getY() - baseY;

        if (localX < 0 || localY < 0 || localX >= 104 || localY >= 104)
            return result;

        tryAddIfWalkable(result, wp, -1, 0, flags, baseX, baseY);
        tryAddIfWalkable(result, wp, 1, 0, flags, baseX, baseY);
        tryAddIfWalkable(result, wp, 0, -1, flags, baseX, baseY);
        tryAddIfWalkable(result, wp, 0, 1, flags, baseX, baseY);

        return result;
    }

    private void tryAddIfWalkable(List<WorldPoint> list, WorldPoint wp, int dx, int dy, int[][] flags, int baseX, int baseY) {
        if (canMove(wp, dx, dy, flags, baseX, baseY))
            list.add(new WorldPoint(wp.getX() + dx, wp.getY() + dy, wp.getPlane()));
    }

    private boolean canMove(WorldPoint from, int dx, int dy, int[][] flags, int baseX, int baseY) {
        int x = from.getX() - baseX;
        int y = from.getY() - baseY;
        int destX = x + dx;
        int destY = y + dy;

        if (destX < 0 || destY < 0 || destX >= 104 || destY >= 104)
            return false;

        int src = flags[x][y];
        int dst = flags[destX][destY];

        if ((dst & (CollisionDataFlag.BLOCK_MOVEMENT_FULL | CollisionDataFlag.BLOCK_MOVEMENT_OBJECT)) != 0)
            return false;

        if (dx == -1 && ((src & CollisionDataFlag.BLOCK_MOVEMENT_WEST) != 0 || (dst & CollisionDataFlag.BLOCK_MOVEMENT_EAST) != 0))
            return false;
        if (dx == 1 && ((src & CollisionDataFlag.BLOCK_MOVEMENT_EAST) != 0 || (dst & CollisionDataFlag.BLOCK_MOVEMENT_WEST) != 0))
            return false;
        if (dy == -1 && ((src & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) != 0 || (dst & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) != 0))
            return false;
        if (dy == 1 && ((src & CollisionDataFlag.BLOCK_MOVEMENT_NORTH) != 0 || (dst & CollisionDataFlag.BLOCK_MOVEMENT_SOUTH) != 0))
            return false;

        return true;
    }

    private List<WorldPoint> reconstructPath(Map<WorldPoint, WorldPoint> cameFrom, WorldPoint start, WorldPoint end) {
        List<WorldPoint> path = new ArrayList<>();
        WorldPoint current = end;

        while (current != null && !current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}
