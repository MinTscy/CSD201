package com.csd201.dungeon;

import com.csd201.dungeon.ds.linkedlist.InventoryList;
import com.csd201.dungeon.model.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryListTest {

    @Test
    void removeOnEmptyShouldBeFalseAndSizeZero() {
        InventoryList inv = new InventoryList();

        assertFalse(inv.removeById(1), "remove on empty should be false");
        assertEquals(0, inv.size(), "size should be 0 on new list");
        assertTrue(inv.isEmpty(), "new list should be empty");
    }

    @Test
    void addAndFindItems() {
        InventoryList inv = new InventoryList();
        inv.addLast(new Item(1, "Potion", "POTION", 20));
        inv.addLast(new Item(2, "Key", "KEY", 1));
        inv.addFirst(new Item(3, "Dagger", "WEAPON", 5));

        assertEquals(3, inv.size(), "size should be 3 after adds");
        assertNotNull(inv.findById(2), "findById 2 should exist");
        assertNotNull(inv.findByName("potion"), "findByName potion should exist (case-insensitive)");
        assertNull(inv.findById(999), "findById 999 should be null");
    }

    @Test
    void removeHeadTailAndLast() {
        InventoryList inv = new InventoryList();
        inv.addLast(new Item(1, "Potion", "POTION", 20));
        inv.addLast(new Item(2, "Key", "KEY", 1));
        inv.addFirst(new Item(3, "Dagger", "WEAPON", 5));

        assertTrue(inv.removeById(3), "remove head id=3 should be true");
        assertEquals(2, inv.size(), "size should be 2 after removing head");

        assertTrue(inv.removeById(2), "remove tail id=2 should be true");
        assertEquals(1, inv.size(), "size should be 1 after removing tail");

        assertTrue(inv.removeById(1), "remove last remaining id=1 should be true");
        assertEquals(0, inv.size(), "size should be 0 after removing last");
        assertTrue(inv.isEmpty(), "inventory should be empty after removing last");
    }
}

