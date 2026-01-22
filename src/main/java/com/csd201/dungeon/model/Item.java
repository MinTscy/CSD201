package com.csd201.dungeon.model;


public class Item {
    private final int id;
    private String name;
    private String type; // POTION, KEY, WEAPON...
    private int value;   // heal amount / atk bonus / etc.

    public Item(int id, String name, String type, int value) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getValue() { return value; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setValue(int value) { this.value = value; }

    @Override
    public String toString() {
        return "Item{id=" + id + ", name='" + name + "', type='" + type + "', value=" + value + "}";
    }
}

