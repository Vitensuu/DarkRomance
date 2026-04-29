package com.team7.game1.world.tiled;

import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class CollisionLayerService {

    private final Array<Rectangle> blockingAreas = new Array<Rectangle>();
    private final Array<Rectangle> climbAreas = new Array<Rectangle>();

    public CollisionLayerService(TiledMap tiledMap, float mapScale) {
        if (tiledMap == null) {
            return;
        }

        MapLayer collisionLayer = tiledMap.getLayers().get(TiledLayerNames.COLLISION);
        if (collisionLayer != null) {
            readLayerCollisionRecursive(collisionLayer, mapScale, 0f, 0f);
        }

        MapLayer climbLayer = tiledMap.getLayers().get(TiledLayerNames.CLIMB);
        if (climbLayer != null) {
            readClimbLayerRecursive(climbLayer, mapScale, 0f, 0f);
        }

        // Fallback for legacy TMX structure in this project.
        if (blockingAreas.size == 0) {
            readFallbackCollisionFromMap(tiledMap, mapScale);
        }
    }

    public boolean isBlocked(Rectangle currentBounds, Rectangle nextBounds, float moveY) {
        if (moveY > 0f && isInClimbArea(nextBounds)) {
            return false;
        }
        for (Rectangle area : blockingAreas) {
            boolean overlapsNow = area.overlaps(currentBounds);
            boolean overlapsNext = area.overlaps(nextBounds);
            // If the player is already intersecting this block (spawn/map legacy),
            // allow movement so they can step out of geometry.
            if (!overlapsNow && overlapsNext) {
                return true;
            }
        }
        return false;
    }

    private boolean isInClimbArea(Rectangle bounds) {
        for (Rectangle area : climbAreas) {
            if (area.overlaps(bounds)) {
                return true;
            }
        }
        return false;
    }

    private void readFallbackCollisionFromMap(TiledMap tiledMap, float mapScale) {
        readFallbackCollisionRecursive(tiledMap.getLayers(), mapScale, 0f, 0f);
    }

    private void readFallbackCollisionRecursive(MapLayers layers, float mapScale, float parentOffsetX, float parentOffsetY) {
        for (MapLayer layer : layers) {
            float layerOffsetX = parentOffsetX + layer.getOffsetX();
            float layerOffsetY = parentOffsetY + layer.getOffsetY();

            if (layer instanceof MapGroupLayer) {
                readFallbackCollisionRecursive(((MapGroupLayer) layer).getLayers(), mapScale, layerOffsetX, layerOffsetY);
                continue;
            }

            String layerName = layer.getName() == null ? "" : layer.getName().toLowerCase();

            if (layer instanceof TiledMapTileLayer) {
                if ("walls".equals(layerName) || "wall".equals(layerName) || "object".equals(layerName) || "objects".equals(layerName)) {
                    readBlockingCellsFromTileLayer((TiledMapTileLayer) layer, mapScale, layerOffsetX, layerOffsetY);
                }
                continue;
            }

            if ("trees".equals(layerName) || "bush".equals(layerName) || "mogils".equals(layerName) || "object".equals(layerName) || "objects".equals(layerName)) {
                readRectanglesFromObjectLayer(layer.getObjects(), mapScale, blockingAreas, layerOffsetX, layerOffsetY);
            }
        }
    }

    private void readLayerCollisionRecursive(MapLayer layer, float mapScale, float parentOffsetX, float parentOffsetY) {
        float layerOffsetX = parentOffsetX + layer.getOffsetX();
        float layerOffsetY = parentOffsetY + layer.getOffsetY();

        if (layer instanceof MapGroupLayer) {
            MapLayers nested = ((MapGroupLayer) layer).getLayers();
            for (MapLayer nestedLayer : nested) {
                readLayerCollisionRecursive(nestedLayer, mapScale, layerOffsetX, layerOffsetY);
            }
            return;
        }

        if (layer instanceof TiledMapTileLayer) {
            readBlockingCellsFromTileLayer((TiledMapTileLayer) layer, mapScale, layerOffsetX, layerOffsetY);
            return;
        }

        readRectanglesFromObjectLayer(layer.getObjects(), mapScale, blockingAreas, layerOffsetX, layerOffsetY);
    }

    private void readClimbLayerRecursive(MapLayer layer, float mapScale, float parentOffsetX, float parentOffsetY) {
        float layerOffsetX = parentOffsetX + layer.getOffsetX();
        float layerOffsetY = parentOffsetY + layer.getOffsetY();

        if (layer instanceof MapGroupLayer) {
            MapLayers nested = ((MapGroupLayer) layer).getLayers();
            for (MapLayer nestedLayer : nested) {
                readClimbLayerRecursive(nestedLayer, mapScale, layerOffsetX, layerOffsetY);
            }
            return;
        }

        if (layer instanceof TiledMapTileLayer) {
            readBlockingCellsFromTileLayer((TiledMapTileLayer) layer, mapScale, layerOffsetX, layerOffsetY, climbAreas);
            return;
        }

        readRectanglesFromObjectLayer(layer.getObjects(), mapScale, climbAreas, layerOffsetX, layerOffsetY);
    }

    private void readBlockingCellsFromTileLayer(TiledMapTileLayer layer, float mapScale, float layerOffsetX, float layerOffsetY) {
        readBlockingCellsFromTileLayer(layer, mapScale, layerOffsetX, layerOffsetY, blockingAreas);
    }

    private void readBlockingCellsFromTileLayer(TiledMapTileLayer layer, float mapScale, float layerOffsetX, float layerOffsetY, Array<Rectangle> target) {
        float tileWidth = layer.getTileWidth() * mapScale;
        float tileHeight = layer.getTileHeight() * mapScale;
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }
                float worldX = (x * layer.getTileWidth() + layerOffsetX) * mapScale;
                float worldY = (y * layer.getTileHeight() + layerOffsetY) * mapScale;
                target.add(new Rectangle(worldX, worldY, tileWidth, tileHeight));
            }
        }
    }

    private void readRectanglesFromObjectLayer(MapObjects objects, float mapScale, Array<Rectangle> target, float layerOffsetX, float layerOffsetY) {
        for (MapObject object : objects) {
            float x = (readFloatProperty(object, "x") + layerOffsetX) * mapScale;
            float yTop = (readFloatProperty(object, "y") + layerOffsetY) * mapScale;
            float width = readFloatProperty(object, "width") * mapScale;
            float height = readFloatProperty(object, "height") * mapScale;
            if (width <= 0f || height <= 0f) {
                continue;
            }

            float yBottom = yTop - height;
            target.add(new Rectangle(x, yBottom, width, height));
        }
    }

    private float readFloatProperty(MapObject object, String key) {
        Object value = object.getProperties().get(key);
        if (value instanceof Float) {
            return (Float) value;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }
        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException ignored) {
                return 0f;
            }
        }
        return 0f;
    }
}
