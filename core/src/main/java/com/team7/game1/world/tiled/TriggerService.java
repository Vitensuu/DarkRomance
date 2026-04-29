package com.team7.game1.world.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class TriggerService {

    public static class TriggerZone {
        private final String id;
        private final Rectangle bounds;
        private final String actionType;
        private final String targetMapPath;
        private final String targetMarker;
        private final float targetX;
        private final float targetY;

        public TriggerZone(String id,
                           Rectangle bounds,
                           String actionType,
                           String targetMapPath,
                           String targetMarker,
                           float targetX,
                           float targetY) {
            this.id = id;
            this.bounds = bounds;
            this.actionType = actionType;
            this.targetMapPath = targetMapPath;
            this.targetMarker = targetMarker;
            this.targetX = targetX;
            this.targetY = targetY;
        }

        public String getId() {
            return id;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public String getActionType() {
            return actionType;
        }

        public String getTargetMapPath() {
            return targetMapPath;
        }

        public String getTargetMarker() {
            return targetMarker;
        }

        public float getTargetX() {
            return targetX;
        }

        public float getTargetY() {
            return targetY;
        }
    }

    private final Array<TriggerZone> triggerZones = new Array<TriggerZone>();

    public TriggerService(TiledMap tiledMap, float mapScale) {
        if (tiledMap == null) {
            return;
        }

        MapLayer triggerLayer = tiledMap.getLayers().get(TiledLayerNames.TRIGGERS);
        if (triggerLayer == null) {
            return;
        }

        MapObjects objects = triggerLayer.getObjects();
        for (MapObject object : objects) {
            TriggerZone zone = buildZone(object, mapScale, triggerZones.size);
            if (zone != null) {
                triggerZones.add(zone);
            }
        }
    }

    public TriggerZone findTriggeredZone(Rectangle actorBounds) {
        for (TriggerZone zone : triggerZones) {
            if (zone.getBounds().overlaps(actorBounds)) {
                return zone;
            }
        }
        return null;
    }

    private TriggerZone buildZone(MapObject object, float mapScale, int fallbackIndex) {
        float x = readFloatProperty(object, "x") * mapScale;
        float yTop = readFloatProperty(object, "y") * mapScale;
        float width = readFloatProperty(object, "width") * mapScale;
        float height = readFloatProperty(object, "height") * mapScale;

        if (width <= 0f || height <= 0f) {
            return null;
        }

        String id = readStringProperty(object, "id", object.getName());
        if (id == null || id.trim().isEmpty()) {
            id = "trigger_" + fallbackIndex;
        }

        String actionType = readStringProperty(object, "action", null);
        if (actionType == null || actionType.trim().isEmpty()) {
            actionType = readStringProperty(object, "type", "log");
        }

        float targetX = readFloatProperty(object, "targetX") * mapScale;
        float targetY = readFloatProperty(object, "targetY") * mapScale;
        String targetMapPath = readStringProperty(object, "targetMap", null);
        String targetMarker = readStringProperty(object, "targetMarker", null);

        float yBottom = yTop - height;
        return new TriggerZone(
            id,
            new Rectangle(x, yBottom, width, height),
            actionType,
            targetMapPath,
            targetMarker,
            targetX,
            targetY
        );
    }

    private String readStringProperty(MapObject object, String key, String fallback) {
        Object value = object.getProperties().get(key);
        if (value == null) {
            return fallback;
        }
        String asString = String.valueOf(value).trim();
        return asString.isEmpty() ? fallback : asString;
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
