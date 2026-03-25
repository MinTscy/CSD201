package com.csd201.dungeon.app;

import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.model.Item;
import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.model.Player;
import com.csd201.dungeon.service.DungeonMapLoader;
import com.csd201.dungeon.service.MonsterBSTLookup;
import com.csd201.dungeon.service.MonsterLookup;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DungeonProjectData {
    private final Player player;
    private final Monster[] monsters;
    private final MonsterLookup monsterLookup;
    private final DungeonGraph graph;
    private final Map<Integer, Item> itemCatalog;

    private DungeonProjectData(Player player, Monster[] monsters, MonsterLookup monsterLookup,
                               DungeonGraph graph, Map<Integer, Item> itemCatalog) {
        this.player = player;
        this.monsters = monsters;
        this.monsterLookup = monsterLookup;
        this.graph = graph;
        this.itemCatalog = itemCatalog;
    }

    public static DungeonProjectData createDefault() throws IOException {
        Player player = new Player("Hero", 100, 15, 1);
        Map<Integer, Item> itemCatalog = createItemCatalog();

        player.getInventory().addLast(copyItem(itemCatalog.get(1)));
        player.getInventory().addLast(copyItem(itemCatalog.get(2)));
        player.getInventory().addFirst(copyItem(itemCatalog.get(3)));

        Monster[] monsters = createMonsters();
        MonsterLookup lookup = new MonsterBSTLookup();
        lookup.build(monsters);

        DungeonMapLoader loader = new DungeonMapLoader();
        DungeonGraph graph;
        try {
            graph = loader.loadFromResource("/dungeon-map.txt");
        } catch (IOException ex) {
            graph = loader.loadFromFile("src/main/resources/dungeon-map.txt");
        }

        return new DungeonProjectData(player, monsters, lookup, graph, itemCatalog);
    }

    public Player getPlayer() {
        return player;
    }

    public Monster[] getMonsters() {
        return monsters.clone();
    }

    public MonsterLookup getMonsterLookup() {
        return monsterLookup;
    }

    public DungeonGraph getGraph() {
        return graph;
    }

    public Map<Integer, Item> getItemCatalog() {
        return itemCatalog;
    }

    public Item createCatalogItemCopy(int itemId) {
        Item item = itemCatalog.get(itemId);
        return item == null ? null : copyItem(item);
    }

    private static Map<Integer, Item> createItemCatalog() {
        Map<Integer, Item> items = new LinkedHashMap<>();
        items.put(1, new Item(1, "Potion", "POTION", 20));
        items.put(2, new Item(2, "Bronze Key", "KEY", 1));
        items.put(3, new Item(3, "Dagger", "WEAPON", 5));
        items.put(4, new Item(4, "Crystal Bomb", "WEAPON", 12));
        items.put(5, new Item(5, "Ancient Coin", "TREASURE", 50));
        items.put(6, new Item(6, "Elixir", "POTION", 35));
        return items;
    }

    private static Monster[] createMonsters() {
        return new Monster[]{
                new Monster(101, "Goblin Guard", 1, 35, 8, 1),
                new Monster(102, "Crystal Slime", 2, 42, 10, 5),
                new Monster(103, "Gate Keeper", 3, 58, 14, 2)
        };
    }

    private static Item copyItem(Item item) {
        return new Item(item.getId(), item.getName(), item.getType(), item.getValue());
    }
}
