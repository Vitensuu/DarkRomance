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

        public TriggerZone(String id, Rectangle bounds, String actionType, String targetMapPath, String targetMarker, float targetX, float targetY) {
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
            float x = readFloatProperty(object, "x") * mapScale;
            float yTop = readFloatProperty(object, "y") * mapScale;
            float width = readFloatProperty(object, "width") * mapScale;
            float height = readFloatProperty(object, "height") * mapScale;
            if (width <= 0f || height <= 0f) {
                continue;
            }

            String id = object.getName();
            if (id == null || id.isEmpty()) {
                id = object.getProperties().get("id", String.class);
            }
            if (id == null || id.isEmpty()) {
                id = "trigger_" + triggerZones.size;
            }

            String actionType = object.getProperties().get("action", String.class);
            if (actionType == null || actionType.isEmpty()) {
                actionType = object.getProperties().get("type", String.class);
            }
            if (actionType == null || actionType.isEmpty()) {
                actionType = "log";
            }

            float targetX = readFloatProperty(object, "targetX") * mapScale;
            float targetY = readFloatProperty(object, "targetY") * mapScale;
            String targetMapPath = object.getProperties().get("targetMap", String.class);
            String targetMarker = object.getProperties().get("targetMarker", String.class);

            float yBottom = yTop - height;
            triggerZones.add(
                new TriggerZone(
                    id,
                    new Rectangle(x, yBottom, width, height),
                    actionType,
                    targetMapPath,
                    targetMarker,
                    targetX,
                    targetY
                )
            );
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
