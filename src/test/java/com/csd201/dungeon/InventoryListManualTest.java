package com.csd201.dungeon;


import com.csd201.dungeon.ds.linkedlist.InventoryList;
import com.csd201.dungeon.model.Item;



public class InventoryListManualTest {
    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new RuntimeException("ASSERT TRUE FAILED: " + msg);
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) throw new RuntimeException("ASSERT FALSE FAILED: " + msg);
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new RuntimeException("ASSERT NOT NULL FAILED: " + msg);
    }

    private static void assertNull(Object obj, String msg) {
        if (obj != null) throw new RuntimeException("ASSERT NULL FAILED: " + msg);
    }

    private static void assertEquals(int expected, int actual, String msg) {
        if (expected != actual) {
            throw new RuntimeException("ASSERT EQUALS FAILED: " + msg + " expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        InventoryList inv = new InventoryList();

        // empty remove
        assertFalse(inv.removeById(1), "remove on empty should be false");
        assertEquals(0, inv.size(), "size should be 0");

        // addLast + addFirst
        inv.addLast(new Item(1, "Potion", "POTION", 20));
        inv.addLast(new Item(2, "Key", "KEY", 1));
        inv.addFirst(new Item(3, "Dagger", "WEAPON", 5));
        assertEquals(3, inv.size(), "size should be 3");

        // find
        assertNotNull(inv.findById(2), "findById 2 should exist");
        assertNotNull(inv.findByName("potion"), "findByName potion should exist");
        assertNull(inv.findById(999), "findById 999 should be null");

        // remove head
        assertTrue(inv.removeById(3), "remove head id=3 should be true");
        assertEquals(2, inv.size(), "size should be 2 after removing head");

        // remove tail
        assertTrue(inv.removeById(2), "remove tail id=2 should be true");
        assertEquals(1, inv.size(), "size should be 1 after removing tail");

        // remove middle / remaining
        assertTrue(inv.removeById(1), "remove last remaining id=1 should be true");
        assertEquals(0, inv.size(), "size should be 0 after removing last");
        assertTrue(inv.isEmpty(), "inventory should be empty");

        System.out.println("ALL MANUAL TESTS PASSED ✅");
    }
}
