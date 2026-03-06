package com.csd201.dungeon.unit;

import com.csd201.dungeon.ds.linkedlist.InventoryList;
import com.csd201.dungeon.model.Item;

public final class InventoryListUnitTest {
    public static void runAll() {
        testEmptyRemove();
        testAddAndFind();
        testRemoveHeadTailAndLast();
    }

    private static void testEmptyRemove() {
        InventoryList inv = new InventoryList();
        Assertions.assertFalse(inv.removeById(1), "remove on empty should be false");
        Assertions.assertEquals(0, inv.size(), "size should be 0 on new list");
        Assertions.assertTrue(inv.isEmpty(), "new list should be empty");
    }

    private static void testAddAndFind() {
        InventoryList inv = new InventoryList();
        inv.addLast(new Item(1, "Potion", "POTION", 20));
        inv.addLast(new Item(2, "Key", "KEY", 1));
        inv.addFirst(new Item(3, "Dagger", "WEAPON", 5));

        Assertions.assertEquals(3, inv.size(), "size should be 3 after adds");
        Assertions.assertNotNull(inv.findById(2), "findById 2 should exist");
        Assertions.assertNotNull(inv.findByName("potion"), "findByName potion should exist (case-insensitive)");
        Assertions.assertNull(inv.findById(999), "findById 999 should be null");
    }

    private static void testRemoveHeadTailAndLast() {
        InventoryList inv = new InventoryList();
        inv.addLast(new Item(1, "Potion", "POTION", 20));
        inv.addLast(new Item(2, "Key", "KEY", 1));
        inv.addFirst(new Item(3, "Dagger", "WEAPON", 5));

        Assertions.assertTrue(inv.removeById(3), "remove head id=3 should be true");
        Assertions.assertEquals(2, inv.size(), "size should be 2 after removing head");

        Assertions.assertTrue(inv.removeById(2), "remove tail id=2 should be true");
        Assertions.assertEquals(1, inv.size(), "size should be 1 after removing tail");

        Assertions.assertTrue(inv.removeById(1), "remove last remaining id=1 should be true");
        Assertions.assertEquals(0, inv.size(), "size should be 0 after removing last");
        Assertions.assertTrue(inv.isEmpty(), "inventory should be empty after removing last");
    }
}

