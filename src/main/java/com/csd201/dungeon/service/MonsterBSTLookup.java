package com.csd201.dungeon.service;

import com.csd201.dungeon.ds.bst.MonsterBST;
import com.csd201.dungeon.model.Monster;

public class MonsterBSTLookup implements MonsterLookup {
    private MonsterBST bst;

    public MonsterBSTLookup() {
        this.bst = new MonsterBST();
    }

    @Override
    public void build(Monster[] data) {
        bst = new MonsterBST();
        if (data == null || data.length == 0) return;

        for (int i = 0; i < data.length; i++) {
            Monster m = data[i];
            if (m != null) bst.insert(m.getId(), m);
        }
    }

    @Override
    public Monster findById(int id) {
        if (bst == null || bst.isEmpty()) return null;
        return bst.search(id);
    }
}

