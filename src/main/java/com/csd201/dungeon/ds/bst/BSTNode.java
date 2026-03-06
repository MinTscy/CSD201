package com.csd201.dungeon.ds.bst;

import com.csd201.dungeon.model.Monster;

public class BSTNode {
    public int key;
    public Monster value;
    public BSTNode left;
    public BSTNode right;

    public BSTNode(int key, Monster value) {
        this.key = key;
        this.value = value;
        this.left = null;
        this.right = null;
    }
}

