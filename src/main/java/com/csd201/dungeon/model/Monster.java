package com.csd201.dungeon.model;


public class Monster {
    private final int id;
    private final String name;
    private final int level;
    private int hp;
    private final int atk;
    private final int dropItemId; // -1 if none

    public Monster(int id, String name, int level, int hp, int atk, int dropItemId) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.hp = hp;
        this.atk = atk;
        this.dropItemId = dropItemId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public int getHp() { return hp; }
    public int getAtk() { return atk; }
    public int getDropItemId() { return dropItemId; }

    public boolean isAlive() { return hp > 0; }

    public void takeDamage(int dmg) {
        if (dmg <= 0) return;
        hp -= dmg;
        if (hp < 0) hp = 0;
    }

    @Override
    public String toString() {
        return "Monster{id=" + id + ", name='" + name + "', level=" + level +
                ", hp=" + hp + ", atk=" + atk + ", dropItemId=" + dropItemId + "}";
    }
}
