package com.csd201.dungeon.ds.bst;
import com.csd201.dungeon.model.Monster;
public class MonsterBST {
    private BSTNode root;
    private int size;
    public MonsterBST() {
        this.root = null;
        this.size = 0;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void insert(int key, Monster value) {
        if (value == null) return;
        root = insertRecursive(root, key, value);
    }

    private BSTNode insertRecursive(BSTNode node, int key, Monster value) {
        if (node == null) {
            size++;
            return new BSTNode(key, value);
        }

        if (key < node.key) {
            node.left = insertRecursive(node.left, key, value);
        } else if (key > node.key) {
            node.right = insertRecursive(node.right, key, value);
        } else {
            // duplicate key: update value, do not change size
            node.value = value;
        }

        return node;
    }

    public Monster search(int key) {
        return searchRecursive(root, key);
    }

    private Monster searchRecursive(BSTNode node, int key) {
        if (node == null) return null;

        if (key == node.key) return node.value;
        if (key < node.key) return searchRecursive(node.left, key);
        return searchRecursive(node.right, key);
    }
}

