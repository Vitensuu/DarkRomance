package com.team7.game1.models;

public class PlayerData {

    public static final int DEFAULT_MAX_HEALTH = 100;
    public static final int DEFAULT_STARTING_LEVEL = 1;

    private String username;
    private int health;
    private int maxHealth;
    private int level;
    private int score;
    private int coins;
    private float worldX;
    private float worldY;

    public PlayerData() {
        this("Player");
    }

    public PlayerData(String username) {
        this(username, DEFAULT_MAX_HEALTH, DEFAULT_MAX_HEALTH, DEFAULT_STARTING_LEVEL, 0, 0, 0f, 0f);
    }

    public PlayerData(String username, int health, int maxHealth, int level, int score, int coins,
                      float worldX, float worldY) {
        setUsername(username);
        this.maxHealth = Math.max(1, maxHealth);
        this.health = clampHealth(health, this.maxHealth);
        this.level = Math.max(DEFAULT_STARTING_LEVEL, level);
        this.score = Math.max(0, score);
        this.coins = Math.max(0, coins);
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        String normalized = username == null ? "" : username.trim();
        this.username = normalized.isEmpty() ? "Player" : normalized;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = clampHealth(health, maxHealth);
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        this.health = clampHealth(health, this.maxHealth);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(DEFAULT_STARTING_LEVEL, level);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public float getWorldX() {
        return worldX;
    }

    public float getWorldY() {
        return worldY;
    }

    public void setWorldPosition(float worldX, float worldY) {
        this.worldX = worldX;
        this.worldY = worldY;
    }

    public void restoreToFullHealth() {
        health = maxHealth;
    }

    public void receiveDamage(int damage) {
        if (damage <= 0) {
            return;
        }
        health = Math.max(0, health - damage);
    }

    public void heal(int amount) {
        if (amount <= 0) {
            return;
        }
        health = Math.min(maxHealth, health + amount);
    }

    public void addScore(int amount) {
        if (amount <= 0) {
            return;
        }
        score += amount;
    }

    public void addCoins(int amount) {
        if (amount <= 0) {
            return;
        }
        coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (amount <= 0 || amount > coins) {
            return false;
        }
        coins -= amount;
        return true;
    }

    public boolean isAlive() {
        return health > 0;
    }

    private int clampHealth(int health, int maxHealth) {
        return Math.max(0, Math.min(health, maxHealth));
    }
}
