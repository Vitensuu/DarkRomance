package com.team7.game1.world.tiled;

public class MapTransitionService {

    public static class MapTransitionRequest {
        private final String mapPath;
        private final String targetMarker;
        private final float spawnX;
        private final float spawnY;

        public MapTransitionRequest(String mapPath, String targetMarker, float spawnX, float spawnY) {
            this.mapPath = mapPath;
            this.targetMarker = targetMarker;
            this.spawnX = spawnX;
            this.spawnY = spawnY;
        }

        public String getMapPath() {
            return mapPath;
        }

        public String getTargetMarker() {
            return targetMarker;
        }

        public float getSpawnX() {
            return spawnX;
        }

        public float getSpawnY() {
            return spawnY;
        }
    }

    private MapTransitionRequest pendingRequest;

    public void requestTransition(String mapPath, String targetMarker, float spawnX, float spawnY) {
        if (mapPath == null || mapPath.trim().isEmpty()) {
            return;
        }
        pendingRequest = new MapTransitionRequest(mapPath.trim(), targetMarker, spawnX, spawnY);
    }

    public boolean hasPendingTransition() {
        return pendingRequest != null;
    }

    public MapTransitionRequest consumePendingTransition() {
        MapTransitionRequest request = pendingRequest;
        pendingRequest = null;
        return request;
    }
}
