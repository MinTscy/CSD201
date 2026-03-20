package com.csd201.dungeon.unit;

import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.ds.graph.PathResult;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.service.DungeonMapLoader;

import java.io.IOException;
import java.util.List;

public final class DungeonGraphUnitTest {
    public static void runAll() {
        testLoadMapFromFile();
        testBfsFindsNearestExit();
        testBfsFindsNearestTreasure();
        testDijkstraUsesWeights();
        testDfsTraversesAllRooms();
    }

    private static void testLoadMapFromFile() {
        DungeonGraph graph = loadGraph();
        Assertions.assertEquals(6, graph.roomCount(), "loader should create all rooms from file");
        Assertions.assertNotNull(graph.getRoom(1), "room 1 should exist");
        Assertions.assertNotNull(graph.getRoom(5), "room 5 should exist");
        Assertions.assertEquals(2, graph.getNeighbors(1).size(), "room 1 should have two connections");
    }

    private static void testBfsFindsNearestExit() {
        DungeonGraph graph = loadGraph();
        PathResult path = graph.findPathToNearestExitBfs(1);

        Assertions.assertTrue(path.isReachable(), "exit should be reachable from room 1");
        Assertions.assertTrue("1 -> 4 -> 5".equals(path.toRoomIdPath()), "BFS should find the shortest path to exit");
        Assertions.assertEquals(2, path.getTotalCost(), "BFS step cost should equal the number of moves");
    }

    private static void testBfsFindsNearestTreasure() {
        DungeonGraph graph = loadGraph();
        PathResult path = graph.findPathToNearestTreasureBfs(1);

        Assertions.assertTrue(path.isReachable(), "treasure should be reachable from room 1");
        Assertions.assertTrue("1 -> 2 -> 3".equals(path.toRoomIdPath()), "nearest treasure should be room 3");
    }

    private static void testDijkstraUsesWeights() {
        DungeonGraph graph = loadGraph();
        PathResult path = graph.findShortestPathDijkstra(1, 6);

        Assertions.assertTrue(path.isReachable(), "room 6 should be reachable from room 1");
        Assertions.assertTrue("1 -> 2 -> 3 -> 6".equals(path.toRoomIdPath()), "Dijkstra should choose the lowest total weight");
        Assertions.assertEquals(3, path.getTotalCost(), "weighted path cost should be minimal");
    }

    private static void testDfsTraversesAllRooms() {
        DungeonGraph graph = loadGraph();
        List<Room> visited = graph.traverseDfs(1);

        Assertions.assertEquals(6, visited.size(), "DFS should visit every connected room");
        Assertions.assertEquals(1, visited.get(0).getId(), "DFS should start from the requested room");
    }

    private static DungeonGraph loadGraph() {
        DungeonMapLoader loader = new DungeonMapLoader();
        try {
            return loader.loadFromFile("src/main/resources/dungeon-map.txt");
        } catch (IOException ex) {
            throw new RuntimeException("Unable to load dungeon map test data", ex);
        }
    }
}
