package com.csd201.dungeon;

import com.csd201.dungeon.ds.bst.MonsterBST;
import com.csd201.dungeon.model.Monster;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MonsterBSTTest {

    @Test
    void searchOnEmptyReturnsNull() {
        MonsterBST bst = new MonsterBST();

        assertNull(bst.search(1), "search on empty BST should return null");
        assertNull(bst.search(999), "search on empty BST should return null (any key)");
    }

    @Test
    void insertThenSearchReturnsCorrectMonster() {
        MonsterBST bst = new MonsterBST();
        Monster goblin = new Monster(10, "Goblin", 1, 30, 5, -1);

        bst.insert(goblin.getId(), goblin);

        Monster found = bst.search(10);
        assertNotNull(found, "search should return non-null after insert");
        assertSame(goblin, found, "search should return the exact inserted Monster instance");
        assertEquals("Goblin", found.getName(), "returned Monster should have expected name");
    }

    @Test
    void duplicateKeyUpdatesValue() {
        MonsterBST bst = new MonsterBST();
        Monster oldM = new Monster(7, "Slime", 1, 20, 2, -1);
        Monster newM = new Monster(7, "MegaSlime", 3, 60, 8, 100);

        bst.insert(7, oldM);
        assertSame(oldM, bst.search(7), "sanity: first insert should be returned by search");

        bst.insert(7, newM); // duplicate key: must update value

        Monster found = bst.search(7);
        assertNotNull(found, "search should return non-null after duplicate-key insert");
        assertSame(newM, found, "duplicate key should update stored value to the new Monster instance");
        assertEquals("MegaSlime", found.getName(), "updated Monster should have expected name");
    }

    @Test
    void insertMultipleKeysThenSearchExistingAndNonExisting() {
        MonsterBST bst = new MonsterBST();

        Monster m5 = new Monster(5, "M5", 1, 10, 1, -1);
        Monster m2 = new Monster(2, "M2", 1, 10, 1, -1);
        Monster m8 = new Monster(8, "M8", 1, 10, 1, -1);
        Monster m1 = new Monster(1, "M1", 1, 10, 1, -1);
        Monster m3 = new Monster(3, "M3", 1, 10, 1, -1);

        bst.insert(5, m5);
        bst.insert(2, m2);
        bst.insert(8, m8);
        bst.insert(1, m1);
        bst.insert(3, m3);

        assertSame(m5, bst.search(5), "should find existing key 5");
        assertSame(m2, bst.search(2), "should find existing key 2");
        assertSame(m8, bst.search(8), "should find existing key 8");
        assertSame(m1, bst.search(1), "should find existing key 1");
        assertSame(m3, bst.search(3), "should find existing key 3");

        assertNull(bst.search(0), "should return null for non-existing key");
        assertNull(bst.search(4), "should return null for non-existing key between existing keys");
        assertNull(bst.search(999), "should return null for non-existing large key");
    }
}

