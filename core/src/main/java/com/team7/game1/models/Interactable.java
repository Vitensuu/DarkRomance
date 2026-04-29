package com.team7.game1.models;

public interface Interactable {

    boolean canInteract(PlayerCharacter player);

    void interact(PlayerCharacter player);

    String getInteractionPrompt();
}
