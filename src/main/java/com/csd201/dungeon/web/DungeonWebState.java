package com.csd201.dungeon.web;

import com.csd201.dungeon.app.DungeonProjectData;
import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.ds.graph.PathResult;
import com.csd201.dungeon.ds.graph.RoomEdge;
import com.csd201.dungeon.model.Item;
import com.csd201.dungeon.model.Monster;
import com.csd201.dungeon.model.Player;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.service.MonsterBSTLookup;
import com.csd201.dungeon.service.MonsterLookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DungeonWebState {
    private final Player player;
    private final DungeonGraph graph;
    private final Map<Integer, Item> itemCatalog;
    private final List<Item> inventoryItems;
    private final Map<Integer, Monster> monstersById;
    private final MonsterLookup monsterLookup;
    private final List<String> activityLog;

    private int nextCustomItemId;
    private int score;
    private boolean escaped;

    public DungeonWebState(DungeonProjectData data) {
        this.player = data.getPlayer();
        this.graph = data.getGraph();
        this.itemCatalog = data.getItemCatalog();
        this.inventoryItems = new ArrayList<>();
        this.monstersById = new LinkedHashMap<>();
        this.monsterLookup = new MonsterBSTLookup();
        this.activityLog = new ArrayList<>();

        seedInventory(data);
        seedMonsters(data);
        nextCustomItemId = 1000;
        score = 0;
        escaped = false;
        log("Adventure started in the Entrance Hall.");
    }

    public synchronized String buildStateJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"player\":").append(playerJson()).append(",");
        json.append("\"currentRoom\":").append(roomJson(currentRoom())).append(",");
        json.append("\"inventory\":").append(inventoryJson()).append(",");
        json.append("\"catalog\":").append(catalogJson()).append(",");
        json.append("\"rooms\":").append(roomsJson()).append(",");
        json.append("\"encounter\":").append(encounterJson()).append(",");
        json.append("\"activityLog\":").append(logJson()).append(",");
        json.append("\"game\":").append(gameJson());
        json.append("}");
        return json.toString();
    }

    public synchronized String findMonsterJson(int monsterId) {
        Monster monster = monsterLookup.findById(monsterId);
        if (monster == null) {
            return "{\"found\":false,\"message\":\"Monster " + monsterId + " was not found in the BST.\"}";
        }
        return "{\"found\":true,\"monster\":" + monsterJson(monster) + "}";
    }

    public synchronized String inspectCurrentRoomMonsterJson() {
        Monster monster = currentEncounter();
        if (monster == null) {
            return "{\"found\":false,\"message\":\"The current room has no active monster.\"}";
        }
        return "{\"found\":true,\"monster\":" + monsterJson(monster) + "}";
    }

    public synchronized String addItemJson(int itemId) {
        Item item = copyCatalogItem(itemId);
        if (item == null) {
            return messageJson("Catalog item " + itemId + " does not exist.");
        }
        player.getInventory().addLast(item);
        inventoryItems.add(item);
        log("Collected " + item.getName() + " and added it to the inventory.");
        return mutationJson("Added " + item.getName() + " to inventory.");
    }

    public synchronized String createItemJson(String name, String type, int value) {
        if (name == null || name.isBlank()) {
            return messageJson("Item name is required.");
        }
        Item item = new Item(nextCustomItemId++, name.trim(), normalizeType(type), value);
        player.getInventory().addLast(item);
        inventoryItems.add(item);
        log("Crafted a custom item: " + item.getName() + ".");
        return mutationJson("Created custom item " + item.getName() + ".");
    }

    public synchronized String updateItemJson(int itemId, String name, String type, int value) {
        Item item = player.getInventory().findById(itemId);
        if (item == null) {
            return messageJson("Item ID " + itemId + " was not found in the linked list.");
        }
        if (name != null && !name.isBlank()) {
            item.setName(name.trim());
        }
        item.setType(normalizeType(type));
        item.setValue(value);
        log("Updated item ID " + itemId + " in inventory.");
        return mutationJson("Updated item ID " + itemId + ".");
    }

    public synchronized String removeItemJson(int itemId) {
        boolean removed = player.getInventory().removeById(itemId);
        if (!removed) {
            return messageJson("Item ID " + itemId + " was not found in the linked list.");
        }
        inventoryItems.removeIf(item -> item.getId() == itemId);
        log("Removed item ID " + itemId + " from inventory.");
        return mutationJson("Removed item ID " + itemId + " from inventory.");
    }

    public synchronized String usePotionJson() {
        Item potion = firstPotion();
        if (potion == null) {
            return messageJson("There is no potion available to use.");
        }
        player.heal(potion.getValue());
        player.getInventory().removeById(potion.getId());
        inventoryItems.remove(potion);
        log("Used " + potion.getName() + " and recovered " + potion.getValue() + " HP.");
        return mutationJson("Used " + potion.getName() + " and restored " + potion.getValue() + " HP.");
    }

    public synchronized String moveHeroJson(int roomId) {
        Room room = graph.getRoom(roomId);
        if (room == null) {
            return messageJson("Room " + roomId + " does not exist.");
        }
        if (roomId == player.getCurrentRoomId()) {
            return messageJson("Hero is already in room " + roomId + ".");
        }
        if (!isNeighbor(player.getCurrentRoomId(), roomId)) {
            return messageJson("Hero can only travel to adjacent rooms.");
        }
        player.setCurrentRoomId(roomId);
        escaped = room.isExit();
        log("Moved to room " + roomId + " - " + room.getName() + ".");
        if (room.isExit()) {
            score += 150;
            log("Reached the exit gate. Escape route secured.");
        }
        return mutationJson("Hero moved to room " + roomId + " - " + room.getName() + ".");
    }

    public synchronized String attackMonsterJson() {
        Monster monster = currentEncounter();
        if (monster == null) {
            return messageJson("There is no monster to attack in this room.");
        }

        monster.takeDamage(player.getAtk());
        StringBuilder message = new StringBuilder("Hero attacked ").append(monster.getName())
                .append(" for ").append(player.getAtk()).append(" damage.");

        if (!monster.isAlive()) {
            Room room = currentRoom();
            room.setMonsterId(-1);
            score += 100;
            message.append(" ").append(monster.getName()).append(" was defeated.");
            log(message.toString());

            if (monster.getDropItemId() > 0) {
                Item drop = copyCatalogItem(monster.getDropItemId());
                if (drop != null) {
                    player.getInventory().addLast(drop);
                    inventoryItems.add(drop);
                    message.append(" Looted ").append(drop.getName()).append(".");
                    log("Loot drop added: " + drop.getName() + ".");
                }
            }
            return mutationJson(message.toString());
        }

        player.takeDamage(monster.getAtk());
        message.append(" ").append(monster.getName()).append(" countered for ").append(monster.getAtk()).append(" damage.");
        if (!player.isAlive()) {
            log("Hero was knocked out by " + monster.getName() + ".");
            return mutationJson(message + " Hero has fallen.");
        }

        log(message.toString());
        return mutationJson(message.toString());
    }

    public synchronized String claimTreasureJson() {
        Room room = currentRoom();
        if (room == null || !room.hasTreasure()) {
            return messageJson("There is no treasure to claim in this room.");
        }
        room.setHasTreasure(false);
        score += 75;

        Item treasure = copyCatalogItem(5);
        if (treasure != null) {
            player.getInventory().addLast(treasure);
            inventoryItems.add(treasure);
            log("Claimed treasure in " + room.getName() + " and stored " + treasure.getName() + ".");
            return mutationJson("Treasure claimed and " + treasure.getName() + " added to inventory.");
        }

        log("Claimed treasure in " + room.getName() + ".");
        return mutationJson("Treasure claimed successfully.");
    }

    public synchronized String findInventoryJson(String name) {
        Item item = player.getInventory().findByName(name);
        if (item == null) {
            return "{\"found\":false,\"message\":\"No inventory item matched " + quote(name) + ".\"}";
        }
        return "{\"found\":true,\"item\":" + itemJson(item) + "}";
    }

    public synchronized String nearestExitJson() {
        return pathEnvelopeJson("BFS path to nearest exit", graph.findPathToNearestExitBfs(player.getCurrentRoomId()));
    }

    public synchronized String nearestTreasureJson() {
        return pathEnvelopeJson("BFS path to nearest treasure", graph.findPathToNearestTreasureBfs(player.getCurrentRoomId()));
    }

    public synchronized String dijkstraJson(int targetRoomId) {
        return pathEnvelopeJson("Dijkstra shortest path", graph.findShortestPathDijkstra(player.getCurrentRoomId(), targetRoomId));
    }

    public synchronized String dfsJson() {
        List<Room> order = graph.traverseDfs(player.getCurrentRoomId());
        StringBuilder json = new StringBuilder();
        json.append("{\"title\":\"DFS traversal from current room\",\"rooms\":[");
        for (int i = 0; i < order.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(roomJson(order.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    private void seedInventory(DungeonProjectData data) {
        addSeedItem(data.createCatalogItemCopy(3));
        addSeedItem(data.createCatalogItemCopy(1));
        addSeedItem(data.createCatalogItemCopy(2));
    }

    private void seedMonsters(DungeonProjectData data) {
        Monster[] baseMonsters = data.getMonsters();
        Monster[] cloned = new Monster[baseMonsters.length];
        for (int i = 0; i < baseMonsters.length; i++) {
            Monster source = baseMonsters[i];
            Monster copy = new Monster(source.getId(), source.getName(), source.getLevel(),
                    source.getHp(), source.getAtk(), source.getDropItemId());
            monstersById.put(copy.getId(), copy);
            cloned[i] = copy;
        }
        monsterLookup.build(cloned);
    }

    private void addSeedItem(Item item) {
        if (item == null) {
            return;
        }
        player.getInventory().addLast(item);
        inventoryItems.add(item);
    }

    private Room currentRoom() {
        return graph.getRoom(player.getCurrentRoomId());
    }

    private Monster currentEncounter() {
        Room room = currentRoom();
        if (room == null || room.getMonsterId() < 0) {
            return null;
        }
        Monster monster = monstersById.get(room.getMonsterId());
        return monster != null && monster.isAlive() ? monster : null;
    }

    private Item firstPotion() {
        for (Item item : inventoryItems) {
            if ("POTION".equalsIgnoreCase(item.getType())) {
                return item;
            }
        }
        return null;
    }

    private boolean isNeighbor(int currentRoomId, int targetRoomId) {
        for (RoomEdge edge : graph.getNeighbors(currentRoomId)) {
            if (edge.getToRoomId() == targetRoomId) {
                return true;
            }
        }
        return false;
    }

    private void log(String entry) {
        activityLog.add(0, entry);
        if (activityLog.size() > 10) {
            activityLog.remove(activityLog.size() - 1);
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "MISC";
        }
        return type.trim().toUpperCase();
    }

    private String mutationJson(String message) {
        return "{\"ok\":true,\"message\":" + quote(message) + ",\"state\":" + buildStateJson() + "}";
    }

    private String messageJson(String message) {
        return "{\"ok\":false,\"message\":" + quote(message) + ",\"state\":" + buildStateJson() + "}";
    }

    private String pathEnvelopeJson(String title, PathResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"title\":").append(quote(title)).append(",");
        json.append("\"reachable\":").append(result.isReachable()).append(",");
        json.append("\"totalCost\":").append(result.getTotalCost()).append(",");
        json.append("\"pathLabel\":").append(quote(result.toRoomIdPath())).append(",");
        json.append("\"rooms\":[");
        List<Room> rooms = result.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(roomJson(rooms.get(i)));
        }
        json.append("]}");
        return json.toString();
    }

    private String playerJson() {
        return "{"
                + "\"name\":" + quote(player.getName()) + ","
                + "\"hp\":" + player.getHp() + ","
                + "\"atk\":" + player.getAtk() + ","
                + "\"alive\":" + player.isAlive() + ","
                + "\"currentRoomId\":" + player.getCurrentRoomId() + ","
                + "\"inventorySize\":" + inventoryItems.size()
                + "}";
    }

    private String gameJson() {
        return "{"
                + "\"score\":" + score + ","
                + "\"escaped\":" + escaped + ","
                + "\"roomCount\":" + graph.roomCount() + ","
                + "\"monsterCount\":" + monstersById.size()
                + "}";
    }

    private String logJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < activityLog.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(quote(activityLog.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String encounterJson() {
        Monster monster = currentEncounter();
        if (monster == null) {
            return "null";
        }
        return monsterJson(monster);
    }

    private String inventoryJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < inventoryItems.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(itemJson(inventoryItems.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String catalogJson() {
        List<Item> items = itemCatalog.values().stream()
                .sorted(Comparator.comparingInt(Item::getId))
                .toList();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(itemJson(items.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String roomsJson() {
        List<Room> rooms = graph.getRooms();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < rooms.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(roomJson(rooms.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private String roomJson(Room room) {
        if (room == null) {
            return "null";
        }
        return "{"
                + "\"id\":" + room.getId() + ","
                + "\"name\":" + quote(room.getName()) + ","
                + "\"description\":" + quote(room.getDescription()) + ","
                + "\"exit\":" + room.isExit() + ","
                + "\"treasure\":" + room.hasTreasure() + ","
                + "\"monsterId\":" + room.getMonsterId() + ","
                + "\"neighbors\":" + neighborsJson(room.getId())
                + "}";
    }

    private String neighborsJson(int roomId) {
        StringBuilder json = new StringBuilder("[");
        List<RoomEdge> neighbors = graph.getNeighbors(roomId);
        for (int i = 0; i < neighbors.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            RoomEdge edge = neighbors.get(i);
            Room room = graph.getRoom(edge.getToRoomId());
            json.append("{")
                    .append("\"toRoomId\":").append(edge.getToRoomId()).append(",")
                    .append("\"weight\":").append(edge.getWeight()).append(",")
                    .append("\"name\":").append(quote(room == null ? "Unknown" : room.getName()))
                    .append("}");
        }
        json.append("]");
        return json.toString();
    }

    private String monsterJson(Monster monster) {
        return "{"
                + "\"id\":" + monster.getId() + ","
                + "\"name\":" + quote(monster.getName()) + ","
                + "\"level\":" + monster.getLevel() + ","
                + "\"hp\":" + monster.getHp() + ","
                + "\"atk\":" + monster.getAtk() + ","
                + "\"dropItemId\":" + monster.getDropItemId() + ","
                + "\"alive\":" + monster.isAlive()
                + "}";
    }

    private String itemJson(Item item) {
        return "{"
                + "\"id\":" + item.getId() + ","
                + "\"name\":" + quote(item.getName()) + ","
                + "\"type\":" + quote(item.getType()) + ","
                + "\"value\":" + item.getValue()
                + "}";
    }

    private Item copyCatalogItem(int itemId) {
        Item item = itemCatalog.get(itemId);
        if (item == null) {
            return null;
        }
        return new Item(item.getId(), item.getName(), item.getType(), item.getValue());
    }

    private String quote(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
        return "\"" + escaped + "\"";
    }
}
