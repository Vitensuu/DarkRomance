package com.team7.game1.models;

public class Item {

    public enum ItemType {
        WEAPON,
        QUEST,
        CONSUMABLE,
        MISC
    }

    private final String id;
    private final String name;
    private final ItemType type;
    private final String description;

    public Item(String id, String name, ItemType type, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
