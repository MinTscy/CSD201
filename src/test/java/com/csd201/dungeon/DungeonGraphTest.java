package com.csd201.dungeon;

import com.csd201.dungeon.ds.graph.DungeonGraph;
import com.csd201.dungeon.ds.graph.PathResult;
import com.csd201.dungeon.model.Room;
import com.csd201.dungeon.service.DungeonMapLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DungeonGraphTest {

    @Test
    void loadMapFromTextFileBuildsRoomsAndEdges() throws IOException {
        DungeonMapLoader loader = new DungeonMapLoader();

        DungeonGraph graph = loader.loadFromFile("src/main/resources/dungeon-map.txt");

        assertEquals(6, graph.roomCount(), "loader should create all rooms from file");
        assertNotNull(graph.getRoom(1), "room 1 should exist");
        assertNotNull(graph.getRoom(5), "room 5 should exist");
        assertEquals(2, graph.getNeighbors(1).size(), "room 1 should have two connections");
    }

    @Test
    void bfsFindsShortestPathToNearestExit() throws IOException {
        DungeonMapLoader loader = new DungeonMapLoader();
        DungeonGraph graph = loader.loadFromResource("/dungeon-map.txt");

        PathResult path = graph.findPathToNearestExitBfs(1);

        assertTrue(path.isReachable(), "exit should be reachable from room 1");
        assertEquals("1 -> 4 -> 5", path.toRoomIdPath(), "BFS should find the shortest unweighted exit path");
        assertEquals(2, path.getTotalCost(), "BFS step cost should equal the number of moves");
    }

    @Test
    void bfsFindsNearestTreasureRoom() throws IOException {
        DungeonMapLoader loader = new DungeonMapLoader();
        DungeonGraph graph = loader.loadFromResource("/dungeon-map.txt");

        PathResult path = graph.findPathToNearestTreasureBfs(1);

        assertTrue(path.isReachable(), "treasure should be reachable from room 1");
        assertEquals("1 -> 2 -> 3", path.toRoomIdPath(), "nearest treasure should be room 3");
    }

    @Test
    void dijkstraUsesWeightsWhenChoosingPath() throws IOException {
        DungeonMapLoader loader = new DungeonMapLoader();
        DungeonGraph graph = loader.loadFromResource("/dungeon-map.txt");

        PathResult path = graph.findShortestPathDijkstra(1, 6);

        assertTrue(path.isReachable(), "room 6 should be reachable from room 1");
        assertEquals("1 -> 2 -> 3 -> 6", path.toRoomIdPath(), "Dijkstra should avoid the heavier direct branch through room 4");
        assertEquals(3, path.getTotalCost(), "weighted path cost should be minimal");
    }

    @Test
    void dfsTraversesConnectedRooms() throws IOException {
        DungeonMapLoader loader = new DungeonMapLoader();
        DungeonGraph graph = loader.loadFromResource("/dungeon-map.txt");

        List<Room> visited = graph.traverseDfs(1);

        assertEquals(6, visited.size(), "DFS should visit every connected room");
        assertEquals(1, visited.get(0).getId(), "DFS should start from the requested room");
    }
}
