package com.csd201.dungeon.service;

import com.csd201.dungeon.model.Monster;

public interface MonsterLookup {
    void build(Monster[] data);
    Monster findById(int id);
}

