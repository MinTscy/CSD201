package com.csd201.dungeon.service;

import com.csd201.dungeon.model.Monster;

public class MonsterListLookup implements MonsterLookup {
    private Monster[] data;

    public MonsterListLookup() {
        this.data = null;
    }

    @Override
    public void build(Monster[] data) {
        this.data = data;
    }

    @Override
    public Monster findById(int id) {
        if (data == null || data.length == 0) return null;

        for (int i = 0; i < data.length; i++) {
            Monster m = data[i];
            if (m != null && m.getId() == id) return m;
        }
        return null;
    }
}

