package com.csd201.dungeon.model;


public class Room {
    private final int id;
    private final String name;
    private final String description;

    private final boolean isExit;
    private boolean hasTreasure;

    private int monsterId; // -1 means none

    public Room(int id, String name, String description, boolean isExit, boolean hasTreasure, int monsterId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isExit = isExit;
        this.hasTreasure = hasTreasure;
        this.monsterId = monsterId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isExit() { return isExit; }
    public boolean hasTreasure() { return hasTreasure; }
    public int getMonsterId() { return monsterId; }

    public void setHasTreasure(boolean hasTreasure) { this.hasTreasure = hasTreasure; }
    public void setMonsterId(int monsterId) { this.monsterId = monsterId; }

    @Override
    public String toString() {
        return "Room{id=" + id + ", name='" + name + "', isExit=" + isExit +
                ", hasTreasure=" + hasTreasure + ", monsterId=" + monsterId + "}";
    }
}
