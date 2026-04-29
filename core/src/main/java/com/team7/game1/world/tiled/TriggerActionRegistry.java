package com.team7.game1.world.tiled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ObjectSet;
import com.team7.game1.models.PlayerCharacter;
import com.team7.game1.models.PlayerData;

public class TriggerActionRegistry {

    private final ObjectSet<String> oneShotConsumed = new ObjectSet<String>();
    private final MapTransitionService mapTransitionService;

    public TriggerActionRegistry(MapTransitionService mapTransitionService) {
        this.mapTransitionService = mapTransitionService;
    }

    public void onZoneEntered(TriggerService.TriggerZone zone, PlayerCharacter player, PlayerData playerData) {
        if (zone == null) {
            return;
        }

        String actionType = zone.getActionType();
        if ("teleport".equalsIgnoreCase(actionType)) {
            handleTeleport(zone, player, playerData);
            return;
        }
        if ("map_transition".equalsIgnoreCase(actionType)) {
            handleMapTransition(zone);
            return;
        }

        if ("oneshot-log".equalsIgnoreCase(actionType)) {
            if (oneShotConsumed.contains(zone.getId())) {
                return;
            }
            oneShotConsumed.add(zone.getId());
            Gdx.app.log("TriggerActionRegistry", "One-shot trigger fired: " + zone.getId());
            return;
        }

        Gdx.app.log("TriggerActionRegistry", "Trigger entered: " + zone.getId() + " action=" + actionType);
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
        mapTransitionService.requestTransition(targetMapPath, zone.getTargetMarker(), zone.getTargetX(), zone.getTargetY());
        Gdx.app.log("TriggerActionRegistry", "Map transition requested: " + zone.getId() + " -> " + targetMapPath);
    }
}
