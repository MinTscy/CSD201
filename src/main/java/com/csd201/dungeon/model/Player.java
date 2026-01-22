package com.csd201.dungeon.model;


import com.csd201.dungeon.ds.linkedlist.InventoryList;

public class Player {
    private final String name;
    private int hp;
    private final int atk;
    private int currentRoomId;

    private final InventoryList inventory;

    public Player(String name, int hp, int atk, int currentRoomId) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
        this.currentRoomId = currentRoomId;
        this.inventory = new InventoryList();
    }

    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getAtk() { return atk; }
    public int getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(int roomId) { this.currentRoomId = roomId; }

    public InventoryList getInventory() { return inventory; }

    public boolean isAlive() { return hp > 0; }

    public void takeDamage(int dmg) {
        if (dmg <= 0) return;
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        hp += amount;
    }

    @Override
    public String toString() {
        return "Player{name='" + name + "', hp=" + hp + ", atk=" + atk + ", currentRoomId=" + currentRoomId + "}";
    }
}
