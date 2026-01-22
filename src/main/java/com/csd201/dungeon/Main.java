package com.csd201.dungeon;

import com.csd201.dungeon.model.Item;
import com.csd201.dungeon.model.Player;

public class Main {
    public static void main(String[] args) {
        Player player = new Player("Hero", 100, 15, 1);

        // Add items to inventory
        player.getInventory().addLast(new Item(1, "Potion", "POTION", 20));
        player.getInventory().addLast(new Item(2, "Bronze Key", "KEY", 1));
        player.getInventory().addFirst(new Item(3, "Dagger", "WEAPON", 5));

        System.out.println(player);
        player.getInventory().printAll();

        System.out.println("\nFind by name 'potion': " + player.getInventory().findByName("potion"));
        System.out.println("Remove id=3 (head): " + player.getInventory().removeById(3));
        System.out.println("Remove id=999 (not exist): " + player.getInventory().removeById(999));
        System.out.println("Remove id=2 (tail maybe): " + player.getInventory().removeById(2));

        System.out.println();
        player.getInventory().printAll();
    }
}
