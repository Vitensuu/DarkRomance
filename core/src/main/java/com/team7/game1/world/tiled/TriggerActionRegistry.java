package com.team7.game1.world.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectSet;
import com.team7.game1.models.PlayerCharacter;
import com.team7.game1.models.PlayerData;

public class TriggerActionRegistry {

    private static final String ACTION_TELEPORT = "teleport";
    private static final String ACTION_MAP_TRANSITION = "map_transition";
    private static final String ACTION_ONESHOT_LOG = "oneshot-log";

    private final ObjectSet<String> oneShotConsumed = new ObjectSet<String>();
    private final MapTransitionService mapTransitionService;

    public TriggerActionRegistry(MapTransitionService mapTransitionService) {
        this.mapTransitionService = mapTransitionService;
    }

    public void onZoneEntered(TriggerService.TriggerZone zone, PlayerCharacter player, PlayerData playerData) {
        if (zone == null) {
            return;
        }

        String actionType = normalizeAction(zone.getActionType());
        if (ACTION_TELEPORT.equals(actionType)) {
            handleTeleport(zone, player, playerData);
            return;
        }

        if (ACTION_MAP_TRANSITION.equals(actionType)) {
            handleMapTransition(zone);
            return;
        }

        if (ACTION_ONESHOT_LOG.equals(actionType)) {
            handleOneShotLog(zone);
            return;
        }

        Gdx.app.log("TriggerActionRegistry", "Trigger entered: " + zone.getId() + " action=" + zone.getActionType());
    }

    private void handleTeleport(TriggerService.TriggerZone zone, PlayerCharacter player, PlayerData playerData) {
        if (zone.getTargetX() == 0f && zone.getTargetY() == 0f) {
            Gdx.app.log("TriggerActionRegistry", "Teleport trigger has empty target: " + zone.getId());
            return;
        }

        player.setBottomLeft(zone.getTargetX(), zone.getTargetY());
        if (playerData != null) {
            playerData.setWorldPosition(player.getCenterX(), player.getY());
        }

        Gdx.app.log("TriggerActionRegistry", "Teleported via trigger: " + zone.getId());
    }

    private void handleMapTransition(TriggerService.TriggerZone zone) {
        String targetMapPath = zone.getTargetMapPath();
        if (targetMapPath == null || targetMapPath.trim().isEmpty()) {
            Gdx.app.log("TriggerActionRegistry", "Map transition trigger has no targetMap: " + zone.getId());
            return;
        }

        mapTransitionService.requestTransition(
            targetMapPath,
            zone.getTargetMarker(),
            zone.getTargetX(),
            zone.getTargetY()
        );
        Gdx.app.log("TriggerActionRegistry", "Map transition requested: " + zone.getId() + " -> " + targetMapPath);
    }

    private void handleOneShotLog(TriggerService.TriggerZone zone) {
        if (oneShotConsumed.contains(zone.getId())) {
            return;
        }

        oneShotConsumed.add(zone.getId());
        Gdx.app.log("TriggerActionRegistry", "One-shot trigger fired: " + zone.getId());
    }

    private String normalizeAction(String actionType) {
        if (actionType == null) {
            return "";
        }
        return actionType.trim().toLowerCase();
    }
}
