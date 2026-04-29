package com.team7.game1.world.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapGroupLayer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class TiledWorld implements Disposable {

    private final float mapScale;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;
    private float worldWidth;
    private float worldHeight;
    private CollisionLayerService collisionLayerService;
    private TriggerService triggerService;

    public TiledWorld(String mapPath, float mapScale, SpriteBatch batch, float fallbackWorldWidth, float fallbackWorldHeight) {
        this.mapScale = mapScale;
        this.worldWidth = fallbackWorldWidth;
        this.worldHeight = fallbackWorldHeight;
        load(mapPath, batch);
    }

    public boolean isLoaded() {
        return tiledMap != null && renderer != null;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }

    public CollisionLayerService getCollisionLayerService() {
        return collisionLayerService;
    }

    public TriggerService getTriggerService() {
        return triggerService;
    }

    public void renderBelowPlayer(OrthographicCamera camera) {
        if (!isLoaded()) {
            return;
        }
        renderer.setView(camera);
        int[] aboveIndices = resolveLayerIndices(TiledLayerNames.ABOVE_PLAYER_LAYERS);
        if (aboveIndices.length == 0) {
            renderer.render();
            return;
        }
        int[] belowIndices = resolveAllLayerIndicesExcept(aboveIndices);
        if (belowIndices.length > 0) {
            renderer.render(belowIndices);
            return;
        }
        renderer.render();
    }

    public void renderAbovePlayer(OrthographicCamera camera) {
        if (!isLoaded()) {
            return;
        }
        renderer.setView(camera);
        int[] layerIndices = resolveLayerIndices(TiledLayerNames.ABOVE_PLAYER_LAYERS);
        if (layerIndices.length > 0) {
            renderer.render(layerIndices);
        }
    }

    public void drawObjectTileLayers(SpriteBatch batch) {
        if (tiledMap == null) {
            return;
        }
        drawLayerTileObjectsRecursive(batch, tiledMap.getLayers(), 0f, 0f);
    }

    public Vector2 findPlayerSpawn(float playerDrawWidth, float playerDrawHeight) {
        if (tiledMap == null) {
            return null;
        }

        float[] bounds = findStartBounds(tiledMap.getLayers());
        if (bounds == null) {
            return null;
        }

        float spawnX = (bounds[0] + bounds[2]) * 0.5f * mapScale - playerDrawWidth * 0.5f;
        float spawnY = (bounds[1] + bounds[3]) * 0.5f * mapScale - playerDrawHeight * 0.5f;
        return new Vector2(spawnX, spawnY);
    }

    public Vector2 findSpawnByMarker(String markerName, float playerDrawWidth, float playerDrawHeight) {
        if (tiledMap == null || markerName == null || markerName.trim().isEmpty()) {
            return null;
        }
        return findSpawnByMarkerRecursive(tiledMap.getLayers(), markerName.trim(), playerDrawWidth, playerDrawHeight, 0f, 0f);
    }

    @Override
    public void dispose() {
        if (renderer != null) {
            renderer.dispose();
        }
        if (tiledMap != null) {
            tiledMap.dispose();
        }
        renderer = null;
        tiledMap = null;
    }

    private void load(String mapPath, SpriteBatch batch) {
        try {
            tiledMap = new TmxMapLoader().load(mapPath);
            renderer = new OrthogonalTiledMapRenderer(tiledMap, mapScale, batch);

            Integer mapTilesWide = tiledMap.getProperties().get("width", Integer.class);
            Integer mapTilesHigh = tiledMap.getProperties().get("height", Integer.class);
            Integer tileWidth = tiledMap.getProperties().get("tilewidth", Integer.class);
            Integer tileHeight = tiledMap.getProperties().get("tileheight", Integer.class);

            if (mapTilesWide != null && mapTilesHigh != null && tileWidth != null && tileHeight != null) {
                worldWidth = mapTilesWide * tileWidth * mapScale;
                worldHeight = mapTilesHigh * tileHeight * mapScale;
            }

            collisionLayerService = new CollisionLayerService(tiledMap, mapScale);
            triggerService = new TriggerService(tiledMap, mapScale);
        } catch (Exception exception) {
            Gdx.app.error("TiledWorld", "Failed to load map " + mapPath, exception);
            tiledMap = null;
            renderer = null;
            collisionLayerService = null;
            triggerService = null;
        }
    }

    private int[] resolveLayerIndices(String[] layerNames) {
        if (tiledMap == null) {
            return new int[0];
        }
        Array<Integer> indices = new Array<Integer>();
        MapLayers layers = tiledMap.getLayers();
        for (String layerName : layerNames) {
            int layerIndex = layers.getIndex(layerName);
            if (layerIndex >= 0) {
                indices.add(layerIndex);
            }
        }

        int[] result = new int[indices.size];
        for (int index = 0; index < indices.size; index++) {
            result[index] = indices.get(index);
        }
        return result;
    }

    private int[] resolveAllLayerIndicesExcept(int[] excludedIndices) {
        if (tiledMap == null) {
            return new int[0];
        }

        Array<Integer> indices = new Array<Integer>();
        MapLayers layers = tiledMap.getLayers();
        for (int layerIndex = 0; layerIndex < layers.getCount(); layerIndex++) {
            boolean excluded = false;
            for (int excludedIndex : excludedIndices) {
                if (layerIndex == excludedIndex) {
                    excluded = true;
                    break;
                }
            }
            if (!excluded) {
                indices.add(layerIndex);
            }
        }

        int[] result = new int[indices.size];
        for (int index = 0; index < indices.size; index++) {
            result[index] = indices.get(index);
        }
        return result;
    }

    private void drawLayerTileObjectsRecursive(SpriteBatch batch, MapLayers layers, float parentOffsetX, float parentOffsetY) {
        for (MapLayer layer : layers) {
            if (!layer.isVisible()) {
                continue;
            }

            float layerOffsetX = parentOffsetX + layer.getOffsetX();
            float layerOffsetY = parentOffsetY + layer.getOffsetY();

            if (layer instanceof MapGroupLayer) {
                drawLayerTileObjectsRecursive(batch, ((MapGroupLayer) layer).getLayers(), layerOffsetX, layerOffsetY);
                continue;
            }

            for (MapObject object : layer.getObjects()) {
                if (!(object instanceof TiledMapTileMapObject)) {
                    continue;
                }

                TiledMapTileMapObject tileObject = (TiledMapTileMapObject) object;
                if (tileObject.getTile() == null || tileObject.getTile().getTextureRegion() == null) {
                    continue;
                }

                TextureRegion region = tileObject.getTile().getTextureRegion();
                float drawX = (tileObject.getX() + layerOffsetX) * mapScale;
                float drawY = (tileObject.getY() + layerOffsetY) * mapScale;
                float drawWidth = region.getRegionWidth() * mapScale;
                float drawHeight = region.getRegionHeight() * mapScale;

                batch.draw(
                    region,
                    drawX,
                    drawY,
                    drawWidth * 0.5f,
                    drawHeight * 0.5f,
                    drawWidth,
                    drawHeight,
                    tileObject.getScaleX(),
                    tileObject.getScaleY(),
                    tileObject.getRotation()
                );
            }
        }
    }

    private Vector2 findSpawnByMarkerRecursive(MapLayers layers, String markerName, float playerDrawWidth, float playerDrawHeight, float parentOffsetX, float parentOffsetY) {
        for (MapLayer layer : layers) {
            float layerOffsetX = parentOffsetX + layer.getOffsetX();
            float layerOffsetY = parentOffsetY + layer.getOffsetY();

            if (layer instanceof MapGroupLayer) {
                Vector2 nested = findSpawnByMarkerRecursive(
                    ((MapGroupLayer) layer).getLayers(),
                    markerName,
                    playerDrawWidth,
                    playerDrawHeight,
                    layerOffsetX,
                    layerOffsetY
                );
                if (nested != null) {
                    return nested;
                }
            }

            for (MapObject object : layer.getObjects()) {
                String objectName = object.getName();
                if (objectName == null || !markerName.equalsIgnoreCase(objectName.trim())) {
                    continue;
                }

                float objectX = (readFloatProperty(object, "x") + layerOffsetX) * mapScale;
                float objectY = (readFloatProperty(object, "y") + layerOffsetY) * mapScale;
                float objectWidth = readFloatProperty(object, "width") * mapScale;
                float objectHeight = readFloatProperty(object, "height") * mapScale;

                float spawnX = objectX - playerDrawWidth * 0.5f;
                float spawnY = objectY - objectHeight - playerDrawHeight * 0.5f;
                if (objectWidth > 0f || objectHeight > 0f) {
                    spawnX = objectX + objectWidth * 0.5f - playerDrawWidth * 0.5f;
                    spawnY = objectY - objectHeight * 0.5f - playerDrawHeight * 0.5f;
                }
                return new Vector2(spawnX, spawnY);
            }
        }

        return null;
    }

    private float[] findStartBounds(Iterable<MapLayer> layers) {
        for (MapLayer layer : layers) {
            if (!(layer instanceof MapGroupLayer)) {
                continue;
            }

            MapGroupLayer group = (MapGroupLayer) layer;
            String groupName = group.getName() == null ? "" : group.getName().toLowerCase();
            if (TiledLayerNames.START_GROUP.equals(groupName)) {
                return findObjectLayerBounds(group.getLayers(), TiledLayerNames.START_OBJECT_LAYER);
            }

            float[] nested = findStartBounds(group.getLayers());
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private float[] findObjectLayerBounds(Iterable<MapLayer> layers, String layerName) {
        String targetName = layerName.toLowerCase();
        for (MapLayer layer : layers) {
            String name = layer.getName() == null ? "" : layer.getName().toLowerCase();
            if (!targetName.equals(name)) {
                continue;
            }

            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;
            boolean found = false;

            for (MapObject object : layer.getObjects()) {
                float x = readFloatProperty(object, "x");
                float y = readFloatProperty(object, "y");
                float width = readFloatProperty(object, "width");
                float height = readFloatProperty(object, "height");
                minX = Math.min(minX, x);
                minY = Math.min(minY, y - height);
                maxX = Math.max(maxX, x + width);
                maxY = Math.max(maxY, y);
                found = true;
            }

            if (found) {
                return new float[] { minX, minY, maxX, maxY };
            }
        }
        return null;
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
